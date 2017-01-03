package org.seamcat.migration;

import org.apache.log4j.Logger;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FileMigrator {

	private static final Logger logger = Logger.getLogger(FileMigrator.class);
	
	private FormatVersion currentVersion;
	private VersionExtractor versionExtractor;
	private MigrationRegistry migrationRegistry;

	/** @return the file into which the migrated data was stored,
	 * or the original file if no migration took place.
	 */
	public File migrate(File file, List<MigrationIssue> migrationIssues) {
		return migrateToVersion(file, migrationIssues, currentVersion);
	}

	public File migrateToVersion(File originalFile, List<MigrationIssue> migrationIssues, FormatVersion targetVersion) {
		FormatVersion originalFileVersion = versionExtractor.extractVersion(originalFile);
		logger.info("File "+originalFile+" is at version "+originalFileVersion.getNumber());
		
		List<FileMigration> migrations = findMigrations(originalFileVersion, targetVersion);
		logger.debug("Found " + migrations.size() + " migrations from version " + originalFileVersion + " to " + targetVersion);
		
		if (migrations.size() > 0) {
            return runMigrations(originalFile, migrations, migrationIssues);
		}		
		else {
			return originalFile;
		}
   }

	private List<FileMigration> findMigrations(FormatVersion originalVersion, FormatVersion targetVersion) {
		if (originalVersion.equals(targetVersion)) {
			return Collections.emptyList();
		}
		else if (originalVersion.isLaterThan(targetVersion)) {
			throw new BackwardMigrationNotSupportedException("Can't migrate backwards");
		}
		else {
			List<FormatVersion> versionRange = FormatVersion.rangeOf(originalVersion, targetVersion);
			return findMigrationsForVersionRange(versionRange);
		}
   }

	private List<FileMigration> findMigrationsForVersionRange(List<FormatVersion> versionRange) {
	   List<FileMigration> migrations = new ArrayList<FileMigration>();
	   
	   for (int i=0; i<versionRange.size()-1; i++) {
	   	FileMigration migration = migrationRegistry.findMigration(versionRange.get(i));
	   	if (migration != null) {
	   		migrations.add(migration);
	   	}
	   	else {
	   		throw new MigrationException("No migration found between versions " + versionRange.get(i) + " and " + versionRange.get(i+1));
	   	}
	   }
	   
	   return migrations;
   }

	private File runMigrations(File originalFile, List<FileMigration> migrations, List<MigrationIssue> migrationIssues) {
      File currentFile = originalFile;
   	for (FileMigration migration: migrations) {
   		File nextFile = IOUtils.createTempFile();
   		logger.info("Running migration " + migration.getClass().getName());
   		logger.debug("(current: "+currentFile+", next: "+nextFile+")");
   		migration.migrate(currentFile, nextFile, migrationIssues);
   		if (currentFile != originalFile) {
   			currentFile.delete();
   		}
   		currentFile = nextFile;
   	}

        return currentFile;
   }

	public void setCurrentVersion(FormatVersion currentVersion) {
		this.currentVersion = currentVersion;
	}

	public void setVersionExtractor(VersionExtractor versionExtractor) {
		this.versionExtractor = versionExtractor;
	}

	public void setMigrationRegistry(MigrationRegistry migrationRegistry) {
		this.migrationRegistry = migrationRegistry;
	}		
}
