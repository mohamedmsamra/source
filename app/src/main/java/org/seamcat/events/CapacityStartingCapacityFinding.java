package org.seamcat.events;

public class CapacityStartingCapacityFinding extends ContextEvent {

    private final int usersPrCell;
    private final int deltaUsers;
    private final double allowableOutage;
    private final int trials;
    private final boolean uplink;
    private final double target;

    public CapacityStartingCapacityFinding(Object context, int usersPrCell, int deltaUsers,
                                           double allowableOutage, int trials, boolean uplink, double target) {
        super(context);
        this.usersPrCell = usersPrCell;
        this.deltaUsers = deltaUsers;
        this.allowableOutage = allowableOutage;
        this.trials = trials;
        this.uplink = uplink;
        this.target = target;
    }

    public double getTarget() {
        return target;
    }

    public boolean isUplink() {
        return uplink;
    }

    public int getTrials() {
        return trials;
    }

    public double getAllowableOutage() {
        return allowableOutage;
    }

    public int getDeltaUsers() {
        return deltaUsers;
    }

    public int getUsersPrCell() {
        return usersPrCell;
    }
}
