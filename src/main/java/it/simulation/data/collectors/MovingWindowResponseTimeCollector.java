package it.simulation.data.collectors;

import it.simulation.data.boundary.MovingWindowResponseTimeCSV;

import java.util.Map;
import java.util.TreeMap;

public class MovingWindowResponseTimeCollector {
    private static final Map<Double, Double> mwResponseTimeByTimestamp = new TreeMap<>();

    public static void collectMWResponseTime(double timestamp, double responseTime) {
        mwResponseTimeByTimestamp.put(timestamp, responseTime);
    }

    public static void pushAndClear() {
        for (Map.Entry<Double, Double> entry : mwResponseTimeByTimestamp.entrySet()) {
            MovingWindowResponseTimeCSV.movingWindowResponseTime(entry.getKey(), entry.getValue());
        }

        mwResponseTimeByTimestamp.clear();
    }
}
