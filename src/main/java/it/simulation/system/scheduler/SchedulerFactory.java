package it.simulation.system.scheduler;

import static it.simulation.configurations.Config.SCHEDULER_TYPE;

public class SchedulerFactory {
    public static Scheduler create() {
        return switch (SCHEDULER_TYPE) {
            case "leastUsed" -> new LeastUsed();
            case "roundRobin" -> new RoundRobin();
            default -> throw new IllegalArgumentException("Invalid scheduler type: " + SCHEDULER_TYPE);
        };
    }
}
