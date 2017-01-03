package org.seamcat.model.simulation.result;

import org.seamcat.model.simulation.SimulationResultGroup;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {

    public static final String DRSS = "dRSS";
    public static final String IRSS_UNWANTED = "iRSS Unwanted";
    public static final String IRSS_BLOCKING = "iRSS Blocking";
    public static final String IRSS_SELECTIVITY = "iRSS Selectivity";
    public static String CDMA_RESULTS  = "CDMA Results";
    public static String OFDMA_RESULTS = "OFDMA Results";

    public static final String dBm        = "dBm";
    public static final String DRSSVector = "dRSS vector";

    private final List<SimulationResultGroup> systemPreSimulationResults;
    private final List<SimulationResultGroup> seamcatResults;
    private final List<SimulationResultGroup> eventProcessingResults;

    public SimulationResult() {
        systemPreSimulationResults = new ArrayList<>();
        seamcatResults = new ArrayList<>();
        eventProcessingResults = new ArrayList<>();
    }

    public SimulationResult( List<SimulationResultGroup> systemPreSimulationResults, List<SimulationResultGroup> seamcatResults, List<SimulationResultGroup> eventProcessingResults ) {
        this.systemPreSimulationResults = systemPreSimulationResults;
        this.seamcatResults = seamcatResults;
        this.eventProcessingResults = eventProcessingResults;
    }

    public List<SimulationResultGroup> getSeamcatResults() {
        return seamcatResults;
    }

    public List<SimulationResultGroup> getSystemPreSimulationResults() {
        return systemPreSimulationResults;
    }

    public SimulationResultGroup getSeamcatResult( String groupName ) {
        for (SimulationResultGroup seamcatResult : seamcatResults) {
            if ( seamcatResult.getName().equals( groupName )) {
                return seamcatResult;
            }
        }
        throw new RuntimeException("Group not found: " + groupName );
    }

    public boolean hasSeamcatGroup( String groupName ) {
        for (SimulationResultGroup seamcatResult : seamcatResults) {
            if ( seamcatResult.getName().equals( groupName )) {
                return true;
            }
        }
        return false;
    }

    public List<SimulationResultGroup> getEventProcessingResults() {
        return eventProcessingResults;
    }
}
