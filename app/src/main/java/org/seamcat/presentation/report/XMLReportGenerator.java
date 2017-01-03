package org.seamcat.presentation.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class XMLReportGenerator {

    public static final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<report>";

    public static void generate( FileWriter writer, List<ReportSection> report ) {
        try{
            writer.write( header );
            for (ReportSection section : report) {
                writer.write("<section title=\""+section.getTitle()+"\">\n");
                for (ReportGroup group : section.getGroups()) {
                    writer.write("   <group title=\""+group.getTitle()+"\">\n");
                    for (ReportValue value : group.getValues()) {
                        String unit="";
                        if ( !value.getUnit().isEmpty() ) {
                            unit = " unit=\""+value.getUnit()+"\"";
                        }

                        // TODO: find a proper html escaper
                        writer.write("      <value name=\""+ value.getName().replace("<", "&lt;")+"\" " +
                                "value=\""+value.getValue()+"\""+unit+"/>");
                    }
                    writer.write("   </group>\n");
                }
                writer.write("</section>\n");
            }
            writer.write("</report>");
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
    }
}
