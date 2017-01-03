package org.seamcat.tabulardataio;

import org.seamcat.util.StringHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DelimiterSeparatedDataSaver implements TabularDataSaver {
	
	private String delimiter = "\t";
	private BufferedWriter writer;
    private int unflushedRows = 0;
	
	public DelimiterSeparatedDataSaver(File file) {
		try {
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addSheet(String title) {

	}

	@Override
	public void addRow(Object... data) {
		try {
			tryAddRow(data);
        }
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void tryAddRow(Object... data) throws IOException {
		for (int i=0; i<data.length; i++) {
			writer.write(StringHelper.objectToString(data[i]));
			if (i<data.length-1) {
				writer.write(delimiter);
			}			
		}
		writer.newLine();
        unflushedRows++;
        if ( unflushedRows > 2000 ) {
            flush();
            unflushedRows = 0;
        }
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
