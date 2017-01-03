package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.Unit;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorResultType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.seamcat.model.cellular.CellularSystem.*;

public class DemoEPP_10_OFDMA_Internals implements EventProcessingPlugin<DemoEPP_10_OFDMA_Internals.Input> {

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {

        Map<String, List<Double>> collectedVectors = new LinkedHashMap<>();

        for (EventResult result : results) {

            LinkedHashMap<String, List<Double>> values = result.getVectorValues();
            for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
                handle(input.requestedSubcarriers(), entry, REQUESTED_SUBCARRIERS, collectedVectors);
                handle(input.subCarrierRatio(), entry, SUB_CARRIER_RATIO, collectedVectors);
                handle(input.frequency(), entry, FREQUENCY, collectedVectors);
                handle(input.thermalNoise(), entry, THERMAL_NOISE, collectedVectors);
                handle(input.bitRateAchieved(), entry, BIT_RATE_ACHIEVED, collectedVectors);
                handle(input.receivedPower(), entry, RECEIVED_POWER, collectedVectors);
                handle(input.SINRAchieved(), entry, SINR_ACHIEVED, collectedVectors);
                handle(input.currentTransmitPower(), entry, CURRENT_TRANSMIT_POWER, collectedVectors);
                handle(input.interferencePower(), entry, INTERFERENCE_POWER, collectedVectors);
                handle(input.powerControlPL(), entry, POWER_CONTROL_PL, collectedVectors);
                handle(input.powerControlPLilx(), entry, POWER_CONTROL_PLILX, collectedVectors);
                handle(input.interSystemInterference(), entry, INTER_SYSTEM_INTERFERENCE, collectedVectors);
                handle(input.externalInterference(), entry, EXTERNAL_INTERFERENCE, collectedVectors);
                handle(input.externalInterferenceBlocking(), entry, EXTERNAL_INTER_BLOC, collectedVectors);
                handle(input.externalInterferenceUnwanted(), entry, EXTERNAL_INTER_UNW, collectedVectors);
                handle(input.baseStationBitRate(), entry, BASE_STATION_BIT_RATE, collectedVectors);
                handle(input.pathLoss(), entry, PATH_LOSS, collectedVectors);
                handle(input.effectivePathLoss(), entry, EFFECTIVE_PATH_LOSS, collectedVectors);
            }
        }
        ResultTypes types = new ResultTypes();
        for (Map.Entry<String, List<Double>> entry : collectedVectors.entrySet()) {
            String unit = Unit.dBm.name();
            if ( entry.getKey().startsWith(FREQUENCY)) {
                unit = "MHz";
            } else if ( entry.getKey().startsWith(SUB_CARRIER_RATIO)) {
                unit = "users";
            } else if ( entry.getKey().startsWith(BIT_RATE_ACHIEVED )) {
                unit = "kbps";
            } else if ( entry.getKey().startsWith(BASE_STATION_BIT_RATE )) {
                unit = "kbps";
            } else if (entry.getKey().startsWith(REQUESTED_SUBCARRIERS)){
                unit = "#";
            }
            types.getVectorResultTypes().add(
                new VectorResultType(entry.getKey(), unit, entry.getKey(), entry.getValue() ));
        }

        return types;
    }

    private void handle(boolean relevant, Map.Entry<String, List<Double>> entry, String prefix, Map<String, List<Double>> collectedVectors) {
        if ( relevant ) {
            if ( entry.getKey().startsWith(prefix)) {
                ensure(collectedVectors, entry.getKey()).addAll( entry.getValue());
            }
        }
    }

    private List<Double> ensure(Map<String, List<Double>> collectedVectors, String name) {
        if ( !collectedVectors.containsKey(name)) {
            collectedVectors.put(name, new ArrayList<Double>());
        }

        return collectedVectors.get(name);
    }

        @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
        if (!(scenario.getVictimSystem() instanceof CellularSystem)
                ||  ((CellularSystem) scenario.getVictimSystem()).getOFDMASettings() == null)
            validator.error("Can only be applied if victim system is an OFDMA system (UL or DL)");
    }


    @Override
    public Description description() {
        return new DescriptionImpl("Demo 10: OFDMA Internals",
                "<html>This plugin will expose internal variables from an OFDMA simulation</html>");
    }

    public interface Input {
        @Config(order = 1, name="Requested resource blocks at the UE")
        boolean requestedSubcarriers();

        @Config(order = 2, name="Number of active users")
        boolean subCarrierRatio();

        @Config(order = 3, name="Frequency")
        boolean frequency();

        @Config(order = 4, name="Thermal noise")
        boolean thermalNoise();

        @Config(order = 5, name="Bit rate achieved")
        boolean bitRateAchieved();

        @Config(order = 6, name="Received power")
        boolean receivedPower();

        @Config(order = 7, name="SINR achieved")
        boolean SINRAchieved();

        @Config(order = 8, name = "Current transmit power")
        boolean currentTransmitPower();

        @Config(order = 9, name = "DL total interference power")
        boolean interferencePower();

        @Config(order = 10, name="Inter system interference")
        boolean interSystemInterference();

        @Config(order = 11, name="External interference")
        boolean externalInterference();

        @Config(order = 12, name="External interference blocking")
        boolean externalInterferenceBlocking();

        @Config(order = 13, name="External interference unwanted")
        boolean externalInterferenceUnwanted();

        @Config(order = 14, name="Base station bit rate achieved")
        boolean baseStationBitRate();

        @Config(order = 15, name = "Power control PL")
        boolean powerControlPL();

        @Config(order = 16, name = "Power control PLilx")
        boolean powerControlPLilx();

        @Config(order = 17, name = "Path loss")
        boolean pathLoss();

        @Config(order = 18, name = "Effective path loss")
        boolean effectivePathLoss();

    }
}
