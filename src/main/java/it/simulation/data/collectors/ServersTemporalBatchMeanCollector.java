package it.simulation.data.collectors;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.servers.ServerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class ServersTemporalBatchMeanCollector implements Collector {

    private final Map<Double, List<ServerStats>> serversStatsByTimestamp;
    private double baselineTimestamp;
    private final Analyzer analyzer;

    public ServersTemporalBatchMeanCollector(Analyzer analyzer) {
        this.analyzer = analyzer;
        serversStatsByTimestamp = new TreeMap<>();
        baselineTimestamp = 0;
    }


    @Override
    public void collect(double timestamp, Infrastructure infrastructure) {
        List<ServerStats> serverStats = infrastructure.getServersStats(timestamp);
        this.serversStatsByTimestamp.computeIfAbsent(timestamp, _ -> new ArrayList<>(serverStats));

        if (timestamp - baselineTimestamp >= 20000) {
            analyzeAndPush(0);
            baselineTimestamp = timestamp;
        }
    }

    @Override
    public void analyzeAndPush(int runId) {
        analyzer.analyzeServersPartially(serversStatsByTimestamp);
        serversStatsByTimestamp.clear();
        baselineTimestamp = 0;
    }
}
