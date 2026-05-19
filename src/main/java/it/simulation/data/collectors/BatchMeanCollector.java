package it.simulation.data.collectors;

import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;

import java.util.Map;
import java.util.TreeMap;

import static it.simulation.configurations.Config.BATCH_SIZE;

public class BatchMeanCollector implements Collector {
    // This map collect system stats by batch
    private final Map<Integer, Map<Double, SystemStats>> statsByBatch;
    private int currentBatch;

    // Number of completion when this batch was opened
    private int baselineCompletion;

    public BatchMeanCollector() {
        this.statsByBatch = new TreeMap<>();
        this.currentBatch = 0;
        this.baselineCompletion = 0;
    }

    @Override
    public void collect(int runId, double timestamp, Infrastructure infrastructure) {
        // Compute system stats
        SystemStats systemStats = infrastructure.computeSystemStats(timestamp);
        // If is the first insert for the index current batch allocate the map
        statsByBatch.computeIfAbsent(currentBatch, _ -> new TreeMap<>())
                .put(timestamp, systemStats);

        // The completions in this batch  are equals to the current completions minus the completions when the batch was opened
        int currentCompletion = systemStats.getTotalCompletion();
        int completionInThisBatch = currentCompletion - this.baselineCompletion;

        // If the completions in the current batch are more than BATCH_SIZE increment currentBatch so that the next stats are stored in a new batch
        if (completionInThisBatch >= BATCH_SIZE) {
            currentBatch ++;
            this.baselineCompletion = currentCompletion;
        }
    }

    @Override
    public Map<Integer, Map<Double, SystemStats>> getCollectedStats() {
        return statsByBatch;
    }

    @Override
    public void clear() {
        statsByBatch.clear();
    }
}
