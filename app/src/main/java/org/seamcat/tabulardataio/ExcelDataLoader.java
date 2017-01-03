package org.seamcat.tabulardataio;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ExcelDataLoader implements TabularDataLoader {

    private final FileInputStream stream;
	private Workbook workbook;
	private Sheet sheet;
	private int nextRowNumber = 0;

	public ExcelDataLoader(File file, Class<? extends Workbook> workbookImplementationClass) {
		try {
			stream = new FileInputStream(file);
			workbook = workbookImplementationClass.getConstructor(InputStream.class).newInstance(stream);
            sheet = workbook.getSheetAt(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public Object[] getRow() {
        Row row = sheet.getRow(nextRowNumber++);
        if ( row == null ) return null;
        int size = row.getLastCellNum();
        Object[] result = new Object[size];
        for ( int i=0; i<size; i++) {
            if ( row.getCell(i).getCellType() == Cell.CELL_TYPE_NUMERIC ) {
                result[i] = row.getCell(i).getNumericCellValue();
            } else {
                result[i] = row.getCell(i).getStringCellValue();
            }
        }
        return result;
    }

	@Override
	public void close() {
		try {
			stream.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
