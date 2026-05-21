package it.simulation.system;

import lombok.Getter;


@Getter
public class SystemStats {
    private final double throughput;          // System throughput
    private final double meanBusyServer;      // Mean busy server over time
    private final double meanServiceTime;     // Mean system service time
    private final double meanResponseTime;    // Mean system response time
    private final int totalCompletion;     // Sum of completion over all web server
    private final double totalBusyTime;       // Sum of busy time over all web server

    public SystemStats(double throughput, double meanBusyServer, double meanServiceTime, double meanResponseTime, int totalCompletion, double totalBusyTime) {
        this.throughput = throughput;
        this.meanBusyServer = meanBusyServer;
        this.meanServiceTime = meanServiceTime;
        this.meanResponseTime = meanResponseTime;
        this.totalCompletion = totalCompletion;
        this.totalBusyTime = totalBusyTime;
    }
}
