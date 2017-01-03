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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SpectrumEmissionMaskSettingsMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrate(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrate(Document document) {
		List allUnwantedEmissionNodes = JXPathContext.newContext(document).selectNodes("//unwantedemission");
		for (int i=0; i<allUnwantedEmissionNodes.size(); i++ ) {
			migrateUE((Element) allUnwantedEmissionNodes.get(i), document, i);
		}
		updateVersion(document);
   }

	private void migrateUE( Element unwantedEmissionNode, Document document, int index ) {
		JXPathContext context = JXPathContext.newContext( unwantedEmissionNode );
		context.setFactory( new DomElementJXPathFactory());


		Element sem = document.createElement("spectrum-emission-mask");
		sem.setAttribute("reference", "DEFAULT_SPECTRUM_EMISSION_MASK_" + index);
		for ( int i=0; i<unwantedEmissionNode.getChildNodes().getLength(); i++ ) {
			sem.appendChild(unwantedEmissionNode.getChildNodes().item(i));
		}

		Node parentNode = unwantedEmissionNode.getParentNode();
		parentNode.removeChild( unwantedEmissionNode );
		parentNode.appendChild( sem );
   }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
		return new FormatVersion(0);
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
