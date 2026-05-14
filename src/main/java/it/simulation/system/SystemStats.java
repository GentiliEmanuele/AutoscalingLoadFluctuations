package it.simulation.system;

public class SystemStats {
    private final double throughput;        // System throughput
    private final double meanUtilization;   // Mean server utilization
    private final double meanBusyServer;    // Mean busy server over time
    private final double meanServiceTime;   // Mean system service time
    private final double meanResponseTime;  // Mean system response time

    public SystemStats() {
        throughput = 0.0;
        meanUtilization = 0.0;
        meanBusyServer = 0.0;
        meanServiceTime = 0.0;
        meanResponseTime = 0.0;
    }

    public SystemStats(double throughput, double meanUtilization, double meanBusyServer, double meanServiceTime, double meanResponseTime) {
        this.throughput = throughput;
        this.meanUtilization = meanUtilization;
        this.meanBusyServer = meanBusyServer;
        this.meanServiceTime = meanServiceTime;
        this.meanResponseTime = meanResponseTime;
    }
}
