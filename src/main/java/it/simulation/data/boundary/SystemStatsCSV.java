package it.simulation.data.boundary;

import com.opencsv.CSVWriter;
import it.simulation.system.SystemStats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.OUTPUT_PATH;

public class SystemStatsCSV {

    private final static String [] HEADER = {
            "RunId",
            "Timestamp",
            "Throughput",
            "Utilization",
            "BusyServer",
            "ServiceTime",
            "ResponseTime"
    };

    public static void systemStatsToCSV(Map<Integer, Map<Double, SystemStats>> statsByRun) {
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(OUTPUT_PATH))) {

            /* Write the header */
            csvWriter.writeNext(HEADER);

            /* Iterate on run id */
            for (Map.Entry<Integer, Map<Double, SystemStats>> entry : statsByRun.entrySet()) {
                int runId = entry.getKey();
                Map<Double, SystemStats> runStats = entry.getValue();

                /* Iterate on timestamp */
                for (Map.Entry<Double, SystemStats> statsByTimestamp : runStats.entrySet()) {
                    // Extract values from system stats instance
                    String[] row = getRow(statsByTimestamp, runId);

                    /* Write the row into the csv */
                    csvWriter.writeNext(row);
                }
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
                String.valueOf(systemStats.getMeanUtilization()),
                String.valueOf(systemStats.getMeanBusyServer()),
                String.valueOf(systemStats.getMeanServiceTime()),
                String.valueOf(systemStats.getMeanResponseTime())
        };
    }
}
