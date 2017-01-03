package org.seamcat.presentation.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HTMLReportGenerator {
    public static final String header = "<html>\n<style>\nbody {font-family: \"Comic Sans MS\"; }\ntd {font-size: 16; white-space: nowrap;}\n"+
            "</style>\n<head><title>SEAMCAT Simulation Report</title></head>\n"+
            "<body><table width='100%' style='background-color:#6699CC'>\n"+
            "<tr><td><font color='#FFFFFF' size=16'><b>SEAMCAT</b> <sup>&#174;</sup> Simulation Report<br/></font></td></tr>\n"+
		    "</table></body>";

    public static final String footer = "</html>";

    public static void generate( FileWriter writer, List<ReportSection> report ) {
        try{
            writer.write( header );
            writer.write( "<br><br>\n\n");

            writer.write("<table width=\"100%\">\n");
            for (ReportSection section : report) {
                writeHRow(writer, section.getTitle(), "", "", "");
                for (ReportGroup group : section.getGroups()) {
                    writeRow(writer, group.getTitle(), "", "", "");
                    for (ReportValue value : group.getValues()) {
                        writeRow(writer, "", value.getName(), ""+value.getValue(), value.getUnit());
                    }
                }
            }
            writer.write(footer);
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
    }

    private static void writeHRow( FileWriter writer, String col1, String col2, String col3, String col4) throws IOException {
        writer.write("<tr><th>"+col1+"</th><td>"+col2+"</td><td>"+col3+"</td><td>"+col4+"</td></tr>\n");
    }

    private static void writeRow( FileWriter writer, String col1, String col2, String col3, String col4) throws IOException {
        writer.write("<tr><td>"+col1+"</td><td>"+col2+"</td><td>"+col3+"</td><td>"+col4+"</td></tr>\n");
    }
}
