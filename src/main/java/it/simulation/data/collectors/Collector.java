package it.simulation.data.collectors;

import it.simulation.system.infrastructures.Infrastructure;

import java.util.Map;

public interface Collector {
    void collect(double timestamp, Infrastructure infrastructure);
    void analyzeAndPush(int runId);
}
