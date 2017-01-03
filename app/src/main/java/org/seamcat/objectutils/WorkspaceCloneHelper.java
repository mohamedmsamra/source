package org.seamcat.objectutils;

import com.rits.cloning.Cloner;
import org.seamcat.model.IdElement;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.simulation.SimulationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceCloneHelper {

    public static Workspace clone(Workspace workspace) {
        Workspace clone = new Workspace();
        clone.setName( workspace.getName() );
        clone.setSimulationControl(workspace.getSimulationControl());
        clone.setVictimSystemId( workspace.getVictimSystemId() );
        clone.setVictimFrequency(workspace.getVictimFrequency());
        clone.setSystemModels(new ArrayList<>(workspace.getSystemModels()));
        clone.setInterferenceLinkUIs(new ArrayList<>(workspace.getInterferenceLinkUIs()));
        clone.getInterferingLinkFrequency().addAll( workspace.getInterferingLinkFrequency());
        clone.setEventProcessingList( new ArrayList<>(workspace.getEventProcessingList()));
        clone.setUseUserDefinedDRSS( workspace.isUseUserDefinedDRSS() );
        clone.setUserDefinedDRSS( workspace.getUserDefinedDRSS());
        return clone;
    }

    public static SimulationState clone(SimulationState state) {
        SimulationResult clone = new SimulationResult();
        final SimulationResult simulationResult = state.getSimulationResult();

        Cloner cloner = new Cloner();
        for (SimulationResultGroup result : simulationResult.getSystemPreSimulationResults()) {
            clone.getSystemPreSimulationResults().add( new SimulationResultGroup(result.getId(), result.getName(),
                    cloner.deepClone(result.getResultTypes()), result.getScenario()));
        }
        for (SimulationResultGroup result : simulationResult.getSeamcatResults()) {
            clone.getSeamcatResults().add( new SimulationResultGroup(result.getId(), result.getName(),
                    cloner.deepClone(result.getResultTypes()), result.getScenario()));
        }
        for (SimulationResultGroup result : simulationResult.getEventProcessingResults()) {
            clone.getEventProcessingResults().add( new SimulationResultGroup(result.getId(), result.getName(),
                    cloner.deepClone(result.getResultTypes()), result.getScenario()));
        }

        List<Map<PanelDefinition<?>, Object>> cloned = new ArrayList<>();
        for (Map<PanelDefinition<?>, Object> customUIState : state.getConfigurations()) {
            cloned.add( new HashMap<PanelDefinition<?>, Object>(customUIState));
        }

        return new SimulationState(cloned, clone);
    }

    public static boolean equals( SimulationState original, SimulationState clone ) {
        boolean equals = DeepEquals.equals( original.getSimulationResult(), clone.getSimulationResult());

        if (!equals) {
            return false;
        }

        List<Map<PanelDefinition<?>, Object>> originalList = original.getConfigurations();
        List<Map<PanelDefinition<?>, Object>> cloneList = clone.getConfigurations();

        if ( originalList.size() != cloneList.size() ) {
            return false;
        }

        for (int i = 0; i < originalList.size(); i++) {

            if ( !equalsCustomUI( originalList.get(i), cloneList.get(i) )) {
                return false;
            }
        }


        return true;
    }

    public static boolean equals(Workspace original, Workspace clone) {
        if (original == null || clone == null) return false;
        // compare victim, interferer, and control
        boolean equals = original.getVictimSystemId().equals( clone.getVictimSystemId() );
        if ( !equals) return false;
        equals = original.getName().equals( clone.getName());
        if ( !equals) return false;
        equals = original.getSystemModels().size() == clone.getSystemModels().size();
        if ( !equals) return false;
        for (int i=0; i<original.getSystemModels().size(); i++) {
            equals = idElement(original.getSystemModels().get(i), clone.getSystemModels().get(i));
            if ( !equals) return false;
        }
        equals = original.getInterferenceLinkUIs().size() == clone.getInterferenceLinkUIs().size();
        if ( !equals) return false;
        for ( int i=0; i<original.getInterferenceLinkUIs().size(); i++) {
            equals = DeepEquals.equals(original.getInterferenceLinkUIs().get(i).getSettings(), clone.getInterferenceLinkUIs().get(i).getSettings());
            if ( !equals) return false;
        }
        equals = DeepEquals.equals( original.getVictimFrequency(), clone.getVictimFrequency());
        if ( !equals ) return false;
        equals = original.getInterferingLinkFrequency().size() == clone.getInterferingLinkFrequency().size();
        if ( !equals ) return false;
        for (int i =0; i<original.getInterferingLinkFrequency().size(); i++) {
            Distribution orig = original.getInterferingLinkFrequency().get(i);
            Distribution cloneDist = clone.getInterferingLinkFrequency().get(i);
            equals = DeepEquals.equals( orig, cloneDist );
            if ( !equals ) return false;
        }
        equals = DeepEquals.equals(original.getSimulationControl(), clone.getSimulationControl());
        if (!equals) return false;

        equals = original.isUseUserDefinedDRSS() == clone.isUseUserDefinedDRSS();
        if ( !equals ) return false;
        equals = DeepEquals.equals( original.getUserDefinedDRSS(), clone.getUserDefinedDRSS());
        if ( !equals ) return false;
        return equalsEPP(original.getEventProcessingList(), clone.getEventProcessingList());
    }

    private static boolean idElement( IdElement<?> a, IdElement<?> b) {
        boolean equals = a.getId().equals(b.getId());
        return equals && DeepEquals.equals(a.getElement(), b.getElement());
    }

    private static boolean equalsEPP(List<EventProcessingConfiguration> original, List<EventProcessingConfiguration> clone) {
        if ( original.size() != clone.size() ) return false;

        for (int i = 0; i < original.size(); i++) {
            EventProcessingConfiguration origEPP = original.get(i);
            EventProcessingConfiguration cloneEPP = clone.get(i);

            if (!DeepEquals.equals(origEPP.getModel(), cloneEPP.getModel())) {
                return false;
            }
        }

        return true;
    }

    private static boolean equalsCustomUI( Map<PanelDefinition<?>, Object> original, Map<PanelDefinition<?>, Object> clone) {
        for (PanelDefinition<?> key : original.keySet()) {
            if ( !clone.containsKey( key) ) return false;

            if ( !DeepEquals.equals( original.get( key), clone.get(key))) {
                return false;
            }
        }

        return true;
    }
}
