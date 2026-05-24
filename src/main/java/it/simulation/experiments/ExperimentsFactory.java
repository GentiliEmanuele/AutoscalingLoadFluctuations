package it.simulation.experiments;

import it.simulation.configurations.TransitoryBaseConfigurations;
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
                return new ValidationBaseExperimentFirst(rngs);
            }
            case "val-base-2" -> {
                ValidationBaseConfigurations.setValidationBaseConfigurationSecond();
                return new ValidationBaseExperimentSecond(rngs);
            }
            case "trans-b-1" -> {
                TransitoryBaseConfigurations.setTransitoryBaseConfiguration(10);
                return new TransitoryBaseExperiments(rngs);
            }
            case "trans-b-2" -> {
                TransitoryBaseConfigurations.setTransitoryBaseConfiguration(80);
                return new TransitoryBaseExperiments(rngs);
            }
            case "trans-b-3" -> {
                TransitoryBaseConfigurations.setTransitoryBaseConfiguration(160);
                return new TransitoryBaseExperiments(rngs);
            }
        };

        throw new IllegalArgumentException("Unsupported experiment: " + EXPERIMENT);
    }
}
