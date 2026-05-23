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
    private final List<SystemStats> systemBatchMeans;
    private final Map<String, String> confidenceIntervals;
    private final Map<Integer, List<ServerStats>> serverBatchMeans;
    private final Map<Integer, Map<String, String>> serversConfidenceIntervals;


    public BatchMeanAnalyzer() {
        systemBatchMeans = new ArrayList<>();
        confidenceIntervals = new TreeMap<>();
        serverBatchMeans = new TreeMap<>();
        serversConfidenceIntervals = new TreeMap<>();
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
        double meanThr = deltaC / deltaT;
        double meanServiceTime = deltaB / deltaC;

        // Compute mean response time in this batch
        double startTotalResponseTime = start.getMeanResponseTime() * start.getTotalCompletion();
        double endTotalResponseTime = end.getMeanResponseTime() * end.getTotalCompletion();
        double deltaResponseTime = endTotalResponseTime - startTotalResponseTime;
        double meanResponseTime = deltaResponseTime / deltaC;

        SystemStats currBatchSystemStats = new SystemStats(
                meanThr,
                meanBusyServers,
                meanServiceTime,
                meanResponseTime,
                deltaC,
                deltaB
        );
        systemBatchMeans.add(currBatchSystemStats);
    }

    @Override
    public void analyzeServersPartially(Map<Double, List<ServerStats>> stats) {
        TreeMap<Double, List<ServerStats>> statsByTimestamp = (TreeMap<Double, List<ServerStats>>) stats;

        // Get first and last run state
        List<ServerStats> start = statsByTimestamp.firstEntry().getValue();
        List<ServerStats> end = statsByTimestamp.lastEntry().getValue();

        // Compute run duration
        double deltaT = statsByTimestamp.lastEntry().getKey() - statsByTimestamp.firstEntry().getKey();

        for (int i = 0; i < end.size(); i++) {
            analyzeServerPartially(start.get(i), end.get(i), deltaT);
        }
    }

    @Override
    public void pushAndClear() {
        ConfidenceIntervalsCSV.systemConfidenceIntervalCSV(confidenceIntervals);
        ConfidenceIntervalsCSV.serversConfidenceIntervalCSV(serversConfidenceIntervals);
        systemBatchMeans.clear();
        confidenceIntervals.clear();
        serversConfidenceIntervals.clear();
        serverBatchMeans.clear();
    }

    @Override
    public void computeSystemConfidenceIntervals() {
        computeCIAndPut("BusyServer", SystemStats::getMeanBusyServer);
        computeCIAndPut("ResponseTime", SystemStats::getMeanResponseTime);
        computeCIAndPut("ServiceTime", SystemStats::getMeanServiceTime);
        computeCIAndPut("Throughput", SystemStats::getThroughput);
        computeCIAndPut("Population", SystemStats::getMeanPopulation);
    }

    @Override
    public void computeServersConfidenceIntervals() {
        for (Map.Entry<Integer, List<ServerStats>> means : serverBatchMeans.entrySet()) {
            computeServerCIAndPut(means.getKey(), "ResponseTime", means.getValue(), ServerStats::getCurrMeanResponseTime);
            computeServerCIAndPut(means.getKey(), "Throughput", means.getValue(), ServerStats::getCurrOutputFrequency);
        }
    }

    private void computeCIAndPut(String label, ToDoubleFunction<SystemStats> extractor) {
        Map.Entry<String, String> ci = computeConfidenceInterval(systemBatchMeans, label, extractor);
        confidenceIntervals.put(ci.getKey(), ci.getValue());
    }

    private void analyzeServerPartially(ServerStats start, ServerStats end, double deltaT) {
        double deltaN = end.getNodeSum() - start.getNodeSum();
        int deltaC = end.getCompletedJobs() - start.getCompletedJobs();
        int deltaA = end.getArrivedJobs() - start.getArrivedJobs();
        double deltaB = end.getServiceSum() - start.getServiceSum();

        double outFreq = deltaC / deltaT;

        double startTotalResponseTime = start.getCurrMeanResponseTime() * start.getCompletedJobs();
        double endTotalResponseTime = end.getCurrMeanResponseTime() * end.getCompletedJobs();
        double deltaResponseTime = endTotalResponseTime - startTotalResponseTime;
        double meanResponseTime = deltaResponseTime / deltaC;

        ServerStats currentServerStats = new ServerStats(start.getServerIndex(), deltaN, deltaB, deltaC, deltaA, meanResponseTime, outFreq);
        serverBatchMeans.computeIfAbsent(start.getServerIndex(), _ -> new ArrayList<>()).add(currentServerStats);
    }

    private void computeServerCIAndPut(int index, String label, List<ServerStats> serverStats, ToDoubleFunction<ServerStats> extractor) {
        Map.Entry<String, String> ci = computeConfidenceInterval(serverStats, label, extractor);
        serversConfidenceIntervals.computeIfAbsent(index, _ -> new TreeMap<>()).put(ci.getKey(), ci.getValue());
    }
}
