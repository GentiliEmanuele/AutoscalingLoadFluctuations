package it.simulation.data.analyzers;


import it.simulation.system.SystemStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

public class BatchMeanAnalyzer implements Analyzer {
    private final List<SystemStats> batchMeans;

    public BatchMeanAnalyzer() {
        batchMeans = new ArrayList<>();
    }

    @Override
    public void analyze(Map<Integer, Map<Double, SystemStats>> statsByBatch) {
        computeBatchesStats(statsByBatch);
        computeStats("System response time", SystemStats::getMeanResponseTime);
        computeStats("Throughput", SystemStats::getThroughput);
        computeStats("Utilization", SystemStats::getMeanUtilization);

        System.out.printf("Batch num. %d \n", batchMeans.size());
    }

    @Override
    public void clear() {
        batchMeans.clear();
    }

    private void computeBatchesStats(Map<Integer, Map<Double, SystemStats>> statsByBatch) {
        for (Map.Entry<Integer, Map<Double, SystemStats>> entry : statsByBatch.entrySet()) {
            TreeMap<Double, SystemStats> statsByTimestamp = (TreeMap<Double, SystemStats>) entry.getValue();

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
    }

    private void computeStats(String label, ToDoubleFunction<SystemStats> extractor) {
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
    }
}
