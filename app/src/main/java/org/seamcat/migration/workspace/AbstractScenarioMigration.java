package org.seamcat.migration.workspace;

import org.apache.commons.io.IOUtils;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.MigrationException;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

abstract public class AbstractScenarioMigration implements FileMigration {

    private List<MigrationIssue> migrationIssues;

    protected List<MigrationIssue> getMigrationIssues() {
        return migrationIssues;
    }

	@Override
	public void migrate(File originalZipFile, File migratedZipFile, List<MigrationIssue> migrationIssues) {
		this.migrationIssues = migrationIssues;
        try {
			ZipFile zipFile = new ZipFile( originalZipFile );

			ZipOutputStream resultStream = new ZipOutputStream( new FileOutputStream(migratedZipFile));
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while ( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				resultStream.putNextEntry( new ZipEntry( entry.getName() ) );
				if ( entry.getName().equals( "scenario.xml" )) {
					Document document = XmlUtils.parse(zipFile.getInputStream(entry));
					migrateScenarioDocument(document);
					XmlUtils.write( document, resultStream );
				} else if ( entry.getName().equals("results.xml")) {
					Document document = XmlUtils.parse(zipFile.getInputStream(entry));
					migrateResultsDocument(document);
					XmlUtils.write( document, resultStream );
				} else {
					IOUtils.copy( zipFile.getInputStream( entry), resultStream);
				}
			}
			resultStream.close();
			zipFile.close();
		} catch (IOException e) {
			throw new MigrationException(e);
		}

	}
	
	abstract void migrateScenarioDocument( Document document );

	abstract void migrateResultsDocument( Document document );
}
