package it.simulation.experiments;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.data.analyzers.AnalyzerFactory;
import it.simulation.data.boundary.SystemStatsCSV;
import it.simulation.data.collectors.Collector;
import it.simulation.data.collectors.CollectorFactory;
import it.simulation.distributions.Distribution;
import it.simulation.distributions.DistributionFactory;
import it.simulation.events.*;
import it.simulation.lib.Rngs;
import it.simulation.system.SystemState;
import it.simulation.system.SystemStats;

import java.util.Map;

import static it.simulation.configurations.Config.*;

public class BaseExperiment implements Experiment {
    private final Rngs rngs;
    protected final Collector collector;
    protected final Analyzer analyzer;

    public BaseExperiment(Rngs rngs) {
        this.rngs = rngs;
        this.collector = CollectorFactory.createCollector();
        this.analyzer = AnalyzerFactory.createAnalyzer();
    }


    @Override
    public void run() throws IllegalLifeException {
        /* Execute the repetition with different values of interArrivalTime */
        for (int i = 0; i < REPETITION_NUMBER; i++) {
            System.out.printf("\nRepetition %d/%d", i, REPETITION_NUMBER);
            runWork(i, ARRIVALS_MU);
        }

        /* Write collected data to CSV */
        Map<Integer, Map<Double, SystemStats>> stats = collector.getCollectedStats();
        analyzer.analyze(stats);
        SystemStatsCSV.systemStatsToCSV(stats);
    }

    protected void runWork(int runId, double meanInterArrivalTime) throws IllegalLifeException {
        /* Plant the first seed for the first run*/
        if (runId == 0) {
            rngs.plantSeeds(SEED);
        }

        /* Create visitor */
        EventVisitor visitor = new EventProcessor();

        /* Create the distribution for services and arrival */
        Distribution arrivalVA = DistributionFactory.createArrivalDistribution(rngs);
        Distribution servicesVA = DistributionFactory.createServiceDistribution(rngs);

        /* Set the current meanInterArrivalTime as mean of arrivalVA */
        arrivalVA.setMean(meanInterArrivalTime);

        /* Compute first arrival time */
        double nextArrival = arrivalVA.gen();
        Event firstArrival = new ArrivalEvent(nextArrival);
        Event firstCompletion = new CompletionEvent(INFINITY);

        /* Create calendar and add the created event */
        EventCalendar calendar = new EventCalendar();
        calendar.addEvent(firstArrival);
        calendar.addEvent(firstCompletion);

        /* Set up the system state */
        SystemState s = new SystemState(calendar, servicesVA, arrivalVA);

        while (Experiment.continueSimulating(s)) {
            /* Compute the next event */
            Event nextEvent = calendar.nextEvent();

            /* Check that the next timestamp is greater than the previous one */
            assert s.getCurrent() < nextEvent.getTimestamp();

            /* Send current data to collector */
            collector.collect(runId, s.getCurrent(), s.getInfrastructure().computeSystemStats(s.getCurrent()));

            /* Process the next-event */
            nextEvent.process(s, visitor);
        }

        s.printStats();
    }
}
