package org.seamcat.cdma;

public class NonInterferedCapacitySearch {

    private boolean converged;
    private int capacity;
    private int deltaUsers;

    public NonInterferedCapacitySearch( int capacity, int deltaUsers ) {
        this.capacity = capacity;
        this.deltaUsers = deltaUsers;
        this.converged = false;
    }

    public NonInterferedCapacitySearch( int capacity ) {
        this.converged = true;
        this.capacity = capacity;
    }


    public boolean isConverged() {
        return converged;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDeltaUsers() {
        return deltaUsers;
    }
}
