package org.seamcat.simulation.result;

import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.scenario.WorkspaceScenario;

import java.util.*;

import static org.seamcat.simulation.LocalEnvironmentSelector.pickLocalEnvironment;

public class MutableEventResult implements EventResult {

    private Map<InterferenceLink, MutableInterferenceLinkResults> result;
    private LinkedHashMap<String, Double> values;
    private LinkedHashMap<String, List<Double>> vectorValues;
    private List<MutableLinkResult> victimSystemLinks;
    private List<ActiveInterferer> interferingElements;

    private final int eventNumber;

    public MutableEventResult(int eventNumber) {
        this.eventNumber = eventNumber;
        victimSystemLinks = new ArrayList<>();
        interferingElements = new ArrayList<>();
        result = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        vectorValues = new LinkedHashMap<>();
    }

    @Override
    public int getEventNumber() {
        return eventNumber;
    }

    public MutableInterferenceLinkResults getInterferenceLinkResult(InterferenceLink link) {
        if ( result.containsKey( link) ) {
            return result.get( link );
        } else {
            return new MutableInterferenceLinkResults(victimSystemLinks);
        }
    }

    public void addInterferenceLinkResult( MutableInterferenceLinkResult linkResult ) {
        InterferenceLink link = linkResult.getInterferenceLink();
        if ( result.get( link) == null ) {
            result.put( link, new MutableInterferenceLinkResults(victimSystemLinks));
        }
        result.get( link ).addInterferenceLinkResult( linkResult );
    }

    @Override
    public LinkedHashMap<String, Double> getValues() {
        return new LinkedHashMap<>(values);
    }

    public void addValue( String name, double value ) {
        values.put(name, value);
    }

    public void addVectorValue( String name, double value) {
        if ( !vectorValues.containsKey( name)) {
            vectorValues.put( name, new ArrayList<Double>());
        }
        vectorValues.get(name).add( value );
    }

    @Override
    public Double getValue(String name) {
        return values.get(name);
    }

    @Override
    public List<MutableLinkResult> getVictimSystemLinks() {
        return Collections.unmodifiableList(victimSystemLinks);
    }

    @Override
    public List<ActiveInterferer> getInterferingElements() {
        return Collections.unmodifiableList(interferingElements);
    }

    public MutableLinkResult createVictimSystemLink(WorkspaceScenario scenario) {
        MutableLinkResult result = new MutableLinkResult();
        result.setValue(GenericSystem.COVERAGE_RADIUS, scenario.getVictimCoverageRadius());
        RadioSystem system = scenario.getVictimSystem();
        result.txAntenna().setLocalEnvironment(pickLocalEnvironment(system.getTransmitter().getLocalEnvironments()));
        result.rxAntenna().setLocalEnvironment(pickLocalEnvironment(system.getReceiver().getLocalEnvironments()));
        victimSystemLinks.add( result );
        return result;
    }

    public void addVictimSystemLink( MutableLinkResult victimLink ) {
        victimSystemLinks.add(victimLink);
    }

    public void addInterferingElement(ActiveInterferer interferer) {
        interferingElements.add( interferer );
    }

    @Override
    public LinkedHashMap<String, List<Double>> getVectorValues() {
        return new LinkedHashMap<>(vectorValues);
    }
}
