package it.simulation.configurations;

import it.simulation.Simulate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static double INFINITY;
    public static double STOP;
    public static int SEED;
    public static double ARRIVALS_MU;
    public static double ARRIVALS_CV;
    public static double SERVICES_Z;
    public static double SERVICES_CV;
    public static String ARRIVALS_DISTRIBUTION;
    public static String SERVICE_DISTRIBUTION;
    public static boolean EMPTY_JOBS;
    public static double WEBSERVER_CAPACITY;
    public static int MAX_NUM_SERVERS;
    public static int START_NUM_SERVERS;
    public static String SCHEDULER_TYPE;
    public static double SPIKE_CAPACITY;
    public static double SI_MAX;
    public static boolean SPIKESERVER_ACTIVE;
    public static double ARRIVALS_TOTAL_PERIOD;
    public static double ARRIVALS_FAST_MU;
    public static double ARRIVALS_FAST_INTERVAL;
    public static String EXPERIMENT;
    public static int REPETITION_NUMBER;
    public static double CONFIDENCE_LEVEL;
    public static String OUTPUT_PATH;
    public static int BATCH_SIZE;
    public static boolean LOG_FINE;

    static {
        readConfiguration();
    }

    public static void readConfiguration() {
        try (InputStream in = Simulate.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.load(in);

            INFINITY = Double.POSITIVE_INFINITY;
            STOP = (Objects.equals(props.getProperty("system.stop"), "INFINITY")) ?
                    Double.POSITIVE_INFINITY : Double.parseDouble(props.getProperty("system.stop"));
            SEED = Integer.parseInt(props.getProperty("random.seed"));
            ARRIVALS_MU = Double.parseDouble(props.getProperty("distribution.arrivals.mu"));
            ARRIVALS_CV = Double.parseDouble(props.getProperty("distribution.arrivals.cv"));
            SERVICES_Z = Double.parseDouble(props.getProperty("distribution.services.z"));
            SERVICES_CV = Double.parseDouble(props.getProperty("distribution.services.cv"));
            ARRIVALS_DISTRIBUTION = props.getProperty("distribution.arrivals.type");
            SERVICE_DISTRIBUTION = props.getProperty("distribution.services.type");
            EMPTY_JOBS =  Boolean.parseBoolean(props.getProperty("system.empty_jobs"));
            WEBSERVER_CAPACITY = Double.parseDouble(props.getProperty("webserver.capacity"));
            MAX_NUM_SERVERS =  Integer.parseInt(props.getProperty("infrastructure.max_num_server"));
            START_NUM_SERVERS = Integer.parseInt(props.getProperty("infrastructure.start_num_server"));
            SCHEDULER_TYPE = props.getProperty("infrastructure.scheduler");
            SPIKE_CAPACITY = Double.parseDouble(props.getProperty("spikeserver.capacity"));
            SI_MAX = Double.parseDouble(props.getProperty("infrastructure.si_max"));
            SPIKESERVER_ACTIVE = Boolean.parseBoolean(props.getProperty("infrastructure.spikeserver.active"));
            ARRIVALS_TOTAL_PERIOD = Double.parseDouble(props.getProperty("distribution.arrivals.total_period"));
            ARRIVALS_FAST_INTERVAL = Double.parseDouble(props.getProperty("distribution.arrivals.fast_interval"));
            ARRIVALS_FAST_MU = Double.parseDouble(props.getProperty("distribution.arrivals.fast_mu"));
            EXPERIMENT = props.getProperty("experiment");
            REPETITION_NUMBER = Integer.parseInt(props.getProperty("repetition.number"));
            CONFIDENCE_LEVEL = Double.parseDouble(props.getProperty("confidence.level"));
            OUTPUT_PATH = props.getProperty("output.path");
            BATCH_SIZE = Integer.parseInt(props.getProperty("batch.size"));
            LOG_FINE = Boolean.parseBoolean(props.getProperty("log.fine"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Impossible loading " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}
