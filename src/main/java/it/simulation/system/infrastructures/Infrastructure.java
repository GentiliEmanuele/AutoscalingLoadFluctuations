package it.simulation.system.infrastructures;

import it.simulation.events.IllegalLifeException;
import it.simulation.system.SystemStats;
import it.simulation.system.jobs.Job;
import it.simulation.system.servers.ServerStats;
import it.simulation.system.servers.WebServer;

import java.util.List;

public interface Infrastructure {
    void computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException;
    void printSystemStats(double currentTs);
    SystemStats computeSystemStats(double currentTs);
    List<ServerStats> getServersStats(double currentTs);
    void assignJob(Job job);
    double computeNextCompletionTs(double endTs);
    boolean activeJobExists();
    ServerStats getServerStatsByIndex(int index, double currentTs);
    WebServer requestScaleOut(double endTs, double turnOnTime);
    WebServer findNextScaleOut();
    void scaleOut(double endTs, WebServer targetWebServer);
}
