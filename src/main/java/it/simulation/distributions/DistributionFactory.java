package it.simulation.distributions;

import it.simulation.lib.Rngs;

import static it.simulation.configurations.Config.*;

public class DistributionFactory {
    public static Distribution createArrivalDistribution(Rngs rngs) {
        return switch (ARRIVALS_DISTRIBUTION) {
            case "exp" -> new Exponential(rngs, 0, ARRIVALS_MU);
            case "h2"  -> new HyperExponential(rngs, ARRIVALS_CV, ARRIVALS_MU, 0, 1, 2);
            default -> throw new IllegalArgumentException("Invalid arrival distribution type: " + ARRIVALS_DISTRIBUTION);
        };
    }

    public static Distribution createServiceDistribution(Rngs rngs) {
        return switch (SERVICE_DISTRIBUTION) {
            case "exp" -> new Exponential(rngs, 3, SERVICES_Z);
            case "h2"  -> new HyperExponential(rngs, SERVICES_CV, SERVICES_Z, 3, 4, 5);
            default -> throw new IllegalArgumentException("Invalid service distribution type: " + SERVICE_DISTRIBUTION);
        };
    }
}
