package it.simulation.system.infrastructures;
import it.simulation.events.IllegalLifeException;
import it.simulation.system.jobs.Job;
import it.simulation.system.servers.ServerState;
import it.simulation.system.servers.WebServer;


import static it.simulation.configurations.Config.INFINITY;
import static it.simulation.configurations.Config.WEBSERVER_CAPACITY;

public class BaseServerInfrastructure implements Infrastructure {
    final WebServer webServer;


    public BaseServerInfrastructure() {
        this.webServer = new WebServer(WEBSERVER_CAPACITY, ServerState.ACTIVE, 1);
    }


    @Override
    public void computeJobsAdvancement(double startTs, double endTs, boolean isCompletion) throws IllegalLifeException {
        Double completedJobResponseTime = null;
        Job completedJob;

        if (isCompletion) {
            /* Get the jobs with the minRemainingLife on all server */
            completedJob = webServer.getMinRemainingLifeJob();
            completedJobResponseTime = endTs - completedJob.getArrivalTime();
            webServer.removeJob(completedJob);
        }

        webServer.computeJobsAdvancement(
                startTs, endTs,
                completedJobResponseTime);
    }

    @Override
    public void printSystemStats(double currentTs) {
        System.out.print("\n\nWeb server : ");
        webServer.printServerStats(currentTs);
    }

    @Override
    public void assignJob(Job job) {
        webServer.addJob(job);
    }

    @Override
    public double computeNextCompletionTs(double endTs) {
        double currRemainingLife, minRemainingLife = INFINITY;

        currRemainingLife = webServer.getMinRemainingLife() * webServer.size() / webServer.getCapacity();
        if (currRemainingLife < minRemainingLife) {
            minRemainingLife = currRemainingLife;
        }

        return endTs + minRemainingLife;
    }

    @Override
    public boolean activeJobExists() {
        return webServer.activeJobExists();
    }

    public int getNumWebServersByState(ServerState state) {
        return 1;
    }

    int webServersSize() {
        return webServer.size();
    }

}
