package it.simulation.data.boundary;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static it.simulation.configurations.Config.EXPERIMENT;

public class MovingWindowResponseTimeCSV {

    private static final String[] HEADER = {
            "Timestamp",
            "MWResponseTime"
    };

    private static boolean isFirstWrite = true;

    public static void movingWindowResponseTime(double timestamp, double mwResponseTime) {
        String outputPath = String.format("output/%s-moving_window-response-time.csv", EXPERIMENT);
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath, true))) {
            if (isFirstWrite) {
                csvWriter.writeNext(HEADER);
                isFirstWrite = false;
            }

            String [] row = {
                    String.valueOf(timestamp),
                    String.valueOf(mwResponseTime)
            };

            csvWriter.writeNext(row);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
