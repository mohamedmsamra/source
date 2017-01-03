package org.seamcat.events;

public class CapacityEndingTrial extends ContextEvent {

    private final double outage;
    private final boolean success;
    private final int trialid;


    public CapacityEndingTrial(Object context, double outage, boolean success, int trialid) {
        super(context);
        this.outage = outage;
        this.success = success;
        this.trialid = trialid;
    }

    public int getTrialid() {
        return trialid;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getOutage() {
        return outage;
    }
}
