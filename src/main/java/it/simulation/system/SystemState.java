package it.simulation.system;

import it.simulation.distributions.Distribution;
import it.simulation.events.Event;
import it.simulation.events.EventCalendar;
import it.simulation.system.infrastructures.Infrastructure;
import it.simulation.system.infrastructures.InfrastructureFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SystemState {
    @Setter private double current; /* current time */
    private final Infrastructure infrastructure;
    private final EventCalendar calendar;
    private final Distribution servicesVA;
    private final Distribution arrivalVA;

    public SystemState(EventCalendar calendar, Distribution servicesVA, Distribution arrivalVA) {
        this.infrastructure = InfrastructureFactory.createInfrastructure();
        this.calendar = calendar;
        this.servicesVA = servicesVA;
        this.arrivalVA = arrivalVA;
    }

    public void addEvent(Event event) {
        this.calendar.addEvent(event);
    }

    public void printStats() {
        this.infrastructure.printServerStats(this.current);
    }

}
