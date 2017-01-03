package org.seamcat.model.generic;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.CognitiveRadioSettingChangedEvent;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalMaskFunction;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.BooleanItem;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;

import java.util.List;

public interface EmissionCharacteristics {

    @Config(order = 1, name = "Power", unit = "dBm", information = "EmissionCharacteristics.power")
    Distribution power();
    Distribution power = Factory.distributionFactory().getConstantDistribution(33);

    @Config(order = 3, name = "Emissions mask", unit = "dBc/Ref.BW")
    EmissionMask emissionMask();
    EmissionMask emissionMask = Defaults.defaultEmissionMask();

    //@Config(order = 4, name = "Emission mask as BEM")
    //boolean emissionMaskAsBEM();

    @Config(order = 5, name = "Emissions floor", unit = "dBm/Ref.BW")
    OptionalMaskFunction emissionFloor();
    OptionalMaskFunction emissionFloor = Defaults.defaultEmissionFloor();

    @Config(order = 6, name = "Power Control", defineGroup = "pc")
    boolean powerControl();

    @Config(order = 7, name = "Power control step size", unit = "dB", group = "pc")
    double stepSize();
    double stepSize = 2.0;

    @Config(order = 8, name = "Min threshold", unit = "dBm",group = "pc")
    double minThreshold();
    double minThreshold = -103.0;

    @Config(order = 9, name = "Dynamic range", unit = "dB", group = "pc")
    double dynamicRange();
    double dynamicRange = 6.0;


    @Config(order = 10, name = "Cognitive radio", defineGroup = "cr", information = "SensingLinkINFO")
    boolean cognitiveRadio();

    @Config(order = 11, name = "Detection threshold", group = "cr")
    Function detectionThreshold();
    Function detectionThreshold = Factory.functionFactory().constantFunction(0.0);

    @Config(order = 12, name = "Probability of failure", unit = "%", group = "cr")
    double probabilityOfFailure();

    @Config(order = 13, name = "Sensing reception bandwidth", unit = "kHz", group = "cr")
    double receptionBandwidth();
    double receptionBandwidth = 200.0;

    @Config(order = 14, name = "e.i.r.p. max in-block limit", group = "cr")
    MaskFunction eirpMax();
    MaskFunction eirpMax = Defaults.defaultEirpMax();

    @Config(order = 15, name = "Sensing link propagation model", group = "cr")
    PropagationModel propagationModel();
    PropagationModel propagationModel = Factory.propagationModelFactory().getHataSE21();


    ChangeListener<EmissionCharacteristics> change = new ChangeListener<EmissionCharacteristics>() {
        @Override
        public void handle(EmissionCharacteristics model, List<AbstractItem> items, AbstractItem changedItem) {
            if ( changedItem instanceof BooleanItem && changedItem.getLabel().equals("Cognitive radio")) {
                EventBusFactory.getEventBus().publish(new CognitiveRadioSettingChangedEvent());
            }
        }
    };
}
