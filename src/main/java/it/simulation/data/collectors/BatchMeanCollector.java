package it.simulation.data.collectors;

import it.simulation.system.SystemStats;

import java.util.Map;

public class BatchMeanCollector implements Collector {
    @Override
    public void collect(int runId, double timestamp, SystemStats systemStats) {

    }

    @Override
    public Map<Integer, Map<Double, SystemStats>> getStatsByRun() {
        return Map.of();
    }
}
