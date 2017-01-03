package org.seamcat.migration.batch;

import org.seamcat.loadsave.Constants;
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

	@Override
	public void migrate(File originalZipFile, File migratedZipFile, List<MigrationIssue> migrationIssues) {
		try {
			ZipFile zipFile = new ZipFile( originalZipFile );

			ZipOutputStream resultStream = new ZipOutputStream( new FileOutputStream(migratedZipFile));
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while ( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				resultStream.putNextEntry( new ZipEntry( entry.getName() ) );
				if ( entry.getName().equals( Constants.BATCH_SCENARIO_ENTRY_NAME )) {
					Document document = XmlUtils.parse(zipFile.getInputStream(entry));
					migrateScenarioDocument(document);
					XmlUtils.write( document, resultStream );
				} else {
					Document document = XmlUtils.parse(zipFile.getInputStream(entry));
					migrateResultsDocument(document);
					XmlUtils.write( document, resultStream );
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
