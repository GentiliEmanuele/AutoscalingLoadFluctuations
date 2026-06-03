package it.simulation.system.infrastructures;

import it.simulation.events.IllegalLifeException;
import it.simulation.system.SystemStats;
import it.simulation.system.jobs.Job;
import it.simulation.system.servers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.simulation.configurations.Config.*;

public class SpikedInfrastructureDecorator implements Infrastructure {
    private final BaseServerInfrastructure base;
    private final SpikeServer spikeServer;
    private final List<AbstractServer> allServers;

    public SpikedInfrastructureDecorator(BaseServerInfrastructure base) {
        this.base = base;
        this.spikeServer = new SpikeServer(SPIKE_CAPACITY);
        this.allServers = new ArrayList<>();
        this.allServers.add(this.spikeServer);
        this.allServers.addAll(base.webServers);
    }

    @Override
    public double computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException {
        Integer completionServerIndex = null;
        Double completedJobResponseTime = null;
        Job completedJob;

        if (isCompletion) {
            completionServerIndex = getCompletingServerIndex();
            AbstractServer minServer = allServers.get(completionServerIndex);
            completedJob = minServer.getMinRemainingLifeJob();
            completedJobResponseTime = endTs - completedJob.getArrivalTime();
            minServer.removeJob(completedJob);
        }

        /* Compute the advancement of each job in each web Server */
        for (int currIndex = 0; currIndex < allServers.size(); currIndex++) {
            AbstractServer server = allServers.get(currIndex);
            server.computeJobsAdvancement(
                    startTs, endTs,
                    Objects.equals(currIndex, completionServerIndex) ? completedJobResponseTime : null);
        }

        this.updateScalingIndicator();
        return base.scalingIndicator;
    }

    private int getCompletingServerIndex() {
        Server minServer = null;
        double lifeRemaining, minRemainingLife = INFINITY;

        for (AbstractServer server : allServers) {
            Job j = server.getMinRemainingLifeJob();
            if (j != null) {
                lifeRemaining = j.getRemainingLife() * server.size() / server.getCapacity();
                if (lifeRemaining < minRemainingLife) {
                    minRemainingLife = lifeRemaining;
                    minServer = server;
                }
            }
        }

        assert minServer != null;
        return allServers.indexOf(minServer);
    }

    @Override
    public void printSystemStats(double currentTs) {
        System.out.print("\n\nSpikeServer : ");
        this.spikeServer.printServerStats(currentTs);
        base.printSystemStats(currentTs);

        // Compute system stats
        SystemStats systemStats = computeSystemStats(currentTs);

        System.out.println("\n\nSystem: ");
        System.out.println("   total throughput ..... =     " + systemStats.getThroughput());
        System.out.println("   total arrived job .... =     " + systemStats.getTotalArrivals());
        System.out.println("   total completed job .. =     " + systemStats.getTotalCompletion());
        System.out.println("   average busy server .. =     " + systemStats.getMeanBusyServer());
        System.out.println("   average service time . =     " + systemStats.getMeanServiceTime());
        System.out.println("   average response time  =     " + systemStats.getMeanResponseTime());
    }

    @Override
    public SystemStats computeSystemStats(double currentTs) {
        if (currentTs < START) return new SystemStats(0, 0, 0, 0, 0, 0, 0);

        // This return web servers metrics
        SystemStats baseSystemStats = base.computeSystemStats(currentTs);

        int totalCompletion = baseSystemStats.getTotalCompletion() + spikeServer.getServerStats().getCompletedJobs();
        double totalBusyTime = baseSystemStats.getTotalBusyTime() + spikeServer.getServerStats().getServiceSum();
        double deltaT = currentTs - START;
        double meanBusyServers = deltaT > 0 ? totalBusyTime / deltaT : 0;
        double totalThroughput = deltaT > 0 ? totalCompletion / deltaT : 0;
        double meanServiceTime = totalCompletion > 0 ? totalBusyTime / totalCompletion : 0;
        double systemResponseTime = getSystemResponseTime(deltaT, totalThroughput);
        int totalArrivals = getBaseTotalArrivals() + spikeServer.getServerStats().getArrivedJobs();

        return new SystemStats(totalThroughput, meanBusyServers, meanServiceTime, systemResponseTime, totalCompletion, totalBusyTime, totalArrivals);
    }

