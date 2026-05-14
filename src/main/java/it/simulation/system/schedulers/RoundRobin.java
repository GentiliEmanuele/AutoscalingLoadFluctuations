package it.simulation.system.schedulers;

import it.simulation.system.servers.AbstractServer;
import it.simulation.system.servers.ServerState;

import java.util.List;

public class RoundRobin implements Scheduler {
    private int nextAssigningServer;

    public RoundRobin(){
        nextAssigningServer = 0;
    }

    @Override
    public AbstractServer select(List<AbstractServer> webServers) {
        for(int currIndex, i = 0; i < webServers.size(); i++) {
            currIndex = (nextAssigningServer + i) % webServers.size();
            AbstractServer server = webServers.get(currIndex);
            if (server.getServerState() == ServerState.ACTIVE) {
                nextAssigningServer = (currIndex + 1) % webServers.size();
                return server;
            }
        }

        throw new RuntimeException("No active server found");
    }
}
