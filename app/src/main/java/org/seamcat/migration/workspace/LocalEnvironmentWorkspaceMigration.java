package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.scenario.MutableLocalEnvironment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class LocalEnvironmentWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);

        List links = context.selectNodes("//InterferingSystemLink");
        Element iTx = null, iRx = null, vTx = null, vRx = null;
        boolean hasVictimTx = false;
        boolean hasVictimRx = false;
        boolean hasInterfererTx = false;
        boolean hasInterfererRx = false;
        boolean first = true;
        for (Object o : links) {
            if ( first ) {
                iTx = (Element) ((Element)o).getElementsByTagName("transmitter").item(0);
                iRx = (Element) ((Element)o).getElementsByTagName("receiver").item(0);
            } else {
                first = false;
            }
        }

        links = context.selectNodes("//VictimSystemLink");
        for (Object o : links) {
            vTx = (Element) ((Element)o).getElementsByTagName("transmitter").item(0);
            vRx = (Element) ((Element)o).getElementsByTagName("receiver").item(0);
        }

        links = context.selectNodes("//InterferenceLink");
        boolean multiple = false;
        for (Object link : links) {
            Element element = (Element) link;
            Element transmitter = (Element) element.getElementsByTagName("transmitter").item(0);
            boolean interfererIsCr = Boolean.parseBoolean(transmitter.getAttribute("interferer_is_cr"));

            Element path = (Element) element.getFirstChild();
            Element pm = (Element) path.getElementsByTagName("plugin-configuration").item(0);
            MutableLocalEnvironment rx = new MutableLocalEnvironment(), tx = new MutableLocalEnvironment();
            if ( cleanPm(pm, rx, tx) ) {
                if ( multiple ) {
                    getMigrationIssues().add( new MigrationIssue("Local environments are skipped for multiple Interfering links"));
                } else {
                    writeEnvironment(rx, vRx, document);
                    hasVictimRx = true;
                    writeEnvironment(tx, iTx, document);
                    hasInterfererTx = true;
                }

            }

            NodeList sensing = element.getElementsByTagName("WantedTransmitterToInterferingTransmitterPath");
            if ( sensing.getLength() > 0 ) {
                MutableLocalEnvironment sRx = new MutableLocalEnvironment(), sTx = new MutableLocalEnvironment();
                if ( cleanPm((Element) sensing.item(0).getFirstChild().getFirstChild(), sRx, sTx) && interfererIsCr ) {
                    if ( multiple ) {
                        getMigrationIssues().add(new MigrationIssue("Local environments are skipped for multiple Interfering links"));
                    } else {
                        writeEnvironment(tx, vTx, document);
                        hasVictimTx = true;
                        writeEnvironment(rx, iRx, document);
                        hasInterfererRx = true;
                    }
                }
            }
            multiple = true;
        }

        links = context.selectNodes("//VictimSystemLink");
        for (Object o : links) {
            handleLink((Element) o, document, hasVictimRx, hasVictimTx );
        }

        links = context.selectNodes("//InterferingSystemLink");
        for (Object o : links) {
            handleLink((Element) o, document, hasInterfererRx, hasInterfererTx);
        }

        updateVersion(document);
	}

    private boolean cleanPm(Element pm, MutableLocalEnvironment rx, MutableLocalEnvironment tx) {
        String cn = pm.getAttribute("classname");

        boolean hasEnvironments = true;
        if ( cn.equals("org.seamcat.model.propagation.HataSE21PropagationModel") ||
                cn.equals("org.seamcat.model.propagation.HataSE24PropagationModel")) {
            rx.setEnvironment( valueOf(rem(pm, "param10")));
            tx.setEnvironment( valueOf(rem(pm, "param11")));
            rx.setWallLoss(Double.parseDouble(rem(pm, "param12")));
            rx.setWallLossStdDev(Double.parseDouble(rem(pm, "param13")));
            tx.setWallLoss( rx.getWallLoss());
            tx.setWallLossStdDev( rx.getWallLossStdDev() );
        } else if (cn.equals("org.seamcat.model.propagation.SDPropagationModel")) {
            // 13 -> 16
            rx.setEnvironment( valueOf(rem(pm,"param13")));
            tx.setEnvironment( valueOf(rem(pm, "param14")));
            rx.setWallLoss(Double.parseDouble(rem(pm,"param15")));
            rx.setWallLossStdDev(Double.parseDouble(rem(pm,"param16")));
            tx.setWallLoss( rx.getWallLoss());
            tx.setWallLossStdDev( rx.getWallLossStdDev() );
        } else {
            hasEnvironments = false;
        }
        return hasEnvironments;
    }

    private void handleLink( Element element, Document document, boolean hasRx, boolean hasTx ) {
        boolean hasEnvironments = true;
        Element pm = (Element) element.getElementsByTagName("PropagationModel").item(0).getFirstChild();
        String cn = pm.getAttribute("classname");
        MutableLocalEnvironment rx = new MutableLocalEnvironment(), tx = new MutableLocalEnvironment();
        if ( cn.equals("org.seamcat.model.propagation.HataSE21PropagationModel") ||
                cn.equals("org.seamcat.model.propagation.HataSE24PropagationModel")) {
            rx.setEnvironment( valueOf(rem(pm, "param10")));
            tx.setEnvironment( valueOf(rem(pm, "param11")));
            rx.setWallLoss(Double.parseDouble(rem(pm, "param12")));
            rx.setWallLossStdDev(Double.parseDouble(rem(pm, "param13")));
            tx.setWallLoss( rx.getWallLoss());
            tx.setWallLossStdDev( rx.getWallLossStdDev() );
        } else if (cn.equals("org.seamcat.model.propagation.SDPropagationModel")) {
            // 13 -> 16
            rx.setEnvironment( valueOf(rem(pm,"param13")));
            tx.setEnvironment( valueOf(rem(pm, "param14")));
            rx.setWallLoss(Double.parseDouble(rem(pm,"param15")));
            rx.setWallLossStdDev(Double.parseDouble(rem(pm,"param16")));
            tx.setWallLoss( rx.getWallLoss());
            tx.setWallLossStdDev( rx.getWallLossStdDev() );
        } else {
            hasEnvironments = false;
        }

        if ( hasEnvironments ) {
            if ( !hasRx ) {
                Node receiver = element.getElementsByTagName("receiver").item(0);
                Element les = document.createElement("LocalEnvironments");
                les.appendChild( add(document.createElement("LocalEnvironment"), rx) );
                receiver.appendChild( les );
            }

            if ( !hasTx ) {
                Node transmitter = element.getElementsByTagName("transmitter").item(0);
                Element les = document.createElement("LocalEnvironments");
                les.appendChild( add(document.createElement("LocalEnvironment"), tx) );
                transmitter.appendChild( les );
            }
        }
    }

    private LocalEnvironment.Environment valueOf(String number) {
        if ( "0".equals(number)) {
            return LocalEnvironment.Environment.Indoor;
        } else return LocalEnvironment.Environment.Outdoor;
    }

    private void writeEnvironment(MutableLocalEnvironment le, Element element, Document document) {
        Element les = document.createElement("LocalEnvironments");
        les.appendChild( add(document.createElement("LocalEnvironment"), le) );
        element.appendChild( les );
    }

    private String rem( Element element, String name ) {
        String attribute = element.getAttribute(name);
        element.removeAttribute(name);
        return attribute;
    }

    private Element add(Element le, MutableLocalEnvironment env) {
        le.setAttribute("environment", env.getEnvironment().name());
        le.setAttribute("prop", Double.toString(env.getProbability()));
        le.setAttribute("wallLoss", Double.toString(env.getWallLoss()));
        le.setAttribute("stdDev", Double.toString(env.getWallLossStdDev()));
        return le;
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
	   return new FormatVersion(16);
   }


}
