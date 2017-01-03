package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

public class EmissionMaskWorkspaceMigration extends AbstractScenarioMigration {


    @Override
    public void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List sList = context.selectNodes("//systems");
        if ( sList.size() > 0 ) {
            Element ss = (Element) sList.get(0);
            NodeList masks = ss.getElementsByTagName("spectrum-emission-mask");
            for ( int i = 0; i<masks.getLength(); i++) {
                Element item = (Element) masks.item(i);
                Node function = item.getFirstChild();
                while (function.hasChildNodes()) {
                    Node child = function.getFirstChild();
                    function.removeChild( child );
                    item.appendChild( child );
                }
            }

            NodeList emissionFloor = ss.getElementsByTagName("emissionFloor");
            for ( int i=0; i<emissionFloor.getLength(); i++) {
                Element item = (Element) emissionFloor.item(i);

                Node function = item.getFirstChild().getFirstChild();
                while (function.hasChildNodes()) {
                    Node child = function.getFirstChild();
                    function.removeChild( child );
                    item.appendChild( child );
                }
            }

        }

        updateVersion(document);
    }

    @Override
    void migrateResultsDocument(Document document) {

    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(48);
    }
}
