package org.seamcat.presentation;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileFilters {
	public static final ExtensionFileFilter FILE_FILTER_BATCH                  = new ExtensionFileFilter("SBJ", "SEAMCAT Batch Files");
	public static final ExtensionFileFilter FILE_FILTER_BATCH_RESULT           = new ExtensionFileFilter("SBR", "SEAMCAT Batch Result Files");
	public static final ExtensionFileFilter FILE_FILTER_LIBRARY                = new ExtensionFileFilter("SLI", "SEAMCAT Library Files");
	public static final ExtensionFileFilter FILE_FILTER_WORKSPACE              = new ExtensionFileFilter("SWS", "SEAMCAT Workspace Files");
	public static final ExtensionFileFilter FILE_FILTER_WORKSPACE_RESULT       = new ExtensionFileFilter("SWR", "SEAMCAT Workspace Result Files");
	public static final ExtensionFileFilter FILE_FILTER_XSL                    = new ExtensionFileFilter("XSL", "Stylesheet Files (.xsl)");
    public static final ExtensionFileFilter FILE_FILTER_JAR                    = new ExtensionFileFilter("JAR", "Jar Files (.jar)");
    public static final ExtensionFileFilter FILE_FILTER_XML                    = new ExtensionFileFilter("XML", "XML Files (.xml)");
    public static final ExtensionFileFilter FILE_FILTER_HTML                   = new ExtensionFileFilter("HTML", "HTML Files (.html)");

    public static class ExtensionFileFilter extends FileFilter {
        private String extension;
        private String description;

        ExtensionFileFilter( String extension, String description ) {
            this.extension = extension;
            this.description = description;
        }

        @Override
        public boolean accept(File file) {
            if ( file == null ) return false;
            if ( file.isDirectory() ) return true;
            String name = file.getName();
            int i = name.lastIndexOf('.');
            return 0 < i && i < name.length() - 1 && name.substring(i + 1).equalsIgnoreCase(extension);
        }

        @Override
        public String getDescription() {
            return description;
        }

        public File align( File file ) {
            String ext = "." + extension.toLowerCase();
            if (!file.getName().toLowerCase().endsWith(ext) ) {
                return new File(file.getParentFile(), file.getName() + ext);
            }

            return file;
        }
    }
}
