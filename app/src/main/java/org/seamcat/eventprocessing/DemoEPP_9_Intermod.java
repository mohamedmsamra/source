package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.Unit;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.NamedVectorResult;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorGroupResultType;
import org.seamcat.simulation.generic.Intermodulation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemoEPP_9_Intermod implements EventProcessingPlugin<DemoEPP_9_Intermod.VoidInput> {


    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, VoidInput input) {
        ResultTypes result = new ResultTypes();

        Map<String, double[]> intermod_vectors = new LinkedHashMap<>();
        for (EventResult event : results) {

            LinkResult victimLink = event.getVictimSystemLinks().get(0);
            for (Map.Entry<String, Double> entry : victimLink.getValues().entrySet()) {
                if ( entry.getKey().startsWith( Intermodulation.VR_INTERMOD)) {
                    ensure( scenario.numberOfEvents(), entry.getKey(), intermod_vectors );
                    intermod_vectors.get(entry.getKey())[event.getEventNumber()] = entry.getValue();
                }
            }

            int i = 1;
            for (InterferenceLink link : scenario.getInterferenceLinks()) {
                for (InterferenceLinkResult iLink : event.getInterferenceLinkResult(link).getInterferenceLinkResults()) {
                    String name = Intermodulation.IRSS + " " + i;
                    ensure( scenario.numberOfEvents(), name, intermod_vectors);
                    intermod_vectors.get( name )[event.getEventNumber()] = iLink.getValue( Intermodulation.IRSS );
                    i++;
                }
            }
        }

        VectorGroupResultType vr_intermod = new VectorGroupResultType("Intermodulation internals", Unit.dBm.name());
        for (Map.Entry<String, double[]> entry : intermod_vectors.entrySet()) {
            vr_intermod.addVector( new NamedVectorResult(entry.getKey(), entry.getValue()));
        }
        result.getVectorGroupResultTypes().add( vr_intermod );

        return result;
    }

    private void ensure(int size, String key, Map<String, double[]> vectors) {
        if ( !vectors.containsKey( key)) {
            vectors.put(key, new double[size]);
        }
    }

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, VoidInput input, Validator<VoidInput> validator) {

    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 9: Intermodulation internals",
                "<html>Collect intermediate variables of the <br>intermodulation calculation</html>");
    }

    public interface VoidInput {

    }

}
