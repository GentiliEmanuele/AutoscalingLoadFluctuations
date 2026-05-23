package it.simulation.experiments;

import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class VerificationBase extends BaseExperiment {

    private static final double[] LAMBDAS = {
            0.6,
            0.8
    };

    private static final String[] SERVICE_DISTRIBUTIONS = {
            "exp",
            "h2"
    };

    public VerificationBase(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() {
        for (String serviceDistribution : SERVICE_DISTRIBUTIONS) {
            SERVICE_DISTRIBUTION = serviceDistribution;
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

}
