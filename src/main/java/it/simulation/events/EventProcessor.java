package it.simulation.events;

import it.simulation.data.boundary.MovingWindowResponseTimeCSV;
import it.simulation.data.collectors.MovingWindowResponseTimeCollector;
import it.simulation.system.SystemState;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.jobs.Job;
import it.simulation.system.servers.ServerState;
import it.simulation.system.servers.WebServer;

import static it.simulation.configurations.Config.*;

public class EventProcessor implements EventVisitor{
    @Override
    public void visit(SystemState s, ArrivalEvent event) throws IllegalLifeException {
        /* Get the servers in the infrastructure */
        Infrastructure infrastructure = s.getInfrastructure();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        double scalingIndicator = infrastructure.computeJobsAdvancement(startTs, endTs, false);

        /* Plan scaling */
        if(SCALING_INDICATOR_TYPE.equals("jobs")) {
            planScaling(s, endTs, scalingIndicator);
        }

        /* Add the next job to the list */
        double nextServiceLife = s.getServicesVA().gen();
        Job newJob = new Job(endTs, nextServiceLife);
        infrastructure.assignJob(newJob);

        /* Compute next completion time */
        double nextCompletionTs = infrastructure.computeNextCompletionTs(endTs);

        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Generate next arrival if simulation is not finished */
        boolean continueSimulating = REPETITION_NUMBER != 1 ?
                endTs < STOP :
                s.getAnalyzer().continueSimulating();

        if (continueSimulating) {
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
        double scalingIndicator = infrastructure.computeJobsAdvancement(startTs, endTs, true);

        if (SCALING_INDICATOR_TYPE.equals("r0") && EXPERIMENT.equals("val-adv-1")) {
            MovingWindowResponseTimeCollector.collectMWResponseTime(s.getCurrent(), scalingIndicator);
        }

        /* Plan scaling */
        planScaling(s, endTs, scalingIndicator);

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
        Infrastructure infrastructure = s.getInfrastructure();

        /* Get the current clock and the one of this arrival */
        double startTs = s.getCurrent();
        double endTs = event.getTimestamp();

        /* Advance job execution */
        infrastructure.computeJobsAdvancement(startTs, endTs, false);

        /* Set server to be effectively active */
        infrastructure.scaleOut(endTs, event.getTarget());

        /* Find the earliest WS to make active */
        WebServer nextScaleOut = infrastructure.findNextScaleOut();
        double nextActivationTS = nextScaleOut == null ? INFINITY : nextScaleOut.getActivationTimestamp();
        s.addEvent(new ScalingOutEvent(nextActivationTS, nextScaleOut));

        /* Generate next completion */
        double nextCompletionTs = infrastructure.activeJobExists() ? infrastructure.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        /* Update the current system clock */
        s.setCurrent(endTs);
    }

    @Override
    public void visit(SystemState s, ScalingInEvent event) throws IllegalLifeException {
        Infrastructure infrastructure = s.getInfrastructure();
        double endTs = event.getTimestamp();

        infrastructure.scaleIn(endTs);

        s.addEvent(new ScalingInEvent(INFINITY));

        /* Generate next completion */
        double nextCompletionTs = infrastructure.activeJobExists() ? infrastructure.computeNextCompletionTs(endTs) : INFINITY;
        Event nextCompletion = new CompletionEvent(nextCompletionTs);
        s.addEvent(nextCompletion);

        s.setCurrent(endTs);
    }


    private void planScaling(SystemState s, double endTs, double scalingIndicator){
        Infrastructure infrastructure = s.getInfrastructure();
        if (SCALING_OUT_THRESHOLD != INFINITY){
            boolean scalingOutPossible;
            boolean scalingOutCondition;
            boolean scalingInPossible;
            boolean scalingInCondition;
            int activatedServer = infrastructure.getNumWebServersByState(ServerState.ACTIVE) + infrastructure.getNumWebServersByState(ServerState.TO_BE_ACTIVE);

            scalingOutPossible = activatedServer < MAX_NUM_SERVERS;
            scalingInPossible = infrastructure.getNumWebServersByState(ServerState.ACTIVE) > 1;

            if (SCALING_INDICATOR_TYPE.equals("r0")) {
                scalingOutCondition = scalingIndicator > SCALING_OUT_THRESHOLD * 1.5;
                scalingInCondition = scalingIndicator < SCALING_OUT_THRESHOLD * 0.5;
            } else if (SCALING_INDICATOR_TYPE.equals("jobs")) {
                double scalingInThr = SCALING_OUT_THRESHOLD * 0.6;

                int requiredServerForScaleOut = (int) (Math.ceil(scalingIndicator / SCALING_OUT_THRESHOLD));
                int requiredServerForScaleIn = (int) (Math.ceil(scalingIndicator / scalingInThr));

                scalingOutCondition = activatedServer < requiredServerForScaleOut;
                scalingInCondition = activatedServer > requiredServerForScaleIn;
            } else {
                throw new IllegalArgumentException("Invalid SCALING_INDICATOR_TYPE");
            }

            if (scalingOutCondition && scalingOutPossible)
                s.addEvent(new ScalingOutReqEvent(endTs));
            else if (scalingInCondition && scalingInPossible)
                s.addEvent(new ScalingInEvent(endTs));
        }

    }
}
