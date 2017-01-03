package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.workspace.PropagationModelWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;

public class PropagationModelSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List pms = context.selectNodes("//PropagationModel");

        for (Object o : pms) {
            PropagationModelWorkspaceMigration.migratePropagationModel((Element) o, document);
        }

        List library = context.selectNodes("//library");
        if ( library.size() > 0 ) {
            Element lib = (Element) library.get(0);

            Element configurations = document.createElement("plugin-configurations");
            lib.appendChild( configurations );
        }
        updateVersion(document);
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(3);
    }
}
