package it.simulation.data.boundary;

import com.opencsv.CSVWriter;
import it.simulation.system.SystemStats;
import it.simulation.system.servers.ServerStats;
import it.simulation.system.servers.ServerStatsUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static it.simulation.configurations.Config.EXPERIMENT;
import static it.simulation.configurations.Config.REPETITION_NUMBER;

public class ServerStatsCSV {
    private final static String [] HEADER = {
            REPETITION_NUMBER == 1 ? "Batch Id" : "Run Id",
            "Timestamp",
            "ServerIndex",
            "Throughput",
            "Utilization",
            "ServiceTime",
            "ResponseTime"
    };

    private static boolean isFirstWrite = true;

    public static void  serverStatsToCSV(int runId, Map<Double, List<ServerStats>> runStats) {
        String outputPath = String.format("output/%s_servers_stats.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {
            /* Write the header */
            if (isFirstWrite) {
                csvWriter.writeNext(HEADER);
                isFirstWrite = false;
            }

            for (Map.Entry<Double, List<ServerStats>> statsByTimestamp : runStats.entrySet()) {
                double timestamp = statsByTimestamp.getKey();
                List<ServerStats> serverStatsList = statsByTimestamp.getValue();
                for (ServerStats serverStats : serverStatsList) {
                    // Extract values from servers stats instance
                    String[] row = getRow(serverStats, runId, timestamp);

                    /* Write the row into the csv */
                    csvWriter.writeNext(row);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getRow(ServerStats serverStats, int runId, double timestamp) {
        return new String[] {
                String.valueOf(runId),
                String.valueOf(timestamp),
                String.valueOf(serverStats.getServerIndex()),
                String.valueOf(ServerStatsUtils.computeThroughput(timestamp, serverStats)),
                String.valueOf(ServerStatsUtils.computeUtilization(timestamp, serverStats)),
                String.valueOf(ServerStatsUtils.computeServiceTime(serverStats)),
                String.valueOf(serverStats.getCurrMeanResponseTime())
        };
    }
}
