package it.simulation.experiments;

import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.EXPERIMENT;

public class ExperimentsFactory {
    public static Experiment createExperiment(Rngs rngs) {
        return switch (EXPERIMENT) {
            case "base" -> new BaseExperiment(rngs);
            case "base-exp-1" -> new BaseExp1(rngs);
            default -> throw new IllegalArgumentException("Unsupported experiment: " + EXPERIMENT);
        };
    }
}
