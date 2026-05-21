package it.simulation.events;

import it.simulation.system.SystemState;

public interface EventVisitor {
    void visit(SystemState s, ArrivalEvent event) throws IllegalLifeException;
    void visit(SystemState s, CompletionEvent event) throws IllegalLifeException;
    void visit(SystemState s, ScalingOutReqEvent event) throws IllegalLifeException;
    void visit(SystemState s, ScalingOutEvent event) throws IllegalLifeException;
    void visit(SystemState s, ScalingInEvent event) throws IllegalLifeException;
}
