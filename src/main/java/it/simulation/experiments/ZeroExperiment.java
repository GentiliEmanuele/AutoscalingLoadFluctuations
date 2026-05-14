package it.simulation.experiments;

import it.simulation.data.boundary.SystemStatsCSV;
import it.simulation.data.collectors.Collector;
import it.simulation.data.collectors.CollectorFactory;
import it.simulation.distributions.Distribution;
import it.simulation.distributions.DistributionFactory;
import it.simulation.events.*;
import it.simulation.lib.Rngs;
import it.simulation.system.SystemState;

import static it.simulation.configurations.Config.*;

public class ZeroExperiment implements Experiment {
    private final Rngs rngs;
    private final Collector collector;

    public ZeroExperiment(Rngs rngs) {
        this.rngs = rngs;
        this.collector = CollectorFactory.createCollector();
    }


    @Override
    public void run() throws IllegalLifeException {
        /* Execute the repetition with different values of interArrivalTime */
        for (int i = 0; i < REPETITION_NUMBER; i++) {
            runWork(i, ARRIVALS_MU);
        }

        /* Write collected data to CSV */
        SystemStatsCSV.systemStatsToCSV(collector.getStatsByRun());
    }

    private void runWork(int runId, double meanInterArrivalTime) throws IllegalLifeException {
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
