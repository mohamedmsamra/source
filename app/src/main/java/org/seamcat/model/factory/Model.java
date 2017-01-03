package org.seamcat.model.factory;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.seamcat.Seamcat;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.commands.LibraryUpdatedEvent;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.loadsave.WorkspaceLoader;
import org.seamcat.loadsave.WorkspaceSaver;
import org.seamcat.marshalling.LibraryMarshaller;
import org.seamcat.migration.settings.SettingsFormatVersionConstants;
import org.seamcat.migration.settings.SettingsMigrator;
import org.seamcat.migration.workspace.WorkspaceMigrator;
import org.seamcat.model.*;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.distributions.DistributionFactoryImpl;
import org.seamcat.model.engines.SimulationPool;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.generic.ReceptionCharacteristics;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;

import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;

import org.seamcat.model.types.*;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.plugin.*;
import org.seamcat.presentation.systems.InterferenceLinksPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

import static java.io.File.separator;
import static org.seamcat.model.factory.Factory.*;

public final class Model {

    private static final Logger LOG = Logger.getLogger(Model.class);
    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private static final SimulationPool simulationPool = new SimulationPool();

    private static boolean SHOW_WELCOME = true;
    private static DocumentBuilderFactory SEAMCAT_DOCUMENT_BUILDER_FACTORY;

    private static final String DEFAULT_SETTINGS_FILE_NAME     = "/default-library.sli";
    private static final String SETTINGS_FILE_NAME             = "settings.xml";
    private static final String PREHISTORIC_SETTINGS_FILE_NAME = "seamcat.xml";

    private static final char[] ILLEGAL_FILENAME_CHARS = new char[] { '\\', '/', ':', '*', '<', '>', '|' };
    public static File seamcatHome;
    public static File seamcatTemp;
    private static final Model MODEL = new Model();
    private static boolean initialized = false;
    public static Preferences preferences;

    private final Vector<String> logPatterns = new Vector<String>();
    private final PatternLayout filePattern = new PatternLayout();

    private FileAppender fileAppender = new FileAppender();
    private Library library;

    public static SimulationPool getSimulationPool() {
        return simulationPool;
    }

    public static String getSeamcatHomeBaseDir() {
        return preferences.get( Seamcat.SEAMCAT_HOME, System.getProperty("user.home") + separator + "seamcat-app");
    }

    public static String getSeamcatHomeDir() {
        return getSeamcatHomeBaseDir() + separator + STRINGLIST.getString("APPLICATION_TITLE");
    }

    public static void setSeamcatBaseDir( String home ) {
        preferences.put( Seamcat.SEAMCAT_HOME, home );
    }

    public static String getSeamcatTempDir() {
        return seamcatTemp.getAbsolutePath();
    }

    private Model() {
        preferences = Preferences.userNodeForPackage(Seamcat.class);
        String scdir = getSeamcatHomeDir();
        seamcatHome = new File(scdir);
        createDir(seamcatHome);
        seamcatTemp = new File(scdir + separator + "temp");
        createDir(seamcatTemp);
        createDir(new File(scdir + separator + "reports"));

        ensureFile(seamcatHome.getAbsolutePath() + separator + DEFAULT_SETTINGS_FILE_NAME, DEFAULT_SETTINGS_FILE_NAME );

        SEAMCAT_DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        SEAMCAT_DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
        SEAMCAT_DOCUMENT_BUILDER_FACTORY.setIgnoringElementContentWhitespace(true);
        SEAMCAT_DOCUMENT_BUILDER_FACTORY.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");

        LOG.debug("Constructing new Model instance");
        Factory.initialize(
                new DistributionFactoryImpl(),
                new PropagationModelFactoryImpl(),
                new BuildersImpl(),
                new AntennaGainFactoryImpl(),
                new FunctionFactoryImpl(),
                new DataExporterImpl());
    }

    private static void createDir(File dir) {
        if ( !dir.exists()) {
            if ( !dir.mkdirs()) {
                LOG.error("Could not create dir: "+ dir.getAbsolutePath() );
            }
        }
    }

    public static DocumentBuilderFactory getSeamcatDocumentBuilderFactory() {
        return SEAMCAT_DOCUMENT_BUILDER_FACTORY;
    }

