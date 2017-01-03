package org.seamcat.presentation.components;

import javax.swing.*;

public class ScrollingBorderPanel extends BorderPanel {

    public ScrollingBorderPanel(JComponent decorated, String borderTitle) {
        super(new JScrollPane(decorated), borderTitle);
    }

    public ScrollingBorderPanel(JComponent decorated, String borderTitle, String infoText) {
        super(new JScrollPane(decorated), borderTitle, infoText);
    }

    public ScrollingBorderPanel(JComponent decorated, String borderTitle, String helpText, String helpLink ) {
        super(new JScrollPane(decorated), borderTitle, helpText, helpLink );
    }

    public ScrollingBorderPanel(JComponent decorated, String borderTitle, String helpText, String helpLink, String infoText){
        super(new JScrollPane(decorated), borderTitle, helpText, helpLink, infoText);
    }
}
