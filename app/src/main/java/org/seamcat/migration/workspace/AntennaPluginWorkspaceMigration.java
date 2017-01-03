package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class AntennaPluginWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {
        updateVersion(document);

        JXPathContext context = JXPathContext.newContext(document);
        List antennas = context.selectNodes("//antenna");

        for (Object o : antennas) {
            Element element = (Element) o;
            migrateAntenna(document, element);
        }
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
        return new FormatVersion(24);
    }


    public static void migrateAntenna(Document doc, Element node) {
        Element newElement = doc.createElement("pluginConfiguration");
        newElement.setAttribute("name", node.getAttribute("reference"));
        NodeList children = node.getChildNodes();
        Element desc = (Element) children.item(0);
        newElement.setAttribute("description", desc.getTextContent());
        newElement.setAttribute("peakGain", node.getAttribute("peak-gain"));
        newElement.setAttribute("location", "BUILT-IN");
        newElement.setAttribute("type", "AntennaGain");
        boolean useHor = Boolean.valueOf(node.getAttribute("use-horizontal-pattern"));
        boolean useVer = Boolean.valueOf(node.getAttribute("use-vertical-pattern"));
        boolean useSph = Boolean.valueOf(node.getAttribute("use-spherical-pattern"));
        if (!useHor && !useVer && !useSph) {
            newElement.setAttribute("classname", "org.seamcat.model.antenna.PeakGainAntenna");
        } else if (useSph ) {
            newElement.setAttribute("classname", "org.seamcat.model.antenna.SphericalAntenna");
            newElement.appendChild( moveFunction((Element) children.item(3), doc.createElement("spherical"), false));
        } else {
            newElement.setAttribute("classname", "org.seamcat.model.antenna.HorizontalVerticalAntenna");
            newElement.appendChild(moveFunction((Element) children.item(1), doc.createElement("horizontal"), useHor));
            newElement.appendChild(moveFunction((Element) children.item(2), doc.createElement("vertical"), useVer));
        }

        Element gain = doc.createElement("antennaGain");
        gain.appendChild( newElement);
        Node parent = node.getParentNode();

        String name = parent.getNodeName();
        if ( name.equals("Omni-Antenna")) {
            Element CDMA = (Element) parent.getParentNode().getParentNode();
            Node baseStation = parent.getParentNode();
            baseStation.removeChild( parent );
            if ( CDMA.getAttribute("cellType").equals("1")) {
                // only here keep it
                baseStation.appendChild( gain );
            }
        } else if ( name.equals("Tri-Sector-Antenna")) {
            Element CDMA = (Element) parent.getParentNode().getParentNode();
            Node baseStation = parent.getParentNode();
            baseStation.removeChild( parent );
            if ( !CDMA.getAttribute("cellType").equals("1")) {
                // only here keep it
                baseStation.appendChild( gain );
            }

        } else {
            parent.removeChild( node );
            parent.appendChild( gain );
        }
    }

    private static Element moveFunction(Element oldFunction, Element newFunction, boolean setEnabled) {
        Element function = (Element) oldFunction.getFirstChild().getFirstChild();
        newFunction.appendChild(function);
        if ( setEnabled ) {
            newFunction.setAttribute("enabled", "true");
        }

        return newFunction;
    }
}
