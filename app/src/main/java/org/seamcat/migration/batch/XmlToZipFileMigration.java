package org.seamcat.migration.batch;

import org.seamcat.loadsave.Constants;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.MigrationException;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class XmlToZipFileMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        try {
            tryMigrate(originalFile, migratedFile);
        }
        catch (Exception e) {
            throw new MigrationException("Failed to migrate XML batch to ZIP batch", e);
        }
    }

    public void tryMigrate(File originalFile, File migratedFile) throws Exception {
        Document parse = XmlUtils.parse(originalFile);

        Element element = parse.getDocumentElement();
        if ( element.getTagName().equals("BatchJobList") ) {
            createZipFileFromScenarioAndResultsFiles(migratedFile, parse);
        }
    }

    private void createZipFileFromScenarioAndResultsFiles(File migratedFile, Document original ) throws Exception {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(migratedFile)));

            ZipEntry scenarioEntry = new ZipEntry(Constants.BATCH_SCENARIO_ENTRY_NAME );
            zipOutputStream.putNextEntry(scenarioEntry);
            XmlUtils.write(original, zipOutputStream);
        }
        finally {
            IOUtils.closeQuietly(zipOutputStream);
        }
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return BatchFormatVersionConstants.PREHISTORIC;
    }
}
