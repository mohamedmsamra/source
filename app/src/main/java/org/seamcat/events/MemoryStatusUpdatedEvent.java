package org.seamcat.events;

public class MemoryStatusUpdatedEvent {

    private int memoryMax;
    private String memoryStatusLabel;
    private int memoryUsageValue;

    public MemoryStatusUpdatedEvent(int memoryMax, String memoryStatusLabel, int memoryUsageValue){
        this.memoryMax = memoryMax;
        this.memoryStatusLabel = memoryStatusLabel;
        this.memoryUsageValue = memoryUsageValue;
    }

    public int getMemoryMax() {
        return memoryMax;
    }

    public String getMemoryStatusLabel() {
        return memoryStatusLabel;
    }

    public int getMemoryUsageValue() {
        return memoryUsageValue;
    }
}
