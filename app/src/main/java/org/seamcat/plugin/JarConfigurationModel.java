package org.seamcat.plugin;

import org.apache.log4j.Logger;
import org.seamcat.exception.SeamcatErrorException;
import org.seamcat.function.MutableLibraryItem;
import org.seamcat.marshalling.Md5;
import org.seamcat.model.types.result.DescriptionImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.String.format;

public class JarConfigurationModel extends MutableLibraryItem {

    private static final Logger LOG = Logger.getLogger(JarConfigurationModel.class);

    // MD5 hash of the jarData
    private String hash;
    private String jarData;
    private List<PluginClass> pluginClasses;
    private PluginClassLoader classLoader;

    protected JarConfigurationModel() {
    }

    public JarConfigurationModel( File jarFile) {
        setDescription( new DescriptionImpl(jarFile.getName(), "") );
        try {
            setJarData( PluginSerializerUtil.serializeJarFile(jarFile) );
            classLoader = new PluginClassLoader(jarFile);
            installPluginClasses( jarFile );
        } catch (IOException e) {
            throw new RuntimeException("Could not install jar file", e);
        }
    }

    public JarConfigurationModel( String base64JarData, String name) {
        setJarData( base64JarData );
        setDescription( new DescriptionImpl(name, "") );
        try {
            File jarFile = PluginSerializerUtil.deserializeJarFile(base64JarData, getHash());
            classLoader = new PluginClassLoader(jarFile);
            installPluginClasses( jarFile );
        } catch (IOException e) {
            throw new RuntimeException("Could not install jar file", e);
        }
    }

    private void installPluginClasses( File jarFile ) {
        pluginClasses = new ArrayList<PluginClass>();
        try {
            JarFile jarFileHandle = new JarFile(jarFile);
            Enumeration<JarEntry> list = jarFileHandle.entries();
            while (list.hasMoreElements()) {
                JarEntry f = list.nextElement();
                String name = f.getName();
                if ( name.endsWith(".class")) {
                    try {
                        Class<?> aClass = loadClassDef(name.substring(0, name.length() - ".class".length()).replaceAll("/", "."));
                        pluginClasses.add(new PluginClass(this, aClass));
                    } catch (RuntimeException re ) {
                        // ignore not of correct type
                    } catch (NoClassDefFoundError e) {
                        LOG.info( "Error loading class '"+ name +"'. Will be skipped");
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private Class<?> loadClassDef(String classname) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(format("Loading plugin class: %s", classname));
        }

        Class<?> c;
        try {
            c = classLoader.verifySandboxAndLoadClass(classname);
        }
        catch (ClassNotFoundException e) {
            throw new SeamcatErrorException(String.format("Error encountered while reading plugin definition (%s). It will be skipped", e.getMessage()));
        }
        catch (UnsupportedClassVersionError e) {
            String version = System.getProperty("java.version");
            String message = String.format("Could not load class: '%s'. SEAMCAT is running java version %s and the plugin is most likely built with a newer java version", classname, version);
            throw new SeamcatErrorException(message);
        }

        return c;
    }

    public PluginClass getPluginClass( String classname ) {
        for (PluginClass pc : pluginClasses) {
            if ( pc.getClassName().equals(classname)) {
                return pc;
            }
        }

        throw new RuntimeException("no such class in jar file " + classname);
    }

    public Class<?> loadClass( String classname ) {
        return loadClassDef( classname );
    }


    public String getJarData() {
        return jarData;
    }

    private void setJarData(String jarData) {
        this.jarData = jarData;
        hash = Md5.md5( jarData );
    }

    protected void setHash( String hash ) {
        this.hash = hash;
    }

    public List<PluginClass> getPluginClasses() {
        return pluginClasses;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JarConfigurationModel)) return false;

        JarConfigurationModel other = (JarConfigurationModel) obj;
        return hash.equals( other.hash );
    }
}
