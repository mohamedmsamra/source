package org.seamcat.tabulardataio;

public interface TabularDataSaver {
	void addSheet(String title);
	void addRow(Object... data);
	void close();
    void flush();
}
