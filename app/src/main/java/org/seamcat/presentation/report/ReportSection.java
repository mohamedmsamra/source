package org.seamcat.presentation.report;

import java.util.ArrayList;
import java.util.List;

public class ReportSection {

    private String title;
    private List<ReportGroup> groups;

    public ReportSection(String title) {
        this.title = title;
        groups = new ArrayList<>();
    }

    public void addGroup(ReportGroup group) {
        groups.add(group);
    }

    public List<ReportGroup> getGroups() {
        return groups;
    }

    public String getTitle() {
        return title;
    }
}
