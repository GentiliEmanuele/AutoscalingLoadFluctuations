package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;

import static it.simulation.configurations.Config.REPETITION_NUMBER;

public class CollectorFactory {
    public static Collector createCollector(Analyzer analyzer) {
        return REPETITION_NUMBER == 1 ? new BatchMeanCollector(analyzer) : new ReplicationCollector(analyzer);
    }
}
