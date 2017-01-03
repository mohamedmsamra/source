package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoEPP_8_Distance implements EventProcessingPlugin<DemoEPP_8_Distance.Input> {

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {
        ResultTypes types = new ResultTypes();

        double[] frequencyVictim = new double[scenario.numberOfEvents()];
        Map<String,List<Double>> frequencyInterferer = new HashMap<>();
        for (EventResult result : results) {
            frequencyVictim[result.getEventNumber()] = result.getVictimSystemLinks().get(0).getTxRxDistance();
            for (InterferenceLink link:scenario.getInterferenceLinks()){
                String key = link.getInterferingSystem().getName();
                for (int i=0;i<result.getInterferenceLinkResult(link).getInterferenceLinkResults().size();i++){
                    InterferenceLinkResult subLinkResult = result.getInterferenceLinkResult(link).getInterferenceLinkResults().get(i);
                    ensureNameDouble(key,frequencyInterferer).add(subLinkResult.getTxRxDistance());
                }
            }
        }

        types.getVectorResultTypes().add( new VectorResultType("Distance between the VLT and VLR", "m", frequencyVictim));

        VectorGroupResultType freqInterferer = new VectorGroupResultType("Distance between ILT and ILR - Vector group", "m");
        for (InterferenceLink link:scenario.getInterferenceLinks()) {
            String key = link.getInterferingSystem().getName();
            freqInterferer.addVector(new NamedVectorResult(key, new VectorResult(frequencyInterferer.get(key))));
        }
        types.getVectorGroupResultTypes().add(freqInterferer);

        return types;
    }

    /**
     * method to either create or to select the appropriate entry set
     * @param name
     * @param container
     * @return the list which is belong to the key
     */
    private List<Double> ensureNameDouble(String name, Map<String, List<Double>> container) {
        if (!container.containsKey(name)) {
            container.put(name, new ArrayList<Double>());
        }
        return container.get(name);
    }

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
        if ( !(scenario.getVictimSystem() instanceof GenericSystem)) {
            validator.error("Can only be applied is victim system is generic");
        }
        for (InterferenceLink link:scenario.getInterferenceLinks()){
            if (!(link.getInterferingSystem() instanceof GenericSystem)){
                validator.error("Can only be applied if interfering system is generic");
            }
        }
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 8: Collect Distance",
                "<html>Shows how to extract the victim and interfering<br>" +
                        "distance between Tx and Rx</html>");
    }

    public interface Input{}
}