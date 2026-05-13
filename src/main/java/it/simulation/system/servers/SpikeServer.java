package it.simulation.system.servers;

import it.simulation.system.jobs.Job;

public class SpikeServer extends AbstractServer {
    public SpikeServer(double capacity) {
        super(capacity, ServerState.ACTIVE, 0);
    }

    @Override
    public void removeJob(Job job) {
        jobs.removeJob(job);
    }

    public void setCapacity(double newCapacity) {
        this.capacity = newCapacity;
    }
}
