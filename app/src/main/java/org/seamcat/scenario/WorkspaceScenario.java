package org.seamcat.scenario;

import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.LinkCalculator;
import org.seamcat.model.*;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIToModelConverter;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import java.util.*;

/**
 * This copies the workspace settings to a simulation scenario (i.e. immutable setting
 * and some other modifications)
 *
 */
public class WorkspaceScenario<T extends RadioSystem> implements Scenario {

    private int numberOfEvents;
    private List<InterferenceLink> interferenceLinks;
    private List<EventProcessing> eventProcessingList;
    private double victimCoverageRadius;
    private Map<InterferenceLink, Double> coverageRadiusMap;
    private Map<InterferenceLink, Integer> originalPositionMap;
    private Map<RadioSystem, PreSimulationResultsImpl> preSimulationResultsMap;
    private Map<String, SystemSimulationModel> systems;

    public SystemSimulationModel getSystem( String id) {
        return systems.get(id);
    }

    public WorkspaceScenario( Workspace workspace ) {
       // this.numberOfEvents = workspace.getSimulationControl().numberOfEvents();
    	this.numberOfEvents = 50;

        eventProcessingList = new ArrayList<>();
        eventProcessingList.addAll(workspace.getEventProcessingList());
        originalPositionMap = new LinkedHashMap<>();
        preSimulationResultsMap = new LinkedHashMap<>();

        systems = new HashMap<>();
        for (IdElement<SystemModel> element : workspace.getSystemModels()) {
            systems.put( element.getId(), UIToModelConverter.convert(element.getElement()));
        }

        //edit
        SystemSimulationModel victimSystemLink = systems.get( workspace.getVictimSystemId());
        // edit
        List<InterferenceLink> links = workspace.getInterferenceLinks();
       
        links.clear();
        for (InterferenceLinkElement element : workspace.getInterferenceLinkUIs()) {
            links.add( UIToModelConverter.convert( victimSystemLink,
                    systems.get( element.getInterferingSystemId()), element.getSettings()));
        }
        
        // setup Co-Location
        List<InterferenceLinkElement> interferenceLinkUIs = workspace.getInterferenceLinkUIs();
        for (int i = 0; i < interferenceLinkUIs.size(); i++) {
            InterferenceLinkElement element = interferenceLinkUIs.get(i);
            if ( element.getSettings().path().relativeLocation().isCoLocated() ) {
                // find Co Locate System
            	//co-located with Victim System transmitter////////////////////////////
                String y = element.getSettings().path().relativeLocation().coLocatedWith();
                int index = getIndexOfLink(y, interferenceLinkUIs);
                InterferenceLink link = links.get(i);
                link.setCoLocation( links.get(index) );
            }
        }

        RadioSystem victim;
        PreSimulationResultsImpl victimPreSimResults = new PreSimulationResultsImpl();
        // in the Simulation Result Groups, see if it already exists
        if ( victimSystemLink.isDMASystem() ) {
            AbstractDmaSystem<?> dmaSystem = victimSystemLink.getDMASystem();
            dmaSystem.setResults(victimPreSimResults);
            CellularSystemImpl sys = (CellularSystemImpl) victimSystemLink.getSystem();
            sys.setFrequency( workspace.getVictimFrequency() );
            victim = sys;
        } else {
            GenericSystemImpl sys = (GenericSystemImpl) victimSystemLink.getSystem();
            sys.setFrequency( workspace.getVictimFrequency() );
            victim = sys;
        }
        if ( victim instanceof GenericSystem) {
            GenericSystem gv = (GenericSystem) victim;
            victimCoverageRadius = gv.getLink().getCoverageRadius().evaluate(gv);
        } else {
            victimCoverageRadius = 0;
        }

        coverageRadiusMap = new HashMap<InterferenceLink, Double>();

        preSimulationResultsMap.put( victim, victimPreSimResults );
        List<InterferenceLink> normal = new ArrayList<>();
        List<InterferenceLink> coLocated = new ArrayList<>();
        int index = 0, offset = 0;
        List<InterferenceLink> interferenceLinks1 = workspace.getInterferenceLinks();
        
        ////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////
        // allocate each interference link on the map depending on if it is colocated or normal!
        for (int i = 0; i < interferenceLinks1.size(); i++) {
            InterferenceLink link = interferenceLinks1.get(i);
            originalPositionMap.put(link, index + offset);
            //colocated system
            if (link.getInterferingLinkRelativePosition().useCoLocatedWith()) {
                offset += link.getInterferingLinkRelativePosition().getCoLocatedWith().getInterferingLinkRelativePosition().getNumberOfActiveTransmitters() - 1;
            } else {
                offset += link.getInterferingLinkRelativePosition().getNumberOfActiveTransmitters() - 1;
            }
            
            link.setVictimSystem(victim);
            PreSimulationResultsImpl iPreSimResults = new PreSimulationResultsImpl();
            if (link.getInterferingLink().isDMASystem()) {
                AbstractDmaSystem<?> dmaSystem = link.getDMASystem();
                dmaSystem.setResults(iPreSimResults);
                CellularSystemImpl sys = (CellularSystemImpl) link.getInterferingLink().getSystem();
                sys.setFrequency( workspace.getInterferingLinkFrequency().get(i) );
                link.setInterferingSystem( sys );
            } else {
            	//generic system configurations
                GenericSystemImpl sys = (GenericSystemImpl) link.getInterferingLink().getSystem();
                sys.setFrequency(workspace.getInterferingLinkFrequency().get(i));
                link.setInterferingSystem( sys );
            }
            
            //////////////////////////////// till here /////////////////////////////////////////////
            preSimulationResultsMap.put(link.getInterferingSystem(), iPreSimResults);
            org.seamcat.model.types.Transmitter it = link.getInterferingSystem().getTransmitter();
            iPreSimResults.setNormalizedEmissionsMask(it.getEmissionsMask().normalize());
            if (it.isUsingEmissionsFloor()) {
                iPreSimResults.setNormalizedEmissionsFloor(it.getEmissionsFloor().normalize());
            }

            if ( it instanceof GenericTransmitter && ((GenericTransmitter) it).isInterfererCognitiveRadio()) {
                iPreSimResults.setNormalizedEIRPInBlockMask( ((GenericTransmitter) it).getSensingLink().getEIRPInBlockMask().normalize() );
            }

            InterferingLinkRelativePosition.CorrelationMode mode = link.getInterferingLinkRelativePosition().getCorrelationMode();
            if ((mode == InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR) || (mode == InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT)
                    || (mode == InterferingLinkRelativePosition.CorrelationMode.VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM)) {
                double rRsimu = LinkCalculator.itSimulationRadius(link.getInterferingLinkRelativePosition().getNumberOfActiveTransmitters(),
                        ((GenericSystem)link.getInterferingSystem()).getInterfererDensity(), link.getInterferingLinkRelativePosition().getProtectionDistance().trial());
                link.setCalculatedSimulationRadius(rRsimu);
            }

            coverageRadiusMap.put(link, 0.0);
            if ( link.getInterferingSystem() instanceof GenericSystem) {
                GenericSystem sys = (GenericSystem) link.getInterferingSystem();
                if ( !sys.getLink().getRelativeLocation().useCorrelatedDistance()) {
                    coverageRadiusMap.put(link, sys.getLink().getCoverageRadius().evaluate(sys));
                }
            }

            if (link.getInterferingLinkRelativePosition().useCoLocatedWith()) coLocated.add(link);
            else normal.add(link);

            index++;
        }
        
        
        normal.addAll( coLocated );
        interferenceLinks = Collections.unmodifiableList(normal);

        List<InterferenceLink> interferenceLinks = workspace.getInterferenceLinks();
        for (InterferenceLink link : interferenceLinks) {
            if ( link.getInterferingLinkRelativePosition().useCoLocatedWith() ) {
                link.setCalculatedSimulationRadius(link.getInterferingLinkRelativePosition().getCoLocatedWith().getCalculatedSimulationRadius());
            }
        }

        // Normalize  victim transmitter unwanted emission
        EmissionMask wtUnwantedEmission = victim.getTransmitter().getEmissionsMask();
        PreSimulationResultsImpl results = preSimulationResultsMap.get(victim);
        results.setNormalizedEmissionsMask(wtUnwantedEmission.normalize());
    }

