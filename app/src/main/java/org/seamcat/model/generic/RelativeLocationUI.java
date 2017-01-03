package org.seamcat.model.generic;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.CorrelatedDistanceSettingChangedEvent;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.BooleanItem;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;

import java.util.List;

public interface RelativeLocationUI {

    @Config(order = 1, name = "Correlated distance (origin = Victim/Interfering transmitter)", defineGroup = "corr")
    boolean useCorrelatedDistance();

    @Config(order = 2, name = "Delta X", unit = "km")
    Distribution deltaX();

    @Config(order = 3, name = "Delta Y", unit = "km")
    Distribution deltaY();

    @Config(order = 4, name = "Path azimuth", unit = "deg", invertedGroup = "corr")
    Distribution pathAzimuth();
    Distribution pathAzimuth = Factory.distributionFactory().getUniformDistribution(0, 360);

    @Config(order = 5, name = "Path distance factor", invertedGroup = "corr")
    Distribution pathDistanceFactor();
    Distribution pathDistanceFactor = Factory.distributionFactory().getUniformPolarDistanceDistribution(1.0);

    @Config(order = 6, name = "Use a polygon", defineGroup = "poly", invertedGroup = "corr")
    boolean usePolygon();

    @Config(order = 7, name = "Shape of the polygon", group = "poly")
    RelativeLocation.Shape shape();
    RelativeLocation.Shape shape = RelativeLocation.Shape.Hexagon;

    @Config(order = 8, name = "Turn ccw", unit = "degree", group= "poly")
    Distribution turnCCW();

    ChangeListener<RelativeLocationUI> change = new ChangeListener<RelativeLocationUI>() {
        @Override
        public void handle(RelativeLocationUI model, List<AbstractItem> items, AbstractItem changedItem) {
            if ( changedItem instanceof BooleanItem && changedItem.getLabel().equals("Correlated distance (origin = Victim/Interfering transmitter)")) {
                EventBusFactory.getEventBus().publish(new CorrelatedDistanceSettingChangedEvent());
            }
        }
    };
}
