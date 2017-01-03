package org.seamcat.presentation.compareVector;

import org.seamcat.presentation.DisplaySignalPanel;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.propagationtest.PropagationHolder;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CompareVectorPanel extends JPanel {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private DisplaySignalPanel displaySignalPanel;
	private String unit = STRINGLIST.getString("COMPARE_VECTOR_DBM_LABEL");

	public CompareVectorPanel(EscapeDialog dialog) {
		setLayout(new BorderLayout());
		displaySignalPanel = new DisplaySignalPanel(dialog, "", "");
		displaySignalPanel.setSelectPanelTitle(STRINGLIST.getString("COMPARE_VECTOR_CHOOSE_TITLE"));
		displaySignalPanel.displayDataSelectionPanel(false);
		add(displaySignalPanel, BorderLayout.CENTER);
		displaySignalPanel.show(null, "", unit, -1, -1);
	}

    public void show(List<PropagationHolder> propagationHolders) {
        displaySignalPanel.show(propagationHolders, "", unit);
    }

    public void show(List<PropagationHolder> propagationHolders, String unit) {
        displaySignalPanel.show(propagationHolders, "", unit);
    }

    public void clearChart() {
        displaySignalPanel.reset();
    }
}
