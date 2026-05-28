package it.simulation.system.infrastructures;
import it.simulation.events.IllegalLifeException;
import it.simulation.system.SystemStats;
import it.simulation.system.jobs.Job;
import it.simulation.system.schedulers.Scheduler;
import it.simulation.system.schedulers.SchedulerFactory;
import it.simulation.system.servers.*;
import lombok.Getter;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static it.simulation.configurations.Config.*;

public class BaseServerInfrastructure implements Infrastructure {
    @Getter  final Scheduler scheduler;
    final List<WebServer> webServers;
    double scalingIndicator;


    public BaseServerInfrastructure() {
        this.scheduler = SchedulerFactory.create();
        this.webServers = new ArrayList<>();
        for (int i = 0; i < MAX_NUM_SERVERS; i++) {
            var serverState = i < START_NUM_SERVERS ? ServerState.ACTIVE : ServerState.REMOVED;
            this.webServers.add(new WebServer(WEBSERVER_CAPACITY, serverState, i + 1));
        }
    }


    @Override
    public double computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException {
        Integer completionServerIndex = null;
        Double completedJobResponseTime = null;
        Job completedJob;

        if (isCompletion) {
            /* Get the jobs with the minRemainingLife on all server */
            completionServerIndex = getCompletingServerIndex();
            WebServer minServer = webServers.get(completionServerIndex);
            completedJob = minServer.getMinRemainingLifeJob();
            completedJobResponseTime = endTs - completedJob.getArrivalTime();
            minServer.removeJob(completedJob);
        }

        /* Compute the advancement of each job in each web Server */
        for (int currIndex = 0; currIndex < webServers.size(); currIndex++) {
            AbstractServer server = webServers.get(currIndex);
            server.computeJobsAdvancement(
                    startTs, endTs,
                    Objects.equals(currIndex, completionServerIndex) ? completedJobResponseTime : null);
        }

        this.updateScalingIndicator();
        return this.scalingIndicator;
    }

    private int getCompletingServerIndex() {
        Server minServer = null;
        double lifeRemaining, minRemainingLife = INFINITY;

        /* Search the job with min remaining life (the next that complete) */
        for (WebServer server : webServers) {
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
        return webServers.indexOf(minServer);
    }

    @Override
    public void printSystemStats(double currentTs) {
        for (WebServer server : webServers) {
            System.out.printf("\nWebServer %d: ", webServers.indexOf(server) + 1);
            server.printServerStats(currentTs);
        }

        if (!SPIKESERVER_ACTIVE) {
            SystemStats baseSystemStats = computeSystemStats(currentTs);
            System.out.println("\nSystem: ");
            System.out.println("   total throughput ..... =     " + baseSystemStats.getThroughput());
            System.out.println("   average busy server .. =     " + baseSystemStats.getMeanBusyServer());
            System.out.println("   average service time . =     " + baseSystemStats.getMeanServiceTime());
            System.out.println("   average response time  =     " + baseSystemStats.getMeanResponseTime());
        }
    }

    @Override
    public SystemStats computeSystemStats(double currentTs) {
        int totalCompletion = 0;
        int totalArrivals = 0;
        double totalBusyTime = 0.0;

        for (AbstractServer server : webServers) {
            totalCompletion += server.getServerStats().getCompletedJobs();
            totalArrivals += server.getServerStats().getArrivedJobs();
            totalBusyTime += server.getServerStats().getServiceSum();
            server.getServerStats().setCurrOutputFrequency(currentTs > 0 ? (double) server.getServerStats().getCompletedJobs() / currentTs : 0);
        }

        double webServersThroughput = currentTs > 0 ? totalCompletion / currentTs : 0;
        double busyWebServers = currentTs > 0 ? totalBusyTime / currentTs : 0;
        double webServersServiceTime = totalCompletion > 0 ? totalBusyTime / totalCompletion : 0;
        double webServersResponseTime = 0.0;

        for (AbstractServer server : webServers) {
            webServersResponseTime += webServersThroughput > 0 ?
                    (server.getServerStats().getCurrOutputFrequency() / webServersThroughput) * server.getServerStats().getCurrMeanResponseTime() :
                    0;
        }

        return new SystemStats(webServersThroughput, busyWebServers, webServersServiceTime, webServersResponseTime, totalCompletion, totalBusyTime, totalArrivals);
    }

    @Override
    public List<ServerStats> getServersStats(double currentTs) {
        return webServers.stream().map(AbstractServer::getServerStats).map(ServerStats::new).toList();
    }

    @Override
    public void assignJob(Job job) {
        List<AbstractServer> baseList = new ArrayList<>(this.webServers);
        AbstractServer target = scheduler.select(baseList);
        target.getStats().incrementArrivedJobs();
        target.addJob(job);
    }

    @Override
    public double computeNextCompletionTs(double endTs) {
        double currRemainingLife, minRemainingLife = INFINITY;

        for (WebServer server : webServers.stream().filter(server -> server.size() != 0).toList()) {
            currRemainingLife = server.getMinRemainingLife() * server.size() / server.getCapacity();
            if (currRemainingLife < minRemainingLife) {
                minRemainingLife = currRemainingLife;
            }
        }

        return endTs + minRemainingLife;
    }

    @Override
    public boolean activeJobExists() {
        for (WebServer server : webServers) {
            if (server.activeJobExists()) return true;
        }
        return false;
    }

    @Override
    public ServerStats getServerStatsByIndex(int index, double currentTs) {
        return new ServerStats(webServers.get(index).getServerStats());
    }

    WebServer findScaleOutTarget() {
        WebServer targetWebServer;

        // Search if there is a server still active but to be removed
        targetWebServer = webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_REMOVED)
                .min(Comparator.comparingDouble(webServers::indexOf))
                .orElse(null);

        // If no servers are active but to be removed, look for a removed one
        if (targetWebServer == null) {
            targetWebServer = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.REMOVED)
                    .min(Comparator.comparingDouble(webServers::indexOf))
                    .orElse(null);
        }

