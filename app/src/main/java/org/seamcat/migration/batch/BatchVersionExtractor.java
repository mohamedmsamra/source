package org.seamcat.migration.batch;

import org.seamcat.loadsave.Constants;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.MigrationException;
import org.seamcat.migration.VersionExtractor;
import org.seamcat.util.IOUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class BatchVersionExtractor extends VersionExtractor {

    public static final String BATCH_FORMAT_VERSION = "batch_format_version";

    public FormatVersion extractVersion(File file) {

        FileType fileType = determineFileType(file);

        switch (fileType) {
            case XML_FILE:
                return extractVersionFromXmlFile(file);
            case ZIP_FILE:
                return extractVersionFromZippedFile(file);
            default:
                throw new MigrationException("File type " + fileType + " not supported for " + file);
        }
    }

    private FormatVersion extractVersionFromXmlFile(File file) {
        try {
            return extractVersionAttributesFromScenarioDocument(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private FormatVersion extractVersionAttributesFromScenarioDocument(InputStream inputStream) {
        try {
            return tryExtractVersionAttributesFromScenarioDocument(inputStream);
        }
        catch (XMLStreamException e) {
            throw new MigrationException(e);
        }
        catch (FactoryConfigurationError e) {
            throw new MigrationException(e);
        }
    }

    private FormatVersion tryExtractVersionAttributesFromScenarioDocument(InputStream inputStream) throws XMLStreamException, FactoryConfigurationError {
        XMLEventReader eventReader = null;
        try {
            eventReader = XMLInputFactory.newFactory().createXMLEventReader(inputStream);
            while (eventReader.peek() != null && !eventReader.peek().isStartElement()) {
                eventReader.next();
            }

            if (eventReader.peek() == null) {
                throw new MigrationException("Document element not found");
            }

            StartElement documentElement = eventReader.peek().asStartElement();

            Attribute version = documentElement.getAttributeByName(new QName( BATCH_FORMAT_VERSION ));
            if ( version == null ) return BatchFormatVersionConstants.PREHISTORIC;

            return new FormatVersion(Integer.parseInt( version.getValue()));

        }
        finally {
            IOUtils.closeQuietly(eventReader);
        }
    }

    private FormatVersion extractVersionFromZippedFile(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry scenarioEntry = zipFile.getEntry(Constants.BATCH_SCENARIO_ENTRY_NAME );
            if (scenarioEntry == null) {
                throw new MigrationException("Scenario entry not found in workspace zip file");
            }
            InputStream scenarioInputStream = zipFile.getInputStream(scenarioEntry);
            return extractVersionAttributesFromScenarioDocument(scenarioInputStream);
        }
        catch (Exception e) {
            throw new MigrationException("Failed extracting version from workspace zip file: " + file, e);
        }
        finally {
            IOUtils.closeQuietly(zipFile);
        }
    }
}
