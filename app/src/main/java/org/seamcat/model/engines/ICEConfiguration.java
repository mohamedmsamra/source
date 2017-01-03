package org.seamcat.model.engines;

import org.seamcat.loadsave.AttributeAccessor;
import org.seamcat.loadsave.ElementProcessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.marshalling.PointMarshaller;
import org.seamcat.model.Workspace;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.seamcat.util.XmlEventFactory.*;

public class ICEConfiguration implements InterferenceCriterionType {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    public static final String[] ERROR = new String[] {
            STRINGLIST.getString("ICECONFIG_ERROR_OK"),
            STRINGLIST.getString("ICECONFIG_ERROR_ONE_SIGNAL"),
            STRINGLIST.getString("ICECONFIG_ERROR_MIN_LESS_THAN_MAX"),
            STRINGLIST.getString("ICECONFIG_ERROR_DRSS_IRSS_CORRELATED"),
            STRINGLIST.getString("ICECONFIG_ERROR_CANCELLED") };

    // Validate result
    public static final int EVERYTHING_OK = 0;
    public static final int MIN_GREATER_THAN_MAX = 2;
    public static final int NO_SIGNAL_TYPE = 1;
    public static final int SIGNAL_IS_CORRELATED = 3;
    public static final int SIGNAL_IS_CORRELATION_NOT_ACCEPTED = 4;

    private boolean allowIntermodulation = true;
    private boolean allowingOverloading = false;

    private boolean unwanted;
    private boolean blocking;
    private boolean overloading;
    private boolean intermodulation;

    private boolean calculationModeIsTranslation;
    private boolean hasBeenCalculated;
    private int interferenceCriterionType = 1;

    private int numberOfSamples;

    // Results and instance arguments
    private double probabilityResult;
    private boolean signalsModifiedByPlugin;
    private double translationMax;
    private double translationMin;
    private int numberOfTotalEvents = Integer.MIN_VALUE;
    private double probabilityTotalN = Double.MIN_VALUE;
    private double ciLevel = Double.MIN_VALUE;
    private double cniLevel = Double.MIN_VALUE;
    private double iniLevel = Double.MIN_VALUE;
    private double niLevel = Double.MIN_VALUE;
    private double sensitivity = Double.MIN_VALUE;

    private int translationParameter;
    private List<Point2D> translationPointResults = new ArrayList<Point2D>();

    private double translationPoints;

    public ICEConfiguration() {
        calculationModeIsTranslation = false;
        unwanted = true;
        blocking = false;
        intermodulation = false;
        interferenceCriterionType = CI;
        numberOfSamples = 20000;
        translationMin = 0;
        translationMax = 100;
        translationPoints = 100;
        hasBeenCalculated = false;
        signalsModifiedByPlugin = false;
    }

    public ICEConfiguration(Element element) throws Exception {
        unwanted = Boolean.valueOf(element.getAttribute("unwanted")).booleanValue();
        blocking = Boolean.valueOf(element.getAttribute("blocking")).booleanValue();
        overloading = Boolean.valueOf(element.getAttribute("overloading")).booleanValue();
        intermodulation = Boolean.valueOf(element.getAttribute("intermodulation")).booleanValue();
        allowIntermodulation = Boolean.valueOf(element.getAttribute("allowIntermodulation")).booleanValue();
        numberOfSamples = Integer.parseInt(element.getAttribute("numberOfSamples"));
        calculationModeIsTranslation = Boolean.valueOf(element.getAttribute("translationMode"));
        translationParameter = Integer.parseInt(element.getAttribute("translationParameter"));
        translationMin = Double.parseDouble(element.getAttribute("translationMin"));
        translationMax = Double.parseDouble(element.getAttribute("translationMax"));
        translationPoints = Double.parseDouble(element.getAttribute("translationPoints"));
        probabilityResult = Double.parseDouble(element.getAttribute("probabilityResult"));

        hasBeenCalculated = false; // Boolean.valueOf(element.getAttribute("unwanted")).booleanValue();
        signalsModifiedByPlugin = Boolean.valueOf(element.getAttribute("signalsModifiedByPlugin")).booleanValue();

        try {
            interferenceCriterionType = Integer.parseInt(element.getAttribute("interference-criterion"));
        } catch (Exception e) {
            // Do nothing;
        }

        NodeList nl = element.getElementsByTagName("point2d");
        for (int x = 0, size = nl.getLength(); x < size; x++) {
            addTranslationPoint(PointMarshaller.fromElement2D((Element) nl.item(x)));
        }
    }

