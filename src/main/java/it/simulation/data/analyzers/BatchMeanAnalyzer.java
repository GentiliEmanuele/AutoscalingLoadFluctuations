package it.simulation.data.analyzers;


import it.simulation.data.boundary.ConfidenceIntervalsCSV;
import it.simulation.system.SystemStats;
import it.simulation.system.servers.ServerStats;

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
    public void analyzeSystemPartially(Map<Double, SystemStats> statsByBatch) {
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
    public void analyzeServersPartially(Map<Double, List<ServerStats>> stats) {

    }

    @Override
    public void pushAndClear() {
        ConfidenceIntervalsCSV.systemConfidenceIntervalCSV(confidenceIntervals);
        batchMeans.clear();
        confidenceIntervals.clear();
    }

    @Override
    public void computeSystemConfidenceIntervals() {
        computeCIAndPut("BusyServer", SystemStats::getMeanBusyServer);
        computeCIAndPut("ResponseTime", SystemStats::getMeanResponseTime);
        computeCIAndPut("ServiceTime", SystemStats::getMeanServiceTime);
        computeCIAndPut("Throughput", SystemStats::getThroughput);
        computeCIAndPut("Utilization", SystemStats::getMeanUtilization);
    }

    @Override
    public void computeServersConfidenceIntervals() {

    }

    private void computeCIAndPut(String label, ToDoubleFunction<SystemStats> extractor) {
        Map.Entry<String, String> ci = computeConfidenceInterval(batchMeans, label, extractor);
        confidenceIntervals.put(ci.getKey(), ci.getValue());
    }
}
