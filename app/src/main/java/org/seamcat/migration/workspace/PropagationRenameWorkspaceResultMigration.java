package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.settings.PropagationRenameSettingsMigration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PropagationRenameWorkspaceResultMigration extends AbstractScenarioMigration {

	@Override
	void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List pms = context.selectNodes("//pluginConfiguration");

        for (Object o : pms) {
            PropagationRenameSettingsMigration.patchBuildIn((Element) o);
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
	   return new FormatVersion(26);
   }


}
