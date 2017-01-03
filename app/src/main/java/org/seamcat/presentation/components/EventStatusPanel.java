package org.seamcat.presentation.components;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.MemoryStatusUpdatedEvent;
import org.seamcat.presentation.SeamcatDistributionPlot;

public class EventStatusPanel extends AbstractStatusPanel {

    public EventStatusPanel() {
        EventBusFactory.getEventBus().subscribe(this);
    }

    public void destroy() {
        EventBusFactory.getEventBus().unsubscribe( this );
    }

    public void startingEventGeneration(final int eventsToBeCalculated) {
        if ( eventsToBeCalculated < SeamcatDistributionPlot.maxEventsToPlot) {
            initialize(eventsToBeCalculated);
        } else {
            initialize(SeamcatDistributionPlot.maxEventsToPlot-1);
        }
    }

	public void eventCompleted() {
        increment();
	}

	public void eventGenerationCompleted() {
        finished();
	}

    @UIEventHandler
    public void handleMemoryStatusRefresh(MemoryStatusUpdatedEvent event) {
        memoryUsage.setMaximum(event.getMemoryMax());
        memoryUsage.setValue(event.getMemoryUsageValue());
        memoryUsageLabel.setText(event.getMemoryStatusLabel());
    }


}
