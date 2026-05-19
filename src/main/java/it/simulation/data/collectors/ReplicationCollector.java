package it.simulation.data.collectors;

import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;

import java.util.Map;
import java.util.TreeMap;

public class ReplicationCollector implements Collector {

    private final Map<Integer, Map<Double, SystemStats>> statsByRun;

    public ReplicationCollector() {
        this.statsByRun = new TreeMap<>();
    }

    @Override
    public void collect(int runId, double timestamp, Infrastructure infrastructure) {
        // Compute system stats
        SystemStats systemStats = infrastructure.computeSystemStats(timestamp);
        // Check if already exists the temporal map for this run
        // If not create it and add the new stats
        this.statsByRun.computeIfAbsent(runId, _ -> new TreeMap<>())
                .put(timestamp, systemStats);
    }

    @Override
    public Map<Integer, Map<Double, SystemStats>> getCollectedStats() {
        return statsByRun;
    }

    @Override
    public void clear() {
        statsByRun.clear();
    }
}
