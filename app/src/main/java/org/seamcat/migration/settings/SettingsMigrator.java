package org.seamcat.migration.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.seamcat.migration.FileMigrator;
import org.seamcat.migration.MigrationException;
import org.seamcat.model.MigrationIssue;


public class SettingsMigrator extends FileMigrator {

	private static final Logger logger = Logger.getLogger(SettingsMigrator.class);
	
	public SettingsMigrator() {
		setCurrentVersion(SettingsFormatVersionConstants.CURRENT_VERSION);
		setVersionExtractor(new SettingsVersionExtractor());
		setMigrationRegistry(new SettingsMigrationRegistry());
	}
	
	public void migrateAndShuffleSettingsFiles(File settingsFile, File prehistoricSettingsFile) {
		if (!settingsFile.exists() && prehistoricSettingsFile.exists()) {
			logger.info("Copying prehistoric settings file to new name");
			copyFile(prehistoricSettingsFile, settingsFile);
		}
		
		if (settingsFile.exists()) {
			File migratedSettingsFile = migrate(settingsFile, new ArrayList<MigrationIssue>());
			if (!migratedSettingsFile.equals(settingsFile)) {
				replaceWithMigratedFile(migratedSettingsFile, settingsFile);
			}
		}
   }

	private void copyFile(File file, File destination) {
		try {
	      FileUtils.copyFile(file, destination);
      } catch (IOException e) {
      	throw new MigrationException("Failed to copy file", e);
      }
   }

	private void replaceWithMigratedFile(File migratedFile, File originalFile) {
		try {
			File tempBackupFile = new File(originalFile.getAbsolutePath() + ".bak");
			FileUtils.moveFile(originalFile, tempBackupFile);
	      FileUtils.moveFile(migratedFile, originalFile);
	      FileUtils.deleteQuietly(tempBackupFile);
      } catch (IOException e) {
      	throw new MigrationException("Failed to move migrated file", e);
      }
   }
}
