package it.simulation.experiments;

import it.simulation.configurations.*;
import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.EXPERIMENT;

public class ExperimentsFactory {
    public static Experiment createExperiment(Rngs rngs) {
        switch (EXPERIMENT) {
            case "base" -> {
                return new BaseExperiment(rngs);
            }
            case "ver-base" -> {
                VerificationBaseConfiguration.setVerificationBaseConfiguration();
                return new VerificationBase(rngs);
            }
            case "val-base-1" -> {
                ValidationBaseConfigurations.setValidationBaseConfigurationFirst();
                return new BaseExperiment(rngs);
            }
            case "val-base-2" -> {
                ValidationBaseConfigurations.setValidationBaseConfigurationSecond();
                return new ValidationBaseExperimentSecond(rngs);
            }
            case "trans-b-1" -> {
                TransitoryBaseConfigurations.setTransitoryBaseConfiguration(10);
                return new BaseExperiment(rngs);
            }
            case "trans-b-2" -> {
                TransitoryBaseConfigurations.setTransitoryBaseConfiguration(80);
                return new BaseExperiment(rngs);
            }
            case "trans-b-3" -> {
                TransitoryBaseConfigurations.setTransitoryBaseConfiguration(160);
                return new BaseExperiment(rngs);
            }
            case "base-exp-1" -> {
                BaseExperimentsConfigurations.setBaseExperimentsConfiguration();
                return new BaseExp1(rngs);
            }
            case "base-exp-2" -> {
                BaseExperimentsConfigurations.setBaseExperimentsConfiguration();
                return new BaseExp2(rngs);
            }
            case "val-adv-1" -> {
                ValidationAdvancedConfiguration.setValidationAdvancedConfiguration("r0", 3);
                return new BaseExperiment(rngs);
            }
            case "val-adv-2" -> {
                ValidationAdvancedConfiguration.setValidationAdvancedConfiguration("jobs", 40);
                return new BaseExperiment(rngs);
            }
            case "trans-adv-1" -> {
                TransitoryAdvancedConfigurations.setTransitoryAdvancedConfiguration("r0", 3);
                return new BaseExperiment(rngs);
            }
            case "trans-adv-2" -> {
                TransitoryAdvancedConfigurations.setTransitoryAdvancedConfiguration("jobs", 40);
                return new BaseExperiment(rngs);
            }
            case "adv-exp-1" -> {
                AdvancedExperimentsConfigurations.setEvaluateAutoScalingConfig("r0");
                return new AdvExps(rngs,  new double[]{1, 1.5, 2, 2.5, 3, 3.5, 4});
            }
            case "adv-exp-2" -> {
                AdvancedExperimentsConfigurations.setEvaluateAutoScalingConfig("jobs");
                return new AdvExps(rngs, new double[]{5, 10, 15, 20, 25, 30, 35});
            }
        };

        throw new IllegalArgumentException("Unsupported experiment: " + EXPERIMENT);
    }
}
