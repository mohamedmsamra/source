package org.seamcat.presentation.report;

import org.seamcat.tabulardataio.TabularDataSaver;

import java.util.List;

public class ExcelReportGenerator {

    public static void generate( TabularDataSaver saver, List<ReportSection> report ) {
        for (ReportSection section : report) {
            saver.addSheet( section.getTitle() );

            for (ReportGroup group : section.getGroups()) {
                saver.addRow( group.getTitle());
                for (ReportValue value : group.getValues()) {
                    saver.addRow( value.getName(), value.getValue(), value.getUnit() );
                }
            }
        }
    }
}
