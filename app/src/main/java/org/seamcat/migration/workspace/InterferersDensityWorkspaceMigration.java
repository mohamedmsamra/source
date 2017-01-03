package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class InterferersDensityWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {

        JXPathContext context = JXPathContext.newContext(document);

        // TODO patch victim system if generic
        List wss = context.selectNodes("//Workspace");
        Element workspace = (Element) wss.get(0);
        String victimId = workspace.getAttribute("victim");

        List lList = context.selectNodes("//link");

        List sList = context.selectNodes("//system");

        List<String> processed = new ArrayList<>();
        for (Object l : lList) {
            Element link = (Element) l;
            String systemId = link.getAttribute("interferingSystemId");
            processed.add( systemId );
            Element system = find(sList, systemId);
            Element composite = (Element) system.getFirstChild();

            if ( composite.getAttribute("class").equals("org.seamcat.model.systems.generic.SystemModelGeneric")) {
                Node path = composite.getElementsByTagName("path").item(0).getFirstChild();

                // move
                Node density = link.getElementsByTagName("density").item(0);
                density.getParentNode().removeChild( density );

                path.appendChild( density );
            }

        }

        // make sure all systems are patched
        for (Object o : sList) {
            Element system = (Element) o;
            if ( !processed.contains(system.getAttribute("id"))) {
                Element composite = (Element) system.getFirstChild();
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
        }

        updateVersion(document);
    }

    private Element find( List sList, String id ) {
        for (Object ss : sList ) {
            Element system = (Element) ss;

            if ( system.getAttribute("id").equals(id)) {
                return system;
            }
        }

        throw new RuntimeException("System not found: " + id);
    }

    @Override
    void migrateResultsDocument(Document document) {
        // nothing to do here
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(47);
    }

}
