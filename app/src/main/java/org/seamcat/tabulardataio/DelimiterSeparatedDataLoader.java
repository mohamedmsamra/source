package org.seamcat.tabulardataio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DelimiterSeparatedDataLoader implements TabularDataLoader {

	private BufferedReader reader;

	public DelimiterSeparatedDataLoader(File file) {
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public Object[] getRow() {
        try {
            String line = reader.readLine();
            if ( line == null ) return null;
            String[] split = line.split("\t");
            Object[] result = new Object[split.length];
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                try {
                    result[i] = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    result[i] = s;
                }
            }
            return result;
        } catch (IOException e) {
            return null;
        }
    }

	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
