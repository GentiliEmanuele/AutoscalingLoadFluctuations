package it.simulation.events;

import it.simulation.system.SystemState;
import lombok.Getter;

@Getter
public abstract class Event {
    private final double timestamp;
    private final EventType eventType;

    protected Event(double timestamp, EventType eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public abstract void process(SystemState s, EventVisitor visitor) throws IllegalLifeException;
}
