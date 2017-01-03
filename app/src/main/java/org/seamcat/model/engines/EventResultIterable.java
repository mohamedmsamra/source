package org.seamcat.model.engines;

import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.simulation.Simulation;

import java.util.Iterator;
import java.util.LinkedList;

public class EventResultIterable implements Iterable<EventResult>, Iterator<EventResult> {

    private boolean lastAdded;
    private final LinkedList<EventResult> results;
    private Simulation simulation;

    public EventResultIterable( Simulation simulation) {
        this.simulation = simulation;
        results = new LinkedList<EventResult>();
        lastAdded = false;
    }

    @Override
    public Iterator<EventResult> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        synchronized (results) {
            while (!lastAdded) {
                if (results.size() == 0 ) {
                    try {
                        results.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return true;
                }
            }

            // main simulation is completed so we should no longer wait for
            // event result, simply consume them as long as they are in the list
            return results.size() > 0;
        }
    }

    @Override
    public EventResult next() {
        synchronized ( results ) {
            return results.removeFirst();
        }
    }

    public void lastAdded() {
        lastAdded = true;
        synchronized (results) {
            results.notifyAll();
        }
    }

    public void addResult( EventResult result ) {
        synchronized (results ) {
            results.addLast( result );
            results.notifyAll();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
