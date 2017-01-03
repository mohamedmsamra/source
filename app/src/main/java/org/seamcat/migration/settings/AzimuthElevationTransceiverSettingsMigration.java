package org.seamcat.migration.settings;

import java.io.File;
import java.util.List;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.Assert;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AzimuthElevationTransceiverSettingsMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrate(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrate(Document document) {
		List allTransceiverNodes = JXPathContext.newContext(document).selectNodes("//transceiver");
		for (Object transceiverNode: allTransceiverNodes) {
			migrateTransceiver(transceiverNode);
		}
		updateVersion(document);
   }

	private void migrateTransceiver(Object transceiverNode) {
		JXPathContext transceiverContext = JXPathContext.newContext(transceiverNode);
		transceiverContext.setFactory(new DomElementJXPathFactory());
		
		transceiverContext.createPathAndSetValue("azimuth/distribution/description", "[Constant(0.0)]");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@constant", "0.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@max", "1.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@max-angle", "360.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@max-distance", "1.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@mean", "0.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@min", "0.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@std-dev", "0.0");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@step", "0.2");
		transceiverContext.createPathAndSetValue("azimuth/distribution/@type", "0");

		transceiverContext.createPathAndSetValue("elevation/distribution/description", "[Constant(0.0)]");	   
		transceiverContext.createPathAndSetValue("elevation/distribution/@constant", "0.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@max", "1.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@max-angle", "360.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@max-distance", "1.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@mean", "0.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@min", "0.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@std-dev", "0.0");
		transceiverContext.createPathAndSetValue("elevation/distribution/@step", "0.2");
		transceiverContext.createPathAndSetValue("elevation/distribution/@type", "0");
   }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
		return SettingsFormatVersionConstants.PREHISTORIC;
   }
	
	private static class DomElementJXPathFactory extends AbstractFactory {
		@Override
		public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
			Assert.isTrue("parent must be a Node", parent instanceof Node);
			Node parentNode = (Node) parent;
			parentNode.appendChild(parentNode.getOwnerDocument().createElement(name));
			return true;
		}
	}
}