    public void setPreSimulationResults( Workspace workspace ) {
        if ( workspace.isHasBeenCalculated() ) {
            List<SimulationResultGroup> list = workspace.getSimulationResults().getSystemPreSimulationResults();
            if ( !list.isEmpty() ) {
                Set<RadioSystem> uniqueSystems = new HashSet<>();
                preSimulationResultsMap.get( getVictimSystem() ).setPreSimulationResults( list.get(0).getResultTypes() );
                uniqueSystems.add( getVictimSystem() );
                int count = 1;
                for (InterferenceLink link : interferenceLinks) {
                    RadioSystem system = link.getInterferingSystem();
                    if ( uniqueSystems.contains(system)) continue;
                    preSimulationResultsMap.get( system ).setPreSimulationResults( list.get(count).getResultTypes() );
                }
            }
        }
    }

    private int getIndexOfLink( String linkId, List<InterferenceLinkElement> links ) {
        for (int i = 0; i < links.size(); i++) {
            InterferenceLinkElement link = links.get(i);
            if (link.getId().equals(linkId)) {
                return i;
            }
        }

        throw new RuntimeException("Unable to locate link with id: " + linkId );
    }


    public Map<InterferenceLink, Integer> getOriginalPositionMap(){
        return originalPositionMap;
    }

    @Override
    public int numberOfEvents() {
        return numberOfEvents;
    }

    @Override
    public List<InterferenceLink> getInterferenceLinks() {
        return interferenceLinks;
    }

    @Override
    public T getVictimSystem() {
        return (T) interferenceLinks.get(0).getVictimSystem();
    }

    @Override
    public List<EventProcessing> getEventProcessingList() {
        return eventProcessingList;
    }

    public double getVictimCoverageRadius() {
        return victimCoverageRadius;
    }

    public double getCoverageRadius(org.seamcat.model.types.InterferenceLink link) {
        return coverageRadiusMap.get(link);
    }

    @Override
    public PreSimulationResultsImpl getPreSimulationResults(RadioSystem system) {
        return preSimulationResultsMap.get(system);
    }

    public Map<RadioSystem, PreSimulationResultsImpl> getPreSimulationResultsMap() {
        return preSimulationResultsMap;
    }
}
