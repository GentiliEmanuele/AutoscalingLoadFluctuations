package it.simulation.system.servers;

import it.simulation.system.jobs.Job;

public class WebServer extends AbstractServer {
    public WebServer(double capacity, ServerState serverState, int index) {
        super(capacity, serverState, index);
    }

    @Override
    public void removeJob(Job job) {
        jobs.removeJob(job);
    }
}
