package org.seamcat.events;

import static org.seamcat.model.simulation.result.SimulationResult.*;

public class RSSEvent extends ContextEvent {

    public RSSEvent(Object context) {
        super(context);
        rss = new VectorValues();
        irssU = new VectorValues();
        irssB = new VectorValues();
    }

    public VectorValues get( String name ) {
        if ( name.equals(DRSS)) {
            return rss;
        } else if ( name.equals(IRSS_BLOCKING)) {
            return irssB;
        } else if ( name.equals(IRSS_UNWANTED)) {
            return irssU;
        }

        return null;
    }

    private int currentEvent;
    private VectorValues rss, irssU, irssB;

    public int getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(int currentEvent) {
        this.currentEvent = currentEvent;
    }

    public VectorValues getRss() {
        return rss;
    }

    public void setRss(VectorValues rss) {
        this.rss = rss;
    }

    public VectorValues getIrssU() {
        return irssU;
    }

    public void setIrssU(VectorValues irssU) {
        this.irssU = irssU;
    }

    public VectorValues getIrssB() {
        return irssB;
    }

    public void setIrssB(VectorValues irssB) {
        this.irssB = irssB;
    }
}
