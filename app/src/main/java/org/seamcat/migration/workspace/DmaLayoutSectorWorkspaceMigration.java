package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class DmaLayoutSectorWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List dmas = context.selectNodes("//CdmaSystem");
		for (Object o : dmas) {
            Element element = (Element) o;
            int type = Integer.parseInt(element.getAttribute("cellType"));
            element.removeAttribute("cellType");
            int layout;
            if ( element.hasAttribute("gridLayout")) {
                layout = Integer.parseInt(element.getAttribute("gridLayout"));
                element.removeAttribute("gridLayout");
            } else {
                layout = 1;
            }

            String sectorSetup;
            if ( type == 1 ) {
                sectorSetup = "SingleSector";
            } else if (layout == 1) {
                sectorSetup = "TriSector3GPP2";
            } else {
                sectorSetup = "TriSector3GPP";
            }
            element.setAttribute("sectorSetup", sectorSetup);
		}

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
	   return new FormatVersion(32);
   }


}
