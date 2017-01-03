package org.seamcat.migration;

import org.seamcat.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class VersionExtractor {

    protected enum FileType {XML_FILE, ZIP_FILE }

    private static final String XML_PROLOG_SIGNATURE_STRING = "<?xml";
    private static final byte[] ZIP_HEADER_SIGNATURE_BYTES = new byte[] { 0x50, 0x4b, 0x03, 0x04 };

    public abstract FormatVersion extractVersion(File file);

    protected FileType determineFileType(File file) {
        if (readFirstChars(file, XML_PROLOG_SIGNATURE_STRING.length()).equals(XML_PROLOG_SIGNATURE_STRING)) {
            return FileType.XML_FILE;
        }
        else if (Arrays.equals(readFirstBytes(file, ZIP_HEADER_SIGNATURE_BYTES.length), ZIP_HEADER_SIGNATURE_BYTES)) {
            return FileType.ZIP_FILE;
        }
        else {
            throw new MigrationException("File type not recognized for " + file);
        }
    }

    private byte[] readFirstBytes(File file, int i) {
        byte[] buffer = new byte[i];
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(buffer);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
        return buffer;
    }

    private Object readFirstChars(File file, int i) {
        char[] buffer = new char[i];
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            new InputStreamReader(inputStream, Charset.forName("latin1")).read(buffer);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
        return new String(buffer);
    }


}