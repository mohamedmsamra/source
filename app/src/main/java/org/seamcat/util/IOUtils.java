package org.seamcat.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;

import org.apache.log4j.Logger;


public class IOUtils {
	
	private final static Logger logger = Logger.getLogger(IOUtils.class);
	private static final int DEFAULT_BUFFER_SIZE = 1024*32;

	public static File createTempFile() {
		try {
	      File file = File.createTempFile("seamcat-", ".tmp");
	      file.deleteOnExit();
			return file;
      } catch (IOException ex) {
      	throw new RuntimeException("Failed to create temp file");
      }
   }

	public static File createTempDir() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File tmpSubDir = new File(tmpDir, "seamcat-" + UUID.randomUUID().toString());
		if (tmpSubDir.mkdir()) {
			tmpSubDir.deleteOnExit();
			return tmpSubDir;
		}
		else {
      	throw new RuntimeException("Failed to create temp directory");
      }
   }

	public static File copyResourceToTempFile(String resourceName) {
		File file = createTempFile();
		copyResourceToFile(resourceName, file);
		return file;
   }

	public static File copyResourceToTempFileWithSameName(String resourceName) {
		File dir = createTempDir();
		File file = new File(dir, lastComponentOfResourceName(resourceName));
		copyResourceToFile(resourceName, file);
		return file;
   }

	private static String lastComponentOfResourceName(String resourceName) {
		int lastSlashIndex = resourceName.lastIndexOf('/');
		if (lastSlashIndex == -1) {
			return resourceName;
		}
		else {
			return resourceName.substring(lastSlashIndex + 1);
		}
   }

	public static void copyResourceToFile(String resourceName, File file) {
	   InputStream resourceStream = IOUtils.class.getClassLoader().getResourceAsStream(resourceName);
		if (resourceStream == null) {
			throw new RuntimeException("Resource not found: " + resourceName); 
		}

		OutputStream fileStream;
      try {
	      fileStream = new FileOutputStream(file);
      } catch (FileNotFoundException ex) {
	      throw new RuntimeException("Failed to open file");
      }
		
      copyStream(resourceStream, fileStream);
		
      closeQuietly(resourceStream);
		closeQuietly(fileStream);
   }

	public static void copyStream(InputStream input, OutputStream output) {
		try {
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}			
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
   }

	public static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();			
		}
		catch (Exception e) {
			logger.warn("Failed to close closeable", e);
		}
   }

	public static void closeQuietly(ZipFile zipFile) {
		if (zipFile != null) {
			try {
				zipFile.close();
			} 
			catch (IOException e) {
				logger.warn("Failed to close ZipFile", e);				
			}
		}
	}

	public static void closeQuietly(XMLEventReader reader) {
		if (reader != null) {
			try {
				reader.close();
			} 
			catch (Exception e) {
				logger.warn("Failed to close XMLEventReader", e);				
			}
		}
	}

	public static void closeQuietly(XMLEventWriter writer) {
		if (writer != null) {
			try {
				writer.close();
			} 
			catch (Exception e) {
				logger.warn("Failed to close XMLEventWriter", e);
			}
		}
	}
}
