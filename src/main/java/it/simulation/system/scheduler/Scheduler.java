package it.simulation.system.scheduler;

import it.simulation.system.servers.AbstractServer;

import java.util.List;

public interface Scheduler {
    AbstractServer select(List<AbstractServer> webServers);
}
