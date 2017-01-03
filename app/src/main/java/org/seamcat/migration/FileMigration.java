package org.seamcat.migration;

import org.seamcat.model.MigrationIssue;

import java.io.File;
import java.util.List;


public interface FileMigration {
	
	/** Migrate something in originalFile and put resulting migrated something
	 * in migratedFile. The migrator can assume that originalFile is in the original version,
	 * and must guarantee that the migrated file is in next version following the original version.
	 */
	public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues);
	
	/** The version which the migrator migrates from
	 */
	public FormatVersion getOriginalVersion();
}
