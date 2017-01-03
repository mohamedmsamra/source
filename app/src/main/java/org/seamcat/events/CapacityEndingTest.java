package org.seamcat.events;

public class CapacityEndingTest extends ContextEvent {

    private final int usersPerCell;
    private final double successRate;

    public CapacityEndingTest(Object context, int usersPerCell, double successRate) {
        super(context);
        this.usersPerCell = usersPerCell;
        this.successRate = successRate;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public int getUsersPerCell() {
        return usersPerCell;
    }
}
