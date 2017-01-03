package org.seamcat.migration;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.settings.SettingsFormatVersionConstants;
import org.seamcat.migration.settings.SettingsVersionExtractor;
import org.seamcat.util.IOUtils;


public class SettingsVersionExtractorTest {

	private VersionExtractor extractor;
	
	@Before
	public void setUp() {
		extractor = new SettingsVersionExtractor();		
	}

	@Test
	public void extractPrehistoricVersion() {
		File settingsFile = IOUtils.copyResourceToTempFileWithSameName("migration/prehistoricSettings.xml");
		FormatVersion extractedVersion = extractor.extractVersion(settingsFile);
		assertEquals(SettingsFormatVersionConstants.PREHISTORIC, extractedVersion);
	}
	
	@Test
	public void extractVersionZero() {
		File settingsFile = IOUtils.copyResourceToTempFileWithSameName("migration/version0Settings.xml");
		FormatVersion extractedVersion = extractor.extractVersion(settingsFile);
		assertEquals(new FormatVersion(0), extractedVersion);
	}	
}
