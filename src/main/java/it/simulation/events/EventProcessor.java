package it.simulation.events;

import it.simulation.system.SystemState;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.jobs.Job;
import it.simulation.system.servers.WebServer;

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

    @Override
    public void visit(SystemState s, ScalingOutReqEvent event) throws IllegalLifeException {
        Infrastructure infrastructure = s.getInfrastructure();
        double endTs = event.getTimestamp();

        /* Generate time for turn on a web sever */
        double turnOnTime = s.getTurnOnVA().gen();

        // From request to effective scale out
        var serverTarget = infrastructure.requestScaleOut(endTs, turnOnTime);

        /* Find the earliest WS to make active: it needs to be done
           in case no scaling out event is already scheduled */
        WebServer nextScaleOut = infrastructure.findNextScaleOut();
        s.addEvent(new ScalingOutEvent(nextScaleOut.getActivationTimestamp(), nextScaleOut));

        // Schedule to INFINITY the next request
        s.addEvent(new ScalingOutReqEvent(INFINITY));

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, ScalingOutEvent event) throws IllegalLifeException {

    }

    @Override
    public void visit(SystemState s, ScalingInEvent event) throws IllegalLifeException {

    }
}
