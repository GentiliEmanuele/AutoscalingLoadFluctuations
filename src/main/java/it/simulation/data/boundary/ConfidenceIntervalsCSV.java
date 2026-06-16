package it.simulation.data.boundary;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.*;

public class ConfidenceIntervalsCSV {

    private final static String [] SYSTEM_HEADER = {
            "Lambda",
            "SCALING_OUT_THR",
            "SCALING_IN_THR",
            "SI_MAX",
            "Service distribution",
            "BusyServer",
            "ResponseTime",
            "ServiceTime",
            "Throughput",
            "Population",
    };

    private final static String [] SERVERS_HEADER = {
            "Lambda",
            "SCALING_OUT_THR",
            "SCALING_IN_THR",
            "SI_MAX",
            "Service distribution",
            "ServerIndex",
            "ResponseTime",
            "Throughput",
            "Utilization"
    };

    private static boolean isFirstWriteSystem = true;
    private static boolean isFirstWriteServers = true;

    public static void systemConfidenceIntervalCSV(Map<String, String> confidenceIntervals) {
        String outputPath = String.format("output/%s-ci.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {

            /* Write the header */
            if (isFirstWriteSystem) {
                csvWriter.writeNext(SYSTEM_HEADER);
                isFirstWriteSystem = false;
            }

            // Map metrics CI and columns
            String[] row = new String[SYSTEM_HEADER.length];
            row[0] = String.valueOf(1 / ARRIVALS_MU);
            if (SCALING_OUT_THRESHOLD == INFINITY) {
                row[1] = String.valueOf(SCALING_OUT_THRESHOLD);
                row[2] = String.valueOf(SCALING_OUT_THRESHOLD);
            } else {
                if (SCALING_INDICATOR_TYPE.equals("r0")) {
                    row[1] = String.valueOf(SCALING_OUT_THRESHOLD * 1.5);
                    row[2] = String.valueOf(SCALING_OUT_THRESHOLD * 0.5);
                } else if (SCALING_INDICATOR_TYPE.equals("jobs")) {
                    row[1] = String.valueOf(SCALING_OUT_THRESHOLD);
                    row[2] = String.valueOf(SCALING_OUT_THRESHOLD * 0.6);
                }
            }
            row[3] = String.valueOf(SI_MAX);
            row[4] = String.valueOf(SERVICE_DISTRIBUTION);
            for (int i = 5; i < SYSTEM_HEADER.length; i++) {
                String metricName = SYSTEM_HEADER[i];
                row[i] = confidenceIntervals.getOrDefault(metricName, "");
            }

            csvWriter.writeNext(row);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void serversConfidenceIntervalCSV(Map<Integer, Map<String, String>> serversConfidenceIntervals) {
        String outputPath = String.format("output/%servers-ci.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {

            /* Write the header */
            if (isFirstWriteServers) {
                csvWriter.writeNext(SERVERS_HEADER);
                isFirstWriteServers = false;
            }

            String[] row = new String[SERVERS_HEADER.length];
            row[0] = String.valueOf(1 / ARRIVALS_MU);
            if (SCALING_OUT_THRESHOLD == INFINITY) {
                row[1] = String.valueOf(SCALING_OUT_THRESHOLD);
                row[2] = String.valueOf(SCALING_OUT_THRESHOLD);
            } else {
                if (SCALING_INDICATOR_TYPE.equals("r0")) {
                    row[1] = String.valueOf(SCALING_OUT_THRESHOLD * 1.5);
                    row[2] = String.valueOf(SCALING_OUT_THRESHOLD * 0.5);
                } else if (SCALING_INDICATOR_TYPE.equals("jobs")) {
                    row[1] = String.valueOf(SCALING_OUT_THRESHOLD);
                    row[2] = String.valueOf(SCALING_OUT_THRESHOLD * 0.6);
                }
            }
            row[3] = String.valueOf(SI_MAX);
            row[4] = String.valueOf(SERVICE_DISTRIBUTION);
            for (Map.Entry<Integer, Map<String, String>> cis : serversConfidenceIntervals.entrySet()) {
                row[5] = String.valueOf(cis.getKey());

                for (int i = 6; i < SERVERS_HEADER.length; i++) {
                    String metricName = SERVERS_HEADER[i];
                    row[i] = cis.getValue().getOrDefault(metricName, "");
                }

                csvWriter.writeNext(row);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
