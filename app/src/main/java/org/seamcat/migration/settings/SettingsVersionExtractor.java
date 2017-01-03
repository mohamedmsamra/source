package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.VersionExtractor;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

import java.io.File;

public class SettingsVersionExtractor extends VersionExtractor {
	
	@Override
	public FormatVersion extractVersion(File file) {
		Document document = XmlUtils.parse(file);
		JXPathContext context = JXPathContext.newContext(document);
		context.setLenient(true);
		
		String settingsFormatVersionAttribute =  (String) context.getValue("seamcat/@settings_format_version");

		if (settingsFormatVersionAttribute != null) {
			return new FormatVersion(Integer.parseInt(settingsFormatVersionAttribute));
		}
		else {
			return SettingsFormatVersionConstants.PREHISTORIC;
		}
   }
}
