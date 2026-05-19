package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.data.analyzers.AnalyzerFactory;
import it.simulation.data.boundary.SystemStatsCSV;
import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;

import java.util.Map;
import java.util.TreeMap;

public class ReplicationCollector implements Collector {

    private final Map<Double, SystemStats> statsByTimestamp;
    private final Analyzer analyzer;

    public ReplicationCollector(Analyzer analyzer) {
        this.statsByTimestamp = new TreeMap<>();
        this.analyzer = analyzer;
    }

    @Override
    public void collect(double timestamp, Infrastructure infrastructure) {
        // Compute system stats
        SystemStats systemStats = infrastructure.computeSystemStats(timestamp);

        // Save the current stats in a temporary map. This map will be cleared when a replica end and the mean was computed.
        this.statsByTimestamp.put(timestamp, systemStats);
    }

    @Override
    public void analyzeAndPush(int runId) {
        analyzer.analyzePartially(statsByTimestamp);
        SystemStatsCSV.systemStatsToCSV(runId, statsByTimestamp);
        statsByTimestamp.clear();
    }
}
