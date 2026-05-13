package it.simulation.system.infrastructures;

import static it.simulation.configurations.Config.SPIKESERVER_ACTIVE;

public class InfrastructureFactory {
    public static Infrastructure createInfrastructure() {
        Infrastructure instance = new BaseServerInfrastructure();

        if (SPIKESERVER_ACTIVE)
            instance = new SpikedInfrastructureDecorator((BaseServerInfrastructure)instance);

        return instance;
    }
}
