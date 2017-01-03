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

public class TransceiverSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List antennaHeights = context.selectNodes("//transceiver/antenna-height");
        for (Object o : antennaHeights) {
            Element element = (Element) o;
            rename(element, document, "antennaHeight");
        }

        List noiseFloor = context.selectNodes("//receiver/noise-floor-distribution");
        for (Object o : noiseFloor) {
            Element element = (Element) o;
            rename(element, document, "noiseFloor");
        }

        List blockingMask = context.selectNodes("//receiver/receiver-blocking-mask");
        for (Object o : blockingMask) {
            Element element = (Element) o;
            rename(element, document, "blockingMask");
        }

        List receivers = context.selectNodes("//receiver");
        for (Object receiver : receivers) {
            Element e = (Element) receiver;
            rename(e, "blocking-attenuation-mode", "blockingAttenuationMode");
            rename(e, "power_control_max_threshold", "receivePower");
            rename(e, "use_power_control_threshold", "use_receivePower");
        }


        updateVersion(document);
    }

    private void rename( Element element, String attribute, String newName ) {
        String value = element.getAttribute(attribute);
        element.removeAttribute(attribute);
        element.setAttribute(newName, value);
    }

    public static void rename( Element orig, Document document, String newName ) {
        Element elm = document.createElement(newName);

        for ( int i=0; i<orig.getChildNodes().getLength(); i++ ) {
            elm.appendChild(orig.getChildNodes().item(i));
        }

        Node parentNode = orig.getParentNode();
        parentNode.removeChild( orig );
        parentNode.appendChild( elm );
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(7);
    }
}
