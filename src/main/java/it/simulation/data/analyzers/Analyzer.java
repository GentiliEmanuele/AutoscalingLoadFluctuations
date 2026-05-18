package it.simulation.data.analyzers;

import it.simulation.system.SystemStats;

import java.util.Map;

public interface Analyzer {
    void analyze(Map<Integer, Map<Double, SystemStats>> stats);
}