    @Override
    public List<ServerStats> getServersStats(double currentTs) {
        return allServers.stream().map(AbstractServer::getServerStats).map(ServerStats::new).toList();
    }

    private double getSystemResponseTime(double currentTs, double totalThroughput) {
        double systemResponseTime = 0.0;

        for (AbstractServer webServer : base.webServers) {
            systemResponseTime += totalThroughput > 0 ?
                    (webServer.getServerStats().getCurrOutputFrequency() / totalThroughput) * webServer.getServerStats().getCurrMeanResponseTime() :
                    0;
        }

        double spikeServerOutputFrequency = currentTs > 0 ? spikeServer.getStats().getCompletedJobs() / currentTs : 0;
        systemResponseTime += totalThroughput > 0 ?
                spikeServerOutputFrequency / totalThroughput * spikeServer.getServerStats().getCurrMeanResponseTime() :
                0;
        return systemResponseTime;
    }

    private int getBaseTotalArrivals() {
        int totalArrival = 0;
        for (AbstractServer webServer : base.webServers) {
            totalArrival += webServer.getStats().getArrivedJobs();
        }

        return totalArrival;
    }

    @Override
    public void assignJob(Job job) {
        boolean webServerCongestion = base.webServersSize() >= SI_MAX * getNumWebServersByState(ServerState.ACTIVE);
        if (!webServerCongestion) {
            base.assignJob(job);
        } else {
            spikeServer.addJob(job);
            spikeServer.getServerStats().incrementArrivedJobs(job.getArrivalTime());
        }
    }

    @Override
    public double computeNextCompletionTs(double endTs) {
        double spikeServerMinRemainingLife = spikeServer.activeJobExists() ?
                endTs + spikeServer.getMinRemainingLife() * spikeServer.size() / spikeServer.getCapacity()
                : INFINITY;
        return Math.min(spikeServerMinRemainingLife, base.computeNextCompletionTs(endTs));
    }

    @Override
    public boolean activeJobExists() {
        return base.activeJobExists() || spikeServer.activeJobExists();
    }

    @Override
    public ServerStats getServerStatsByIndex(int index, double currentTs) {
        return new ServerStats(allServers.get(index).getServerStats());
    }

    @Override
    public WebServer requestScaleOut(double endTs, double turnOnTime) {
        return base.requestScaleOut(endTs, turnOnTime);
    }

    @Override
    public WebServer findNextScaleOut() {
        return base.findNextScaleOut();
    }

    @Override
    public void scaleOut(double endTs, WebServer targetWebServer) {
        base.scaleOut(endTs, targetWebServer);
        this.spikeServer.setCapacity(SPIKE_CAPACITY);
    }

    @Override
    public void scaleIn(double endTs) {
        base.scaleIn(endTs);
        this.spikeServer.setCapacity(SPIKE_CAPACITY);
    }

    @Override
    public int getNumWebServersByState(ServerState state) {
        return base.getNumWebServersByState(state);
    }

    void updateScalingIndicator() {
        if (SCALING_INDICATOR_TYPE.equals("r0")) {
            base.scalingIndicator = 0.0;
            List<AbstractServer> activeServers = allServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.ACTIVE)
                    .toList();

            for (AbstractServer server : activeServers) {
                base.scalingIndicator += server.getWindowedMeanResponseTime() / activeServers.size();
            }
        } else if (SCALING_INDICATOR_TYPE.equals("jobs")) {
            base.scalingIndicator = allServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.ACTIVE || ws.getServerState() == ServerState.TO_BE_REMOVED)
                    .map(AbstractServer::size)
                    .reduce(0, Integer::sum);
        } else {
            throw new IllegalArgumentException("Invalid type of scaling indicator");
        }
    }
}
