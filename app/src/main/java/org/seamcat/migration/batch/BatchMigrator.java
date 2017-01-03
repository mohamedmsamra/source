package org.seamcat.migration.batch;


import org.seamcat.migration.FileMigrator;

public class BatchMigrator extends FileMigrator {

    public BatchMigrator() {
        setCurrentVersion(BatchFormatVersionConstants.CURRENT_VERSION);
        setVersionExtractor(new BatchVersionExtractor());
        setMigrationRegistry(new BatchMigrationRegistry());
    }

}
