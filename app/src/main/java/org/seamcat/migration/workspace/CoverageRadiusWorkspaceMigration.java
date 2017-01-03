package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class CoverageRadiusWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {
        updateVersion(document);

        JXPathContext context = JXPathContext.newContext(document);

        // migrate TransmitterToReceiverPath
        List trp = context.selectNodes("//TransmitterToReceiverPath");
        for (Object o : trp) {
            migrateCoverageRadius((Element) o, document);
        }
        // clean up Transmitter
        List txs = context.selectNodes("//transmitter");
        for (Object tx : txs) {
            cleanupTransmitter((Element)tx);
        }
    }

    private void cleanupTransmitter(Element tx) {
        tx.removeAttribute("number_of_channels");
        tx.removeAttribute("number_of_users_per_channel");
    }

    @Override
    void migrateResultsDocument(Document document) {
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(11);
    }


    private void migrateCoverageRadius(Element cr, Document document) {
        Element parent = (Element) cr.getParentNode();
        double availability = attDouble(cr, "availability");
        double coverageRadius = attDouble(cr, "coverageRadius");
        int coverageRadiusCalculatinMode = attInt(cr, "coverageRadiusCalculatinMode");
        double density = attDouble(cr, "density");
        double fadingStdDev = attDouble(cr, "fadingStdDev");
        int frequencyCluster = attInt(cr, "frequencyCluster");
        double maximumDistance = attDouble(cr, "maximumDistance");
        double minimumDistance = attDouble(cr, "minimumDistance");
        int numberOfChannels = attInt(cr, "numberOfChannels");
        int numberOfUsersPerChannel = attInt(cr, "numberOfUsersPerChannel");
        double referenceReceiverAntennaHeight = attDouble(cr, "referenceReceiverAntennaHeight");
        double referenceTransmitterAntennaHeight = attDouble(cr, "referenceTransmitterAntennaHeight");
        double referenceTransmitterFrequency = attDouble(cr, "referenceTransmitterFrequency");
        double referenceTransmitterPower = attDouble(cr, "referenceTransmitterPower");

        Element pluginConfiguration = document.createElement("plugin-configuration");
        if ( parent.getNodeName().equals("systemLink")) {
            // coverage radius child element must be added
            if ( coverageRadiusCalculatinMode == 1 ) {
                // noise limited
                pluginConfiguration.setAttribute("classname", "org.seamcat.simulation.coverageradius.NoiseLimitedCoverageRadius");
                pluginConfiguration.setAttribute("param1", Double.toString(referenceReceiverAntennaHeight));
                pluginConfiguration.setAttribute("param2", Double.toString(referenceTransmitterAntennaHeight));
                pluginConfiguration.setAttribute("param3", Double.toString(referenceTransmitterFrequency));
                pluginConfiguration.setAttribute("param4", Double.toString(referenceTransmitterPower));
                pluginConfiguration.setAttribute("param5", Double.toString(minimumDistance));
                pluginConfiguration.setAttribute("param6", Double.toString(maximumDistance));
                pluginConfiguration.setAttribute("param7", Double.toString(availability));
                pluginConfiguration.setAttribute("param8", Double.toString(fadingStdDev));
            } else if ( coverageRadiusCalculatinMode == 2 ) {
                pluginConfiguration.setAttribute("classname", "org.seamcat.simulation.coverageradius.TrafficLimitedNetworkCoverageRadius");
                pluginConfiguration.setAttribute("param1", Double.toString(density));
                pluginConfiguration.setAttribute("param2", Integer.toString(numberOfChannels));
                pluginConfiguration.setAttribute("param3", Integer.toString(numberOfUsersPerChannel));
                pluginConfiguration.setAttribute("param4", Integer.toString(frequencyCluster));
            } else {
                // user defined
                pluginConfiguration.setAttribute("classname", "org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius");
                pluginConfiguration.setAttribute("param1", Double.toString(coverageRadius));
            }
            Element radius = document.createElement("CoverageRadius");
            radius.appendChild( pluginConfiguration );
            cr.appendChild( radius );
        }
    }

    private double attDouble( Element cr, String attribute ) {
        String value = cr.getAttribute(attribute);
        cr.removeAttribute(attribute);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e ) {
            return 0.0;
        }
    }

    private int attInt( Element cr, String attribute ) {
        String value = cr.getAttribute(attribute);
        cr.removeAttribute(attribute);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e ) {
            return 0;
        }
    }

}
