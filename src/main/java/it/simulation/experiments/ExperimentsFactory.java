package it.simulation.experiments;

import it.simulation.configurations.ValidationBaseConfigurations;
import it.simulation.configurations.VerificationBaseConfiguration;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.EXPERIMENT;

public class ExperimentsFactory {
    public static Experiment createExperiment(Rngs rngs) {
        switch (EXPERIMENT) {
            case "base" -> {
                return new BaseExperiment(rngs);
            }
            case "base-exp-1" -> {
                return new BaseExp1(rngs);
            }
            case "ver-base" -> {
                VerificationBaseConfiguration.setVerificationBaseConfiguration();
                return new VerificationBase(rngs);
            }
            case "val-base-1" -> {
                ValidationBaseConfigurations.setValidationBaseConfigurationFirst();
                return new ValidationBaseExperiments(rngs);
            }
        };

        throw new IllegalArgumentException("Unsupported experiment: " + EXPERIMENT);
    }
}
