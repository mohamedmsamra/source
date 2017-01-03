package org.seamcat.tabulardataio;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.seamcat.util.StringHelper;

import java.io.File;
import java.io.FileOutputStream;

public class ExcelDataSaver implements TabularDataSaver {
	
	private File file;
	private Workbook workbook;
	private Sheet sheet;
	private int nextRowNumber = 0;

	public ExcelDataSaver(File file, Class<? extends Workbook> workbookImplementationClass) {
		try {
			this.file = file; 
			this.workbook = workbookImplementationClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addSheet(String title) {
		sheet = workbook.createSheet( title );
        nextRowNumber = 0;
	}

	@Override
	public void addRow(Object... data) {
		if ( sheet == null ) {
			sheet = workbook.createSheet();
		}
		Row row = sheet.createRow(nextRowNumber++);
		for (int i=0; i<data.length; i++) {
			Cell cell = row.createCell(i);
			if (data[i] instanceof Number) {
				cell.setCellValue(((Number) data[i]).doubleValue());
			}
			else {
				cell.setCellValue(StringHelper.objectToString(data[i]));
			}
		}
	}

	@Override
	public void close() {
		try {
			workbook.write(new FileOutputStream(file));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public void flush() {

    }
}
