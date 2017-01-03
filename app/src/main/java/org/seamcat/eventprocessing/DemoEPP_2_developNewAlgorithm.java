package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.InterferenceLinkResults;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorResultType;

import java.util.ArrayList;
import java.util.List;

public class DemoEPP_2_developNewAlgorithm implements EventProcessingPlugin<DemoEPP_2_developNewAlgorithm.Input> {

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
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 2: Develop a new algorithm","<html>Calculate the Received Signal Strength at the ILR: This Event <br>Processing Plugin allows to generate the received signal strength at the <br>Interfering Link Receiver with or without PC</html>");
	}

	@Override
	public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {
        ResultTypes types = new ResultTypes();
        List<VectorResultType> vectors = types.getVectorResultTypes();

		List<List<Double>> vectorValues = new ArrayList<List<Double>>();
		String nameDecription;

		if ( input.display() ){
			for(InterferenceLink link : scenario.getInterferenceLinks()) {
				vectorValues.add( new ArrayList<Double>());
				vectorValues.add( new ArrayList<Double>());
			}

			for (EventResult res : results) {
				for(int i=0; i<scenario.getInterferenceLinks().size(); i++ ) {
					InterferenceLink link = scenario.getInterferenceLinks().get(i);
					InterferenceLinkResults linkResult = res.getInterferenceLinkResult(link);

                    LinkResult interfererResult = linkResult.getInterferenceLinkResults().get(0).getInterferingSystemLink();
                    double rPowerSupplied = interfererResult.getTxPower();
					double rGainItWr = interfererResult.txAntenna().getGain();
					double rGainWrIt = interfererResult.rxAntenna().getGain();
					double rPathLossItWr = interfererResult.getTxRxPathLoss();

					double rResultPowerGain;
					double rResult;
					double rStep = input.stepSize();
					double rPmin = input.minThreshold();
					double rRange = input.dynamicRange();

					double rPinit = rPowerSupplied + rGainItWr - rPathLossItWr + rGainWrIt;

					if( input.usePowerControl() ){
						if (rPinit > rPmin && rPinit < rPmin + rRange) {
							rResultPowerGain = -rStep * Math.floor((rPinit - rPmin) / rStep);
						} else if (rPinit <= rPmin) {
							rResultPowerGain = 0;
						} else {
							rResultPowerGain = -rRange;
						}
						rResult = rPinit + rResultPowerGain;
					}else{
						rResult = rPinit;
					}
					
					vectorValues.get(i).add(rResult);
				}
			}
			for(int i=0; i<scenario.getInterferenceLinks().size(); i++ ) {
				InterferenceLink link = scenario.getInterferenceLinks().get(i);
					if ( input.usePowerControl() ){
						nameDecription = link.getInterferingSystem() + " ILR signal strength with PC"; 
					}else{
						nameDecription = link.getInterferingSystem() +" ILR signal strength without PC";
					}
					vectors.add( new VectorResultType(nameDecription, "dBm", vectorValues.get(i)));
				
			}
		}
		return types;
	}
}
