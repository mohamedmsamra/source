package org.seamcat.presentation.report;

import java.util.ArrayList;
import java.util.List;

public class ReportGroup {

    private String title;
    private List<ReportValue> values;

    public ReportGroup(String title) {
        this.title = title;
        values = new ArrayList<>();
    }

    public void addValue( String name, Object value ) {
        values.add(new ReportValue(name, value, ""));
    }

    public void addValue( String name, Object value, String unit ) {
        values.add( new ReportValue(name, value, unit));
    }


    public String getTitle() {
        return title;
    }

    public List<ReportValue> getValues() {
        return values;
    }
}
