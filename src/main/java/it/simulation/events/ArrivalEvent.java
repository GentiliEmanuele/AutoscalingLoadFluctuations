package it.simulation.events;

import it.simulation.system.SystemState;

public class ArrivalEvent extends Event {
    public ArrivalEvent(double timestamp) {
        super(timestamp, EventType.ARRIVAL);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s,this);
    }
}
