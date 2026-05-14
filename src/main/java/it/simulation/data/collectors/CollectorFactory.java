package it.simulation.data.collectors;

import static it.simulation.configurations.Config.REPETITION_NUMBER;

public class CollectorFactory {
    public static Collector createCollector() {
        return REPETITION_NUMBER == 1 ? new BatchMeanCollector() : new ReplicationCollector();
    }
}
