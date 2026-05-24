package it.simulation.experiments;

import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class ValidationBaseExperimentSecond extends BaseExperiment {

    private static final int [] NUM_SERVER_LIST = {1, 2};

    public ValidationBaseExperimentSecond(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() {
        for (int numServer : NUM_SERVER_LIST) {
            /* Execute the repetition with different values of interArrivalTime */
            for (int i = 0; i < REPETITION_NUMBER; i++) {
                START_NUM_SERVERS = numServer;
                MAX_NUM_SERVERS = numServer;
                System.out.printf("\nRepetition %d/%d for Num server = %d", i, REPETITION_NUMBER, numServer);
                runWork(i);
                collector.analyzeAndPush(i);
            }

            analyzer.computeSystemConfidenceIntervals();
            analyzer.computeServersConfidenceIntervals();
            analyzer.pushAndClear();
        }
    }
}
