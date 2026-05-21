package it.simulation.events;

import it.simulation.system.SystemState;

public class ScalingInEvent extends Event {
    public ScalingInEvent(double timestamp) {
        super(timestamp, EventType.SCALING_IN);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }

}
