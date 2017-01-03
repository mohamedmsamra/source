package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.List;

public class PluginWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {
        updateVersion(document);

        JXPathContext context = JXPathContext.newContext(document);
        List plugins = context.selectNodes("//plugin-configuration");

        for (Object o : plugins) {
            Element element = (Element) o;
            migratePlugins(document, element);
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
        return new FormatVersion(23);
    }


    public static void migratePlugins(Document doc, Element node) {
        Element newElement = doc.createElement("pluginConfiguration");
        List<String> envs = new ArrayList<String>();
        envs.add("Urban");
        envs.add("Suburban");
        envs.add("Rural");

        List<String> prop = new ArrayList<String>();
        prop.add("Above Roof");
        prop.add("Below Roof");

        if ( node.getAttribute("classname").equals("org.seamcat.model.propagation.HataSE21PropagationModel") ) {
            move(newElement, node, "variation", "generalEnvironment", "propagationEnvironment",
                    "wallLossInIn", "wallLossStdDev", "adjacentFloorLoss", "empiricalParameters",
                    "sizeOfRoom", "floorHeight");
            if ( envs.contains(newElement.getAttribute("generalEnvironment"))) {
                // change to number
                String ge = newElement.getAttribute("generalEnvironment");
                newElement.setAttribute("generalEnvironment", Integer.toString(envs.indexOf(ge)));
            }
            if ( prop.contains(newElement.getAttribute("propagationEnvironment"))) {
                String pe = newElement.getAttribute("propagationEnvironment");
                newElement.setAttribute("propagationEnvironment", Integer.toString(prop.indexOf(pe)));
            }
        } else if ( node.getAttribute("classname").equals("org.seamcat.model.propagation.HataSE24PropagationModel") ) {
            move(newElement, node, "variation","generalEnvironment","propagationEnvironment",
                    "wallLossInIn","wallLossStdDev", "adjacentFloorLoss", "empiricalParameters",
                    "sizeOfRoom", "floorHeight");
            if ( envs.contains(newElement.getAttribute("generalEnvironment"))) {
                // change to number
                String ge = newElement.getAttribute("generalEnvironment");
                newElement.setAttribute("generalEnvironment", Integer.toString(envs.indexOf(ge)));
            }
            if ( prop.contains(newElement.getAttribute("propagationEnvironment"))) {
                String pe = newElement.getAttribute("propagationEnvironment");
                newElement.setAttribute("propagationEnvironment", Integer.toString(prop.indexOf(pe)));
            }
        } else if ( node.getAttribute("classname").equals("org.seamcat.model.propagation.FreeSpacePropagationModel") ) {
            move(newElement, node, "variation", "stdDev");
        } else if (node.getAttribute("classname").equals("org.seamcat.model.propagation.P452ver14PropagationModel") ) {
            move(newElement, node, "variation", "diffraction", "troposphericScatter",
                    "layerReflection", "waterConcentration", "surfacePressure",
                    "refractionIndex", "surfaceTemperature", "latitude",
                    "clutterLossTx", "clutterLossRx","antennaGainTx", "antennaGainRx",
                    "seaLevelSurfaceRefractivity");
        } else if (node.getAttribute("classname").equals("org.seamcat.model.propagation.P1546ver3PropagationModel")) {
            move(newElement, node, "variation", "generalEnvironment", "system", "timePercentage", "localClutter",
                    "stdDev", "area", "terminalDesignations", "uniformBuildingHeight");

        } else if (node.getAttribute("classname").equals("org.seamcat.model.propagation.R370PropagationModel")) {
            move(newElement, node, "variation", "generalEnvironment", "system", "timePercentage", "clutterHeight");
        } else if ( node.getAttribute("classname").equals("org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius")) {
            newElement.setAttribute("coverageRadius", removeAttr(node, "param1"));
        } else if ( node.getAttribute("classname").equals("org.seamcat.simulation.coverageradius.TrafficLimitedNetworkCoverageRadius")) {
            move(newElement, node, "density", "numberOfChannels", "numberOfUsers", "frequencyCluster");
        } else if (node.getAttribute("classname").equals("org.seamcat.simulation.coverageradius.NoiseLimitedCoverageRadius")) {
            move(newElement, node, "rxAntennaHeight", "txAntennaHeight", "frequency", "txPower", "minDistance",
                    "maxDistance", "availability", "fadingStdDev");
        }

        NamedNodeMap attrs = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr2 = (Attr) doc.importNode(attrs.item(i), true);
            newElement.getAttributes().setNamedItem(attr2);
        }
        while (node.hasChildNodes()) {
            newElement.appendChild(node.getFirstChild());
        }
        node.getParentNode().replaceChild(newElement, node);
    }

    private static void move(Element newElement, Element old, String... names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            newElement.setAttribute(name, removeAttr(old, "param"+(i+1)));
        }
    }

    private static String removeAttr( Element node, String att ) {
        String attribute = node.getAttribute(att);
        node.removeAttribute(att);
        return attribute;
    }

}
