package org.seamcat.migration.workspace;

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


public class WorkspaceVersionExtractor extends VersionExtractor {

    public FormatVersion extractVersion(File file) {

        FileType fileType = determineFileType(file);

        switch (fileType) {
            case XML_FILE:
                return extractVersionFromXmlWorkspaceFile(file);
            case ZIP_FILE:
                return extractVersionFromZippedWorkspaceFile(file);
            default:
                throw new MigrationException("File type " + fileType + " not supported for " + file);
        }
    }

    private FormatVersion extractVersionFromXmlWorkspaceFile(File file) {
        try {
            return extractVersionFromScenarioDocumentInputStream(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static class WorkspaceVersionAttributes {
        String formatVersion;
        String seamcatVersion;
    }

    private FormatVersion extractVersionFromScenarioDocumentInputStream(InputStream inputStream) {
        WorkspaceVersionAttributes versionAttributes = extractVersionAttributesFromScenarioDocument(inputStream);

        if (versionAttributes.formatVersion != null) {
            return new FormatVersion(Integer.parseInt(versionAttributes.formatVersion));
        }
        else if (versionAttributes.seamcatVersion != null
                && !isPre323SeamcatVersionString(versionAttributes.seamcatVersion)) {
            return WorkspaceFormatVersionConstants.POST_3_2_3;
        }
        else if (versionAttributes.seamcatVersion != null
                && isPre323SeamcatVersionString(versionAttributes.seamcatVersion)) {
            return WorkspaceFormatVersionConstants.PRE_3_2_3;
        }
        else {
            return WorkspaceFormatVersionConstants.PREHISTORIC;
        }
    }

    private WorkspaceVersionAttributes extractVersionAttributesFromScenarioDocument(InputStream inputStream) {
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

    private WorkspaceVersionAttributes tryExtractVersionAttributesFromScenarioDocument(InputStream inputStream) throws XMLStreamException, FactoryConfigurationError {
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

            WorkspaceVersionAttributes result = new WorkspaceVersionAttributes();
            Attribute workspaceFormatVersionAttribute = documentElement.getAttributeByName(new QName("workspace_format_version"));
            result.formatVersion = workspaceFormatVersionAttribute != null ? workspaceFormatVersionAttribute.getValue() : null;
            Attribute seamcatVersionAttribute = documentElement.getAttributeByName(new QName("seamcat_version"));
            result.seamcatVersion = seamcatVersionAttribute != null ? seamcatVersionAttribute.getValue() : null;
            return result;
        }
        finally {
            IOUtils.closeQuietly(eventReader);
        }
    }

    public static boolean isPre323SeamcatVersionString(String versionString) {
        String[] versions = versionString.split(" ");
        boolean foundRevision = false;
        for ( String sub : versions ) {
            if ( sub.equals( "rev" ) ) {
                foundRevision = true;
            }
        }

        try {

            if ( foundRevision ) {
                int revision = Integer.parseInt( versions[ versions.length -1 ] );
                if ( revision <= 938 ) {
                    return true;
                }
            } else {
                // look for version number
                // do migration if version is less than 3.2.3 - rev938
                if ( versions.length >= 2) { //using format "SEAMCAT x.y.z"
                    String[] version = versions[1].split("[.]");
                    for ( int i=0; i<version.length; i++) {
                        int parseInt = Integer.parseInt( version[i] );
                        if ( i==0 && parseInt < 3) return true;
                        if ( i==1 && parseInt < 2) return true;
                        if ( i==2 && parseInt < 3) return true;
                    }
                }else{//using format "x.y.z"
                    String[] version = versions[0].split("[.]");
                    for ( int i=0; i<version.length; i++) {
                        int parseInt = Integer.parseInt( version[i] );
                        if ( i==0 && parseInt < 3) return true;
                        if ( i==1 && parseInt < 2) return true;
                        if ( i==2 && parseInt < 3) return true;
                    }
                }
            }
        } catch( ArrayIndexOutOfBoundsException e) {
            // ignore
        } catch (NumberFormatException e) {
            // ignore
        }
        return false;
    }

    private FormatVersion extractVersionFromZippedWorkspaceFile(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry scenarioEntry = zipFile.getEntry("scenario.xml");
            if (scenarioEntry == null) {
                throw new MigrationException("Scenario entry not found in workspace zip file");
            }
            InputStream scenarioInputStream = zipFile.getInputStream(scenarioEntry);
            return extractVersionFromScenarioDocumentInputStream(scenarioInputStream);
        }
        catch (Exception e) {
            throw new MigrationException("Failed extracting version from workspace zip file: " + file, e);
        }
        finally {
            IOUtils.closeQuietly(zipFile);
        }
    }
}
