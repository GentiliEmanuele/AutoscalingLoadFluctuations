package it.simulation.experiments;

import it.simulation.configurations.VerificationBaseConfiguration;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class VerificationBase extends BaseExperiment {

    private static final double[] LAMBDAS = {
            0.6,
            0.8
    };

    public VerificationBase(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() {
        for (double lambda : LAMBDAS) {
            // Execute required repetition
            for (int i = 0; i < REPETITION_NUMBER; i++) {
                ARRIVALS_MU = 1 / lambda;
                runWork(i);
                collector.analyzeAndPush(i);
            }

            analyzer.computeSystemConfidenceIntervals();
            analyzer.computeServersConfidenceIntervals();
            analyzer.pushAndClear();
        }
    }

}
