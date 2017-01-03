package org.seamcat.migration.workspace;

import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BatchWorkspaceMigrator {

    private FormatVersion workspaceVersion;

    public void migrate( Element wsElement, boolean scenario ) {
        Node node = wsElement.getParentNode();
        node.removeChild( wsElement );
        Document document = XmlUtils.createDocument();
        document.adoptNode( wsElement );
        document.appendChild( wsElement );

        if ( scenario) {
            int format = Integer.parseInt(wsElement.getAttribute("workspace_format_version"));
            workspaceVersion = new FormatVersion(format);
        }

        FormatVersion version = workspaceVersion.nextVersion();
        WorkspaceMigrationRegistry registry = new WorkspaceMigrationRegistry();
        while ( !WorkspaceFormatVersionConstants.CURRENT_VERSION.equals( version) ) {
            FileMigration migration = registry.findMigration(version);

            AbstractScenarioMigration scenarioMigration = (AbstractScenarioMigration) migration;
            if ( scenario ) {
                scenarioMigration.migrateScenarioDocument(document);
            } else {
                scenarioMigration.migrateResultsDocument( document );
            }
            version = version.nextVersion();
        }
    }


}
