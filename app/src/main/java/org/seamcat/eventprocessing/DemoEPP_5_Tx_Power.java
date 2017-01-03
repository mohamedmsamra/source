package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorResultType;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.types.result.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class DemoEPP_5_Tx_Power implements EventProcessingPlugin<DemoEPP_5_Tx_Power.Input> {

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {
        ResultTypes types = new ResultTypes();

        double[] txPower = new double[scenario.numberOfEvents()];
        Map<String,List<Double>> iRSSpower = new HashMap<>();
        for (EventResult result : results) {
            txPower[result.getEventNumber()] = result.getVictimSystemLinks().get(0).getTxPower();
            for (InterferenceLink link:scenario.getInterferenceLinks()){
                String key = link.getInterferingSystem().getName();
                for (int i=0;i<result.getInterferenceLinkResult(link).getInterferenceLinkResults().size();i++){
                    InterferenceLinkResult subLinkResult = result.getInterferenceLinkResult(link).getInterferenceLinkResults().get(i);
                    ensureNameDouble(key,iRSSpower).add(subLinkResult.getTxPower());
                }
            }
        }

        types.getVectorResultTypes().add( new VectorResultType("Tx Power results (victim)", "dBm", txPower));

        VectorGroupResultType irss = new VectorGroupResultType("Tx Power results (Interferer - Vector group)", "dBm");
        for (InterferenceLink link:scenario.getInterferenceLinks()) {
            String key = link.getInterferingSystem().getName();
            irss.addVector(new NamedVectorResult(key, new VectorResult(iRSSpower.get(key))));
        }
        types.getVectorGroupResultTypes().add(irss);

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
        return new DescriptionImpl("Demo 5: Collect Tx power",
                "<html>Shows how to extract the victim and interfering Tx power</html>");
    }

    public interface Input{}
}