    public static File getReportDirectory() {
        return new File( seamcatHome.getAbsoluteFile() + separator + "reports" + separator);
    }

    private static File getDefaultLibraryFile() {
        return new File(seamcatHome.getAbsolutePath(), DEFAULT_SETTINGS_FILE_NAME);
    }

    private static File getSettingsFile() {
        return new File(seamcatHome.getAbsolutePath(), SETTINGS_FILE_NAME);
    }

    public static File getPrehistoricSettingsFile() {
        return new File(seamcatHome.getAbsolutePath(), PREHISTORIC_SETTINGS_FILE_NAME);
    }

    private void init() {
        File settingsFile = getSettingsFile();
        new SettingsMigrator().migrateAndShuffleSettingsFiles(settingsFile, getPrehistoricSettingsFile());

        try {
            DocumentBuilder db = SEAMCAT_DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            db.setErrorHandler(new XmlValidationHandler(true));
            if (settingsFile.exists()) {
                LOG.debug("Reading settings.xml");
                Document doc = db.parse(settingsFile);
                Element seamcat = doc.getDocumentElement();
                Element lib = (Element) seamcat.getElementsByTagName("library").item(0);
                LibraryMarshaller libraryMarshaller = new LibraryMarshaller();
                library = libraryMarshaller.fromElement(lib);

                try {
                    LOG.debug("Reading log patterns");
                    Element logPat = (Element) seamcat.getElementsByTagName("log-patterns").item(0);
                    NodeList pats = logPat.getElementsByTagName("pattern");
                    for (int i = 0, stop = pats.getLength(); i < stop; i++) {
                        String pattern = ((Element) pats.item(i)).getAttribute("value");
                        logPatterns.add(pattern);
                        if (((Element) pats.item(i)).getAttribute("selected").equals(Boolean.toString(true))) {
                            filePattern.setConversionPattern(pattern);
                        }
                    }
                } catch (Exception ex) {
                    LOG.error("Error reading model", ex);
                }
            } else {
                library = getDefaultLibrary();
                restoreDefaultLogPatterns();
            }
        } catch (Exception e) {
            library = new Library();
            restoreDefaultLogPatterns();
        }
        if (logPatterns.size() == 0 || (filePattern == null)) {
            restoreDefaultLogPatterns();
        }
        fileAppender.setAppend(true);
        fileAppender.setLayout(getLogFilePattern());
        fileAppender.setFile(seamcatHome.getAbsolutePath() + separator + "seamcat.log");
        fileAppender.activateOptions();

        LOG.debug("Model initiated successfully");
    }

    public static Library getDefaultLibrary() {
        try {
            File defaultLibrary = getDefaultLibraryFile();

            new SettingsMigrator().migrateAndShuffleSettingsFiles(defaultLibrary, getPrehistoricSettingsFile());

            DocumentBuilder db = SEAMCAT_DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            db.setErrorHandler(new XmlValidationHandler(true));
            Document doc = db.parse(defaultLibrary);
            Element seamcat = doc.getDocumentElement();
            Element lib = (Element) seamcat.getElementsByTagName("library").item(0);
            LibraryMarshaller libraryMarshaller = new LibraryMarshaller();
            return libraryMarshaller.fromElement(lib);
        } catch (Exception e ){
            LOG.error( "Error loading default library", e);
            return new Library();
        }
    }


    public void addPattern(String pattern) {
        filePattern.setConversionPattern(pattern);
        if (!logPatterns.contains(pattern)) {
            logPatterns.add(pattern);
        }
        persist();
    }

    private static void ensureFile(String url, String origUrl) {
        try {
            File file = new File(url);

            InputStream ins = new BufferedInputStream(Model.class.getResourceAsStream(origUrl));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[4096];
            int readLength;
            while ((readLength = ins.read(buffer)) > -1) {
                out.write(buffer, 0, readLength);
            }
            ins.close();
            out.close();
        } catch (IOException ex) {
            LOG.warn("Error copying file: " + origUrl, ex);
        }
    }

    public void restoreDefaultLogPatterns() {
        logPatterns.clear();
        logPatterns.add("(%F:%L[%M]) - %m%n");
        logPatterns.add("%-5p [%d]: %m%n");
        logPatterns.add("%-5p: %m%n");
        logPatterns.add("[%t]: %m%n");
        logPatterns.add("%m%n");
        logPatterns.add("%d{dd MMM yyyy HH:mm:ss,SSS} %5p [%t] (%F:%L[%M]) - %m%n");

        filePattern.setConversionPattern(logPatterns.get(0));
    }

