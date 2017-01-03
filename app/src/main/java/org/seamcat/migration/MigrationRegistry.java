package org.seamcat.migration;

import java.util.HashMap;
import java.util.Map;


/** The registry keeps track of migrations based on original version, ie. 
 * a migration migrates from the original version to the version immediately after 
 * the original version.
 */
public class MigrationRegistry {
	
	private Map<FormatVersion, FileMigration> entryMap = new HashMap<FormatVersion, FileMigration>();

	/** Returns null if no migration found
	 */
	public FileMigration findMigration(FormatVersion originalVersion) {		
	   return entryMap.get(originalVersion);
   }
	
	public void registerMigration(FileMigration migration) {
		entryMap.put(migration.getOriginalVersion(), migration);
	}	
}
