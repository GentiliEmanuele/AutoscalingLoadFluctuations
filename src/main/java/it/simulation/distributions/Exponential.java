package it.simulation.distributions;

import it.simulation.lib.Rngs;

public class Exponential extends Distribution {
    protected Exponential(Rngs r, Integer stream, double mean) {
        super(r, stream, mean);
    }

    @Override
    protected double newTime() {
        return -this.mean * Math.log(1.0 - this.r.random());
    }

    @Override
    public void setMean(double mean) {
        this.mean = mean;
    }
}
