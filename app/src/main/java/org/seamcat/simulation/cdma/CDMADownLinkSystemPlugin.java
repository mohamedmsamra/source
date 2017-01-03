package org.seamcat.simulation.cdma;

import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.systems.ParallelSimulation;
import org.seamcat.model.systems.SystemPlugin;
import org.seamcat.model.systems.UIToModelConverter;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.seamcat.model.systems.SystemPlugin.CorrelationMode.CORRELATED;
import static org.seamcat.model.systems.SystemPlugin.CorrelationMode.NONE;

public class CDMADownLinkSystemPlugin implements SystemPlugin<SystemModelCDMADownLink, CellularSystem> {

    private CellularSystem system;
    private PreSimulationResultsImpl preSimulationResults;


    public CDMADownLinkSystemPlugin() {
        preSimulationResults = new PreSimulationResultsImpl();
    }

    @Override
    public List<CorrelationMode> getCorrelationModes() {
        return Arrays.asList( NONE, CORRELATED);
    }

    @Override
    public CellularSystem convert(SystemModelCDMADownLink ui) {
        // create systemconvert
        SystemSimulationModel convert = UIToModelConverter.convert(ui);
        system = (CellularSystem) convert.getSystem();
        return system;
    }

    @Override
    public List<String> getCorrelationPointNames() {
        return Collections.singletonList("BS ref. cell");
    }

    @Override
    public ParallelSimulation simulationInstance() {
        return new CDMADownLinkSimulation(system, preSimulationResults);
    }


}
