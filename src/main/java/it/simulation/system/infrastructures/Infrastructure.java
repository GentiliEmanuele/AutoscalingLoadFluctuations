package it.simulation.system.infrastructures;

import it.simulation.events.IllegalLifeException;
import it.simulation.system.SystemStats;
import it.simulation.system.jobs.Job;

public interface Infrastructure {
    void computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException;
    void printSystemStats(double currentTs);
    SystemStats computeSystemStats(double currentTs);
    void assignJob(Job job);
    double computeNextCompletionTs(double endTs);
    boolean activeJobExists();
}
