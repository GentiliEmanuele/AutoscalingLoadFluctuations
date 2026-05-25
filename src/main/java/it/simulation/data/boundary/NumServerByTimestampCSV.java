package it.simulation.data.boundary;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.EXPERIMENT;

public class NumServerByTimestampCSV {

    private static final String[] HEADER = {
            "Run id",
            "Timestamp",
            "Num Servers"
    };

    private static boolean isFirstWrite = true;

    public static void numServerByTimestampCSV(int runId, Map<Double, Integer> numServersByTimestamp) {
        String outputPath = String.format("output/%s-num-servers.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {
            if (isFirstWrite) {
                csvWriter.writeNext(HEADER);
                isFirstWrite = false;
            }

            for (Map.Entry<Double, Integer> entry : numServersByTimestamp.entrySet()) {
                Double timestamp = entry.getKey();
                Integer numServers = entry.getValue();
                csvWriter.writeNext(new String[]{
                        String.valueOf(runId),
                        String.valueOf(timestamp),
                        String.valueOf(numServers)
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }
}
