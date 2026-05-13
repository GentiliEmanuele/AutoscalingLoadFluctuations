package it.simulation.system.servers;

import it.simulation.events.IllegalLifeException;
import it.simulation.system.jobs.Job;

public interface Server {
    void computeJobsAdvancement(double startTs, double endTs, Double completedJobResponseTime) throws IllegalLifeException;

    void addJob(Job job);

    void removeJob(Job job);

    Job getMinRemainingLifeJob();

    boolean activeJobExists();

    int size();

    double getMinRemainingLife();

    void printServerStats(double currentTs);

    ServerStats getServerStats();
}
