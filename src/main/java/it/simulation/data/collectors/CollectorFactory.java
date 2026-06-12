package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;

import static it.simulation.configurations.Config.REPETITION_NUMBER;
import static it.simulation.configurations.Config.SERVERS_LEVEL_BATCH_MEAN;

public class CollectorFactory {
    public static Collector createCollector(Analyzer analyzer) {
        return REPETITION_NUMBER == 1 ?
                SERVERS_LEVEL_BATCH_MEAN ? new ServersTemporalBatchMeanCollector(analyzer) : new BatchMeanCollector(analyzer) :
                new ReplicationCollector(analyzer);
    }
}
