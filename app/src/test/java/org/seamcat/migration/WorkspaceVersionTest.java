package org.seamcat.migration;

import org.junit.Ignore;
import org.junit.Test;
import org.seamcat.marshalling.WorkspaceMarshaller;
import org.seamcat.migration.workspace.WorkspaceFormatVersionConstants;
import org.seamcat.model.Workspace;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;


public class WorkspaceVersionTest {

	@Ignore // Unstable - may open dialog from workspace init code.
	@Test
	public void workspaceSavesCurrentVersion() {
		Workspace workspace = new Workspace();
		Document document = XmlUtils.createDocument();
		document.appendChild(WorkspaceMarshaller.toElement(workspace, document));
		XPathAssert xpathAssert = new XPathAssert(document);
		xpathAssert.nodeValueEquals(
				Integer.toString(WorkspaceFormatVersionConstants.CURRENT_VERSION.getNumber()), 
				"Workspace/@workspace_format_version");
	}
}
