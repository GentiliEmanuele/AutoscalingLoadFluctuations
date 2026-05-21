package it.simulation.events;

import it.simulation.system.SystemState;
import it.simulation.system.servers.WebServer;
import lombok.Getter;
import lombok.Setter;

public class ScalingOutEvent extends Event {
    @Getter @Setter
    private WebServer target;

    protected ScalingOutEvent(double timestamp, WebServer target) {
        super(timestamp, EventType.SCALING_OUT);
        this.target = target;
    }

    @Override
    public void process(SystemState s, EventVisitor visitor) throws IllegalLifeException {
        visitor.visit(s, this);
    }
}
