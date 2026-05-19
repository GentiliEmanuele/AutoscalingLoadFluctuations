package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.data.analyzers.AnalyzerFactory;
import it.simulation.data.boundary.SystemStatsCSV;
import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;

import java.util.Map;
import java.util.TreeMap;

import static it.simulation.configurations.Config.BATCH_SIZE;

public class BatchMeanCollector implements Collector {
    private final Map<Double, SystemStats> statsByTimestamp;
    private final Analyzer analyzer;


    // Number of completion when this batch was opened
    private int baselineCompletion;

    public BatchMeanCollector(Analyzer analyzer) {
        this.statsByTimestamp = new TreeMap<>();
        this.analyzer = analyzer;
        this.baselineCompletion = 0;
    }

    @Override
    public void collect(double timestamp, Infrastructure infrastructure) {
        // Compute system stats
        SystemStats systemStats = infrastructure.computeSystemStats(timestamp);
        // If is the first insert for the index current batch allocate the map
        statsByTimestamp.put(timestamp, systemStats);

        // The completions in this batch  are equals to the current completions minus the completions when the batch was opened
        int currentCompletion = systemStats.getTotalCompletion();
        int completionInThisBatch = currentCompletion - this.baselineCompletion;

        // If the completions in the current batch are more than BATCH_SIZE compute stats for this batch, save data in a CSV file and delete data
        if (completionInThisBatch >= BATCH_SIZE) {
            analyzeAndPush(0);
            this.baselineCompletion = currentCompletion;
        }
    }

    @Override
    public void analyzeAndPush(int runId) {
        analyzer.analyzePartially(statsByTimestamp);
        SystemStatsCSV.systemStatsToCSV(runId, statsByTimestamp);
        statsByTimestamp.clear();
    }
}
