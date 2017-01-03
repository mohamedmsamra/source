package org.seamcat.presentation.components;

import javax.swing.*;

public class BorderPanelBuilder {

    private JComponent component;
    private String title;
    private String helpText, helpLink;
    private String infoText;

    public BorderPanelBuilder(JComponent component, String title) {
        this.component = component;
        this.title = title;
    }

    public BorderPanelBuilder help( String helpText, String helpLink ) {
        this.helpText = helpText;
        this.helpLink = helpLink;
        return this;
    }

    public BorderPanelBuilder info( String infoText ) {
        this.infoText = infoText;
        return this;
    }

    public BorderPanel build() {
        if ( info() && help() ) {
            return new BorderPanel(component, title, helpText, helpLink, infoText);
        }

        if ( info() ) {
            return new BorderPanel(component, title, infoText);
        }

        if ( help() ) {
            return new BorderPanel(component, title, helpText, helpLink);
        }

        return new BorderPanel(component, title);

    }

    private boolean info() {
        return infoText != null;
    }

    private boolean help() {
        return helpText != null;
    }
}
