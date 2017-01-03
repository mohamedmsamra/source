package org.seamcat.presentation.library;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.presentation.systems.cdma.CDMALinkLevelDataEditorPanel;

import javax.swing.*;
import java.awt.*;

public class LinkLevelDataDetailPanel extends JPanel {

	public LinkLevelDataDetailPanel(JFrame parent, CDMALinkLevelData model) {
		setLayout(new BorderLayout());
        CDMALinkLevelDataEditorPanel detailPanel = new CDMALinkLevelDataEditorPanel(parent);
        detailPanel.setModel( model );
        add( detailPanel, BorderLayout.CENTER);
	}
	
}