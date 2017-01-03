package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.UUID;

public class SystemFifthWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
        List workspaces = context.selectNodes("//Workspace");
        if ( workspaces.size() != 1 ) throw new RuntimeException("ERROR");
        Element workspace = (Element) workspaces.get(0);

        Element systems = document.createElement("systems");


        List links = context.selectNodes("//InterferenceLink");
		for (Object o : links) {
            Element element = (Element) o;
            Element system = (Element) element.getElementsByTagName("system").item(0);
            String id = UUID.randomUUID().toString();
            system.setAttribute("id", id);
            element.setAttribute("interferingSystem", id);
            element.removeChild(system);
            systems.appendChild(system);
        }
        Element system = (Element) workspace.getElementsByTagName("system").item(0);
        String victimId = UUID.randomUUID().toString();
        system.setAttribute("id", victimId );
        workspace.setAttribute("victim", victimId );
        workspace.appendChild( systems );

        updateVersion(document);
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
	   return new FormatVersion(37);
   }


}
