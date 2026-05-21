package it.simulation.system.servers;

import it.simulation.system.jobs.Job;
import lombok.Getter;
import lombok.Setter;

public class WebServer extends AbstractServer {
    @Getter @Setter
    private Double activationTimestamp;

    public WebServer(double capacity, ServerState serverState, int index) {
        super(capacity, serverState, index);
    }

    @Override
    public void removeJob(Job job) {
        jobs.removeJob(job);
    }
}
