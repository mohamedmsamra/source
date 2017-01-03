package org.seamcat.model;

public class MigrationIssue {

    private String message;

    public MigrationIssue( String message ) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
