package it.simulation.data.boundary;

import com.opencsv.CSVWriter;
import it.simulation.system.SystemStats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.*;

public class ConfidenceIntervalCSV {

    private final static String [] HEADER = {
            "SI_MAX",
            "BusyServer",
            "ResponseTime",
            "ServiceTime",
            "Throughput",
            "Utilization",
    };

    private static boolean isFirstWrite = true;

    public static void confidenceIntervalCSV(Map<String, String> confidenceIntervals) {
        String outputPath = String.format("output/%s-ci", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {

            /* Write the header */
            if (isFirstWrite) {
                csvWriter.writeNext(HEADER);
                isFirstWrite = false;
            }

            // Map metrics CI and columns
            String[] row = new String[HEADER.length];
            row[0] = String.valueOf(SI_MAX);
            for (int i = 1; i < HEADER.length; i++) {
                String metricName = HEADER[i];
                row[i] = confidenceIntervals.getOrDefault(metricName, "");
            }

            csvWriter.writeNext(row);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
