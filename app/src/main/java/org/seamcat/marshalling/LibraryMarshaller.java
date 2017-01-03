package org.seamcat.marshalling;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.cdma.xml.CDMAXMLUtils;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.marshalling.types.CompositeMarshaller;
import org.seamcat.migration.settings.SettingsFormatVersionConstants;
import org.seamcat.model.Library;
import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.plugin.PluginConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryMarshaller {

    private static final Logger LOG = Logger.getLogger(LibraryMarshaller.class);

    public void exportLibrary(List<LibraryItem> selected, File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            List<SystemModel> systems = new ArrayList<SystemModel>();
            List<ReceiverModel> receivers = new ArrayList<ReceiverModel>();
            List<T_ReceiverModel> t_receivers = new ArrayList<T_ReceiverModel>();
            List<TransmitterModel> transmitters = new ArrayList<TransmitterModel>();
            List<CDMALinkLevelData> llds = new ArrayList<CDMALinkLevelData>();
            List<EmissionMaskImpl> sems = new ArrayList<EmissionMaskImpl>();
            List<BlockingMaskImpl> rbms = new ArrayList<BlockingMaskImpl>();
            List<PluginConfiguration> plugins = new ArrayList<PluginConfiguration>();
            List<JarConfigurationModel> jars = new ArrayList<JarConfigurationModel>();

            for (LibraryItem item : selected) {
                if ( item instanceof SystemModel ) systems.add((SystemModel) item);
                else if ( item instanceof ReceiverModel ) receivers.add((ReceiverModel) item);
                else if ( item instanceof T_ReceiverModel ) t_receivers.add((T_ReceiverModel) item);
                else if ( item instanceof TransmitterModel ) transmitters.add((TransmitterModel) item);
                else if ( item instanceof CDMALinkLevelData ) llds.add((CDMALinkLevelData) item);
                else if ( item instanceof EmissionMaskImpl) sems.add((EmissionMaskImpl) item);
                else if ( item instanceof BlockingMaskImpl) rbms.add((BlockingMaskImpl) item);
                else if ( item instanceof PluginConfiguration ) {
                    PluginConfiguration configuration = ((PluginConfiguration) item);
                    if ( !configuration.isBuiltIn() ) {
                        JarConfigurationModel jar = PluginJarFiles.getJarConfiguration(configuration.getLocation().getJarId());
                        if ( !jars.contains( jar )) {
                            jars.add( jar );
                        }
                    }
                    plugins.add(configuration);
                }
                else if ( item instanceof JarConfigurationModel ) {
                    jars.add((JarConfigurationModel) item);
                }
            }

            Element seamcat = doc.createElement("seamcat");
            seamcat.setAttribute("settings_format_version", Integer.toString(SettingsFormatVersionConstants.CURRENT_VERSION.getNumber()));
            doc.appendChild(seamcat).appendChild(toElement(systems, receivers, t_receivers,transmitters,
                    llds, sems, rbms, plugins, jars, doc));

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new FileOutputStream(file));

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();

            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Element toElement(Library model, Document doc) {
        return toElement(model.getSystems(), model.getReceivers(),model.getTReceivers(),model.getTransmitters(),model.getCDMALinkLevelData(),
                model.getSpectrumEmissionMasks(), model.getReceiverBlockingMasks(), model.getPluginConfigurations(), model.getInstalledJars(), doc);
    }

    private Element toElement(List<SystemModel> systems, List<ReceiverModel> receivers,List<T_ReceiverModel> t_receivers ,List<TransmitterModel> transmitters,
                              List<CDMALinkLevelData> llds, List<EmissionMaskImpl> sems,
                              List<BlockingMaskImpl> rmbs, List<PluginConfiguration> plugins, List<JarConfigurationModel> jars, Document doc) {
        Element library = doc.createElement("library");
        library.appendChild( appendGroup("systems", systems, doc));
        library.appendChild( appendGroup( "receivers", receivers, doc));
        library.appendChild( appendGroup( "receivers", t_receivers, doc));
        library.appendChild( appendGroup("transmitters", transmitters, doc));
        library.appendChild(CDMAXMLUtils.createCDMALibraryElement(doc, llds));
        library.appendChild( appendGroup("spectrum-emission-masks", sems, doc));
        library.appendChild( appendGroup("receiver-blocking-masks", rmbs, doc));
        library.appendChild( appendGroup("pluginConfigurations", plugins, doc));
        library.appendChild( appendGroup("installed-jars", jars, doc));
        return library;
    }
    


    

    public Library fromElement(final Element element) {
        final List<SystemModel> systems = new ArrayList<SystemModel>();
        final List<ReceiverModel> receivers = new ArrayList<ReceiverModel>();
        final List<T_ReceiverModel> t_receivers = new ArrayList<T_ReceiverModel>();
        final List<TransmitterModel> transmitters = new ArrayList<TransmitterModel>();
        final List<EmissionMaskImpl> spectrumEmissionMasks = new ArrayList<EmissionMaskImpl>();
        final List<BlockingMaskImpl> receiverBlockingMasks = new ArrayList<BlockingMaskImpl>();
        List<CDMALinkLevelData> cdmalinklevel= new ArrayList<CDMALinkLevelData>();
        final List<PluginConfiguration> configurations = new ArrayList<PluginConfiguration>();
        final Set<String> installedJars = new HashSet<String>();

        processElements(element, "systems", "composite", new Process() {
            @Override
            public void process(Element element) {
                systems.add(CompositeMarshaller.fromElement(SystemModel.class, element));
            }
        });

        processElements(element, "installed-jars", "jar", new Process() {
            @Override
            public void process(Element element) {
                JarConfigurationModel model = LibraryFunctionMarshaller.fromElement(element);
                PluginJarFiles.addJarConfiguration( model );
                installedJars.add( model.getHash() );
            }
        });
        processElements(element, "receivers", "composite", new Process() {
            public void process(Element element) {
                receivers.add( CompositeMarshaller.fromElement(ReceiverModel.class, element));
            }
        });
        
        processElements(element, "t_receivers", "composite", new Process() {
            public void process(Element element) {
                t_receivers.add( CompositeMarshaller.fromElement(T_ReceiverModel.class, element));
            }
        });
        processElements(element, "transmitters", "composite", new Process() {
            public void process(Element element) {
                transmitters.add( CompositeMarshaller.fromElement(TransmitterModel.class, element));
            }
        });
        processElements(element, "spectrum-emission-masks", "spectrum-emission-mask", new Process() {
            public void process(Element element ) {
                spectrumEmissionMasks.add( LibraryFunctionMarshaller.semFromElement(element) );
            }
        });
        processElements(element, "receiver-blocking-masks", "blockingMask", new Process() {
            public void process(Element element) {
                receiverBlockingMasks.add( LibraryFunctionMarshaller.rbmFromElement(element));
            }
        });
        processElements(element, "pluginConfigurations", "pluginConfiguration", new Process() {
            public void process(Element element) {
                try {
                    PluginConfiguration configuration = LibraryFunctionMarshaller.fromPluginElement(element);
                    if ( configuration != null ) {
                        configurations.add(configuration);
                    }
                } catch (RuntimeException e ) {
                    // report this to the UI
                    LOG.error("Could not read plugin configuration", e);
                } catch (AbstractMethodError error ) {
                    LOG.error("Could not instantiate plugin");
                }
            }
        });
        // CDMA
        List<CDMALinkLevelData> llds = CDMAXMLUtils.getDataFromLibrary(element);
        if (llds.size() > 0) {
            cdmalinklevel.addAll(llds);
        }

        Log.debug("Initializing library containers");
        return new Library(installedJars, receivers, t_receivers,transmitters, cdmalinklevel, spectrumEmissionMasks,
                receiverBlockingMasks, systems, configurations);
    }

    private void processElements( Element element, String groupName, String itemName, Process process ) {
        LOG.debug("Reading "+groupName);
        try {
            NodeList top = element.getElementsByTagName( groupName );
            NodeList nl = top.getLength() == 0 ? element.getElementsByTagName( itemName ) : ((Element)top.item(0)).getElementsByTagName(itemName );
            if (nl.getLength() != 0) {
                for ( int i=0;i<nl.getLength(); i++ ) {
                    if ( nl.item(i).getParentNode() == top.item(0)) {
                        process.process((Element) nl.item(i));
                    }
                }
            }
        } catch (Exception e ) {
            LOG.error("Error reading " + groupName + " from library", e);
        }
    }

    interface Process {
        void process(Element element);
    }

    private <T> Element toElement( T t, Document doc ) {
        if ( t instanceof SystemModel ) {
            return CompositeMarshaller.toElementSystem(t, doc);
        } else if ( t instanceof TransmitterModel) {
            return CompositeMarshaller.toElement(TransmitterModel.class, t, doc);
        } else if ( t instanceof EmissionMaskImpl ) {
            return LibraryFunctionMarshaller.toElement((EmissionMaskImpl) t, doc);
        } else if ( t instanceof BlockingMaskImpl ) {
            return LibraryFunctionMarshaller.toElement((BlockingMaskImpl) t, doc);
        } else if ( t instanceof PluginConfiguration) {
            return LibraryFunctionMarshaller.toElement((PluginConfiguration) t, doc);
        } else if ( t instanceof JarConfigurationModel) {
            return LibraryFunctionMarshaller.toElement((JarConfigurationModel) t, doc);
        } else if ( t instanceof ReceiverModel ) {
            return CompositeMarshaller.toElement(ReceiverModel.class, t, doc);
        } else if ( t instanceof T_ReceiverModel ) {
            return CompositeMarshaller.toElement(T_ReceiverModel.class, t, doc);
        }
        throw new RuntimeException();
    }

    private <T> Element appendGroup( String groupName, List<T> list, Document doc ) {
        Element antennasElement = doc.createElement(groupName);
        for (T t : list ) {
            antennasElement.appendChild(toElement(t, doc) );
        }
        return antennasElement;
    }

    public synchronized Library importLibrary( File importFile )
            throws ParserConfigurationException, SAXException, IOException {

        if (importFile == null) {
            throw new NullPointerException();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(importFile);

        Element root = doc.getDocumentElement();
        return fromElement((Element) root.getElementsByTagName("library").item(0));
    }
}
