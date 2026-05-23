package it.simulation.data.collectors;

import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.servers.ServerStats;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface Collector {
    void collect(double timestamp, Infrastructure infrastructure);
    void analyzeAndPush(int runId);

    static void collectServersJobsNumber(Map<Integer, Map<Double, Integer>> serversJobsNumbersByTimestamp, List<ServerStats> serverStats, Double timestamp) {
        for (ServerStats serverStat : serverStats) {
            serversJobsNumbersByTimestamp.computeIfAbsent(serverStat.getServerIndex(), _ -> new TreeMap<>())
                    .put(timestamp, serverStat.getArrivedJobs() - serverStat.getCompletedJobs());
        }
    }
}
