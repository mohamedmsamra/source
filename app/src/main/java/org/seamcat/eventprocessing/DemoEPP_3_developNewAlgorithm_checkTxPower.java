package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.simulation.result.InterferenceLinkResults;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorResultType;

import java.util.ArrayList;
import java.util.List;

public class DemoEPP_3_developNewAlgorithm_checkTxPower implements EventProcessingPlugin<DemoEPP_3_developNewAlgorithm_checkTxPower.Input> {

    public interface Input {
        @Config(order = 1, name = "Use power control") boolean usePowerControl();
        @Config(order = 2, name = "Power control step size", unit = "dB") double stepSize();
        @Config(order = 3, name = "Min Threshold", unit = "dBm") double minThreshold();
        @Config(order = 4, name = "Dynamic Range", unit = "dB") double dynamicRange();
        @Config(order = 5, name = "Display the output vector") boolean display();
        static double stepSize = 2.0;
        static double minThreshold = -103;
        static double dynamicRange = 6;
    }

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
        boolean genericISUsingPowerControl = false;
        for (InterferenceLink link : scenario.getInterferenceLinks()) {
            if ( link.getInterferingSystem() instanceof GenericSystem) {
                GenericSystem is = (GenericSystem) link.getInterferingSystem();
                genericISUsingPowerControl = is.getTransmitter().isUsingPowerControl();
            }
        }

        if ( !genericISUsingPowerControl ) {
            validator.error( "Have at least on generic interfering system using power control" );
        }
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 3: Develop a new algorithm",
                "<html>Compare results of computation from the plugin and directly from SEAMCAT: <br>" +
                        "This Event Processing Plugin allows to check the interfering Tx power by <br>" +
                        "extracting it (for each link) from SEAMCAT internal calculator and by comparing <br>" +
                        "it with the value computed by the algorithm of the plugin</html>");
	}

	@Override
	public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {
        ResultTypes types = new ResultTypes();
        List<VectorResultType> vectors = types.getVectorResultTypes();

		List<List<Double>> vectorValues = new ArrayList<List<Double>>();
		String textDecription1;
		String textDecription2;

		if ( input.display() ){
			for(InterferenceLink link : scenario.getInterferenceLinks()) {
				vectorValues.add( new ArrayList<Double>());
				vectorValues.add( new ArrayList<Double>());
			}

			for (EventResult res : results) {
				for(int i=0; i<scenario.getInterferenceLinks().size(); i++ ) {
					InterferenceLink link = scenario.getInterferenceLinks().get(i);
					if ( !(link.getInterferingSystem() instanceof GenericSystem) ) continue;
                    InterferenceLinkResults linkResult = res.getInterferenceLinkResult(link);

                    for (InterferenceLinkResult result : linkResult.getInterferenceLinkResults()) {
                        LinkResult interfererResult = result.getInterferingSystemLink();

                        double rPowerSupplied = interfererResult.getTxPower();
                        double rGainItWr = interfererResult.txAntenna().getGain();
                        double rGainWrIt = interfererResult.rxAntenna().getGain();
                        double rPathLossItWr = interfererResult.getTxRxPathLoss();

                        double rResultPowerGain;
                        double rResult;
                        double rPmin = input.minThreshold();
                        double rPinit = rPowerSupplied + rGainItWr - rPathLossItWr + rGainWrIt;

                        if( input.usePowerControl() ){
                            if (rPinit > rPmin && rPinit < rPmin + input.dynamicRange()) {
                                rResultPowerGain = -input.stepSize() * Math.floor((rPinit - rPmin) / input.stepSize());
                            } else if (rPinit <= rPmin) {
                                rResultPowerGain = 0;
                            } else {
                                rResultPowerGain = -input.dynamicRange();
                            }
                            rResult = rPowerSupplied + rResultPowerGain;
                        }else{
                            rResult = rPowerSupplied;
                        }
                        int index = i*2;
                        vectorValues.get(index).add(rResult);
                        vectorValues.get(index+1).add(rPowerSupplied + interfererResult.getValue(GenericSystem.TX_POWER_CONTROL_GAIN));
                    }
				}
			}
			for(int i=0; i<scenario.getInterferenceLinks().size(); i++ ) {
				InterferenceLink link = scenario.getInterferenceLinks().get(i);
					int index = i*2;
					if ( input.usePowerControl() ){
						textDecription1 = link.getInterferingSystem() + " Interfering Tx power with PC"; 
					}else{
						textDecription1 = link.getInterferingSystem() +" Interfering Tx power without PC";
					}
					vectors.add( new VectorResultType(textDecription1, "dBm", vectorValues.get(index)));
				 
				
					if (((GenericSystem)link.getInterferingSystem()).getTransmitter().isUsingPowerControl()) {
						textDecription2 = link.getInterferingSystem() + " ref. (from SEAMCAT) Interfering Tx power with PC";
					} else {
						textDecription2 = link.getInterferingSystem() + "ref. (from SEAMCAT) Interfering Tx power without PC";
					}
					vectors.add( new VectorResultType(textDecription2, "dBm", vectorValues.get(index+1)));
				
			}
		}
		return types;
	}
}
