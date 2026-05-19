package it.simulation.data.analyzers;


import it.simulation.data.boundary.ConfidenceIntervalCSV;
import it.simulation.system.SystemStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

public class BatchMeanAnalyzer implements Analyzer {
    private final List<SystemStats> batchMeans;
    private final Map<String, String> confidenceIntervals;


    public BatchMeanAnalyzer() {
        batchMeans = new ArrayList<>();
        confidenceIntervals = new TreeMap<>();

    }

    @Override
    public void analyzePartially(Map<Double, SystemStats> statsByBatch) {
        TreeMap<Double, SystemStats> statsByTimestamp = (TreeMap<Double, SystemStats>) statsByBatch;

        // Get first and last batch state
        SystemStats start = statsByTimestamp.firstEntry().getValue();
        SystemStats end = statsByTimestamp.lastEntry().getValue();

        // Compute batch duration
        double deltaT = statsByTimestamp.lastEntry().getKey() - statsByTimestamp.firstEntry().getKey();

        // Compute delta for completion and busy time
        int deltaC = end.getTotalCompletion() - start.getTotalCompletion();
        double deltaB = end.getTotalBusyTime() - start.getTotalBusyTime();

        // Compute mean metrics in this batch
        double meanBusyServers = deltaB / deltaT;
        double meanUtilization = meanBusyServers / 2; // TODO change based on effective num server
        double meanThr = deltaC / deltaT;
        double meanServiceTime = deltaB / deltaC;

        // Compute mean response time in this batch
        double startTotalResponseTime = start.getMeanResponseTime() * start.getTotalCompletion();
        double endTotalResponseTime = end.getMeanResponseTime() * end.getTotalCompletion();
        double deltaResponseTime = endTotalResponseTime - startTotalResponseTime;
        double meanResponseTime = deltaResponseTime / deltaC;

        SystemStats currBatchSystemStats = new SystemStats(
                meanThr,
                meanUtilization,
                meanBusyServers,
                meanServiceTime,
                meanResponseTime,
                deltaC,
                deltaB
        );
        batchMeans.add(currBatchSystemStats);
    }

    @Override
    public void pushAndClear() {
        ConfidenceIntervalCSV.confidenceIntervalCSV(confidenceIntervals);
        batchMeans.clear();
        confidenceIntervals.clear();
    }

    @Override
    public void computeConfidenceIntervals() {
        computeConfidenceInterval("BusyServer", SystemStats::getMeanBusyServer);
        computeConfidenceInterval("ResponseTime", SystemStats::getMeanResponseTime);
        computeConfidenceInterval("ServiceTime", SystemStats::getMeanServiceTime);
        computeConfidenceInterval("Throughput", SystemStats::getThroughput);
        computeConfidenceInterval("Utilization", SystemStats::getMeanUtilization);
    }

    private void computeConfidenceInterval(String label, ToDoubleFunction<SystemStats> extractor) {
        // Extract desired values
        List<Double> values = Analyzer.extractMetric(this.batchMeans, extractor);

        double mean = Analyzer.computeMean(values);
        double var = Analyzer.computeVariance(values, mean);
        double rho = Analyzer.computeAutocorrelation(values, mean, var);
        double rhoLimit = 2 / Math.sqrt(batchMeans.size());
        double halfWidth = Analyzer.computeHalfWidth(values.size(), var);

        if (rho > rhoLimit) System.out.printf("Autocorrelation is more than %.6f\n", rhoLimit);

        System.out.printf("[%s] Mean: %.6f | Variance: %.6f | Autocorrelation: %.6f\n",
                label, mean, var, rho);
        System.out.printf("CI 95%%: [%.6f, %.6f] (± %.6f)\n", mean - halfWidth, mean + halfWidth, halfWidth);

        // Save the confidence interval for write it into CSV file
        confidenceIntervals.put(label, String.format("%.6f +/- %.6f", mean, halfWidth));
    }
}
