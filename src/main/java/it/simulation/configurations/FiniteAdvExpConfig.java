package it.simulation.configurations;

public class FiniteAdvExpConfig extends Config {
    public static void setFiniteAdvConfig() {
        SEED = 42;
        ARRIVALS_MU = 0.15;
        ARRIVALS_CV = 4;
        SERVICES_Z = 0.16;
        SERVICES_CV = 4;
        ARRIVALS_DISTRIBUTION = "h2";
        SERVICE_DISTRIBUTION = "h2";
        EMPTY_JOBS = false;
        WEBSERVER_CAPACITY = 1;
        MAX_NUM_SERVERS = 3;
        START_NUM_SERVERS = 1;
        SCHEDULER_TYPE = "leastUsed";
        SPIKE_CAPACITY = 1;
        SI_MAX = 3;// It's a don't care because it will set after
        SPIKESERVER_ACTIVE = true;
        ARRIVALS_TOTAL_PERIOD = 500;
        ARRIVALS_FAST_INTERVAL = 100;
        ARRIVALS_FAST_MU = 0.15 / 4;
        REPETITION_NUMBER = 32;
        START = 0;
        STOP = 900;
        BATCH_SIZE = 32768; // It's a don't care because batch mean method is not used
        BATCH_NUM = 16; // It's a don't care because batch mean method is not used
        CONFIDENCE_LEVEL = 0.95;
        OUTPUT_DIR = "output";
        LOG_FINE = true;
        TURN_ON_MU = 5;
        TURN_ON_STD = 0.5;
        SCALING_INDICATOR_TYPE = "r0"; // It's a don't care because it will set after
        SCALING_OUT_THRESHOLD = 3; // It's a don't care because it will set after
        SLIDING_WINDOW_SIZE = 10;
    }
}
