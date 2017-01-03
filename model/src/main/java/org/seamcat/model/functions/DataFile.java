package org.seamcat.model.functions;

public interface DataFile {

    void addRow(String... data );

    void addRow(Number... data);

    void close();
}
