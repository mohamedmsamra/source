package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.List;

public class TransmitterCompositeSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List transmitters = context.selectNodes("//transmitters/transmitter");
        for (Object o : transmitters) {
            Element transmitter = (Element) o;
            migrateTransmitter(transmitter, document);
        }

        updateVersion(document);
    }

    public static void migrateTransmitter(Element transmitter, Document doc) {
        Element composite = doc.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.systems.generic.TransmitterModel");

        Element desc = doc.createElement("description");

        Element chars = doc.createElement("emissionCharacteristics");
        moveAtt("cognitiveRadio", transmitter, chars);
        moveAtt("dynamicRange", transmitter, chars);
        moveAtt("minThreshold", transmitter, chars);
        moveAtt("powerControl", transmitter, chars);
        moveAtt("stepSize", transmitter, chars);
        moveChild("power", transmitter, chars);
        moveChild("emissionMask", transmitter, chars);
        moveChild("emissionFloor", transmitter, chars);

        Element transceiver = (Element) transmitter.getElementsByTagName("transceiver").item(0);
        Element pointing = doc.createElement("antennaPointing");
        moveAtt("antennaPointingAzimuth", transceiver, pointing);
        moveAtt("antennaPointingElevation", transceiver, pointing);
        moveChild("antennaHeight", transceiver, pointing);
        moveChild("azimuth", transceiver, pointing);
        moveChild("elevation", transceiver, pointing);

        String reference = transceiver.getAttribute("reference");
        transceiver.removeAttribute("reference");
        desc.setAttribute("name", reference);

        composite.appendChild(desc);
        moveChild("antennaGain", transceiver, composite);
        composite.appendChild(pointing);
        composite.appendChild(chars);

        Node node = transmitter.getParentNode();
        node.removeChild( transmitter );
        node.appendChild( composite );
    }


    private static void moveAtt(String name, Element from, Element to) {
        String attValue = from.getAttribute(name);
        from.removeAttribute(name);
        to.setAttribute(name, attValue );
    }

    private static void moveChild(String name, Element from, Element to) {
        Node child = from.getElementsByTagName(name).item(0);
        from.removeChild( child );
        to.appendChild( child );
    }


    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(13);
    }
}
