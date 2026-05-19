package it.simulation.system.servers;

public class ServerStatsUtils {
    public static double computeThroughput(double timestamp, ServerStats serverStats) {
        return timestamp > 0 ? serverStats.getCompletedJobs() / timestamp : 0.0;
    }

    public static double computeUtilization(double timestamp, ServerStats serverStats) {
        return timestamp > 0 ? serverStats.getServiceSum() / timestamp : 0.0;
    }

    public static double computeServiceTime(ServerStats serverStats) {
        return serverStats.getCompletedJobs() > 0 ? serverStats.getServiceSum() / serverStats.getCompletedJobs() : 0.0;
    }
}
