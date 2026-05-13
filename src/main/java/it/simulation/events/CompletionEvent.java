package it.simulation.events;

import it.simulation.system.SystemState;

public class CompletionEvent extends Event {
    public CompletionEvent(double timestamp) {
        super(timestamp, EventType.COMPLETION);
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }
}
