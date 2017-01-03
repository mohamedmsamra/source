package org.seamcat.eventprocessing;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.*;

import java.util.ArrayList;
import java.util.List;

public class DemoEPP_4_generate_CoverI_results implements EventProcessingPlugin<DemoEPP_4_generate_CoverI_results.Input> {

    public interface Input {
        @Config(order = 1, name = "use the sensitivity constraint")
        boolean useSensitivity();
        static boolean useSensitivity = true;
    }

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
        RadioSystem victimSystem = scenario.getVictimSystem();
        if ( !(victimSystem instanceof GenericSystem) ) {
            validator.error("VictimSystem must be of type Generic");
        }

    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 4: Generate C/I type of results: ",
                "<html>This Event Processing Plugin allows you to check various C/I, C/(N+I), <br>" +
                        "(N+I)/N and I/N vectors from the calculated dRSS and iRSS. You can select whether <br>" +
                        "you want the sensitivity constraint activated</html>");
	}

	@Override
	public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {
		ResultTypes types = new ResultTypes();
        List<SingleValueTypes<?>> resultTypes = types.getSingleValueTypes();
        List<VectorResultType> vectors = types.getVectorResultTypes();
        List<Double> vectorDrss = new ArrayList<Double>();


        List<List<Double>> vectorValues = new ArrayList<List<Double>>();

			for(InterferenceLink link : scenario.getInterferenceLinks()) {
				vectorValues.add( new ArrayList<Double>());
				vectorValues.add( new ArrayList<Double>());
				vectorValues.add( new ArrayList<Double>());
				vectorValues.add( new ArrayList<Double>());
			}

			double sensitivity = 0;
			double noiseFloor = 0;
			double CoverI;
			double cOverNI;
			double nIOverN;
			double iOverN;
			double dRSS;
			double iRSS;
			double rA, rB, rC;

			int numberOfSimulatedEvent = scenario.numberOfEvents();
			String textDescription = "Number of event where dRSS > sensitivity out of " + numberOfSimulatedEvent;
			
			double numberOfEventdRSSTrue = 0.0;

			for (EventResult res : results) {
                dRSS = res.getValue(SimulationResult.DRSSVector);
                vectorDrss.add(dRSS);

                for(int i=0; i<scenario.getInterferenceLinks().size(); i++ ) { // loop over all the interferers (including the number of Tx fof each interfering links
                    InterferenceLink link = scenario.getInterferenceLinks().get(i);

                    iRSS = 0; //Calculate the summation of all the Tx over one single interfering link.
                    for (InterferenceLinkResult linkResult : res.getInterferenceLinkResult(link).getInterferenceLinkResults()) {
                        iRSS += Math.pow(10, linkResult.getRiRSSUnwantedValue() / 10 );
                    }
                    iRSS = 10 * Math.log10( iRSS );

					sensitivity = ((GenericSystem)link.getVictimSystem()).getReceiver().getSensitivity();
					noiseFloor = ((GenericSystem)link.getVictimSystem()).getReceiver().getNoiseFloor().trial();
					
					int index = i*4;
					if ( input.useSensitivity() ){
						if (dRSS	>  sensitivity){
							numberOfEventdRSSTrue++;
							//C/I
							CoverI = dRSS - iRSS;
							vectorValues.get(index).add(CoverI);

							//C/(N+I)
							rA = Math.pow(10, dRSS / 10);
							rB = Math.pow(10, iRSS / 10);
							rC = Math.pow(10, noiseFloor / 10);

							cOverNI = Mathematics.linear2dB(rA / (rB + rC));
							vectorValues.get(index+1).add(cOverNI);
							
							//(N+I)/N
							rB = Math.pow(10, iRSS / 10);
							rC = Math.pow(10, noiseFloor / 10);

							nIOverN = Mathematics.linear2dB((rC + rB) / rC);
							vectorValues.get(index+2).add(nIOverN);
							
							//I/N
							iOverN = iRSS - noiseFloor;
							vectorValues.get(index+3).add(iOverN);
							
						}
					}
						
				}
			}

        numberOfEventdRSSTrue = numberOfEventdRSSTrue / (double) scenario.getInterferenceLinks().size();

        vectors.add( new VectorResultType("dRSS", "dBm", vectorDrss));
        resultTypes.add( new DoubleResultType("Sensitivity", "dBm", sensitivity));
        resultTypes.add( new DoubleResultType("Noise floor", "dBm", noiseFloor));
        resultTypes.add( new DoubleResultType(textDescription , "event", numberOfEventdRSSTrue));


			for(int i=0; i<scenario.getInterferenceLinks().size(); i++ ) {
				InterferenceLink link = scenario.getInterferenceLinks().get(i);
				int index = i*4;
				String textDescription1 = link.getInterferingSystem() + "C/I";
				String textDescription2 = link.getInterferingSystem() + "C/(N+I)";
				String textDescription3 = link.getInterferingSystem() + "(N+I)/N";
				String textDescription4 = link.getInterferingSystem() + "I/N";
				vectors.add( new VectorResultType(textDescription1, "dBm", vectorValues.get(index)));
				vectors.add( new VectorResultType(textDescription2, "dBm", vectorValues.get(index+1)));
				vectors.add( new VectorResultType(textDescription3, "dBm", vectorValues.get(index+2)));
				vectors.add( new VectorResultType(textDescription4, "dBm", vectorValues.get(index+3)));
			}
		return types;
	}
}
