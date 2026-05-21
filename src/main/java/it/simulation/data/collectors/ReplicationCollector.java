package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.data.boundary.SystemStatsCSV;
import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.servers.ServerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static it.simulation.configurations.Config.LOG_FINE;

public class ReplicationCollector implements Collector {

    private final Map<Double, SystemStats> systemStatsByTimestamp;
    private final Map<Double, List<ServerStats>> serversStatsByTimestamp;
    private final Analyzer analyzer;

    public ReplicationCollector(Analyzer analyzer) {
        this.systemStatsByTimestamp = new TreeMap<>();
        this.serversStatsByTimestamp = new TreeMap<>();
        this.analyzer = analyzer;
    }

    @Override
    public void collect(double timestamp, Infrastructure infrastructure) {
        // Compute system and servers stats
        SystemStats systemStats = infrastructure.computeSystemStats(timestamp);
        List<ServerStats> serverStats = infrastructure.getServersStats(timestamp);

        // Save the current (system and servers) stats in a temporary map. This map will be cleared when a replica end and the mean was computed.
        this.systemStatsByTimestamp.put(timestamp, systemStats);
        this.serversStatsByTimestamp.computeIfAbsent(timestamp, _ -> new ArrayList<>(serverStats));
    }

    @Override
    public void analyzeAndPush(int runId) {
        analyzer.analyzeSystemPartially(systemStatsByTimestamp);
        analyzer.analyzeServersPartially(serversStatsByTimestamp);
        if (LOG_FINE) SystemStatsCSV.systemStatsToCSV(runId, systemStatsByTimestamp);
        systemStatsByTimestamp.clear();
        serversStatsByTimestamp.clear();
    }
}
