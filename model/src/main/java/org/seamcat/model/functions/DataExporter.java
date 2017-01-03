package org.seamcat.model.functions;

public interface DataExporter {

    DataFile chooseFile();

    DataFile chooseFile( String fileName );
}
