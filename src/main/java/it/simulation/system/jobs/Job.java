package it.simulation.system.jobs;

import it.simulation.events.IllegalLifeException;
import lombok.Getter;

@Getter
public class Job {
    private final double arrivalTime;
    private double remainingLife;

    public Job(double arrivalTime, double executionLife) {
        this.arrivalTime = arrivalTime;
        this.remainingLife = executionLife;
    }

    public void decreaseRemainingLife(double executedTime) throws IllegalLifeException {
        this.remainingLife -= executedTime;
        if (remainingLife < 0) {
            System.out.println("Remaining life: " + remainingLife);
            throw new IllegalLifeException("Remaining time cannot be negative");
        }
    }
}
