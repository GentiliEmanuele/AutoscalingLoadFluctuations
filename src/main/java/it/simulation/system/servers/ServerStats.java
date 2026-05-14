package it.simulation.system.servers;

import lombok.Getter;

@Getter
public class ServerStats {
    private final int serverIndex; /* server index */
    private double nodeSum; /* mean population in the node */
    private double serviceSum; /* mean population in service */
    private int completedJobs; /* number of completed jobs */
    private double currMeanResponseTime; /* current mean response time */


    public ServerStats(int serverIndex){
        this.serverIndex            = serverIndex;
        this.nodeSum                = 0.0;
        this.serviceSum             = 0.0;
        this.completedJobs          =   0;
    }

    public ServerStats(ServerStats serverStats) {
        this.serverIndex            = serverStats.serverIndex;
        this.nodeSum                = serverStats.nodeSum;
        this.serviceSum             = serverStats.serviceSum;
        this.completedJobs          = serverStats.completedJobs;
        this.currMeanResponseTime   = serverStats.currMeanResponseTime;
    }

    public void updateServerStats(double startTs, double endTs, double jobNum, Double completedJobResponseTime, ServerState serverState) {

        assert startTs >= 0 && endTs >= startTs && jobNum >= 0;

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

}
