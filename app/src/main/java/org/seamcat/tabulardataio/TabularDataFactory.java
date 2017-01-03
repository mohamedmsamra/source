package org.seamcat.tabulardataio;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class TabularDataFactory {

    private static final FileFormat TAB_SEPARATED_TEXT = new FileFormat("Tab separated text", "txt");
    private static final FileFormat XSL_SPREADSHEET = new FileFormat("XLS spreadsheet", "xls");
    private static final FileFormat XSLX_SPREADSHEET = new FileFormat("XLSX spreadsheet", "xlsx");

    public static Collection<FileFormat> allFormats = Arrays.asList(TAB_SEPARATED_TEXT, XSL_SPREADSHEET, XSLX_SPREADSHEET);


    public static TabularDataSaver newSaverForFile(File file) {
		FileFormat fileFormat = findByFile(file);
		if (TAB_SEPARATED_TEXT.equals(fileFormat)) {
			return new DelimiterSeparatedDataSaver(file);
		}
		else if (XSL_SPREADSHEET.equals(fileFormat)) {
			return new ExcelDataSaver(file, HSSFWorkbook.class);
		}
		else if (XSLX_SPREADSHEET.equals(fileFormat)) {
			return new ExcelDataSaver(file, XSSFWorkbook.class);
		}
		else { 
			throw new RuntimeException("No saver found for file "+file);
		}
	}

    public static TabularDataLoader newLoaderForFile(File file) {
        FileFormat fileFormat = findByFile(file);
        if (TAB_SEPARATED_TEXT.equals(fileFormat)) {
            return new DelimiterSeparatedDataLoader(file);
        }
        else if (XSL_SPREADSHEET.equals(fileFormat)) {
            return new ExcelDataLoader(file, HSSFWorkbook.class);
        }
        else if (XSLX_SPREADSHEET.equals(fileFormat)) {
            return new ExcelDataLoader(file, XSSFWorkbook.class);
        }
        else {
            throw new RuntimeException("Unknown file format. Cannot load file: "+file);
        }
    }


    public static Collection<FileFormat> allFormats() {
        return allFormats;
    }

    public static FileFormat findByFile(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return findByExtension(extension);
    }

    private static FileFormat findByExtension(String extension) {
        for (FileFormat fileFormat: allFormats) {
            if (fileFormat.getExtension().equals(extension)) {
                return fileFormat;
            }
        }

        return null;
    }

}
