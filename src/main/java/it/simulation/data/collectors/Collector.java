package it.simulation.data.collectors;

import it.simulation.system.SystemStats;

import java.util.Map;

public interface Collector {
    void collect(int runId, double timestamp, SystemStats systemStats);
    Map<Integer, Map<Double, SystemStats>> getStatsByRun();
}
