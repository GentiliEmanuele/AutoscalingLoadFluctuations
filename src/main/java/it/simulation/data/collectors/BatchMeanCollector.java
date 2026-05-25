package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.data.boundary.NumServerByTimestampCSV;
import it.simulation.data.boundary.ServersJobsNumberByTimestamp;
import it.simulation.data.boundary.SystemStatsCSV;
import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.servers.ServerState;
import it.simulation.system.servers.ServerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static it.simulation.configurations.Config.*;

public class BatchMeanCollector implements Collector {
    private final Map<Double, SystemStats> statsByTimestamp;
    private final Map<Double, List<ServerStats>> serversStatsByTimestamp;
    private final Map<Double, Integer> numServersByTimestamp;
    private final Map<Integer, Map<Double, Integer>> serversJobsNumberByTimestamp;
    private final Analyzer analyzer;


    // Number of completion when this batch was opened
    private int baselineCompletion;

    public BatchMeanCollector(Analyzer analyzer) {
        this.statsByTimestamp = new TreeMap<>();
        this.serversStatsByTimestamp = new TreeMap<>();
        this.analyzer = analyzer;
        this.baselineCompletion = 0;
        this.serversJobsNumberByTimestamp = new TreeMap<>();
        this.numServersByTimestamp = new TreeMap<>();
    }

    @Override
    public void collect(double timestamp, Infrastructure infrastructure) {
        // Compute system and servers stats
        SystemStats systemStats = infrastructure.computeSystemStats(timestamp);
        List<ServerStats> serverStats = infrastructure.getServersStats(timestamp);

        statsByTimestamp.put(timestamp, systemStats);
        this.serversStatsByTimestamp.computeIfAbsent(timestamp, _ -> new ArrayList<>(serverStats));

        // The completions in this batch  are equals to the current completions minus the completions when the batch was opened
        int currentCompletion = systemStats.getTotalCompletion();
        int completionInThisBatch = currentCompletion - this.baselineCompletion;

        Collector.collectServersJobsNumber(serversJobsNumberByTimestamp, serverStats, timestamp);

        // If the completions in the current batch are more than BATCH_SIZE compute stats for this batch, save data in a CSV file and delete data
        if (completionInThisBatch >= BATCH_SIZE) {
            analyzeAndPush(0);
            this.baselineCompletion = currentCompletion;
        }

        // Save the number of server at the current timestamp
        numServersByTimestamp.put(timestamp, infrastructure.getNumWebServersByState(ServerState.ACTIVE));
    }

    @Override
    public void analyzeAndPush(int runId) {
        analyzer.analyzeSystemPartially(statsByTimestamp);
        analyzer.analyzeServersPartially(serversStatsByTimestamp);
        if (LOG_FINE) {
            SystemStatsCSV.systemStatsToCSV(runId, statsByTimestamp);
            ServersJobsNumberByTimestamp.serversJobsNumberByTimestampCSV(serversJobsNumberByTimestamp);
            NumServerByTimestampCSV.numServerByTimestampCSV(runId, numServersByTimestamp);
        }
        statsByTimestamp.clear();
        serversStatsByTimestamp.clear();
        serversJobsNumberByTimestamp.clear();
    }
}
