package org.seamcat.marshalling;

import org.apache.log4j.Logger;
import org.seamcat.marshalling.types.CompositeMarshaller;
import org.seamcat.marshalling.types.TypeMarshaller;
import org.seamcat.migration.workspace.WorkspaceFormatVersionConstants;
import org.seamcat.model.*;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.plugin.eventprocessing.CustomUI;
import org.seamcat.model.plugin.eventprocessing.CustomUITab;
import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.systems.generic.SystemModelGeneric;

import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.Configuration;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.PluginLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.seamcat.util.XmlEventFactory.endElement;
import static org.seamcat.util.XmlEventFactory.startElement;

public class WorkspaceMarshaller {

    private static final Logger LOG = Logger.getLogger(WorkspaceMarshaller.class);

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    public static void saveResultsToXmlStream( Workspace model, XMLEventWriter eventWriter) throws XMLStreamException {
        eventWriter.add(startElement("workspaceResults"));

        eventWriter.add(startElement("iceConfigurations"));
        for (ICEConfiguration ice : model.getIceConfigurations() ) {
            ice.saveToXmlStream(eventWriter);
        }
        eventWriter.add(endElement("iceConfigurations"));

        SimulationResult results = model.getSimulationResults();
        WorkspaceResultMarshaller.saveToXmlStream(eventWriter, results.getSystemPreSimulationResults(), "PreResults", "PreResult");
        WorkspaceResultMarshaller.saveToXmlStream(eventWriter, results.getSeamcatResults(), "SEAMCATResults", "SEAMCATResult");
        WorkspaceResultMarshaller.saveToXmlStream(eventWriter, results.getEventProcessingResults(), "EventProcessingResults", "EventProcessingResult");

        if( model.getScenarioOutlineModel() !=null) {
            ScenarioOutlineModelMarshaller.toXmlStream( model.getScenarioOutlineModel(), eventWriter );
        }
        eventWriter.add(endElement("workspaceResults"));
    }

    public static Element toElement( Workspace model, Document doc) {
        Element element = doc.createElement("Workspace");
        element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        element.setAttribute("seamcat_version", STRINGLIST.getString("APPLICATION_TITLE"));
        element.setAttribute("workspace_format_version", Integer.toString(WorkspaceFormatVersionConstants.CURRENT_VERSION.getNumber()));
        element.setAttribute("workspace_reference", model.getName());
        element.setAttribute("hasBeenCalculated", Boolean.toString(model.isHasBeenCalculated()));
        element.setAttribute("victim", model.getVictimSystemId());

        if ( model.getVictimSystem() instanceof SystemModelGeneric ) {
            Element drss = doc.createElement("dRSS");
            drss.setAttribute("enabled", Boolean.toString(model.isUseUserDefinedDRSS()));
            Element drssDist = DistributionMarshaller.toElement(doc, (AbstractDistribution) model.getUserDefinedDRSS());
            drss.appendChild( drssDist);
            element.appendChild( drss);
        }
        
     

        Set<String> jars = new HashSet<String>();
        Element embeddedJars = doc.createElement("embeddedJars");
        for (EventProcessingConfiguration configuration : model.getEventProcessingList()) {
            resolveExternalDependencies(jars, configuration);
        }

        List<PluginConfiguration> plugins = new ArrayList<PluginConfiguration>();
        for (IdElement<SystemModel> elm : model.getSystemModels()) {
            Class<SystemModel> aClass = Library.getSystemModelClass(elm.getElement());
            collectSystemPlugins(plugins, aClass, elm.getElement());
        }
        for (InterferenceLinkElement elm : model.getInterferenceLinkUIs()) {
            collectSystemPlugins(plugins, InterferenceLinkUI.class, elm.getSettings());
        }
        for (PluginConfiguration plugin : plugins) {
            resolveExternalDependencies(jars, plugin);
        }

        for (EventProcessingConfiguration configuration : model.getEventProcessingList()) {
            for (Map.Entry<PanelDefinition<?>, Object> entry : configuration.getCustomUIState().get().entrySet()) {
                handle(jars, entry.getKey().getModelClass(), entry.getValue());
            }
        }

        for (String jar : jars) {
            addJar(jar, embeddedJars, doc );
            // maybe prune model for unused jars
        }
        if ( embeddedJars.hasChildNodes() ) {
            element.appendChild( embeddedJars );
        }

        Element systems = doc.createElement("systems");
        element.appendChild( systems );
        for (IdElement<SystemModel> system : model.getSystemModels()) {
            Element elm = doc.createElement("system");
            elm.setAttribute("id", system.getId());
            elm.appendChild( CompositeMarshaller.toElementSystem( system.getElement(), doc ) );
            systems.appendChild( elm );
        }
        Element links = doc.createElement("links");
        element.appendChild( links );
        for (InterferenceLinkElement il : model.getInterferenceLinkUIs()) {
            Element link = doc.createElement("link");
            link.setAttribute("id", il.getId());
            link.setAttribute("interferingSystemId", il.getInterferingSystemId());
            link.setAttribute("name", il.getName());
            link.appendChild( CompositeMarshaller.toElement( InterferenceLinkUI.class, il.getSettings(), doc) );
            links.appendChild( link );
        }

        Element frequencies = doc.createElement("frequencies");
        element.appendChild( frequencies );
        appendFrequency(doc, frequencies, model.getVictimFrequency());
        for (Distribution distribution : model.getInterferingLinkFrequency()) {
            appendFrequency(doc, frequencies, distribution);
        }

        /*if (model.isStoreResults()) {
            // output seamcat results
            Element iceConfigurationsElement = doc.createElement("iceConfigurations");
            for (ICEConfiguration ice : model.getIceConfigurations()) {
                iceConfigurationsElement.appendChild(ice.toElement(doc));
            }
            element.appendChild(iceConfigurationsElement);
        }*/
        Element eppElement = doc.createElement("plugin-configurations");
        element.appendChild( eppElement );
        if ( !model.getEventProcessingList().isEmpty() ) {
            for (EventProcessingConfiguration wrapper : model.getEventProcessingList()) {
                Element configuration = LibraryFunctionMarshaller.toElement(wrapper, doc);
                eppElement.appendChild(configuration);

                if ( wrapper.getId() == null ) {
                    continue;
                }

                configuration.setAttribute( "id", wrapper.getId() );
                Element customUIs = doc.createElement("customUIs");
                configuration.appendChild( customUIs );
                for (Map.Entry<PanelDefinition<?>, Object> entry : wrapper.getCustomUIState().get().entrySet()) {
                    Element custom = doc.createElement("customUI");
                    Element customModel = doc.createElement("model");
                    customModel.setAttribute("name", entry.getKey().getName());
                    customModel.setAttribute("class", entry.getKey().getModelClass().getName());
                    TypeMarshaller.toElement((Class<Object>) entry.getKey().getModelClass(), doc, customModel, entry.getValue());
                    custom.appendChild( customModel );
                    customUIs.appendChild( custom );
                }

            }
        }
        Element simulationControl = doc.createElement("simulationControl");
        element.appendChild(simulationControl);
        TypeMarshaller.toElement(SimulationControl.class, doc, simulationControl, model.getSimulationControl());
        return element;
    }

