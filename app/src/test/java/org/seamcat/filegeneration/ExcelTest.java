package org.seamcat.filegeneration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Ignore;
import org.junit.Test;


public class ExcelTest {

    @Ignore
	@Test
	public void testGenerateExcelFile() throws IOException {
		HSSFWorkbook wb =  new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet( "SheetName" );
		
		HSSFRow row = sheet.createRow( 0 );
		row.createCell(0).setCellValue( "Hello" );
	
		FileOutputStream stream = new FileOutputStream( "test.xls" );
		wb.write( stream );
		stream.close();
	}

    @Ignore
	@Test
	public void testLoadExcelFile() throws IOException {
		FileInputStream stream = new FileInputStream( "test.xls" );
		
		HSSFWorkbook wb =  new HSSFWorkbook( stream );
		HSSFSheet sheet = wb.getSheetAt( 0 );
		
		HSSFRow row = sheet.getRow( 0 );
		HSSFCell cell = row.getCell( 0 );
		Assert.assertEquals( "Hello", cell.getStringCellValue() );
	}

}
