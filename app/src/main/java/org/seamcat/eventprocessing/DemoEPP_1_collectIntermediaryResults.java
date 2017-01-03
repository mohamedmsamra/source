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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemoEPP_1_collectIntermediaryResults implements EventProcessingPlugin<DemoEPP_1_collectIntermediaryResults.Input> {

    public interface Input {
        @Config(order = 1, name = "Victim position Rx (X)") boolean vlrX();
        @Config(order = 2, name = "Victim position Rx (Y)") boolean vlrY();
        @Config(order = 3, name = "Victim position Tx (X)") boolean vltX();
        @Config(order = 4, name = "Victim position Tx (y)") boolean vltY();
        @Config(order = 5, name = "Victim Distance") boolean victimDistance();
        @Config(order = 6, name = "Victim Angle") boolean victimAngle();
        @Config(order = 7, name = "Interferer position Rx (X)") boolean ilrX();
        @Config(order = 8, name = "Interferer position Rx (Y)") boolean ilrY();
        @Config(order = 9, name = "Interferer position Tx (X)") boolean iltX();
        @Config(order = 10, name = "Interferer position Tx (Y)") boolean iltY();
        @Config(order = 11, name = "Interferer Distance") boolean interfererDistance();
        @Config(order = 12, name = "Interferer Angle") boolean interfererAngle();
    }

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 1: Collect intermediary results","<html>This Event Processing Plugin alllow to select the parameter<br>vector that you want to display after simulation</html>");
    }

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {
        ResultTypes types = new ResultTypes();
        List<VectorResultType> vectors = types.getVectorResultTypes();

        List<Double> vectorVLR_X = new ArrayList<Double>();
        List<Double> vectorVLR_Y = new ArrayList<Double>();
        List<Double> vectorVLT_X = new ArrayList<Double>();
        List<Double> vectorVLT_Y = new ArrayList<Double>();
        List<Double> vectorDistanceVictim = new ArrayList<Double>();
        List<Double> vectorAngleVictim = new ArrayList<Double>();
        Map<String,List<Double>> vectorILR_X = new LinkedHashMap<String, List<Double>>();
        Map<String,List<Double>> vectorILR_Y = new LinkedHashMap<String, List<Double>>();
        Map<String, List<Double>> vectorILT_X = new LinkedHashMap<String, List<Double>>();
        Map<String, List<Double>> vectorILT_Y = new LinkedHashMap<String, List<Double>>();
        Map<String, List<Double>> vectorDistanceInterferer = new LinkedHashMap<String, List<Double>>();
        Map<String, List<Double>> vectorAngleInterferer = new LinkedHashMap<String, List<Double>>();

        for (EventResult res : results) {
            for(InterferenceLink link : scenario.getInterferenceLinks()) {
                InterferenceLinkResults linkResult = res.getInterferenceLinkResult(link);
                LinkResult victimResult = linkResult.getVictimSystemLinks().get(0);
                vectorVLR_X.add(victimResult.rxAntenna().getPosition().getX());
                vectorVLR_Y.add(victimResult.rxAntenna().getPosition().getY());
                vectorVLT_X.add(victimResult.txAntenna().getPosition().getX());
                vectorVLT_Y.add(victimResult.txAntenna().getPosition().getY());
                vectorDistanceVictim.add(victimResult.getTxRxDistance());
                vectorAngleVictim.add(victimResult.getTxRxAngle());
                for (int i=0; i<linkResult.getInterferenceLinkResults().size(); i++) {
                    LinkResult subLink = linkResult.getInterferenceLinkResults().get(i).getInterferingSystemLink();
                    String iName = link.getInterferingSystem().toString() + "_subLink_" +i;
                    ensureName(iName, vectorILR_X).add(subLink.rxAntenna().getPosition().getX());
                    ensureName(iName, vectorILR_Y).add(subLink.rxAntenna().getPosition().getY());
                    ensureName(iName, vectorILT_X).add(subLink.txAntenna().getPosition().getX());
                    ensureName(iName, vectorILT_Y).add(subLink.txAntenna().getPosition().getY());
                    ensureName(iName, vectorDistanceInterferer).add(subLink.getTxRxDistance());
                    ensureName(iName, vectorAngleInterferer).add(subLink.getTxRxAngle());
                }
            }
        }
        if ( input.vlrX() )
            vectors.add( new VectorResultType("VLR_X pos", "km", vectorVLR_X));

        if ( input.vlrY() )
            vectors.add( new VectorResultType("VLR_Y pos", "km", vectorVLR_Y));

        if ( input.vltX() )
            vectors.add( new VectorResultType("VLT_X pos", "km", vectorVLT_X));

        if ( input.vltY() )
            vectors.add( new VectorResultType("VLT_Y pos", "km", vectorVLT_Y));

        if ( input.victimDistance() )
            vectors.add( new VectorResultType("Victim distance", "km", vectorDistanceVictim));

        if ( input.victimAngle() )
            vectors.add( new VectorResultType("Victim angle pos", "deg", vectorAngleVictim));

        if ( input.ilrX() ) {
            for (Map.Entry<String, List<Double>> entry : vectorILR_X.entrySet()) {
                vectors.add( new VectorResultType(entry.getKey() + "_ILR_X pos", "km", entry.getValue()));
            }
        }

        if ( input.ilrY() ) {
            for (Map.Entry<String, List<Double>> entry : vectorILR_Y.entrySet()) {
                vectors.add( new VectorResultType(entry.getKey() + "_ILR_Y pos", "km", entry.getValue()));
            }
        }

        if ( input.iltX() ) {
            for (Map.Entry<String, List<Double>> entry : vectorILT_X.entrySet()) {
                vectors.add( new VectorResultType(entry.getKey() + "_ILT_X pos", "km", entry.getValue()));
            }
        }

        if ( input.iltY() )  {
            for (Map.Entry<String, List<Double>> entry : vectorILT_Y.entrySet()) {
                vectors.add( new VectorResultType(entry.getKey()+"_ILT_Y pos", "km", entry.getValue()));
            }
        }

        if ( input.interfererDistance() ){
            for (Map.Entry<String, List<Double>> entry : vectorDistanceInterferer.entrySet()) {
                vectors.add( new VectorResultType(entry.getKey()+"_Interferer distance", "km", entry.getValue()));
            }
        }

        if ( input.interfererAngle() ) {
            for (Map.Entry<String, List<Double>> entry : vectorAngleInterferer.entrySet()) {
                vectors.add( new VectorResultType(entry.getKey()+"_Interferer angle pos", "deg", entry.getValue()));
            }
        }

        return types;
    }

    private List<Double> ensureName(String name, Map<String, List<Double>> container) {
        if ( !container.containsKey(name) ) {
            container.put(name, new ArrayList<Double>());
        }
        return container.get(name);
    }
}