    public static Model getInstance() {
    	
    	
    	if (!initialized) {
            initialized = true;
            MODEL.init();
            MODEL.getLibrary().ensureConsistentLibrary();
        }
        return MODEL;
        
     
    }

    public final Library getLibrary() {
        if ( library == null ) {
            getInstance();
        }
        return library;
    }

    public Element toElement(Document doc) {
        Element seamcat = doc.createElement("seamcat");
        seamcat.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        seamcat.setAttribute("seamcat_version", STRINGLIST.getString("APPLICATION_TITLE"));
        seamcat.setAttribute("settings_format_version", Integer.toString(SettingsFormatVersionConstants.CURRENT_VERSION.getNumber()));

        seamcat.appendChild(new LibraryMarshaller().toElement(getLibrary(), doc));

        Element logPat = doc.createElement("log-patterns");
        for (String logPattern : logPatterns) {
            Element pat = doc.createElement("pattern");
            pat.setAttribute("value", logPattern);
            pat.setAttribute("selected", Boolean.toString(filePattern.getConversionPattern().equals(logPattern)));
            logPat.appendChild(pat);
        }
        seamcat.appendChild(logPat);
        return seamcat;
    }

    public Appender getLogFileAppender() {
        return fileAppender;
    }

    public PatternLayout getLogFilePattern() {
        return filePattern;
    }

