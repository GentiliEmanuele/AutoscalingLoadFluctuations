package it.simulation.data.analyzers;

import it.simulation.system.SystemStats;
import it.simulation.system.servers.ServerStats;
import org.apache.commons.math3.distribution.TDistribution;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static it.simulation.configurations.Config.CONFIDENCE_LEVEL;
import static it.simulation.configurations.Config.REPETITION_NUMBER;

public interface Analyzer {
    void analyzeSystemPartially(Map<Double, SystemStats> stats);
    void analyzeServersPartially(Map<Double, List<ServerStats>> stats);
    void pushAndClear();
    void computeSystemConfidenceIntervals();
    void computeServersConfidenceIntervals();

    static double computeMean(List<Double> means) {
        if (means.isEmpty()) return 0.0;
        // System.out.println(means.stream().mapToDouble(Double::doubleValue).filter(Double::isNaN).count());
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

    default <T> List<Double> extractMetric(List<T> stats, ToDoubleFunction<T> extractor) {
        assert stats.stream()
                .mapToDouble(extractor)
                .filter(Double::isInfinite).findAny().isEmpty() :
                "Infinite value found";

        assert stats.stream()
                .mapToDouble(extractor)
                .filter(Double::isNaN).findAny().isEmpty() :
                "NaN value found";


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

    default <T> Map.Entry<String, String> computeConfidenceInterval(List<T> inputStats, String label, ToDoubleFunction<T> extractor) {
        // Extract desired values
        List<Double> values = extractMetric(inputStats, extractor);

        double mean = Analyzer.computeMean(values);
        double var = Analyzer.computeVariance(values, mean);
        double rho = Analyzer.computeAutocorrelation(values, mean, var);
        double rhoLimit = 2 / Math.sqrt(inputStats.size());
        double halfWidth = Analyzer.computeHalfWidth(values.size(), var);

        // Check autocorrelation only for batch mean method
        if (REPETITION_NUMBER == 1) assert Math.abs(rho) <= rhoLimit : String.format("Autocorrelation is more than %.6f for %s\n", rhoLimit, label);

        return Map.entry(label, String.format("%.6f +/- %.6f", mean, halfWidth));
    }
}
