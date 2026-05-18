package it.simulation.data.analyzers;


import static it.simulation.configurations.Config.REPETITION_NUMBER;

public class AnalyzerFactory {
    public static Analyzer createAnalyzer() {
        return REPETITION_NUMBER == 1 ? new BatchMeanAnalyzer() : new ReplicationAnalyzer();
    }
}
