package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.workspace.AntennaPluginWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

public class AntennaPluginSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List antennas = context.selectNodes("//antenna");
        for (Object o : antennas) {
            Element element = (Element) o;
            AntennaPluginWorkspaceMigration.migrateAntenna(document, element);
        }

        List configurationsList = context.selectNodes("//pluginConfigurations");
        Element configurations;
        if ( configurationsList.isEmpty() ) {
            // no configurations available
            org.w3c.dom.Node library = document.getElementsByTagName("library").item(0);
            configurations = document.createElement("pluginConfigurations");
            library.appendChild( configurations );
        } else {
            configurations = (Element) configurationsList.get(0);

        }
        List rootAntennas = context.selectNodes("//antennas");
        if ( rootAntennas.size() > 0 ) {
            Element root = (Element) rootAntennas.get(0);

            NodeList childNodes = root.getChildNodes();
            for ( int i=0; i<childNodes.getLength(); i++) {
                Element conf = (Element) childNodes.item(i).getFirstChild();
                childNodes.item(i).removeChild(conf);
                configurations.appendChild( conf );
            }

            root.getParentNode().removeChild(root);
        }

        updateVersion(document);
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(8);
    }
}
