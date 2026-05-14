package it.simulation.system.schedulers;

import it.simulation.system.servers.AbstractServer;

import java.util.List;

public interface Scheduler {
    AbstractServer select(List<AbstractServer> webServers);
}
