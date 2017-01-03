package org.seamcat.plugin;

import org.apache.log4j.Logger;
import org.seamcat.model.factory.Model;

import javax.xml.bind.DatatypeConverter;
import java.io.*;

import static java.lang.String.format;

public class PluginSerializerUtil {
	private static final Logger LOG = Logger.getLogger(PluginSerializerUtil.class);
	
	protected static String serializeJarFile(File jarFile) throws IOException {
		if (jarFile.exists()) {
			if (jarFile.canRead()) {
				InputStream iStream = new BufferedInputStream(new FileInputStream(jarFile));
				byte[] bytes = new byte[iStream.available()];
				iStream.read(bytes);
				iStream.close();

                return DatatypeConverter.printBase64Binary(bytes);
			}
			else {
				throw new IOException(format("Cannot read jar-file: %s", jarFile.getName()));
			}
		}
		else {
			throw new FileNotFoundException(jarFile.getName());
		}
	}

    protected static File deserializeJarFile(String jarData, String hash) throws IOException {
        File jarFile = new File(Model.getSeamcatTempDir() + File.separator + hash + ".jar");
        if ( jarFile.exists() ) return jarFile;
        if (LOG.isDebugEnabled()) {
            LOG.debug(format("De-serialising  pluginfile: %s", jarFile.getPath()));
        }

        OutputStream oStream = new BufferedOutputStream(new FileOutputStream(jarFile, false));
        oStream.write(DatatypeConverter.parseBase64Binary(jarData));
        oStream.flush();
        oStream.close();

        if (LOG.isDebugEnabled()) {
            LOG.debug("JarFile de-serialised and installed in classpath");
        }
        return jarFile;
    }
}
