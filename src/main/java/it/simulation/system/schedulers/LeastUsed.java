package it.simulation.system.schedulers;

import it.simulation.system.servers.AbstractServer;
import it.simulation.system.servers.ServerState;

import java.util.Comparator;
import java.util.List;

public class LeastUsed implements Scheduler {
    @Override
    public AbstractServer select(List<AbstractServer> webServers) {
        return webServers
                .stream()
                .filter(server -> server.getServerState() == ServerState.ACTIVE)
                .min(Comparator.comparingDouble(s -> s.size() / s.getCapacity()))
                .orElseThrow(() -> new IllegalStateException("No active server found"));
    }
}
