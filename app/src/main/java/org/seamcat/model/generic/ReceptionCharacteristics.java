package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalDoubleValue;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.FunctionLibraryItem;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;

import java.util.List;
import java.util.ResourceBundle;

public interface ReceptionCharacteristics {

	
	
    @Config(order = 1, name = "Noise Floor", unit = "dBm", information = "RECEPTION_CHARACTERISTICS_NOISE_FLOOR_INFORMATION")
    Distribution noiseFloor();
    Distribution noiseFloor = Factory.distributionFactory().getConstantDistribution(-114.0);

    @Config(order = 2, name = "Blocking mode")
    GenericReceiver.BlockingAttenuationMode blockingAttenuationMode();

    @Config(order = 3, name = "Blocking mask")
    BlockingMask blockingMask();

    @Config(order = 4, name = "Intermodulation rejection", unit = "dB", rangeUnit = "MHz")
    OptionalFunction intermodulation_rejection();
    OptionalFunction intermodulation_rejection = new OptionalFunction(false, Factory.functionFactory().constantFunction(0.0));

    @Config(order = 5, name = "Receive power dynamic range", unit = "dB")
    OptionalDoubleValue receivePower();
    OptionalDoubleValue receivePower = new OptionalDoubleValue(false, 30.0);

    @Config(order = 6, name="Sensitivity", unit = "dBm")
    double sensitivity();
    double sensitivity = -98.0;
   

    @Config(order = 7, name = "Reception Bandwidth", unit = "kHz")
    double reception_bandwith();
    double reception_bandwith = 200.0;

    @Config(order = 8, name="Overloading", defineGroup = "overloading")
    boolean use_receiver_overloading();

    @Config(order = 9, name = "Overloading threshold", group = "overloading", unit = "dBm")
    Function overloading_mask();

    @Config(order = 10, name ="Receiver filter", group = "overloading", unit = "dB")
    Function receiver_filter();

    ChangeListener<ReceptionCharacteristics> change = new ChangeListener<ReceptionCharacteristics>() {
        public void handle(ReceptionCharacteristics model, List<AbstractItem> items, AbstractItem changedItem) {
            ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
            FunctionLibraryItem fl = null;
            for (AbstractItem item : items) {
                if ( item instanceof FunctionLibraryItem) {
                    fl = (FunctionLibraryItem) item;
                }
            }
            if ( fl == null ) return;
            if ( model.blockingAttenuationMode().equals(GenericReceiver.BlockingAttenuationMode.MODE_SENSITIVITY)) {
                fl.setUnit("dBm");
                fl.axisNames("MHz", "dBm");
                fl.functionDialogTitle(STRINGLIST.getString("RECEPTION_CHARACTERISTICS_BLOCKING_RESPONSE_DBM_TITLE"));
            } else {
                fl.setUnit("dB");
                fl.axisNames("MHz", "dB");
                fl.functionDialogTitle(STRINGLIST.getString("RECEPTION_CHARACTERISTICS_BLOCKING_RESPONSE_DB_TITLE"));
            }
        }
    };
}
