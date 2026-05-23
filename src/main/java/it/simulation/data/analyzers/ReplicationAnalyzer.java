package it.simulation.data.analyzers;

import it.simulation.data.boundary.ConfidenceIntervalsCSV;
import it.simulation.system.SystemStats;
import it.simulation.system.servers.ServerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

public class ReplicationAnalyzer implements Analyzer {

    private final List<SystemStats> systemRunMeans;
    private final Map<String, String> systemConfidenceIntervals;
    private final Map<Integer, List<ServerStats>> serverRunMeans;
    private final Map<Integer, Map<String, String>> serversConfidenceIntervals;

    public ReplicationAnalyzer() {
        systemRunMeans = new ArrayList<>();
        systemConfidenceIntervals = new TreeMap<>();
        serverRunMeans = new TreeMap<>();
        serversConfidenceIntervals = new TreeMap<>();
    }

    @Override
    public void analyzeSystemPartially(Map<Double, SystemStats> runStats) {
        TreeMap<Double, SystemStats> statsByTimestamp = (TreeMap<Double, SystemStats>) runStats;

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
        double meanThr = deltaC / deltaT;
        double meanServiceTime = deltaB / deltaC;

        // Compute mean response time in this run
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

        systemRunMeans.add(currBatchSystemStats);
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
        ConfidenceIntervalsCSV.systemConfidenceIntervalCSV(systemConfidenceIntervals);
        ConfidenceIntervalsCSV.serversConfidenceIntervalCSV(serversConfidenceIntervals);
        systemRunMeans.clear();
        serverRunMeans.clear();
        systemConfidenceIntervals.clear();
        serversConfidenceIntervals.clear();
    }

    @Override
    public void computeSystemConfidenceIntervals() {
        computeSystemCIAndPut("BusyServer", SystemStats::getMeanBusyServer);
        computeSystemCIAndPut("ResponseTime", SystemStats::getMeanResponseTime);
        computeSystemCIAndPut("ServiceTime", SystemStats::getMeanServiceTime);
        computeSystemCIAndPut("Throughput", SystemStats::getThroughput);
    }

    @Override
    public void computeServersConfidenceIntervals() {
        for (Map.Entry<Integer, List<ServerStats>> means : serverRunMeans.entrySet()) {
            computeServerCIAndPut(means.getKey(), "ResponseTime", means.getValue(), ServerStats::getCurrMeanResponseTime);
            computeServerCIAndPut(means.getKey(), "Throughput", means.getValue(), ServerStats::getCurrOutputFrequency);
        }
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
        serverRunMeans.computeIfAbsent(start.getServerIndex(), _ -> new ArrayList<>()).add(currentServerStats);
    }

    private void computeSystemCIAndPut(String label, ToDoubleFunction<SystemStats> extractor) {
        Map.Entry<String, String> ci = computeConfidenceInterval(systemRunMeans, label, extractor);
        systemConfidenceIntervals.put(ci.getKey(), ci.getValue());
    }

    private void computeServerCIAndPut(int index, String label, List<ServerStats> serverStats, ToDoubleFunction<ServerStats> extractor) {
        Map.Entry<String, String> ci = computeConfidenceInterval(serverStats, label, extractor);
        serversConfidenceIntervals.computeIfAbsent(index, _ -> new TreeMap<>()).put(ci.getKey(), ci.getValue());
    }

}