    private static void appendFrequency(Document doc, Element frequencies, Distribution dist) {
        Element frequency = doc.createElement("frequency");
        frequency.appendChild( DistributionMarshaller.toElement(doc, (AbstractDistribution) dist) );
        frequencies.appendChild( frequency );
    }

    private static void collectSystemPlugins(List<PluginConfiguration> plugins, Class<?> clazz, Object model) {
        for (Method method : clazz.getDeclaredMethods()) {
            Object value;
            try {
                value = method.invoke(model);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if ( Configuration.class.isAssignableFrom(method.getReturnType())) {
                plugins.add((PluginConfiguration) value);
            } else {
                if ( method.getAnnotation(UITab.class) != null ) {
                    collectSystemPlugins(plugins, method.getReturnType(), value);
                } else if ( method.getAnnotation(UIPosition.class) != null ) {
                    collectSystemPlugins(plugins, method.getReturnType(), value);
                }
            }
        }
    }

    private static void resolveExternalDependencies( Set<String> external, PluginConfiguration configuration ) {
        PluginLocation location = configuration.getLocation();
        if ( !location.isBuiltIn() ) {
            external.add(location.getJarId());
        }
        handle(external, configuration.getModelClass(), configuration.getModel());
    }

    private static void handle(Set<String> external, Class<?> modelClass, Object model) {
        for (Method method : modelClass.getDeclaredMethods()) {
            Class<?> type = method.getReturnType();
            if ( PropagationModel.class.isAssignableFrom(type)  ||
                    AntennaGain.class.isAssignableFrom(type)) {
                try {
                    PluginConfiguration inner = (PluginConfiguration) method.invoke(model);
                    resolveExternalDependencies( external, inner);
                } catch (Exception e) {
                    // error getting result value
                }
            }
        }
    }

    private static void addJar( String jarId, Element embeddedJars, Document doc ) {
        JarConfigurationModel jar = PluginJarFiles.getJarConfiguration(jarId);
        if ( jar != null ) {
            embeddedJars.appendChild(LibraryFunctionMarshaller.toElement(jar, doc));
        } else {
            LOG.warn("Jar file not found '"+jarId+"'");
        }
    }

    public static Workspace fromElement( Element element) {
        Workspace model = new Workspace();
        model.setName(element.getAttribute("workspace_reference"));
        NodeList userdrss = element.getElementsByTagName("dRSS");
        if ( userdrss.getLength() > 0 ) {
            Element drss = (Element) userdrss.item(0);
            model.setUseUserDefinedDRSS( Boolean.parseBoolean(drss.getAttribute("enabled")));
            AbstractDistribution drssDist = DistributionMarshaller.fromElement((Element) drss.getFirstChild());
            model.setUserDefinedDRSS( drssDist );
        }

        NodeList embeddedJars = element.getElementsByTagName("embeddedJars");
        if ( embeddedJars.getLength() > 0 ) {
            Element embeddedJarsElm = (Element) embeddedJars.item(0);
            NodeList nodes = embeddedJarsElm.getChildNodes();
            for (int i=0; i<nodes.getLength(); i++) {
                Element jar = (Element) nodes.item(i);
                JarConfigurationModel jarModel = LibraryFunctionMarshaller.fromElement(jar);
                PluginJarFiles.addJarConfiguration( jarModel );
            }
        }

        model.setVictimSystemId( element.getAttribute("victim"));

        NodeList systems = element.getElementsByTagName("system");
        model.setSystemModels(new ArrayList<IdElement<SystemModel>>());
        if ( systems.getLength() > 0 ) {
            for (int i=0; i<systems.getLength(); i++) {
                Element sysElem = (Element) systems.item(i);
                SystemModel sys = CompositeMarshaller.fromElement(SystemModel.class, (Element) sysElem.getFirstChild());
                model.getSystemModels().add(new IdElement<>(sysElem.getAttribute("id"), sys));
            }
        }

        NodeList links = element.getElementsByTagName("link");
        model.setInterferenceLinkUIs(new ArrayList<InterferenceLinkElement>());
        for ( int i=0; i<links.getLength(); i++) {
            Element link = (Element) links.item(i);
            InterferenceLinkUI ui = CompositeMarshaller.fromElement(InterferenceLinkUI.class, (Element) link.getFirstChild());
            model.getInterferenceLinkUIs().add( new InterferenceLinkElement(link.getAttribute("id"), link.getAttribute("interferingSystemId"), link.getAttribute("name"), ui));
        }

        Element frequencies = (Element) element.getElementsByTagName("frequencies").item(0);
        NodeList frequency = frequencies.getElementsByTagName("frequency");
        for ( int i=0; i<frequency.getLength(); i++) {
            Element item = (Element) frequency.item(i);
            AbstractDistribution distribution = DistributionMarshaller.fromElement((Element) item.getFirstChild());
            if ( i == 0 ) {
                model.setVictimFrequency( distribution );
            } else {
                model.getInterferingLinkFrequency().add( distribution );
            }
        }
        model.setHasBeenCalculated(Boolean.valueOf(element.getAttribute("hasBeenCalculated")));

        NodeList plugins = element.getElementsByTagName("plugin-configurations");
        if ( plugins.getLength() > 0 ) {
            Element pluginContainer = (Element) plugins.item(0);
            NodeList pluginList = pluginContainer.getElementsByTagName("pluginConfiguration");

            if ( pluginList.getLength() > 0 ) {
                List<EventProcessingConfiguration> configurations = new ArrayList<EventProcessingConfiguration>();
                for ( int i=0; i<pluginList.getLength(); i++) {
                    Element plugin = (Element) pluginList.item(i);
                    if ( plugin.getParentNode() != pluginContainer ) continue;
                    EventProcessingConfiguration configuration = (EventProcessingConfiguration) LibraryFunctionMarshaller.fromPluginElement(plugin);
                    if ( configuration != null ) {
                        configuration.setId(plugin.getAttribute("id"));
                        configurations.add(configuration);
                        NodeList customModels = plugin.getElementsByTagName("customUIs");
                        if ( customModels.getLength() >0 ) {
                            Element models = (Element) customModels.item(0);
                            NodeList customModel = models.getElementsByTagName("customUI");
                            if ( customModel.getLength() >0 ) {
                                Class<?> clazz = configuration.getPluginClass();
                                Class<? extends CustomUI>[] classes = clazz.getAnnotation(CustomUITab.class).value();
                                final JarConfigurationModel jar = PluginJarFiles.getJarConfiguration(configuration.getLocation().getJarId());
                                for ( int j=0; j<customModel.getLength(); j++) {
                                    Element customUI = (Element) customModel.item(j);

                                    NodeList uiModels = customUI.getElementsByTagName("model");
                                    if ( uiModels.getLength() > 0 ) {
                                        for ( int k=0; k<uiModels.getLength(); k++) {
                                            Element uiModelElm = (Element) uiModels.item(k);
                                            PanelDefinition<?> pc = new PanelDefinition(uiModelElm.getAttribute("name"), jar.loadClass(uiModelElm.getAttribute("class")));
                                            Object uiModel = TypeMarshaller.fromElement(pc.getModelClass(), uiModelElm);

                                            configuration.getCustomUIState().get().put(pc, uiModel);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if ( !configurations.isEmpty() ) {
                    model.setEventProcessingList( configurations );
                }
            }
        }
        model.setSimulationControl(TypeMarshaller.fromElement(SimulationControl.class, (Element) element.getElementsByTagName("simulationControl").item(0)));
        return model;
    }

    private static String getCurrentDate() {
        String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
}
