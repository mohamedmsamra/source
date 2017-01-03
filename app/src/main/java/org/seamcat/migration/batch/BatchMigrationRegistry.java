package org.seamcat.migration.batch;

import org.seamcat.migration.MigrationRegistry;

public class BatchMigrationRegistry extends MigrationRegistry {

    public BatchMigrationRegistry() {
		registerMigration(new XmlToZipFileMigration());
        registerMigration(new RemoveCorrelationsBatchMigration());
        registerMigration(new ConfigurationResultsBatchMigration());
	}
}
