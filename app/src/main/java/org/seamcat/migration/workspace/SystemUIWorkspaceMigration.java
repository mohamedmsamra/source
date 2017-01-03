package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemUIWorkspaceMigration extends AbstractScenarioMigration {


    public static Map<Integer, String> modeIntMap = new HashMap<>();
    public static Map<Integer, Integer> oldModeNewMode = new HashMap<>();

    static {
        modeIntMap.put(0, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR");
        modeIntMap.put(1, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR");
        modeIntMap.put(2, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR");
        modeIntMap.put(3, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR");
        modeIntMap.put(4, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT");
        modeIntMap.put(5, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT");
        modeIntMap.put(6, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR");
        modeIntMap.put(7, "VICTIM_DMA_INTERFERER_DMA_COR");
        modeIntMap.put(8, "VICTIM_DMA_INTERFERER_DMA_DYN");
        modeIntMap.put(9, "VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT");
        modeIntMap.put(10, "VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT");
        modeIntMap.put(11, "VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR");
        modeIntMap.put(12, "VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR");
        modeIntMap.put(13, "VICTIM_DMA_INTERFERER_CLASSICAL_NONE");
        modeIntMap.put(14, "VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM");
        modeIntMap.put(15, "VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST");
        modeIntMap.put(16, "VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT");
        modeIntMap.put(17, "VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT");
        modeIntMap.put(18, "VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR");
        modeIntMap.put(19, "VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR");
        modeIntMap.put(20, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT");
        modeIntMap.put(21, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT");
        modeIntMap.put(22, "VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT");
        oldModeNewMode.put(0,0);
        oldModeNewMode.put(1,1);
        oldModeNewMode.put(2,2);
        oldModeNewMode.put(20,3);
        oldModeNewMode.put(21,4);
        oldModeNewMode.put(22,5);
        oldModeNewMode.put(3,6);
        oldModeNewMode.put(4,7);
        oldModeNewMode.put(5,8);
        oldModeNewMode.put(6,9);
        oldModeNewMode.put(7,10);
        oldModeNewMode.put(8,11);
        oldModeNewMode.put(9,12);
        oldModeNewMode.put(10,13);
        oldModeNewMode.put(11,14);
        oldModeNewMode.put(12,15);
        oldModeNewMode.put(13,16);
        oldModeNewMode.put(14,17);
        oldModeNewMode.put(15,18);
        oldModeNewMode.put(16,19);
        oldModeNewMode.put(17,20);
        oldModeNewMode.put(18,21);
        oldModeNewMode.put(19,22);
    }

    @Override
    void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List systems = context.selectNodes("//system");

        for (Object o : systems) {
            Element system = (Element) o;
            Element general = (Element) system.getElementsByTagName("general").item(0);
            Element composite = document.createElement("composite");
            moveChild(system, composite, "general");

            Element description = document.createElement("description");
            description.setAttribute("description", general.getAttribute("description"));
            description.setAttribute("name", general.getAttribute("name"));
            composite.appendChild(description);

            boolean isCDMA = Boolean.parseBoolean(system.getAttribute("isCDMA"));
            if ( isCDMA ) {
                //Element cdmaElement = (Element) system.getElementsByTagName("CdmaSystem").item(0);
                //description.setAttribute("name", cdmaElement.getAttribute("component-reference"));
                handleDMA(system, composite, document);
            } else {
                composite.setAttribute("class", "org.seamcat.model.systems.generic.SystemModelGeneric");

                Node receiverEnvs = fixReceiver(system, composite, document);
                Node transmitterEnvs = fixTransmitter(system, composite, document);
                fixPath(system, composite, document, receiverEnvs, transmitterEnvs);
            }

            while (system.hasChildNodes() ) {
                system.removeChild( system.getFirstChild());
            }
            system.appendChild( composite );
        }

        Element workspace = (Element) context.selectNodes("//Workspace").get(0);

        Element links = document.createElement("links");
        List list = context.selectNodes("//InterferenceLink");
        for (Object o : list) {
            Element il = (Element) o;
            Element link = document.createElement("link");
            link.setAttribute("id", il.getAttribute("interferingSystem"));

            Element composite = document.createElement("composite");
            composite.setAttribute("class", "org.seamcat.model.workspace.InterferenceLinkUI");
            link.appendChild( composite );

            path(composite, (Element) il.getElementsByTagName("InterferenceLinkConfiguration").item(0), document);
            sensingLink( composite, il, document );

            links.appendChild( link );
            workspace.removeChild(il);
        }
        workspace.appendChild( links );

        Element simulationControl = document.createElement("simulationControl");


        Element dataElm = (Element) workspace.getElementsByTagName("EventGenerationData").item(0);
        simulationControl.setAttribute("debugMode", dataElm.getAttribute("debugMode"));
        simulationControl.setAttribute("use_limitTime", dataElm.getAttribute("timeLimited"));
        simulationControl.setAttribute("limitTime", dataElm.getAttribute("expectedDuration"));
        simulationControl.setAttribute("numberOfEvents", dataElm.getAttribute("numberOfEvents"));
        workspace.appendChild( simulationControl );

        updateVersion(document);
    }

    private void handleDMA(Element system, Element composite, Document document) {
        Element cdmaElement = (Element) system.getElementsByTagName("CdmaSystem").item(0);

        int direction = Integer.valueOf(cdmaElement.getAttribute("linkComponentDownlink"));
        String type = cdmaElement.getAttribute("system_type");
        if ( type.equals("cdma")) {
            if ( direction == 0 ) {
                handleCDMAUpLink(system, cdmaElement, composite, document);
            } else {
                handleCDMADownLink(system, cdmaElement, composite, document);
            }
        } else {
            if ( direction == 0 ) {
                handleOFDMAUpLink(system, cdmaElement, composite, document);
            } else {
                handleOFDMADownLink(system, cdmaElement, composite, document);
            }
        }
    }

    private void handleOFDMADownLink(Element system, Element cdmaElement, Element composite, Document document) {
        composite.setAttribute("class", "org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink");

        Element positioning = document.createElement("positioning");
        Element posComposite = composite(document, positioning, "org.seamcat.model.systems.ofdma.OFDMAPositioningTab");
        handlePositioning(document, cdmaElement, posComposite);
        composite.appendChild(positioning);

        //general settings
        Element generalSettings = document.createElement("generalSettings");
        Element genComposite = composite(document, generalSettings, "org.seamcat.model.systems.ofdma.OFDMADownLinkGeneralTab");

        Element cap = document.createElement("ofdmaCapacity");
        cap.setAttribute("usersPerBS", cdmaElement.getAttribute("users_per_cell"));
        genComposite.appendChild( cap );

        Element pl = document.createElement("pathLossCorrelation");
        pl.setAttribute("correlationFactor", cdmaElement.getAttribute("correlation_inter"));
        pl.setAttribute("pathLossVariance", cdmaElement.getAttribute("correlation_variance"));
        pl.setAttribute("usePathLossCorrelation", cdmaElement.getAttribute("use_correlation"));
        genComposite.appendChild( pl );

        Element ulOfdma = (Element) cdmaElement.getElementsByTagName("downlink-ofdma").item(0);
        Element ofdmaDownLink = document.createElement("ofdmaDownLink");
        ofdmaDownLink.setAttribute("bsMaximumTransmitPower", ulOfdma.getAttribute("max_transmit_power"));
        genComposite.appendChild( ofdmaDownLink );

        moveRenameChild(document, cdmaElement, "PropagationModel", genComposite, "propagationModel");

        Element transmitterSettings = document.createElement("transmitterSettings");
        Element oldTrans = (Element) system.getElementsByTagName("transmitter").item(0);
        moveChild(oldTrans, transmitterSettings, "emissionMask");
        moveChild(oldTrans, transmitterSettings, "emissionFloor");
        genComposite.appendChild( transmitterSettings );

        Element receiverSettings = document.createElement("receiverSettings");
        Element oldRec = (Element) system.getElementsByTagName("receiver").item(0);
        moveChild(oldRec, receiverSettings, "blockingMask");
        genComposite.appendChild( receiverSettings );

        genComposite.appendChild( handleDMALocalEnvs(document, oldRec, oldTrans));

        Element generalSettings1 = document.createElement("generalSettings");
        generalSettings1.setAttribute("bandwidth", cdmaElement.getAttribute("systemBandwith"));
        generalSettings1.setAttribute("bandwidthResourceBlock", cdmaElement.getAttribute("resource_block_size"));
        generalSettings1.setAttribute("handoverMargin", cdmaElement.getAttribute("handover"));
        generalSettings1.setAttribute("maxSubcarriersBs", cdmaElement.getAttribute("max_subcarriers"));
        generalSettings1.setAttribute("maxSubcarriersMs", cdmaElement.getAttribute("number_of_subcarriers"));
        generalSettings1.setAttribute("minimumCouplingLoss", cdmaElement.getAttribute("minimum_coupling_loss"));
        generalSettings1.setAttribute("receiverNoiseFigure", cdmaElement.getAttribute("receiverNoiseFigure"));
        generalSettings1.setAttribute("sinrMin", cdmaElement.getAttribute("sinr_minimum"));
        moveRenameChild(document, cdmaElement, "bitrate_mapping", generalSettings1, "bitRateMapping");
        genComposite.appendChild( generalSettings1 );

        composite.appendChild( generalSettings );
    }

    private void handleOFDMAUpLink(Element system, Element cdmaElement, Element composite, Document document) {
        composite.setAttribute("class", "org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink");

        Element positioning = document.createElement("positioning");
        Element posComposite = composite(document, positioning, "org.seamcat.model.systems.ofdma.OFDMAPositioningTab");
        handlePositioning(document, cdmaElement, posComposite);
        composite.appendChild(positioning);


        //general settings
        Element generalSettings = document.createElement("generalSettings");
        Element genComposite = composite(document, generalSettings, "org.seamcat.model.systems.ofdma.OFDMAUpLinkGeneralTab");

        Element cap = document.createElement("ofdmaCapacity");
        cap.setAttribute("usersPerBS", cdmaElement.getAttribute("users_per_cell"));
        genComposite.appendChild( cap );

        Element pl = document.createElement("pathLossCorrelation");
        pl.setAttribute("correlationFactor", cdmaElement.getAttribute("correlation_inter"));
        pl.setAttribute("pathLossVariance", cdmaElement.getAttribute("correlation_variance"));
        pl.setAttribute("usePathLossCorrelation", cdmaElement.getAttribute("use_correlation"));
        genComposite.appendChild( pl );

        Element ulOfdma = (Element) cdmaElement.getElementsByTagName("uplink-ofdma").item(0);
        Element ofdmaUpLink = document.createElement("ofdmaUpLink");
        ofdmaUpLink.setAttribute("getBalancingFactor", ulOfdma.getAttribute("balancing_factor") );
        ofdmaUpLink.setAttribute("getMaximumAllowedDisconnectAttempts", ulOfdma.getAttribute("max_disconnect_attempts") );
        ofdmaUpLink.setAttribute("getMaximumAllowedTransmitPowerOfMS", ulOfdma.getAttribute("max_transmit_power") );
        ofdmaUpLink.setAttribute("getMinimumTransmitPowerOfMS", ulOfdma.getAttribute("min_transmit_power") );
        ofdmaUpLink.setAttribute("getPowerScalingThreshold", ulOfdma.getAttribute("powerControlThreshold") );
        genComposite.appendChild( ofdmaUpLink );

        moveRenameChild(document, cdmaElement, "PropagationModel", genComposite, "propagationModel");

        Element transmitterSettings = document.createElement("transmitterSettings");
        Element oldTrans = (Element) system.getElementsByTagName("transmitter").item(0);
        moveChild(oldTrans, transmitterSettings, "emissionMask");
        moveChild(oldTrans, transmitterSettings, "emissionFloor");
        genComposite.appendChild( transmitterSettings );

        Element receiverSettings = document.createElement("receiverSettings");
        Element oldRec = (Element) system.getElementsByTagName("receiver").item(0);
        moveChild(oldRec, receiverSettings, "blockingMask");
        genComposite.appendChild( receiverSettings );

        genComposite.appendChild( handleDMALocalEnvs(document, oldRec, oldTrans));

        Element generalSettings1 = document.createElement("generalSettings");
        generalSettings1.setAttribute("bandwidth", cdmaElement.getAttribute("systemBandwith"));
        generalSettings1.setAttribute("bandwidthResourceBlock", cdmaElement.getAttribute("resource_block_size"));
        generalSettings1.setAttribute("handoverMargin", cdmaElement.getAttribute("handover"));
        generalSettings1.setAttribute("maxSubcarriersBs", cdmaElement.getAttribute("max_subcarriers"));
        generalSettings1.setAttribute("maxSubcarriersMs", cdmaElement.getAttribute("number_of_subcarriers"));
        generalSettings1.setAttribute("minimumCouplingLoss", cdmaElement.getAttribute("minimum_coupling_loss"));
        generalSettings1.setAttribute("receiverNoiseFigure", cdmaElement.getAttribute("receiverNoiseFigure"));
        generalSettings1.setAttribute("sinrMin", cdmaElement.getAttribute("sinr_minimum"));
        moveRenameChild(document, cdmaElement, "bitrate_mapping", generalSettings1, "bitRateMapping");
        genComposite.appendChild( generalSettings1 );

        composite.appendChild( generalSettings );
    }

    private static Element handleDMALocalEnvs(Document document, Element oldRec, Element oldTrans) {
        Element localEnvironments = document.createElement("localEnvironments");
        localEnvironments.appendChild( handleLocalEnv(oldRec, document) );
        localEnvironments.appendChild( handleLocalEnv(oldTrans, document));
        return localEnvironments;
    }

    private Element composite(Document document, Element parent, String className) {
        Element composite = document.createElement("composite");
        composite.setAttribute("class", className);
        parent.appendChild( composite );
        return composite;
    }


    private void handlePositioning(Document document, Element cdmaElement, Element posComposite) {
        Element baseSt = (Element) cdmaElement.getElementsByTagName("BaseStation").item(0);
        Element baseStation = document.createElement("baseStation");
        moveRenameChild(document, baseSt, "antenna-height", baseStation, "antennaHeight");
        moveRenameChild(document, baseSt, "antenna-tilt", baseStation, "antennaTilt");
        moveChild(baseSt, baseStation, "antennaGain");
        posComposite.appendChild( baseStation );

        Element mob = (Element) cdmaElement.getElementsByTagName("MobileStation").item(0);
        Element mobile = document.createElement("mobile");
        moveRenameChild(document, mob, "antenna-height", mobile, "antennaHeight");
        moveRenameChild(document, mob, "AntennaGain", mobile, "antennaGain");
        moveRenameChild(document, mob, "UserMobility", mobile, "mobility");
        posComposite.appendChild( mobile );

        Element position = document.createElement("position");
        position.setAttribute("cellRadius", cdmaElement.getAttribute("cell_radius"));
        position.setAttribute("generateWrapAround", cdmaElement.getAttribute("use_wrap_around"));
        Boolean edge = Boolean.valueOf(cdmaElement.getAttribute("simulate_network_edge"));
        if ( !edge ) {
            position.setAttribute("layout", "0");
        } else {
            Boolean left = Boolean.valueOf(cdmaElement.getAttribute("simulate_left_network_edge"));
            position.setAttribute("layout", left ? "1" : "2");
        }
        position.setAttribute("measureFromEntireCluster", cdmaElement.getAttribute("interference_from_cluster"));
        position.setAttribute("referenceCellId", cdmaElement.getAttribute("reference_cell_id"));
        position.setAttribute("referenceSector", cdmaElement.getAttribute("referenceSector"));
        String sectorSetup = cdmaElement.getAttribute("sectorSetup");
        if ( sectorSetup.equals("SingleSector") ) {
            position.setAttribute("sectorType", "0");
        } else if ( sectorSetup.equals("TriSector3GPP")) {
            position.setAttribute("sectorType", "1");
        } else {
            position.setAttribute("sectorType", "2");
        }

        position.setAttribute("tiers", cdmaElement.getAttribute("number_of_tiers"));
        posComposite.appendChild( position );
    }

    private void handleCDMADownLink(Element system, Element cdmaElement, Element composite, Document document) {
        composite.setAttribute("class", "org.seamcat.model.systems.cdma.SystemModelCDMADownLink");

        Element positioning = document.createElement("positioning");
        Element posComposite = composite(document, positioning, "org.seamcat.model.systems.cdma.CDMAPositioningTab");

        handlePositioning(document, cdmaElement, posComposite);
        composite.appendChild( positioning );

        //general settings
        Element generalSettings = document.createElement("generalSettings");
        Element genComposite = composite(document, generalSettings, "org.seamcat.model.systems.cdma.CDMADownLinkGeneralTab");

        Element bs = (Element) cdmaElement.getElementsByTagName("BaseStation").item(0);
        Element cdmaDownLink = document.createElement("cdmaDownLink");
        cdmaDownLink.setAttribute("maxBroadcastPower", bs.getAttribute("maxPower"));
        cdmaDownLink.setAttribute("maxTrafficChannelFraction", bs.getAttribute("maxPowerFraction"));
        cdmaDownLink.setAttribute("overheadChannelFraction", bs.getAttribute("overheadFraction"));
        cdmaDownLink.setAttribute("pilotChannelFraction", bs.getAttribute("pilotFraction"));
        cdmaDownLink.setAttribute("successThreshold", cdmaElement.getAttribute("successThreshold"));
        genComposite.appendChild( cdmaDownLink);

        Element transmitterSettings = document.createElement("transmitterSettings");
        Element oldTrans = (Element) system.getElementsByTagName("transmitter").item(0);
        moveChild(oldTrans, transmitterSettings, "emissionMask");
        moveChild(oldTrans, transmitterSettings, "emissionFloor");
        genComposite.appendChild( transmitterSettings );

        Element receiverSettings = document.createElement("receiverSettings");
        Element oldRec = (Element) system.getElementsByTagName("receiver").item(0);
        moveChild(oldRec, receiverSettings, "blockingMask");
        genComposite.appendChild( receiverSettings );

        Element cdmaCapacity = document.createElement("cdmaCapacity");
        cdmaCapacity.setAttribute("deltaUsersPerCell", cdmaElement.getAttribute("delta_users_per_cell"));
        cdmaCapacity.setAttribute("initUsersPerCell", cdmaElement.getAttribute("users_per_cell"));
        cdmaCapacity.setAttribute("numberOfTrials", cdmaElement.getAttribute("number_of_trials"));
        cdmaCapacity.setAttribute("simulateNonInterferedCapacity", cdmaElement.getAttribute("simulate_capacity"));
        cdmaCapacity.setAttribute("toleranceOfInitialOutage", cdmaElement.getAttribute("tolerance-initial-outage"));
        genComposite.appendChild( cdmaCapacity );

        Element generalSettings1 = document.createElement("generalSettings");
        generalSettings1.setAttribute("bandwidth", cdmaElement.getAttribute("systemBandwith"));
        generalSettings1.setAttribute("callDropThreshold", cdmaElement.getAttribute("callDropThreshold"));
        generalSettings1.setAttribute("handoverMargin", cdmaElement.getAttribute("handover"));
        generalSettings1.setAttribute("minimumCouplingLoss", cdmaElement.getAttribute("minimum_coupling_loss"));
        generalSettings1.setAttribute("receiverNoiseFigure", cdmaElement.getAttribute("receiverNoiseFigure"));
        generalSettings1.setAttribute("voiceBitRate", cdmaElement.getAttribute("voiceBitrate"));
        generalSettings1.setAttribute("voiceActivityFactor", cdmaElement.getAttribute("voice_activity_factor"));
        moveChild(cdmaElement, generalSettings1, "CDMA-Link-level-data");
        genComposite.appendChild( generalSettings1 );

        genComposite.appendChild( handleDMALocalEnvs(document, oldRec, oldTrans));

        moveRenameChild(document, cdmaElement, "PropagationModel", genComposite, "propagationModel");
        composite.appendChild( generalSettings );
    }

    private void handleCDMAUpLink(Element system, Element cdmaElement, Element composite, Document document) {
        composite.setAttribute("class", "org.seamcat.model.systems.cdma.SystemModelCDMAUpLink");

        Element positioning = document.createElement("positioning");
        Element posComposite = composite(document, positioning, "org.seamcat.model.systems.cdma.CDMAPositioningTab");

        handlePositioning(document, cdmaElement, posComposite);
        composite.appendChild( positioning );

        //general settings
        Element generalSettings = document.createElement("generalSettings");
        Element genComposite = composite(document, generalSettings, "org.seamcat.model.systems.cdma.CDMAUpLinkGeneralTab");

        Element mob = (Element) cdmaElement.getElementsByTagName("MobileStation").item(0);
        Element cdmaUplink = document.createElement("cdmaUplink");
        cdmaUplink.setAttribute("msMaximumTransmitPower", mob.getAttribute("maxPower"));
        cdmaUplink.setAttribute("msPowerControlRange", mob.getAttribute("powerRange"));
        cdmaUplink.setAttribute("pcConvergencePrecision", cdmaElement.getAttribute("powerControlThreshold"));
        if ( cdmaElement.hasAttribute("target_cell_noise")) {
            cdmaUplink.setAttribute("targetCellNoiseRise", cdmaElement.getAttribute("target_cell_noise"));
        } else {
            cdmaUplink.setAttribute("targetCellNoiseRise", "0.1");
        }
        cdmaUplink.setAttribute("targetNetworkNoiseRise", cdmaElement.getAttribute("target_noise"));
        cdmaUplink.setAttribute("use_targetCellNoiseRise", cdmaElement.getAttribute("cell-noise-rise-selection"));
        genComposite.appendChild( cdmaUplink);

        Element transmitterSettings = document.createElement("transmitterSettings");
        Element oldTrans = (Element) system.getElementsByTagName("transmitter").item(0);
        moveChild(oldTrans, transmitterSettings, "emissionMask");
        moveChild(oldTrans, transmitterSettings, "emissionFloor");
        genComposite.appendChild( transmitterSettings );

        Element receiverSettings = document.createElement("receiverSettings");
        Element oldRec = (Element) system.getElementsByTagName("receiver").item(0);
        moveChild(oldRec, receiverSettings, "blockingMask");
        genComposite.appendChild( receiverSettings );

        Element cdmaCapacity = document.createElement("cdmaCapacity");
        cdmaCapacity.setAttribute("deltaUsersPerCell", cdmaElement.getAttribute("delta_users_per_cell"));
        cdmaCapacity.setAttribute("initUsersPerCell", cdmaElement.getAttribute("users_per_cell"));
        cdmaCapacity.setAttribute("numberOfTrials", cdmaElement.getAttribute("number_of_trials"));
        cdmaCapacity.setAttribute("simulateNonInterferedCapacity", cdmaElement.getAttribute("simulate_capacity"));
        cdmaCapacity.setAttribute("targetNoiseRisePrecision", cdmaElement.getAttribute("target-noise-rise-precision"));
        genComposite.appendChild( cdmaCapacity );

        Element generalSettings1 = document.createElement("generalSettings");
        generalSettings1.setAttribute("bandwidth", cdmaElement.getAttribute("systemBandwith"));
        generalSettings1.setAttribute("callDropThreshold", cdmaElement.getAttribute("callDropThreshold"));
        generalSettings1.setAttribute("handoverMargin", cdmaElement.getAttribute("handover"));
        generalSettings1.setAttribute("minimumCouplingLoss", cdmaElement.getAttribute("minimum_coupling_loss"));
        generalSettings1.setAttribute("receiverNoiseFigure", cdmaElement.getAttribute("receiverNoiseFigure"));
        generalSettings1.setAttribute("voiceBitRate", cdmaElement.getAttribute("voiceBitrate"));
        generalSettings1.setAttribute("voiceActivityFactor", cdmaElement.getAttribute("voice_activity_factor"));
        moveChild(cdmaElement, generalSettings1, "CDMA-Link-level-data");
        genComposite.appendChild( generalSettings1 );

        genComposite.appendChild( handleDMALocalEnvs(document, oldRec, oldTrans));

        moveRenameChild(document, cdmaElement, "PropagationModel", genComposite, "propagationModel");
        composite.appendChild( generalSettings );
    }

    private void path(Element parent, Element il, Document document) {
        Element path = document.createElement("path");
        parent.appendChild( path );
        Element composite = document.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.workspace.InterferenceLinkPathUI");
        path.appendChild( composite );

        moveRenameChild(document, il, "pathlossCorrelation", composite, "pathLossCorrelation");
        moveRenameChild(document, il, "PropagationModel", composite, "propagationModel");

        Element relativePosition = (Element) il.getElementsByTagName("relativePosition").item(0);

        Element relativeLocation = document.createElement("relativeLocation");
        moveRename(relativePosition, "colocated", relativeLocation, "isCoLocated");
        moveRename(relativePosition, "colocation_delta_x", relativeLocation, "coLocationX");
        moveRename(relativePosition, "colocation_delta_y", relativeLocation, "coLocationY");
        Integer correlationMode = Integer.valueOf(relativePosition.getAttribute("correlationMode"));
        //relativeLocation.setAttribute("mode", modeIntMap.get(correlationMode));
        relativeLocation.setAttribute("mode", Integer.toString(oldModeNewMode.get(correlationMode)));
        moveAtt(relativePosition, relativeLocation, "minimumCouplingLoss");
        moveAtt(relativePosition, relativeLocation, "numberOfActiveTransmitters");
        moveAtt(relativePosition, relativeLocation, "protectionDistance");
        moveAtt(relativePosition, relativeLocation, "simulationRadius");
        moveRename(relativePosition, "wrCenterOfItDistribution", relativeLocation, "setILRatTheCenter");

        Element relLoc = (Element) relativePosition.getElementsByTagName("relativeLocation").item(0);
        relativeLocation.setAttribute("deltaX", relLoc.getAttribute("deltaX"));
        relativeLocation.setAttribute("deltaY", relLoc.getAttribute("deltaY"));
        moveChild(relLoc, relativeLocation, "pathAzimuth");
        moveChild(relLoc, relativeLocation, "pathDistanceFactor");
        composite.appendChild( relativeLocation );

        Node density = il.getElementsByTagName("interferersDensity").item(0);
        document.renameNode( density, null, "density");
        il.removeChild( density );
        composite.appendChild(density);
    }

    private void moveRename(Element from, String fromName, Element to, String name) {
        String attribute = from.getAttribute(fromName);
        to.setAttribute(name, attribute);
        from.removeAttribute(fromName);
    }

    private void sensingLink(Element parent, Element il, Document document) {
        Element sensingLink = document.createElement("sensingLink");
        parent.appendChild( sensingLink );
        Element composite = document.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.workspace.SensingLinkUI");
        sensingLink.appendChild( composite );

        Element characteris = (Element) il.getElementsByTagName("SensingLink").item(0);
        Node propagationModel = characteris.getElementsByTagName("PropagationModel").item(0);
        document.renameNode(propagationModel, null, "propagationModel");
        characteris.removeChild(propagationModel);

        composite.appendChild( propagationModel );

        document.renameNode(characteris, null, "sensingCharacteristics");
        il.removeChild(characteris);
        composite.appendChild( characteris );

    }

    private void fixPath(Element system, Element owner, Document document, Node receiverEnvs, Node transmitterEnvs) {
        Element path = document.createElement("path");
        Element composite = document.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.systems.generic.TransmitterReceiverPathModel");

        Element pathElm = (Element) system.getElementsByTagName("TransmitterToReceiverPath").item(0);
        Node propagationModel = pathElm.getElementsByTagName("PropagationModel").item(0);
        document.renameNode( propagationModel, null, "propagationModel");
        Node coverageRadius = pathElm.getElementsByTagName("CoverageRadius").item(0);
        document.renameNode( coverageRadius, null, "coverageRadius");

        composite.appendChild(coverageRadius);
        Element envs = document.createElement("localEnvironments");
        envs.appendChild(receiverEnvs);
        envs.appendChild(transmitterEnvs);
        composite.appendChild( envs );
        composite.appendChild(propagationModel);

        Element relativeLocation = document.createElement("relativeLocation");
        moveAtt(pathElm, relativeLocation, "deltaX");
        moveAtt(pathElm, relativeLocation, "deltaY");
        moveAtt(pathElm, relativeLocation, "useCorrelatedDistance");
        moveChild(pathElm, relativeLocation, "pathAzimuth");
        moveChild(pathElm, relativeLocation, "pathDistanceFactor");
        composite.appendChild( relativeLocation );
        path.appendChild( composite );

        system.removeChild( pathElm );
        owner.appendChild(path);
    }

    private static Node fixReceiver( Element system, Element owner, Document document) {
        Element receiver = (Element) system.getElementsByTagName("receiver").item(0);
        Element target = document.createElement("receiver");
        Element composite = document.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.systems.generic.ReceiverModel");
        target.appendChild( composite );
        owner.appendChild( target );

        Element transceiver = (Element) receiver.getElementsByTagName("transceiver").item(0);
        receiver.removeChild( transceiver );
        composite.appendChild( transceiver );
        document.renameNode(transceiver, null, "antennaPointing");


        Element receiverDesc = document.createElement("description");
        receiverDesc.setAttribute("name", transceiver.getAttribute("reference"));
        composite.appendChild( receiverDesc );

        Node antennaGain = transceiver.getElementsByTagName("antennaGain").item(0);
        transceiver.removeChild( antennaGain );
        composite.appendChild( antennaGain );

        Element interferenceCriteria = document.createElement("interferenceCriteria");
        moveAtt(receiver, interferenceCriteria, "extended_protection_ratio");
        moveAtt(receiver, interferenceCriteria, "interference_to_noise_ratio");
        moveAtt(receiver, interferenceCriteria, "noise_augmentation");
        moveAtt(receiver, interferenceCriteria, "protection_ratio");
        composite.appendChild( interferenceCriteria );

        Element receptionCharacteristics = document.createElement("receptionCharacteristics");
        moveAtt(receiver, receptionCharacteristics, "blockingAttenuationMode");
        moveAtt(receiver, receptionCharacteristics, "receivePower");
        moveAtt(receiver, receptionCharacteristics, "reception_bandwith");
        moveAtt(receiver, receptionCharacteristics, "sensitivity");
        moveAtt(receiver, receptionCharacteristics, "use_receivePower");
        moveAtt(receiver, receptionCharacteristics, "use_receiver_overloading");

        moveChild(receiver, receptionCharacteristics, "noiseFloor");
        moveChild(receiver, receptionCharacteristics, "blockingMask");
        Element intermod = (Element) moveChild(receiver, receptionCharacteristics, "intermodulation_rejection");
        if ( !intermod.hasAttribute("enabled") ) {
            intermod.setAttribute("enabled", "false");
        }
        moveChild(receiver, receptionCharacteristics, "overloading_mask");
        moveChild(receiver, receptionCharacteristics, "receiver_filter");
        composite.appendChild( receptionCharacteristics );

        system.removeChild( receiver );

        return handleLocalEnv(receiver, document);
    }

    private static Node handleLocalEnv(Element container, Document document) {
        Node localEnvironments = container.getElementsByTagName("LocalEnvironments").item(0);
        if ( localEnvironments == null ) {
            return defaultLocalEnv(document);
        } else {
            container.removeChild( localEnvironments );
            return localEnvironments;
        }
    }

    private static Element defaultLocalEnv(Document document) {
        Element localEnvironments = document.createElement("LocalEnvironments");
        Element localEnvironment = document.createElement("LocalEnvironment");
        localEnvironment.setAttribute("environment", "Outdoor");
        localEnvironment.setAttribute("prop", "1.0");
        localEnvironment.setAttribute("stdDev", "5.0");
        localEnvironment.setAttribute("wallLoss", "10.0");
        localEnvironments.appendChild( localEnvironment );
        return localEnvironments;
    }


    private static Node fixTransmitter( Element system, Element owner, Document document) {
        Element transmitter = (Element) system.getElementsByTagName("transmitter").item(0);
        Element composite = document.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.systems.generic.TransmitterModel");
        Element target = document.createElement("transmitter");
        owner.appendChild( target );
        target.appendChild( composite );

        Element transceiver = (Element) transmitter.getElementsByTagName("transceiver").item(0);
        transmitter.removeChild( transceiver );
        composite.appendChild( transceiver );
        document.renameNode(transceiver, null, "antennaPointing");


        Element receiverDesc = document.createElement("description");
        receiverDesc.setAttribute("name", transceiver.getAttribute("reference"));
        composite.appendChild( receiverDesc );

        Node antennaGain = transceiver.getElementsByTagName("antennaGain").item(0);
        transceiver.removeChild( antennaGain );
        composite.appendChild( antennaGain );

        Element emissionCharacteristics = document.createElement("emissionCharacteristics");
        moveAtt(transmitter, emissionCharacteristics, "cognitiveRadio");
        moveAtt(transmitter, emissionCharacteristics, "dynamicRange");
        moveAtt(transmitter, emissionCharacteristics, "minThreshold");
        moveAtt(transmitter, emissionCharacteristics, "powerControl");
        moveAtt(transmitter, emissionCharacteristics, "stepSize");
        moveChild(transmitter, emissionCharacteristics, "power");
        moveChild(transmitter, emissionCharacteristics, "emissionMask");
        moveChild(transmitter, emissionCharacteristics, "emissionFloor");
        composite.appendChild( emissionCharacteristics );

        system.removeChild( transmitter );

        return handleLocalEnv(transmitter, document);
    }


    private static void moveAtt(Element from, Element to, String name) {
        String attribute = from.getAttribute(name);
        to.setAttribute(name, attribute);
        from.removeAttribute(name);
    }

    private static void moveRenameChild(Document document, Element from, String fromName, Node to, String name) {
        Node node = from.getElementsByTagName(fromName).item(0);
        document.renameNode(node, null, name);
        from.removeChild( node );
        to.appendChild( node );
    }

    private static Node moveChild(Element from, Node to, String child) {
        Node item = from.getElementsByTagName(child).item(0);
        from.removeChild( item );
        to.appendChild(item);
        return item;
    }

	@Override
	void migrateResultsDocument(Document document) {
		// nothing to do here
	}

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(38);
   }


}
