package org.seamcat.events;

public class CapacityStartingTest extends ContextEvent {

    private final int usersPrCell;
    private final int trials;

    public CapacityStartingTest(Object context, int usersPrCell, int trials) {
        super(context);
        this.usersPrCell = usersPrCell;
        this.trials = trials;
    }

    public int getTrials() {
        return trials;
    }

    public int getUsersPrCell() {
        return usersPrCell;
    }

}
