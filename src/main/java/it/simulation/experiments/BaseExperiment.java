package it.simulation.experiments;

import it.simulation.data.analyzers.Analyzer;
import it.simulation.data.analyzers.AnalyzerFactory;
import it.simulation.data.collectors.Collector;
import it.simulation.data.collectors.CollectorFactory;
import it.simulation.distributions.Distribution;
import it.simulation.distributions.DistributionFactory;
import it.simulation.events.*;
import it.simulation.lib.Rngs;
import it.simulation.system.SystemState;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.servers.ServerStats;

import static it.simulation.configurations.Config.*;

public class BaseExperiment implements Experiment {
    private final Rngs rngs;
    protected final Collector collector;
    protected final Analyzer analyzer;

    public BaseExperiment(Rngs rngs) {
        this.rngs = rngs;
        this.analyzer = AnalyzerFactory.createAnalyzer();
        this.collector = CollectorFactory.createCollector(analyzer);
    }


    @Override
    public void run() throws IllegalLifeException {
        /* Execute the repetition with different values of interArrivalTime */
        for (int i = 0; i < REPETITION_NUMBER; i++) {
            System.out.printf("\nRepetition %d/%d", i, REPETITION_NUMBER);
            runWork(i);
            collector.analyzeAndPush(i);
        }

        analyzer.computeSystemConfidenceIntervals();
        analyzer.computeServersConfidenceIntervals();
        analyzer.pushAndClear();
    }

    protected void runWork(int runId) throws IllegalLifeException {
        /* Plant the first seed for the first run*/
        if (runId == 0) {
            rngs.plantSeeds(SEED);
        }

        /* Create visitor */
        EventVisitor visitor = new EventProcessor();

        /* Create the distribution for services and arrival */
        Distribution arrivalVA = DistributionFactory.createArrivalDistribution(rngs);
        Distribution servicesVA = DistributionFactory.createServiceDistribution(rngs);
        Distribution turnOnVa = DistributionFactory.createTurnOnDistribution(rngs);

        /* Set the current meanInterArrivalTime as mean of arrivalVA */
        arrivalVA.setMean(ARRIVALS_MU);

        /* Compute first arrival time */
        double nextArrival = arrivalVA.gen();
        Event firstArrival = new ArrivalEvent(nextArrival);
        Event firstCompletion = new CompletionEvent(INFINITY);

        /* Create calendar and add the created event */
        EventCalendar calendar = new EventCalendar();
        calendar.addEvent(firstArrival);
        calendar.addEvent(firstCompletion);

        /* Set up the system state */
        SystemState s = new SystemState(calendar, servicesVA, arrivalVA, turnOnVa);

        /* Fluctuation values computation (used only if needed) */
        double slowPercentage = (ARRIVALS_TOTAL_PERIOD - ARRIVALS_FAST_INTERVAL) / ARRIVALS_TOTAL_PERIOD;
        double fastPercentage = ARRIVALS_FAST_INTERVAL / ARRIVALS_TOTAL_PERIOD;

        double meanLambda = 1 / ARRIVALS_MU;
        double fastLambda = 1 / ARRIVALS_FAST_MU;
        double slowLambda = (meanLambda - fastLambda * fastPercentage) / slowPercentage;
        double slowMu = 1 / slowLambda;

        while (Experiment.continueSimulating(s)) {
            /* Compute the next event */
            Event nextEvent = calendar.nextEvent();

            /* Check that the next timestamp is greater than the previous one */
            assert s.getCurrent() < nextEvent.getTimestamp();

            /* Send current data to collector */
            collector.collect(s.getCurrent(), s.getInfrastructure());

            /* Simulate long-term fluctuation */
            boolean hasLongTermFluctuations = ARRIVALS_FAST_INTERVAL != 0;
            if (hasLongTermFluctuations) {
                if (nextEvent.getTimestamp() % ARRIVALS_TOTAL_PERIOD < slowPercentage * ARRIVALS_TOTAL_PERIOD) {
                    // Slow arrivals
                    s.getArrivalVA().setMean(slowMu);
                } else {
                    // Fast arrivals
                    s.getArrivalVA().setMean(ARRIVALS_FAST_MU);
                }
            }

            /* Process the next-event */
            nextEvent.process(s, visitor);
        }

        assertArrivalsAndCompletionsEquality(s.getInfrastructure(), s.getCurrent());

        s.printStats();
    }

    private void assertArrivalsAndCompletionsEquality(Infrastructure infrastructure, double lastTs) {
        // Assertion can be verified only if EMPTY_JOBS is true
        if (!EMPTY_JOBS) return;

        for (ServerStats serverStats : infrastructure.getServersStats(lastTs)) {
            assert serverStats.getArrivedJobs() == serverStats.getCompletedJobs() :
                "There are arrived jobs that are not completed";
        }
    }
}
