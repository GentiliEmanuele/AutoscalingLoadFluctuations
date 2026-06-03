package it.simulation.system.servers;

import lombok.Getter;
import lombok.Setter;

import static it.simulation.configurations.Config.START;

@Getter
public class ServerStats {
    private final int serverIndex; /* server index */
    private double nodeSum; /* mean population in the node */
    private double serviceSum; /* mean population in service */
    private int arrivedJobs; /* number of arrived jobs */
    private int completedJobs; /* number of completed jobs */
    private double currMeanResponseTime; /* current mean response time */
    @Setter
    private double currOutputFrequency;

    public ServerStats(int serverIndex){
        this.serverIndex            = serverIndex;
        this.nodeSum                = 0.0;
        this.serviceSum             = 0.0;
        this.completedJobs          =   0;
        this.arrivedJobs           =   0;
    }

    public ServerStats(ServerStats serverStats) {
        this.serverIndex            = serverStats.serverIndex;
        this.nodeSum                = serverStats.nodeSum;
        this.serviceSum             = serverStats.serviceSum;
        this.completedJobs          = serverStats.completedJobs;
        this.arrivedJobs           = serverStats.arrivedJobs;
        this.currMeanResponseTime   = serverStats.currMeanResponseTime;
    }

    public ServerStats(int serverIndex, double nodeSum, double serviceSum, int completedJobs, int arrivedJobs, double currMeanResponseTime, double currOutputFrequency) {
        this.serverIndex = serverIndex;
        this.nodeSum = nodeSum;
        this.serviceSum = serviceSum;
        this.completedJobs = completedJobs;
        this.currMeanResponseTime = currMeanResponseTime;
        this.currOutputFrequency = currOutputFrequency;
        this.arrivedJobs = arrivedJobs;
    }

    public void updateServerStats(double startTs, double endTs, double jobNum, Double completedJobResponseTime, ServerState serverState) {

        assert startTs >= 0 && endTs >= startTs && jobNum >= 0;

        if (endTs < START) return;

        if(jobNum > 0) {
            this.nodeSum    += (endTs - startTs) * jobNum;
            this.serviceSum += (endTs - startTs);
        }

        /* Check if there has been a completion */
        boolean isCompletion = completedJobResponseTime != null;
        if (isCompletion) {
            this.completedJobs++;
            currMeanResponseTime += (completedJobResponseTime - currMeanResponseTime) / completedJobs;
        }
    }

    public void incrementArrivedJobs(double arrivalTime) {
        if (arrivalTime < START) return;
        this.arrivedJobs++;
    }

}
