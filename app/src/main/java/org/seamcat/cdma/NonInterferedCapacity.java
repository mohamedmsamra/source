package org.seamcat.cdma;

public class NonInterferedCapacity {
    private int usersPerCell;
    private double meanNoiseRise;

    public NonInterferedCapacity( int usersPerCell, double meanNoiseRise ) {
        this.usersPerCell = usersPerCell;
        this.meanNoiseRise = meanNoiseRise;
    }

    public int getUsersPerCell() {
        return usersPerCell;
    }

    public double getMeanNoiseRise() {
        return meanNoiseRise;
    }
}
