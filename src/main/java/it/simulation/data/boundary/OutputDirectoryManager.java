package it.simulation.data.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static it.simulation.configurations.Config.OUTPUT_DIR;

public class OutputDirectoryManager {
    public static void prepareOutputDirectory() {
        Path path = Paths.get(OUTPUT_DIR);

        try {
            if (Files.exists(path)) {
                cleanOutputDirectory(path);
            } else {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Error while managing output directory : " + e.getMessage());
        }
    }

    private static void cleanOutputDirectory(Path path) throws IOException{
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(path))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + p, e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }
}
