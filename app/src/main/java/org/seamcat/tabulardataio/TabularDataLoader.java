package org.seamcat.tabulardataio;

public interface TabularDataLoader {
	Object[] getRow();
    void close();
}
