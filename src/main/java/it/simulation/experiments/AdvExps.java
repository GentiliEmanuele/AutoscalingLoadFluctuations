package it.simulation.experiments;

import it.simulation.events.IllegalLifeException;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class AdvExps extends BaseExperiment {

    public final double[] THRESHOLDS;
    public final double[] SI_MAX_LIST = {10, 50, 90, 130, 160};

    public AdvExps(Rngs rngs, double[] THRESHOLDS) {
        super(rngs);
        this.THRESHOLDS = THRESHOLDS;
    }

    @Override
    public void run() throws IllegalLifeException {
        for (double threshold : THRESHOLDS) {
            for (double siMax : SI_MAX_LIST) {
                SI_MAX = siMax;
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
}
