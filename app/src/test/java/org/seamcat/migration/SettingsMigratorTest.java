package org.seamcat.migration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.seamcat.migration.settings.SettingsFormatVersionConstants;
import org.seamcat.migration.settings.SettingsMigrator;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.IOUtils;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;


@Ignore
public class SettingsMigratorTest {

	private SettingsMigrator migrator;
	
	@Before
	public void setUp() {
		migrator = new SettingsMigrator();
	}

	@Test
	public void migratePrehistoricVersion() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/prehistoricSettings.xml");
		migrator.migrateToVersion(originalFile, new ArrayList<MigrationIssue>(), SettingsFormatVersionConstants.PREHISTORIC.nextVersion());
	}
	
	@Test
	public void migrateAlreadyAtCurrentVersion() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/version0Settings.xml");
		File migratedFile = migrator.migrateToVersion(originalFile, new ArrayList<MigrationIssue>(), new FormatVersion(0));
		assertSame(originalFile, migratedFile);
	}
	
	@Test(expected = MigrationException.class)
	public void migrateBackwardsThrowsException() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/version0Settings.xml");
		migrator.migrateToVersion(originalFile, new ArrayList<MigrationIssue>(), SettingsFormatVersionConstants.PREHISTORIC);
	}
	
	@Test
	public void migrateAndShuffleSettingsFilesGivenNoFiles() {
		File settingsDir = IOUtils.createTempDir();
		File prehistoricSettingsFile = new File(settingsDir, "seamcat.xml");
		File settingsFile = new File(settingsDir, "settings.xml");

		migrator.migrateAndShuffleSettingsFiles(settingsFile, prehistoricSettingsFile);
		
		assertFalse(settingsFile.exists());
		assertFalse(prehistoricSettingsFile.exists());
	}

	@Test
	public void migrateAndShuffleSettingsFilesGivenPrehistoricFile() {
		assertTrue(SettingsFormatVersionConstants.CURRENT_VERSION.isLaterThan(SettingsFormatVersionConstants.PREHISTORIC));
		File settingsDir = IOUtils.createTempDir();
		File prehistoricSettingsFile = new File(settingsDir, "seamcat.xml");
		File settingsFile = new File(settingsDir, "settings.xml");
		IOUtils.copyResourceToFile("migration/prehistoricSettings.xml", prehistoricSettingsFile);

		migrator.migrateAndShuffleSettingsFiles(settingsFile, prehistoricSettingsFile);
		
		assertTrue(settingsFile.exists());
		assertTrue(prehistoricSettingsFile.exists());
	}

	@Test
	public void migrateAndShuffleSettingsFilesGivenPrehistoricAndSettingsFile() {
		File settingsDir = IOUtils.createTempDir();
		File prehistoricSettingsFile = new File(settingsDir, "seamcat.xml");
		File settingsFile = new File(settingsDir, "settings.xml");
		IOUtils.copyResourceToFile("migration/prehistoricSettings.xml", prehistoricSettingsFile);
		IOUtils.copyResourceToFile("migration/version0Settings.xml", settingsFile);

		migrator.migrateAndShuffleSettingsFiles(settingsFile, prehistoricSettingsFile);
		
		assertTrue(settingsFile.exists());
		assertTrue(prehistoricSettingsFile.exists());
	}

	@Test
	public void migrateAndShuffleSettingsFilesGivenSettingsFile() {
		File settingsDir = IOUtils.createTempDir();
		File prehistoricSettingsFile = new File(settingsDir, "seamcat.xml");
		File settingsFile = new File(settingsDir, "settings.xml");
		IOUtils.copyResourceToFile("migration/version0Settings.xml", settingsFile);

		migrator.migrateAndShuffleSettingsFiles(settingsFile, prehistoricSettingsFile);
		
		assertTrue(settingsFile.exists());
		assertFalse(prehistoricSettingsFile.exists());
	}
}
