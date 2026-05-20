package it.simulation.experiments;

import it.simulation.events.IllegalLifeException;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class BaseExp1 extends BaseExperiment {

    private static final int[] SI_MAX_LIST = {
            10,
            20,
            30,
            40,
            50,
            60,
            70,
            80,
            90,
            100,
            110,
            120,
            130,
            140,
            150,
            160
    };

    public BaseExp1(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() throws IllegalLifeException {
        for (int si_max : SI_MAX_LIST) {
            /* Execute the repetition with different values of interArrivalTime */
            for (int i = 0; i < REPETITION_NUMBER; i++) {
                SI_MAX = si_max;
                System.out.printf("\nRepetition %d/%d for SI_MAX = %d", i, REPETITION_NUMBER, si_max);
                runWork(i, ARRIVALS_MU);
                collector.analyzeAndPush(i);
            }

            analyzer.computeSystemConfidenceIntervals();
            analyzer.computeServersConfidenceIntervals();
            analyzer.pushAndClear();
        }
    }
}
