package org.seamcat.presentation;

import javax.swing.*;
import java.awt.*;

public abstract class DisposableJPanel extends JPanel {

    public DisposableJPanel(){}

    public DisposableJPanel(LayoutManager layoutManager) {
        super(layoutManager);
    }

    public abstract void dispose();
}
