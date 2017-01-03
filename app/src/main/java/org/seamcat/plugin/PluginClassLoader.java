package org.seamcat.plugin;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/** Class loader for plugins. Works together with the SandboxedPluginSecurityPolicy
 * to sandbox plugin code loaded by this class loader.
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger logger = Logger.getLogger(PluginClassLoader.class);

    public PluginClassLoader(File jarFile) throws MalformedURLException {
        super(new URL[]{jarFile.toURI().toURL()});
    }

    public Class<?> verifySandboxAndLoadClass(String name) throws ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading plugin class: " + name);
        }
        SandboxInitializer.verifySandbox();
        return loadClass(name);
    }

    @Override
    public String toString() {
        return getURLs()[0].toString();
    }
}
