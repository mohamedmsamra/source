package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.settings.SensingSettingsMigration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class SensingWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {

        JXPathContext context = JXPathContext.newContext(document);

        List sList = context.selectNodes("//system");
        List lList = context.selectNodes("//link");

        // patch all system to make sure settings are there
        for (Object s : sList) {
            Element emi = find((Element) s, "emissionCharacteristics");
            if ( emi != null ) {
                SensingSettingsMigration.appendSensing( emi, document );
            }
        }
        /*Element ws = (Element) context.selectNodes("//Workspace").get(0);
        String victim = ws.getAttribute("victim");
        Element victimElement = find(sList, victim);

        Element vEmissions = find(victimElement, "emissionCharacteristics");
        if ( vEmissions != null ) {
            SensingSettingsMigration.appendSensing( vEmissions, document );
        }*/

        for (Object ll : lList) {
            Element link = (Element) ll;

            Element origin = (Element) link.getElementsByTagName("sensingCharacteristics").item(0);

            String id = link.getAttribute("interferingSystemId");
            // find system with id
            Element system = find(sList, id);

            Element emissions = find(system, "emissionCharacteristics");
            if ( emissions != null ) {

                emissions.setAttribute("probabilityOfFailure", origin.getAttribute("probabilityOfFailure"));
                emissions.setAttribute("receptionBandwidth", origin.getAttribute("receptionBandwidth"));

                // clear old history
                NodeList nodes = emissions.getChildNodes();
                List<Node> toBeRemoved = new ArrayList<>();
                for ( int i = 0; i<nodes.getLength(); i++) {
                    Node item = nodes.item(i);
                    String name = item.getNodeName();
                    if ( name.equals("eirpMax") || name.equals("detectionThreshold") || name.equals("propagationModel")) {
                        toBeRemoved.add(item);
                    }
                }

                for (Node node : toBeRemoved) {
                    emissions.removeChild( node );
                }
                while ( origin.hasChildNodes() ) {
                    Node child = origin.getFirstChild();
                    origin.removeChild( child );
                    emissions.appendChild( child );
                }
                Node parent = origin.getParentNode();
                parent.removeChild(origin);
                emissions.appendChild(parent.getFirstChild());
            }
        }

        updateVersion(document);
    }

    private Element find( Element origin, String name ) {
        NodeList list = origin.getElementsByTagName(name);
        if ( list.getLength() == 1 ) {
            return (Element) list.item(0);
        }

        return null;
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
        return new FormatVersion(46);
    }

}
