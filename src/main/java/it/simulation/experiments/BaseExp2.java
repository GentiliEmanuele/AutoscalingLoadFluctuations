package it.simulation.experiments;

import it.simulation.events.IllegalLifeException;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class BaseExp2 extends BaseExperiment {

    private static final double[] SI_MAX_LIST = {
            INFINITY, 40, 80, 120, 160
    };

    private static final double[] LAMBDAS = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
    };

    public BaseExp2(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() throws IllegalLifeException {
        for (double lambda : LAMBDAS) {
            ARRIVALS_MU = 1 / lambda;
            for (double siMax : SI_MAX_LIST) {
                // Spike is active only if the threshold is finite
                SPIKESERVER_ACTIVE = siMax != INFINITY;

                // Without spikes control response time grows to infinity
                if (!SPIKESERVER_ACTIVE && lambda > 6) continue;
                SI_MAX = siMax;
                for (int i = 0; i < REPETITION_NUMBER; i++) {
                    System.out.printf("\nRepetition %d/%d for SI_MAX = %f and lambda=%f", i, REPETITION_NUMBER, siMax, lambda);
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