        return targetWebServer;
    }

    @Override
    public WebServer requestScaleOut(double endTs, double turnOnTime) {
        WebServer targetWebServer = findScaleOutTarget();

        /* If found server, make it active */
        if (targetWebServer != null) {
            targetWebServer.setServerState(ServerState.TO_BE_ACTIVE);
            targetWebServer.setActivationTimestamp(endTs + turnOnTime);

            return targetWebServer;
        }

        /* If no server is found, all servers are active */
        else System.out.println("All servers are active");

        return null;
    }

    @Override
    public WebServer findNextScaleOut() {
        return webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_ACTIVE)
                .min(Comparator.comparingDouble(WebServer::getActivationTimestamp))
                .orElse(null);
    }

    @Override
    public void scaleOut(double endTs, WebServer targetWebServer) {
        targetWebServer.setServerState(ServerState.ACTIVE);
    }

    WebServer findScaleInTarget() {
        WebServer targetWebServer;

        // Search if there is a server still active
        targetWebServer = webServers.stream()
                .filter(ws -> ws.getServerState() == ServerState.TO_BE_ACTIVE)
                .max(Comparator.comparingDouble(webServers::indexOf))
                .orElse(null);

        // If no servers are to be active, look for an active one
        if (targetWebServer == null) {
            targetWebServer = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.ACTIVE)
                    .max(Comparator.comparingDouble(webServers::indexOf))
                    .orElse(null);
        }

        return targetWebServer;
    }


    @Override
    public void scaleIn(double endTs) {
        WebServer minServer = findScaleInTarget();

        /* If found server, make it to be removed */
        if (minServer != null) {
            if (minServer.size() == 0) {
                minServer.setServerState(ServerState.REMOVED);
                minServer.resetMovingExpMeanResponseTime();
            } else {
                minServer.setServerState(ServerState.TO_BE_REMOVED);
                minServer.resetMovingExpMeanResponseTime();
            }
        }

        /* If no server is found, only 1 server is active */
        else System.out.println("No active servers found!");
    }

    @Override
    public int getNumWebServersByState(ServerState state) {
        return (int) webServers.stream().filter(server -> server.getServerState() == state).count();
    }

    int webServersSize() {
        int size = 0;
        for (WebServer server : webServers) {
            size += server.size();
        }
        return size;
    }

    void updateScalingIndicator() {
        if (SCALING_INDICATOR_TYPE.equals("r0")) {
            this.scalingIndicator = 0.0;
            List<WebServer> activeServers = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.ACTIVE)
                    .toList();

            for (AbstractServer server : activeServers) {
                this.scalingIndicator += server.getWindowedMeanResponseTime() / activeServers.size();
            }
        } else if (SCALING_INDICATOR_TYPE.equals("jobs")) {
            this.scalingIndicator = webServers.stream()
                    .filter(ws -> ws.getServerState() == ServerState.ACTIVE || ws.getServerState() == ServerState.TO_BE_REMOVED)
                    .map(AbstractServer::size)
                    .reduce(0, Integer::sum);
        } else {
            throw new IllegalArgumentException("Invalid type of scaling indicator");
        }
    }

}
