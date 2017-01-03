package org.seamcat.presentation;

public class Argument {

    private String title, value, unit;

    public Argument(String title, String value, String unit) {
        this.title = title;
        this.value = value;
        this.unit = unit;
    }

    public String getTitle() {
        return title;
    }
    public String getValue() {
        return value;
    }
    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return title + ": " + value+ " (" + unit + " )";
    }

    public String toHTMLTableRow() {
        StringBuilder sb = new StringBuilder("<tr>");
        sb.append("<td>").append(title).append("</td>");
        sb.append("<td>").append(value).append("</td>");
        sb.append("<td>").append(unit).append("</td>");
        sb.append("</tr>");
        return sb.toString();
    }
}
