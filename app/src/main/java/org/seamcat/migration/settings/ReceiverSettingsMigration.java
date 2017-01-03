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

public class ReceiverSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List receivers = context.selectNodes("//receivers/receiver");
        for (Object o : receivers) {
            Element receiver = (Element) o;
            migrateReceiver( receiver, document);
        }

        updateVersion(document);
    }

    public static void migrateReceiver(Element receiver, Document doc) {
        Element composite = doc.createElement("composite");
        composite.setAttribute("class", "org.seamcat.model.systems.generic.ReceiverModel");

        Element desc = doc.createElement("description");

        Element crit = doc.createElement("interferenceCriteria");
        moveAtt("extended_protection_ratio", receiver, crit);
        moveAtt("interference_to_noise_ratio", receiver, crit);
        moveAtt("noise_augmentation", receiver, crit);
        moveAtt("protection_ratio", receiver, crit);

        Element recep = doc.createElement("receptionCharacteristics");
        moveAtt("blockingAttenuationMode", receiver, recep);
        moveAtt("receivePower", receiver, recep);
        moveAtt("reception_bandwith", receiver, recep);
        moveAtt("sensitivity", receiver, recep);
        moveAtt("use_receivePower", receiver, recep);
        moveAtt("use_receiver_overloading", receiver, recep);
        moveChild("noiseFloor", receiver, recep);
        moveChild("blockingMask", receiver, recep);
        moveChild("intermodulation_rejection", receiver, recep);
        moveChild("overloading_mask", receiver, recep);
        moveChild("receiver_filter", receiver, recep);

        Element transceiver = (Element) receiver.getElementsByTagName("transceiver").item(0);
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
        composite.appendChild(crit);
        composite.appendChild(recep);

        Node node = receiver.getParentNode();
        node.removeChild( receiver );
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
        return new FormatVersion(12);
    }
}
