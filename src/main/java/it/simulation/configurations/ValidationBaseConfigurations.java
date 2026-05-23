package it.simulation.configurations;

public class ValidationBaseConfigurations extends Config {
    public static void setValidationBaseConfigurationFirst() {
        SEED = 42;
        ARRIVALS_MU = 0.15;
        ARRIVALS_CV = 4;
        SERVICES_Z = 0.16;
        SERVICES_CV = 4;
        ARRIVALS_DISTRIBUTION = "h2";
        SERVICE_DISTRIBUTION = "h2";
        EMPTY_JOBS = false;
        WEBSERVER_CAPACITY = 1;
        MAX_NUM_SERVERS = 1;
        START_NUM_SERVERS = 1;
        SCHEDULER_TYPE = "leastUsed";
        SPIKE_CAPACITY = 1;
        SI_MAX = 140;
        SPIKESERVER_ACTIVE = true;
        ARRIVALS_TOTAL_PERIOD = 0; // It's a don't care because long term fluctuations are not active
        ARRIVALS_FAST_INTERVAL = 0; // It's a don't care because long term fluctuations are not active
        ARRIVALS_FAST_MU = 0; // It's a don't care because long term fluctuations are not active
        REPETITION_NUMBER = 1;
        CONFIDENCE_LEVEL = 0.95;
        OUTPUT_DIR = "output";
        BATCH_SIZE = 16384;
        LOG_FINE = true;
        TURN_ON_MU = 5;
        TURN_ON_STD = 0.5;
        SCALING_INDICATOR_TYPE = "r0"; // It's a don't care because scaling mechanism is not active
        SCALING_OUT_THRESHOLD = INFINITY; // It's a don't care because scaling mechanism is not active
        SLIDING_WINDOW_SIZE = 1; // It's a don't care because scaling mechanism is not active
    }
}
