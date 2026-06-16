package it.simulation.experiments;

import it.simulation.events.IllegalLifeException;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;
import static it.simulation.configurations.Config.REPETITION_NUMBER;

public class FiniteAdvExp extends BaseExperiment {

    private record AutoscalerConfiguration(double siMax, double scalingOutThr, String autoscalingType) {}

    private final AutoscalerConfiguration [] configurations = {
            new AutoscalerConfiguration(50, 1, "r0"),
            new AutoscalerConfiguration(50, 5, "jobs"),
            new AutoscalerConfiguration(50, INFINITY, "r0")
    };

    public FiniteAdvExp(Rngs rngs) {
        super(rngs);
    }

    @Override
    public void run() throws IllegalLifeException {
        for (AutoscalerConfiguration conf : configurations) {
            SI_MAX = conf.siMax;
            SCALING_OUT_THRESHOLD = conf.scalingOutThr;
            SCALING_INDICATOR_TYPE = conf.autoscalingType;

            for (int i = 0; i < REPETITION_NUMBER; i++) {
                System.out.printf("\nRepetition %d/%d for SI_MAX = %.1f, SCALING_THR = %.1f, SCALING_INDICATOR_TYPE = %s", i, REPETITION_NUMBER, SI_MAX, SCALING_OUT_THRESHOLD, SCALING_INDICATOR_TYPE);
                runWork(i);
                collector.analyzeAndPush(i);
            }

            analyzer.computeSystemConfidenceIntervals();
            analyzer.computeServersConfidenceIntervals();
            analyzer.pushAndClear();
        }
    }
}