    public ICEConfiguration(XmlEventStream eventStream) throws XMLStreamException {
        StartElement element = eventStream.checkAndSkipStartElement("ICEConfiguration");
        AttributeAccessor attributes = new AttributeAccessor(element);

        unwanted = Boolean.valueOf(attributes.value("unwanted")).booleanValue();
        blocking = Boolean.valueOf(attributes.value("blocking")).booleanValue();
        overloading = Boolean.valueOf(attributes.value("overloading")).booleanValue();
        intermodulation = Boolean.valueOf(attributes.value("intermodulation")).booleanValue();
        allowIntermodulation = Boolean.valueOf(attributes.value("allowIntermodulation")).booleanValue();
        numberOfSamples = Integer.parseInt(attributes.value("numberOfSamples"));
        calculationModeIsTranslation = Boolean.valueOf(attributes.valueOrDefault("translationMode", "false"));
        translationParameter = Integer.parseInt(attributes.value("translationParameter"));
        translationMin = Double.parseDouble(attributes.value("translationMin"));
        translationMax = Double.parseDouble(attributes.value("translationMax"));
        translationPoints = Double.parseDouble(attributes.value("translationPoints"));
        probabilityResult = Double.parseDouble(attributes.value("probabilityResult"));
        signalsModifiedByPlugin = Boolean.valueOf(attributes.valueOrDefault("signalsModifiedByPlugin", "false")).booleanValue();
        interferenceCriterionType = Integer.parseInt(attributes.valueOrDefault("interference-criterion", Integer.toString(CI)));

        hasBeenCalculated = false;

        eventStream.processWrappedElementSequence("translationPointResults", "point2d", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                addTranslationPoint(PointMarshaller.fromStream2D(eventStream));
            }
        });

        eventStream.checkAndSkipEndElement("ICEConfiguration");
    }

    public ICEConfiguration(ICEConfiguration _ice) {
        calculationModeIsTranslation = _ice.calculationModeIsTranslation;
        unwanted = _ice.unwanted;
        blocking = _ice.blocking;
        overloading = _ice.overloading;
        intermodulation = _ice.intermodulation;
        interferenceCriterionType = _ice.interferenceCriterionType;
        numberOfSamples = _ice.numberOfSamples;
        translationMin = _ice.translationMin;
        translationMax = _ice.translationMax;
        translationPoints = _ice.translationPoints;
        translationParameter = _ice.translationParameter;
        hasBeenCalculated = false;
        signalsModifiedByPlugin = false;
        allowIntermodulation = _ice.allowIntermodulation;
    }

    public void addTranslationPoint(Point2D p) {
        translationPointResults.add(p);
    }

    public boolean allowIntermodulation() {
        return allowIntermodulation;
    }

    public boolean calculationModeIsTranslation() {
        return calculationModeIsTranslation;
    }

    public boolean getHasBeenCalculated() {
        return this.hasBeenCalculated;
    }

    public int getInterferenceCriterionType() {
        return interferenceCriterionType;
    }

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public int getNumberOfTotalEvents() {
        return numberOfTotalEvents;
    }

    public double getPropabilityResult() {
        return this.probabilityResult;
    }

    public double getTranslationMax() {
        return translationMax;
    }

    public double getTranslationMin() {
        return translationMin;
    }

    public int getTranslationParameter() {
        return translationParameter;
    }

    public double getTranslationPoints() {
        return translationPoints;
    }

    public List<Point2D> getTranslationResults() {
        return translationPointResults;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public boolean isIntermodulation() {
        return intermodulation;
    }

    public boolean isUnwanted() {
        return unwanted;
    }

    public void setAllowIntermodulation(boolean value) {
        allowIntermodulation = value;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void setCalculationModeIsTranslation(boolean calculationMode) {
        this.calculationModeIsTranslation = calculationMode;
        // No overloading when using translationmode
        if (calculationModeIsTranslation) {
            setOverloading(false);
        }
    }

    public void setHasBeenCalculated(boolean value) {
        this.hasBeenCalculated = value;
    }

    public void setInterferenceCriterionType(int interferenceCriterionType) {
        this.interferenceCriterionType = interferenceCriterionType;
    }

    public void setIntermodulation(boolean intermodulation) {
        this.intermodulation = intermodulation;
    }

    public void setNumberOfSamples(int numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public void setnumberOfTotalEvents(int _numberOfTotalEvents) {
        this.numberOfTotalEvents = _numberOfTotalEvents;
    }

    public void setProbabilityResult(double value) {
        this.probabilityResult = value;
    }

    public void setTranslationMax(double translationMax) {
        this.translationMax = translationMax;
    }

    public void setTranslationMin(double translationMin) {
        this.translationMin = translationMin;
    }

    public void setTranslationParameter(int translationParameter) {
        this.translationParameter = translationParameter;
    }

    public void setTranslationPoints(double translationPoints) {
        this.translationPoints = translationPoints;
    }

    public void setUnwanted(boolean unwanted) {
        this.unwanted = unwanted;
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("ICEConfiguration");

        element.setAttribute("unwanted", Boolean.toString(unwanted));
        element.setAttribute("blocking", Boolean.toString(blocking));
        element.setAttribute("overloading", Boolean.toString(overloading));
        element.setAttribute("intermodulation", Boolean.toString(intermodulation));
        element.setAttribute("allowIntermodulation", Boolean.toString(allowIntermodulation));
        element.setAttribute("numberOfSamples", Integer.toString(numberOfSamples));
        element.setAttribute("algorithm", Integer.toString(0));
        element.setAttribute("translationParameter", Integer.toString(translationParameter));
        element.setAttribute("translationMin", Double.toString(translationMin));
        element.setAttribute("translationMax", Double.toString(translationMax));
        element.setAttribute("translationPoints", Double.toString(translationPoints));
        element.setAttribute("probabilityResult", Double.toString(probabilityResult));
        element.setAttribute("hasBeenCalculated", Boolean.toString(hasBeenCalculated));
        element.setAttribute("signalsModifiedByPlugin", Boolean.toString(signalsModifiedByPlugin));
        element.setAttribute("interference-criterion", Integer.toString(interferenceCriterionType));
        element.setAttribute("translationMode", Boolean.toString(calculationModeIsTranslation));

        Element translationPointResultsElement = doc.createElement("translationPointResults");
        for (Point2D p : translationPointResults) {
            translationPointResultsElement.appendChild(PointMarshaller.toElement2D(doc, p));
        }
        element.appendChild(translationPointResultsElement);

        return element;
    }

    public void saveToXmlStream(XMLEventWriter eventWriter) throws XMLStreamException {
        eventWriter.add(startElement("ICEConfiguration"));
        eventWriter.add(attribute("unwanted", Boolean.toString(unwanted)));
        eventWriter.add(attribute("blocking", Boolean.toString(blocking)));
        eventWriter.add(attribute("overloading", Boolean.toString(overloading)));
        eventWriter.add(attribute("intermodulation", Boolean.toString(intermodulation)));
        eventWriter.add(attribute("allowIntermodulation", Boolean.toString(allowIntermodulation)));
        eventWriter.add(attribute("numberOfSamples", Integer.toString(numberOfSamples)));
        eventWriter.add(attribute("algorithm", Integer.toString(0)));
        eventWriter.add(attribute("translationParameter", Integer.toString(translationParameter)));
        eventWriter.add(attribute("translationMin", Double.toString(translationMin)));
        eventWriter.add(attribute("translationMax", Double.toString(translationMax)));
        eventWriter.add(attribute("translationPoints", Double.toString(translationPoints)));
        eventWriter.add(attribute("probabilityResult", Double.toString(probabilityResult)));
        eventWriter.add(attribute("hasBeenCalculated", Boolean.toString(hasBeenCalculated)));
        eventWriter.add(attribute("signalsModifiedByPlugin", Boolean.toString(signalsModifiedByPlugin)));
        eventWriter.add(attribute("interference-criterion", Integer.toString(interferenceCriterionType)));
        eventWriter.add(attribute("translationMode", Boolean.toString(calculationModeIsTranslation)));
        eventWriter.add(startElement("translationPointResults"));
        for (Point2D p : translationPointResults) {
            PointMarshaller.saveToXmlStream(eventWriter, p);
        }
        eventWriter.add(endElement("translationPointResults"));
        eventWriter.add(endElement("ICEConfiguration"));
    }

    public int validate() {
        int val;
        if (!(unwanted || blocking || overloading || intermodulation)) {
            val = NO_SIGNAL_TYPE;
        } else if (translationMax < translationMin) {
            val = MIN_GREATER_THAN_MAX;
        } else {
            val = EVERYTHING_OK;
        }
        return val;
    }

    public boolean isOverloading() {
        return overloading;
    }

    public void setOverloading(boolean overloading) {
        this.overloading = overloading;
    }

    public boolean isAllowingOverloading() {
        return allowingOverloading && !calculationModeIsTranslation();
    }

    public void updateConf(Workspace workspace) {
        if ( workspace.getVictimSystemLink().getSystem() instanceof GenericSystem ) {
            GenericSystem sys = (GenericSystem) workspace.getVictimSystemLink().getSystem();
            allowingOverloading = sys.getReceiver().isUsingOverloading();
        } else {
            allowingOverloading = false;
        }
    }

    public double getProbabilityTotalN() {
        return probabilityTotalN;
    }

    public void setProbabilityTotalN(double probabilityTotalN) {
        this.probabilityTotalN = probabilityTotalN;
    }

    public double getCiLevel() {
        return ciLevel;
    }

    public void setCiLevel(double ciLevel) {
        this.ciLevel = ciLevel;
    }

    public double getCniLevel() {
        return cniLevel;
    }

    public void setCniLevel(double cniLevel) {
        this.cniLevel = cniLevel;
    }

    public double getIniLevel() {
        return iniLevel;
    }

    public void setIniLevel(double iniLevel) {
        this.iniLevel = iniLevel;
    }

    public double getNiLevel() {
        return niLevel;
    }

    public void setNiLevel(double niLevel) {
        this.niLevel = niLevel;
    }

    public double getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

}
