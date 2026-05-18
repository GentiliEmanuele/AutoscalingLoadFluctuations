package it.simulation.data.analyzers;

import it.simulation.data.boundary.ConfidenceIntervalCSV;
import it.simulation.system.SystemStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

public class ReplicationAnalyzer implements Analyzer {

    private final List<SystemStats> runMeans;
    private final Map<String, String> confidenceIntervals;

    public ReplicationAnalyzer() {
        runMeans = new ArrayList<>();
        confidenceIntervals = new TreeMap<>();
    }

    @Override
    public void analyze(Map<Integer, Map<Double, SystemStats>> statsByRun) {
        computeReplicationStats(statsByRun);
        computeStats("BusyServer", SystemStats::getMeanBusyServer);
        computeStats("ResponseTime", SystemStats::getMeanResponseTime);
        computeStats("ServiceTime", SystemStats::getMeanServiceTime);
        computeStats("Throughput", SystemStats::getThroughput);
        computeStats("Utilization", SystemStats::getMeanUtilization);

        ConfidenceIntervalCSV.confidenceIntervalCSV(confidenceIntervals);
    }

    private void computeReplicationStats(Map<Integer, Map<Double, SystemStats>> statsByRun) {
        for (Map.Entry<Integer, Map<Double, SystemStats>> entry : statsByRun.entrySet()) {
            TreeMap<Double, SystemStats> statsByTimestamp = (TreeMap<Double, SystemStats>) entry.getValue();

            // Get first and last run state
            SystemStats start = statsByTimestamp.firstEntry().getValue();
            SystemStats end = statsByTimestamp.lastEntry().getValue();

            // Compute run duration
            double deltaT = statsByTimestamp.lastEntry().getKey() - statsByTimestamp.firstEntry().getKey();

            // Compute delta for completion and busy time
            int deltaC = end.getTotalCompletion() - start.getTotalCompletion();
            double deltaB = end.getTotalBusyTime() - start.getTotalBusyTime();

            // Compute mean metrics in this run
            double meanBusyServers = deltaB / deltaT;
            double meanUtilization = meanBusyServers / 2; // TODO change based on effective num server
            double meanThr = deltaC / deltaT;
            double meanServiceTime = deltaB / deltaC;

            // Compute mean response time in this run
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

            runMeans.add(currBatchSystemStats);
        }
    }

    private void computeStats(String label, ToDoubleFunction<SystemStats> extractor) {
        // Extract desired values
        List<Double> values = Analyzer.extractMetric(this.runMeans, extractor);

        double mean = Analyzer.computeMean(values);
        double var = Analyzer.computeVariance(values, mean);
        double rho = Analyzer.computeAutocorrelation(values, mean, var);
        double rhoLimit = 2 / Math.sqrt(runMeans.size());
        double halfWidth = Analyzer.computeHalfWidth(values.size(), var);

        if (rho > rhoLimit) System.out.printf("Autocorrelation is more than %.6f\n", rhoLimit);

        System.out.printf("[%s] Mean: %.6f | Variance: %.6f | Autocorrelation: %.6f\n",
                label, mean, var, rho);
        System.out.printf("CI 95%%: [%.6f, %.6f] (± %.6f)\n", mean - halfWidth, mean + halfWidth, halfWidth);

        // Save the confidence interval for write it into CSV file
        confidenceIntervals.put(label, String.format("%.6f +/- %.6f", mean, halfWidth));
    }
}
