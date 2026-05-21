package it.simulation.events;

import it.simulation.system.SystemState;

public class ScalingOutReqEvent extends Event {
    public ScalingOutReqEvent(double timestamp) {
        super(timestamp, EventType.SCALING_OUT_REQ);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }
}