    public final synchronized void persist() {
        try {
            DocumentBuilder db = SEAMCAT_DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document doc = db.newDocument();

            doc.appendChild(toElement(doc));

            File file = seamcatHome;

            if (!file.exists()) {
                file.mkdirs();
            }

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new FileOutputStream(getSettingsFile()));

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            EventBusFactory.getEventBus().publish( new LibraryUpdatedEvent() );
        } catch (Exception e) {
            LOG.error( "Error persisting model", e);
        }
    }

    public Vector<String> getLogPatterns() {
        return logPatterns;
    }

    public static boolean checkFilename(String filename) {
        for (int x = 0, filechars = filename.length(); x < filechars; x++) {
            char filechar = filename.charAt(x);
            for (char ILLEGAL_FILENAME_CHAR : ILLEGAL_FILENAME_CHARS) {
                if (filechar == ILLEGAL_FILENAME_CHAR) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String getWorkspacePath() {
        return seamcatHome.getAbsolutePath() + separator + "workspaces" + separator;
    }

    public static SystemModelGeneric defaultGeneric() {
        SystemModelGeneric system = ProxyHelper.newComposite(SystemModelGeneric.class, "Generic System");
        SystemModelGeneric systemProto = prototype(SystemModelGeneric.class, system);
        when(systemProto.receiver()).thenReturn(ProxyHelper.newComposite(ReceiverModel.class, "DEFAULT_RX"));
        when(systemProto.transmitter()).thenReturn(ProxyHelper.newComposite(TransmitterModel.class, "DEFAULT_TX"));
        return build(systemProto);
    }
    
    public static SystemModelGeneric defaultTrain() {
        SystemModelGeneric system = ProxyHelper.newComposite(SystemModelGeneric.class, "Railway");
       
        //system.general().frequency() =  Factory.distributionFactory().getConstantDistribution(900.0);
      
      
        SystemModelGeneric systemProto = prototype(SystemModelGeneric.class, system);
        when(systemProto.receiver()).thenReturn(ProxyHelper.newComposite(ReceiverModel.class, "BaseStation Rx"));
        when(systemProto.transmitter()).thenReturn(ProxyHelper.newComposite(TransmitterModel.class, "Moving_Train TX"));
        return build(systemProto);
    }

    public static Workspace openDefaultWorkspace() {
        Workspace ws = new Workspace();
        ws.setSystemModels(new ArrayList<IdElement<SystemModel>>());
        String id = UUID.randomUUID().toString();
      
        ws.getSystemModels().add( new IdElement<SystemModel>(id, defaultGeneric()));
        ws.getSystemModels().add( new IdElement<SystemModel>(id, defaultTrain()));
        
        
        
        ///////////////////////////////////////////////////////////
        ws.setVictimFrequency( Factory.distributionFactory().getConstantDistribution(900));
        ws.setVictimSystemId(id);
        ws.setInterferenceLinkUIs( new ArrayList<InterferenceLinkElement>());
        InterferenceLinkElement ilElement = new InterferenceLinkElement(id, InterferenceLinksPanel.getILName(1, ws.getVictimSystem()), ProxyHelper.newComposite(InterferenceLinkUI.class));
        ws.addInterferenceLink( ilElement, Factory.distributionFactory().getConstantDistribution(900));
        ws.setSimulationControl( ProxyHelper.newInstance(SimulationControl.class));
        ws.setName(STRINGLIST.getString("WORKSPACE_NAME_PREFIX"));
        return ws;
    }

    public static void saveWorkspace(Workspace workspace) {
        File outputURL = workspace.getPath();
        if ( outputURL == null ) {
            outputURL = ensureDefaultWorkspaceDirectory();
        } else {
            outputURL = outputURL.getParentFile();
        }
        String filename = workspace.getName();
        // Check file name for illegal characters
        if (!checkFilename(filename)) {
            throw new RuntimeException("File contains illegal characters: " + filename );
        }
        File file = new File(outputURL, workspace.getName() + workspace.getFileExtension());
        workspace.setPath( file );
        Workspace results = null;
        if ( workspace.isHasBeenCalculated() ) {
            results = workspace;
        }

        WorkspaceSaver workspaceSaver = new WorkspaceSaver(workspace, results);
        workspaceSaver.saveToFile(file);
    }

    public static Workspace openWorkspace(File file) throws Exception {
        List<MigrationIssue> migrationIssues = new ArrayList<MigrationIssue>();
        File fileToLoad = new WorkspaceMigrator().migrate(file, migrationIssues);

        WorkspaceLoader loader = new WorkspaceLoader();
        Workspace workspace = loader.loadFromFile(fileToLoad);
        workspace.setMigrationIssues( migrationIssues);
        workspace.setPath(file.getAbsoluteFile());
        if ( loader.isCancelled() ) return null;
        return workspace;
    }

    public boolean showWelcomeScreen() {
        return SHOW_WELCOME;
    }

    public void setShowWelcome( boolean showWelcome ) {
        SHOW_WELCOME = showWelcome;
    }


    public static File ensureDefaultWorkspaceDirectory() {
        File outputURL = new File(seamcatHome.getAbsolutePath() + separator + "workspaces" + separator);
        if (!outputURL.exists()) {
            if ( !outputURL.mkdirs() ) {
                LOG.error( "Could not create workspaces directory" );
            }
        }
        return outputURL;
    }

    public static <M extends LibraryItem> List<M> getDefaultsForType(Class<M> clazz) {
        // open default library and load all of specified class
        Library library = getDefaultLibrary();
        library.ensureConsistentLibrary();

        if ( clazz == SystemModel.class) {
            return (List<M>) library.getSystems();
        } else if ( clazz == EmissionMask.class ) {
            return (List<M>) library.getSpectrumEmissionMasks();
        } else if ( clazz == BlockingMask.class ) {
            return (List<M>) library.getReceiverBlockingMasks();
        } else if ( clazz == ReceiverModel.class) {
            return (List<M>) library.getReceivers();
            
        } 
        else if ( clazz == T_ReceiverModel.class) {
            return (List<M>) library.getTReceivers();
        }else if ( clazz == TransmitterModel.class) {
            return (List<M>) library.getTransmitters();
        } else if ( clazz == CDMALinkLevelData.class ) {
            return (List<M>) library.getCDMALinkLevelData();
        } else if ( clazz == AntennaGain.class) {
            return (List<M>) library.getPluginConfigurations(AntennaGainConfiguration.class);
        } else if ( clazz == CoverageRadius.class) {
            return (List<M>) library.getPluginConfigurations(CoverageRadiusConfiguration.class);
        } else if ( clazz == EventProcessing.class ) {
            return (List<M>) library.getPluginConfigurations(EventProcessingConfiguration.class);
        } else if ( clazz == PropagationModel.class) {
            return (List<M>) library.getPluginConfigurations(PropagationModelConfiguration.class);
        }

        throw new RuntimeException("No default types for class: " + clazz);
    }


}