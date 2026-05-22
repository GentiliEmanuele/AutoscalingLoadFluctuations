package it.simulation.data.boundary;

import com.opencsv.CSVWriter;
import it.simulation.system.SystemStats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.*;

public class SystemStatsCSV {

    private final static String [] HEADER = {
            REPETITION_NUMBER == 1 ? "Batch Id" : "Run Id",
            "Timestamp",
            "Throughput",
            "BusyServer",
            "ServiceTime",
            "ResponseTime"
    };

    public static void systemStatsToCSV(int runId, Map<Double, SystemStats> runStats) {
        String outputPath = String.format("%s/%s.csv", OUTPUT_DIR, EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {
            /* Write the header */
            csvWriter.writeNext(HEADER);

            /* Iterate on timestamp */
            for (Map.Entry<Double, SystemStats> statsByTimestamp : runStats.entrySet()) {
                // Extract values from system stats instance
                String[] row = getRow(statsByTimestamp, runId);

                /* Write the row into the csv */
                csvWriter.writeNext(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getRow(Map.Entry<Double, SystemStats> statsByTimestamp, int runId) {
        double timestamp = statsByTimestamp.getKey();
        SystemStats systemStats = statsByTimestamp.getValue();
        return new String[]{
                String.valueOf(runId),
                String.valueOf(timestamp),
                String.valueOf(systemStats.getThroughput()),
                String.valueOf(systemStats.getMeanBusyServer()),
                String.valueOf(systemStats.getMeanServiceTime()),
                String.valueOf(systemStats.getMeanResponseTime())
        };
    }
}
