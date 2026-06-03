package it.simulation.experiments;

import it.simulation.events.IllegalLifeException;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class AdvExp1 extends BaseExperiment {

    public static final double[] THRESHOLDS = {1, 1.5, 2, 2.5, 3, 3.5, 4};

    public AdvExp1(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() throws IllegalLifeException {
        for (double threshold : THRESHOLDS) {
            /* Execute the repetition with different values of interArrivalTime */
            for (int i = 0; i < REPETITION_NUMBER; i++) {
                SCALING_OUT_THRESHOLD = threshold;
                System.out.printf("\nRepetition %d/%d for SCALING_THRESHOLD = %f", i, REPETITION_NUMBER, threshold);
                runWork(i);
                collector.analyzeAndPush(i);
            }

            analyzer.computeSystemConfidenceIntervals();
            analyzer.computeServersConfidenceIntervals();
            analyzer.pushAndClear();
        }
    }
}
