package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.plugin.Empty;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.CustomUITab;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.ResultTypes;

import java.util.List;

@CustomUITab(ExampleCustomUIEPP.class)
public class DemoEPP_6_CellularInternals implements EventProcessingPlugin<Empty> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Empty input, Validator<Empty> validator) {
    }

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Empty empty) {
        return null;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Demo 6: Custom UI showcase",
                "<html>Shows how to have a custom result that generates additional results</html>");
    }
}
