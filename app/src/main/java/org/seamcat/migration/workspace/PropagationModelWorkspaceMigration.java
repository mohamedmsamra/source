package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class PropagationModelWorkspaceMigration extends AbstractScenarioMigration {

    private static String[] ENV = { "Urban", "Suburban", "Rural", "Dense Urban" };
    private static final String[] DOOR = { "Indoor", "Outdoor" };
    private static final String[] ROOF = { "Above Roof", "Below Roof" };
    private static final String[] SYSTEM = { "Digital (Bw < 1MHz)","Digital (Bw > 1MHz)", "Analogue" };
    private static final String[] SYSTEM_P1546_4 = { "Mobile","Broadcasting Digital", "Broadcasting Analogue" };
    private static final String[] AREA_LOCATION_VARIABILITY = { "500 x 500 m","< 2 km radius", "< 50 km radius" };
    private static final Logger LOG = Logger.getLogger(PropagationModelWorkspaceMigration.class);


    @Override
    void migrateScenarioDocument(Document document) {
        updateVersion(document);

        JXPathContext context = JXPathContext.newContext(document);
        List pms = context.selectNodes("//PropagationModel");

        for (Object o : pms) {
            migratePropagationModel((Element) o, document);
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
        return new FormatVersion(10);
    }


    public static void migratePropagationModel(Element pm, Document document) {
        Element node = (Element) pm.getFirstChild();
        String nodeName = node.getNodeName();

        Element element = document.createElement("plugin-configuration");
        String className = "org.seamcat.model.propagation.HataSE21PropagationModel";
        if ( nodeName.equals("HataSE21Model") || nodeName.equals("HataSE24Model")) {
            Element main = (Element) node.getFirstChild();
            if ( nodeName.equals("HataSE24Model")) {
                className = "org.seamcat.model.propagation.HataSE24PropagationModel";
                main = (Element) main.getFirstChild();
            }
            Element builtIn = (Element) main.getFirstChild().getFirstChild();

            element.setAttribute("param1", builtIn.getAttribute("variationsSelected"));
            element.setAttribute("param2", builtIn.getAttribute("generalEnv"));
            element.setAttribute("param3", builtIn.getAttribute("rxLocalEnv"));
            element.setAttribute("param4", builtIn.getAttribute("txLocalEnv"));
            element.setAttribute("param5", builtIn.getAttribute("propagEnv"));
            element.setAttribute("param6", main.getAttribute("wiLoss"));
            element.setAttribute("param7", main.getAttribute("wiStdDev"));
            element.setAttribute("param8", main.getAttribute("weLoss"));
            element.setAttribute("param9", main.getAttribute("weStdDev"));
            element.setAttribute("param10", main.getAttribute("floorLoss"));
            element.setAttribute("param11", main.getAttribute("b"));
            element.setAttribute("param12", main.getAttribute("roomSize"));
            element.setAttribute("param13", main.getAttribute("floorHeight"));
        } else if ( nodeName.equals("FreeSpaceModel")) {
            className = "org.seamcat.model.propagation.FreeSpacePropagationModel";
            Element builtIn = (Element) node.getFirstChild().getFirstChild();
            element.setAttribute("param1", builtIn.getAttribute("variationsSelected"));
            element.setAttribute("param2", node.getAttribute("rWeStdDev"));
        } else if ( nodeName.equals("P452ver14Model")) {
            className = "org.seamcat.model.propagation.P452ver14PropagationModel";
            element.setAttribute("param1", node.getAttribute("variationsSelected"));
            element.setAttribute("param2", node.getAttribute("diffractionSelected"));
            element.setAttribute("param3", node.getAttribute("troposcatterSelected"));
            element.setAttribute("param4", node.getAttribute("ductingSelected"));
            element.setAttribute("param5", node.getAttribute("waterCtr"));
            element.setAttribute("param6", node.getAttribute("pressure"));
            element.setAttribute("param7", node.getAttribute("refrIndexGradient"));
            element.setAttribute("param8", node.getAttribute("temperature"));
            element.setAttribute("param9", node.getAttribute("latitude"));
            element.setAttribute("param10", node.getAttribute("clutterLossesTransmitter"));
            element.setAttribute("param11", node.getAttribute("clutterlossesReceiver"));
            element.setAttribute("param12", node.getAttribute("antennaGainTransmitter"));
            element.setAttribute("param13", node.getAttribute("antennaGainReceiver"));
            element.setAttribute("param14", node.getAttribute("seaLevelSurfaceRefractivity"));
            // default to this
            //setAt(settings, 14,new Parameter(new UniformDistribution(50,50)));
        } else if ( nodeName.equals("P1546ver3Model")) {
            className = "org.seamcat.model.propagation.P1546ver3PropagationModel";

            NodeList childNodes = node.getChildNodes();
            for ( int i=0; i<childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if ( item.getNodeName().equals("BuiltInModel")) {
                    Element general = (Element) item.getFirstChild();
                    element.setAttribute("param1", general.getAttribute("variationsSelected"));
                    element.setAttribute("param2", general.getAttribute("generalEnv"));
                }
            }
            element.setAttribute("param3", node.getAttribute("systemType"));
            //element.setAttribute("param4", new UniformDistribution(50,50));
            if ( node.hasAttribute("clutterHeight") ) {
                element.setAttribute("param5", node.getAttribute("clutterHeight"));
                element.setAttribute("use_clutterHeight", "true");
            } else {
                element.setAttribute("param5", "0.0");
                element.setAttribute("use_clutterHeight", "false");
            }
            if ( node.hasAttribute("standardDeviation") ) {
                element.setAttribute("param6", node.getAttribute("standardDeviation"));
                element.setAttribute("use_standardDeviation", "true");
            } else {
                element.setAttribute("param6", "0.0");
                element.setAttribute("use_standardDeviation", "false");
            }
            element.setAttribute("param7", node.getAttribute("locationAreaType"));
            element.setAttribute("param8", node.getAttribute("terminalDesignation"));
            element.setAttribute("param9", node.getAttribute("BuildingOfUniformHeightSelected"));
        } else if ( nodeName.equals("R370Model")) {
            className = "org.seamcat.model.propagation.R370PropagationModel";
            Element general = (Element) node.getFirstChild().getNextSibling().getFirstChild();
            element.setAttribute("param1", general.getAttribute("variationsSelected"));
            element.setAttribute("param2", general.getAttribute("generalEnv"));
            element.setAttribute("param3", node.getAttribute("systemType"));
            element.setAttribute("param4", "");

            if ( node.hasAttribute("clutterHeight")) {
                element.setAttribute("use_clutterHeight", "true");
                element.setAttribute("param5", node.getAttribute("clutterHeight"));
            } else {
                element.setAttribute("use_clutterHeight", "false");
                element.setAttribute("param5", "0.0");
            }
        } else if ( nodeName.equals("SDModel")) {
            /*configuration = new PropagationModelConfiguration(new PluginLocation(SDPropagationModel.class.getName()), new SDPropagationModel());
            List<Parameter> settings = configuration.getCurrentSettings();
            setAt(settings, 11, new Parameter(Double.parseDouble(node.getAttribute("waterCtr"))));
            setAt(settings, 12, new Parameter(Double.parseDouble(node.getAttribute("earthSurfaceAdmittance"))));
            setAt(settings, 13, new Parameter(Double.parseDouble(node.getAttribute("refrIndexGradient"))));
            setAt(settings, 14, new Parameter(Double.parseDouble(node.getAttribute("refrLayerProb"))));
            setAt(settings, 15, new Parameter(new UniformDistribution(50,50)));

            Element main = (Element) node.getLastChild();
            setAt(settings, 3, new Parameter(Double.parseDouble(main.getAttribute("wiLoss"))));
            setAt(settings, 4, new Parameter(Double.parseDouble(main.getAttribute("wiStdDev"))));
            setAt(settings, 5, new Parameter(Double.parseDouble(main.getAttribute("weLoss"))));
            setAt(settings, 6, new Parameter(Double.parseDouble(main.getAttribute("weStdDev"))));
            setAt(settings, 7, new Parameter(Double.parseDouble(main.getAttribute("floorLoss"))));
            setAt(settings, 8, new Parameter(Double.parseDouble(main.getAttribute("b"))));
            setAt(settings, 9, new Parameter(Double.parseDouble(main.getAttribute("roomSize"))));
            setAt(settings, 10,new Parameter(Double.parseDouble(main.getAttribute("floorHeight"))));
            Element builtIn = (Element) main.getFirstChild().getFirstChild();

            int rxLocal = Integer.parseInt(builtIn.getAttribute("rxLocalEnv"));
            int txLocal = Integer.parseInt(builtIn.getAttribute("txLocalEnv"));
            setAt(settings, 0, new Parameter(Boolean.parseBoolean(builtIn.getAttribute("variationsSelected"))));
            setAt(settings, 1, new Parameter(DOOR[rxLocal]));
            setAt(settings, 2, new Parameter(DOOR[txLocal])); */
        } else {
            LOG.warn("Obsolete PM found in workspace. Will be replaced by FreespaceModel");
            className = "org.seamcat.model.propagation.FreeSpacePropagationModel";
            element.setAttribute("param1", "true");
            element.setAttribute("param2", "0.0");
        }

        element.setAttribute("classname", className);
        element.setAttribute("location", "BUILT-IN");
        pm.removeChild( node );
        pm.appendChild(element);
    }
}
