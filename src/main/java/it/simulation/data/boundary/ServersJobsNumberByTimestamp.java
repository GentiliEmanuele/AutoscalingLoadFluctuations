package it.simulation.data.boundary;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.EXPERIMENT;

public class ServersJobsNumberByTimestamp {

    private static final String [] HEADER= {
            "Server Index",
            "Timestamp",
            "Jobs Number"
    };

    private static boolean isFirstWrite = true;

    public static void serversJobsNumberByTimestampCSV(Map<Integer, Map<Double, Integer>> serversJobsNumberByTimestamp) {
        String outputPath = String.format("output/%servers-jobs-number.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {
            if (isFirstWrite) {
                csvWriter.writeNext(HEADER);
                isFirstWrite = false;
            }

            for (Map.Entry<Integer, Map<Double, Integer>> entry : serversJobsNumberByTimestamp.entrySet()) {
                Integer serverIndex = entry.getKey();
                Map<Double, Integer> serverJobsNumberByTimestamp = entry.getValue();

                for (Map.Entry<Double, Integer> serverJobsNumberByTimestampEntry : serverJobsNumberByTimestamp.entrySet()) {
                    Double timestamp = serverJobsNumberByTimestampEntry.getKey();
                    Integer currentJobs = serverJobsNumberByTimestamp.get(timestamp);

                    String[] row = {
                            String.valueOf(serverIndex),
                            String.valueOf(timestamp),
                            String.valueOf(currentJobs)
                    };
                    csvWriter.writeNext(row);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }
}
