package org.seamcat.presentation.systems.cdma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class CDMASystemPlotPanel extends JPanel {

	private final DetailedSystemPlot cdmaplot;
	private final DrawingControlPanel controls;
	private final InspectSystemDetailsPanel infoPanel;
	private CDMASystemsComboBoxModel model;

	public CDMASystemPlotPanel( final Map<String, CDMAPlotModel> model, int eventNumber ) {
		super(new BorderLayout());

		this.model = new CDMASystemsComboBoxModel(model);
		
		cdmaplot = new DetailedSystemPlot();
		controls = new DrawingControlPanel(cdmaplot);
		infoPanel = new InspectSystemDetailsPanel(eventNumber);

		infoPanel.addSystemSelectionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actevent) {
				CDMAPlotModel model = (CDMAPlotModel) ((JComboBox) actevent.getSource()).getSelectedItem();
				if (model != null) {
					cdmaplot.setModel(model);
                    infoPanel.setDetailedSystemPlot(cdmaplot);
					cdmaplot.repaint();
				}
			}

		});

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cdmaplot,
		      infoPanel);
		sp.setOneTouchExpandable(true);
		sp.setDividerLocation(800);
		add(sp, BorderLayout.CENTER);
		add(controls, BorderLayout.NORTH);
		// add(infoPanel, BorderLayout.EAST);
	}

	public DetailedSystemPlot getPlot() {
		return cdmaplot;
	}

	public void reset() {
		cdmaplot.setModel(null);
		cdmaplot.repaint();
		infoPanel.reset();
	}

	public void setModel(CDMAPlotModel model) {
		cdmaplot.setModel(model);
		infoPanel.setDetailedSystemPlot(cdmaplot);
		controls.updateCheckBoxes();
	}

	public void refresh() {
		model.refresh();
		if (model.getSize() > 0) {
			setModel((CDMAPlotModel) model.getElementAt(0));
			infoPanel.setSystemSelectionModel(model);
		}
	}

	public void close() {
		model.close();
	}

	public void addRemoveBehaviour(Runnable runnable) {
		controls.addRemoveBehaviour( runnable );
	}
}
