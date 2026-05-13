package it.simulation.system.infrastructures;

import it.simulation.events.IllegalLifeException;
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
        this.allServers.add(base.webServer);
    }

    @Override
    public void computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException {
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
    public void printServerStats(double currentTs) {
        System.out.print("\n\nSpikeServer : ");
        this.spikeServer.printServerStats(currentTs);
        base.printServerStats(currentTs);
    }

    @Override
    public void assignJob(Job job) {
        boolean webServerCongestion = base.webServersSize() >= SI_MAX * getNumWebServersByState(ServerState.ACTIVE);
        if (!webServerCongestion) {
            base.assignJob(job);
        } else {
            spikeServer.addJob(job);
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

    public int getNumWebServersByState(ServerState state) {
        return base.getNumWebServersByState(state);
    }
}
