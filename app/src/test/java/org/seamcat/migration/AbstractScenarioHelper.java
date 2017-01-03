package org.seamcat.migration;

import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// This is probably not the best way to do this. Refactor
abstract public class AbstractScenarioHelper {

	public void findScenarioAndResults( File originalZipFile ) {
		try {
			ZipFile zipFile = new ZipFile( originalZipFile );

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while ( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				if ( entry.getName().equals( "scenario.xml" )) {
					Document document = XmlUtils.parse(zipFile.getInputStream(entry));
					migrateScenarioDocument(document);
				} else if ( entry.getName().equals("results.xml")) {
					Document document = XmlUtils.parse(zipFile.getInputStream(entry));
					migrateResultsDocument(document);
				}
			}
			zipFile.close();
		} catch (IOException e) {
			throw new MigrationException(e);
		}

	}

	abstract void migrateScenarioDocument( Document document );

	abstract void migrateResultsDocument( Document document );

}
