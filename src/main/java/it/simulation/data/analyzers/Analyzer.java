package it.simulation.data.analyzers;

import it.simulation.system.SystemStats;
import org.apache.commons.math3.distribution.TDistribution;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static it.simulation.configurations.Config.CONFIDENCE_LEVEL;

public interface Analyzer {
    void analyze(Map<Integer, Map<Double, SystemStats>> stats);

    static double computeMean(List<Double> means) {
        if (means.isEmpty()) return 0.0;
        return means.stream().mapToDouble(Double::doubleValue).sum() / means.size();
    }

    static double computeVariance(List<Double> means, double mean) {
        if (means.size() < 2) return 0.0;

        double sumSq = 0.0;
        for (double m : means) {
            sumSq += Math.pow(m - mean, 2);
        }

        return sumSq / (means.size() - 1);
    }

    static double computeAutocorrelation(List<Double> means, double mean, double variance) {
        int n = means.size();
        if (n < 2 || variance == 0) return 0.0;

        double covarianceSum = 0.0;
        for (int i = 0; i < n - 1; i++) {
            covarianceSum += (means.get(i) - mean) * (means.get(i + 1) - mean);
        }

        return (covarianceSum / (n - 1)) / variance;
    }

    static List<Double> extractMetric(List<SystemStats> stats, ToDoubleFunction<SystemStats> extractor) {
        return stats.stream()
                .mapToDouble(extractor)
                .boxed()
                .collect(Collectors.toList());
    }

    static double computeHalfWidth(int n, double variance) {
        TDistribution tDist = new TDistribution(n - 1);
        double tCritical = Math.abs(tDist.inverseCumulativeProbability((1 - CONFIDENCE_LEVEL) / 2.0));
        double stdError = Math.sqrt(variance) / Math.sqrt(n);
        return tCritical * stdError;
    }
}
