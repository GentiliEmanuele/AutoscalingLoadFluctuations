package it.simulation.data.collectors;

import it.simulation.system.SystemStats;
import it.simulation.system.infrastructures.Infrastructure;

import java.util.Map;

public interface Collector {
    void collect(int runId, double timestamp, Infrastructure infrastructure);
    Map<Integer, Map<Double, SystemStats>> getCollectedStats();
    void clear();
}
