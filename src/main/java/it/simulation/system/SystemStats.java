package it.simulation.system;

import lombok.Getter;


@Getter
public class SystemStats {
    private final double throughput;          // System throughput
    private final double meanUtilization;     // Mean server utilization
    private final double meanBusyServer;      // Mean busy server over time
    private final double meanServiceTime;     // Mean system service time
    private final double meanResponseTime;    // Mean system response time
    private final double totalCompletion;     // Sum of completion over all web server
    private final double totalBusyTime;       // Sum of busy time over all web server

    public SystemStats(double throughput, double meanUtilization, double meanBusyServer, double meanServiceTime, double meanResponseTime, double totalCompletion, double totalBusyTime) {
        this.throughput = throughput;
        this.meanUtilization = meanUtilization;
        this.meanBusyServer = meanBusyServer;
        this.meanServiceTime = meanServiceTime;
        this.meanResponseTime = meanResponseTime;
        this.totalCompletion = totalCompletion;
        this.totalBusyTime = totalBusyTime;
    }
}
