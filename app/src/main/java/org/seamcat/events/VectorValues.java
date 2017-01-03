package org.seamcat.events;

public class VectorValues {

    private String median, mean, stdDev;

    public VectorValues( String mean, String median, String stdDev ) {
        this.mean = mean;
        this.median = median;
        this.stdDev = stdDev;
    }

    public VectorValues() {
        mean = "Calculating...";
    }

    public String getMedian() {
        return median;
    }

    public void setMedian(String median) {
        this.median = median;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getStdDev() {
        return stdDev;
    }

    public void setStdDev(String stdDev) {
        this.stdDev = stdDev;
    }
}
