package it.simulation.system.servers;

import it.simulation.events.IllegalLifeException;
import it.simulation.system.jobs.Job;
import it.simulation.system.jobs.JobList;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import static it.simulation.configurations.Config.SLIDING_WINDOW_SIZE;
import static it.simulation.configurations.Config.STOP;

public abstract class AbstractServer implements Server {
    @Getter @Setter private ServerState serverState;
    protected final JobList jobs;
    @Getter protected double capacity;
    @Getter private final ServerStats stats;
    private final DecimalFormat format;
    @Getter private Deque<Double> movingWindowResponseTime;

    public AbstractServer(double capacity, ServerState serverState, int index) {
        this.serverState = serverState;
        this.jobs = new JobList();
        this.capacity = capacity;
        this.stats = new ServerStats(index);
        this.format = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        this.format.applyPattern("###0.00000000");
        this.movingWindowResponseTime = new ArrayDeque<>();
    }

    @Override
    public void computeJobsAdvancement(double startTs, double endTs, Double completedJobResponseTime) throws IllegalLifeException {
        int completedJob = completedJobResponseTime == null ? 0 : 1;
        int jobAdvanced = jobs.size() + completedJob;

        stats.updateServerStats(startTs, endTs, jobAdvanced, completedJobResponseTime, this.serverState);

        // Update moving window response time
        if(completedJob == 1) {
            if(this.movingWindowResponseTime.size() == SLIDING_WINDOW_SIZE) {
                this.movingWindowResponseTime.removeFirst();
            }
            this.movingWindowResponseTime.addLast(completedJobResponseTime);
        }

        double quantum = (this.capacity / jobAdvanced) * (endTs - startTs);

        try{
            for (Job job : this.jobs.getJobs()) {
                job.decreaseRemainingLife(quantum);
            }
        } catch (IllegalLifeException e) {
            System.out.printf("endTs: %f", endTs);
            throw e;
        }
    }

    @Override
    public void addJob(Job job) {
        jobs.add(job);
    }

    @Override
    public boolean activeJobExists(){
        return jobs.activeJobExists();
    }

    @Override
    public int size() {
        return jobs.size();
    }

    @Override
    public double getMinRemainingLife() {
        return jobs.minRemainingLife();
    }

    @Override
    public Job getMinRemainingLifeJob() {
        return jobs.getMinRemainingLifeJob();
    }

    @Override
    public void printServerStats(double currentTs) {
        System.out.println("for " + stats.getCompletedJobs() + " jobs");
        // STOP instead of currentTs, because the arrival process ends at STOP and the simulation ends when all the servers are empty
        System.out.println("   average interarrival time =   " + format.format(STOP / stats.getCompletedJobs()));
        System.out.println("   average response time ... =   " + format.format(stats.getNodeSum() / stats.getCompletedJobs()));
        System.out.println("   average service time .... =   " + format.format(stats.getServiceSum() / stats.getCompletedJobs()));
        System.out.println("   average # in the node ... =   " + format.format(stats.getNodeSum() / currentTs));
        System.out.println("   utilization ............. =   " + format.format(stats.getServiceSum() / currentTs));
        System.out.println("   throughput  ............. =   " + format.format(stats.getCompletedJobs() / currentTs));
    }

    @Override
    public ServerStats getServerStats() {
        return stats;
    }


    public void resetMovingExpMeanResponseTime() {
        this.movingWindowResponseTime = new ArrayDeque<>();
    }

    public double getWindowedMeanResponseTime() {
        double sum = 0.0;
        for(Double d : movingWindowResponseTime) {
            sum += d / movingWindowResponseTime.size();
        }
        return sum;
    }

}
