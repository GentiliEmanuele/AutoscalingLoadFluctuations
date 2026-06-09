package it.simulation.data.boundary;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;

import static it.simulation.configurations.Config.*;


public class SeedCSV {

    private static final String [] HEADER = {
            "RunId",
            "Stream",
            "Seed"
    };

    private static boolean isFirstWrite;

    public static void seedToCSV(int runId, int stream, long seed) {
        String outputPath = String.format("output/%s-seed.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {

            /* Write the header */
            if (isFirstWrite) {
                csvWriter.writeNext(HEADER);
                isFirstWrite = false;
            }

            String [] row = {
                    String.valueOf(runId),
                    String.valueOf(stream),
                    String.valueOf(seed)
            };

            csvWriter.writeNext(row);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
