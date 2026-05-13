package it.simulation.events;

import it.simulation.system.SystemState;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.jobs.Job;

import static it.simulation.configurations.Config.INFINITY;
import static it.simulation.configurations.Config.STOP;

public class EventProcessor implements EventVisitor{
    @Override
    public void visit(SystemState s, ArrivalEvent event) throws IllegalLifeException {
        /* Get the servers in the infrastructure */
        Infrastructure infrastructure = s.getInfrastructure();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        infrastructure.computeJobsAdvancement(startTs, endTs, false);

        /* Add the next job to the list */
        double nextServiceLife = s.getServicesVA().gen();
        Job newJob = new Job(endTs, nextServiceLife);
        infrastructure.assignJob(newJob);

        /* Compute next completion time */
        double nextCompletionTs = infrastructure.computeNextCompletionTs(endTs);

        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Generate next arrival if simulation is not finished */
        if (endTs < STOP) {
            double nextArrivalTs = endTs + s.getArrivalVA().gen();
            Event nextArrival = new ArrivalEvent(nextArrivalTs);
            s.addEvent(nextArrival);
        }  else {
            s.addEvent(new ArrivalEvent(INFINITY));
        }

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, CompletionEvent event) throws IllegalLifeException {
        /* Get the servers in the infrastructure */
        Infrastructure infrastructure = s.getInfrastructure();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Compute job advancement */
        infrastructure.computeJobsAdvancement(startTs, endTs, true);

        /* Generate next completion */
        double nextCompletionTs = infrastructure.activeJobExists() ? infrastructure.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Update the current system clock */
        s.setCurrent(endTs);
    }
}
