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

public class InterferersDensitySettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List sList = context.selectNodes("//composite");
        for (Object o : sList) {
            Element composite = (Element) o;
            if ( composite.getAttribute("class").equals("org.seamcat.model.systems.generic.SystemModelGeneric")) {
                Node path = composite.getElementsByTagName("path").item(0).getFirstChild();

                // default
                Element densityElm = document.createElement("density");
                densityElm.setAttribute("densityTx", "1.0");
                densityElm.setAttribute("hourOfDay", "1.0");
                densityElm.setAttribute("probabilityOfTransmission", "1.0");

                Element activity = document.createElement("activity");
                Element constantFunction = document.createElement("ConstantFunction");
                constantFunction.setAttribute("value", "1.0");
                activity.appendChild(constantFunction);

                densityElm.appendChild( activity );

                path.appendChild(densityElm);
            }
        }

        updateVersion(document);
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(16);
    }
}
