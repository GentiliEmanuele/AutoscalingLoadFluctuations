package it.simulation.experiments;

import it.simulation.events.IllegalLifeException;
import it.simulation.system.SystemState;

import static it.simulation.configurations.Config.EMPTY_JOBS;
import static it.simulation.configurations.Config.STOP;

public interface Experiment {
    void run() throws IllegalLifeException;

    static boolean continueSimulating(SystemState s) {
        return (s.getCurrent() < STOP || (EMPTY_JOBS && s.getInfrastructure().activeJobExists()));
    }
}
