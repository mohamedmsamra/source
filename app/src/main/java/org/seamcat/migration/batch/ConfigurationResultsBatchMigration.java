package org.seamcat.migration.batch;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.workspace.BatchWorkspaceMigrator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class ConfigurationResultsBatchMigration extends AbstractScenarioMigration {

    private BatchWorkspaceMigrator migrater = new BatchWorkspaceMigrator();

    @Override
    void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);

        // migrate workspaces
        Element wss = document.createElement("workspaces");
        List workspaces = context.selectNodes("//Workspace");
        for (Object obj : workspaces) {
            Element ws = (Element) obj;
            migrater.migrate( ws, true );
            //String possibleResults = ws.getAttribute("workspace_reference") + "_results.xml";
            document.adoptNode( ws );
            wss.appendChild( ws );
        }

        Node batchJobList = document.getElementsByTagName("BatchJobList").item(0);
        while ( batchJobList.hasChildNodes() ) {
            batchJobList.removeChild( batchJobList.getFirstChild() );
        }
        batchJobList.appendChild( wss );

        updateVersion(document);
    }

    @Override
    void migrateResultsDocument(Document document) {
        final Element results = (Element) document.getFirstChild();
        migrater.migrate(results, false );

        document.adoptNode( results );
        document.appendChild( results );
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("BatchJobList/@batch_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(1);
    }


}
