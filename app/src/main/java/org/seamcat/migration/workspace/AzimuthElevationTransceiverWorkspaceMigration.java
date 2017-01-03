package org.seamcat.migration.workspace;

import java.io.File;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AzimuthElevationTransceiverWorkspaceMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrateDocument(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrateDocument(Document document) {
		move( document );
		updateVersion(document);
   }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	private void move( Document document ) {
		
		move( "Workspace/VictimSystemLink/systemLink/txRxAzimuth",   "Workspace/VictimSystemLink/WantedTransmitter/transmitter/transceiver", "azimuth", document );
		move( "Workspace/VictimSystemLink/systemLink/txRxElevation", "Workspace/VictimSystemLink/WantedTransmitter/transmitter/transceiver", "elevation", document );

		move( "Workspace/VictimSystemLink/systemLink/rxTxAzimuth",   "Workspace/VictimSystemLink/VictimReceiver/receiver/transceiver", "azimuth", document );
		move( "Workspace/VictimSystemLink/systemLink/rxTxElevation", "Workspace/VictimSystemLink/VictimReceiver/receiver/transceiver", "elevation", document );

		JXPathContext context = JXPathContext.newContext(document);
		List list = context.selectNodes("Workspace/InterferenceLink");
		for (Object o : list) {
			Element interferer = (Element) o;
			move( "InterferingSystemLink/systemLink/txRxAzimuth",   "InterferingSystemLink/InterferingTransmitter/transmitter/transceiver", "azimuth", interferer );
			move( "InterferingSystemLink/systemLink/txRxElevation", "InterferingSystemLink/InterferingTransmitter/transmitter/transceiver", "elevation", interferer );

			move( "InterferingSystemLink/systemLink/rxTxAzimuth",   "InterferingSystemLink/WantedReceiver/receiver/transceiver", "azimuth", interferer );
			move( "InterferingSystemLink/systemLink/rxTxElevation", "InterferingSystemLink/WantedReceiver/receiver/transceiver", "elevation", interferer );

		}

	}
	
	private void move( String sourcePath, String targetPath, String newName, Node node ) {
		JXPathContext context = JXPathContext.newContext(node);

		Element elevation;
		if ( node instanceof Document ) {
			elevation = ((Document)node).createElement(newName);
		} else {
			elevation = node.getOwnerDocument().createElement(newName);
		}

		Element source = (Element) context.selectSingleNode(sourcePath);
		Element target = (Element) context.selectSingleNode(targetPath);

		NodeList childNodes = source.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			source.removeChild( child );
			elevation.appendChild(child);
		}
		source.getParentNode().removeChild( source );
		target.appendChild(elevation);
	}

	@Override
   public FormatVersion getOriginalVersion() {
	   return WorkspaceFormatVersionConstants.POST_3_2_3;
   }
}
