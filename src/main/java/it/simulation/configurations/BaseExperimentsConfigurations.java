package it.simulation.configurations;

public class BaseExperimentsConfigurations extends Config {
    public static void setBaseExperimentFirstConfiguration() {
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
        SI_MAX = 1; // It's a don't care because this value is set after
        SPIKESERVER_ACTIVE = true;
        ARRIVALS_TOTAL_PERIOD = 0; // It's a don't care because long term fluctuations are not active
        ARRIVALS_FAST_INTERVAL = 0; // It's a don't care because long term fluctuations are not active
        ARRIVALS_FAST_MU = 0; // It's a don't care because long term fluctuations are not active
        REPETITION_NUMBER = 32;
        CONFIDENCE_LEVEL = 0.95;
        OUTPUT_DIR = "output";
        BATCH_SIZE = 1; // It's a don't care because batch method is not used
        LOG_FINE = false;
        TURN_ON_MU = 5; // It's a don't care because scaling mechanism is not active
        TURN_ON_STD = 0.5; // It's a don't care because scaling mechanism is not active
        SCALING_INDICATOR_TYPE = "r0"; // It's a don't care because scaling mechanism is not active
        SCALING_OUT_THRESHOLD = INFINITY;
        SLIDING_WINDOW_SIZE = 1; // It's a don't care because scaling mechanism is not active
    }
}
