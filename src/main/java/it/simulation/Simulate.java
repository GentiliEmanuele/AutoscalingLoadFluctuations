package it.simulation;

import it.simulation.data.boundary.OutputDirectoryManager;
import it.simulation.experiments.Experiment;
import it.simulation.experiments.ExperimentsFactory;
import it.simulation.lib.Rngs;

public class Simulate {
    private static final Rngs rngs = new Rngs();

    public static void main(String[] args) {
        Experiment experiment = ExperimentsFactory.createExperiment(rngs);
        OutputDirectoryManager.prepareOutputDirectory();
        experiment.run();
    }
}