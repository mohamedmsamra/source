package org.seamcat.presentation.systems.cdma;

import org.seamcat.presentation.SeamcatIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DrawingControlPanel extends JPanel {

    private final JButton bClose = new JButton(SeamcatIcons.getImageIcon("SEAMCAT_ICON_WORKSPACE_CLOSE"));
	private final JCheckBox plotActiveUsers = new JCheckBox("Users");
	private final JCheckBox plotAntennaPattern = new JCheckBox("Antenna Pattern");
	private final JCheckBox plotCellID = new JCheckBox("Cell ID#");
	private final JCheckBox plotConnectionLines = new JCheckBox(
	      "Connection Lines");
	private final JCheckBox plotDroppedUsers = new JCheckBox("Dropped Users");
	private final JCheckBox plotExternalInterferers = new JCheckBox(
	      "External Interferers");
	private final JCheckBox plotLegend = new JCheckBox("Legend");
	private final JCheckBox plotSizeOfActivelist = new JCheckBox(
	      "Size of Activelist");
	private final JCheckBox plotTips = new JCheckBox("Display tips");
	private final JCheckBox plotTransmitStats = new JCheckBox("TX Stats");
	private DetailedSystemPlot tp;

	public DrawingControlPanel(DetailedSystemPlot _tp) {
		super();

        add( new JLabel("Close tab"));
        add( bClose );
		this.add(new JLabel("Plot: "));
		tp = _tp;
		plotActiveUsers.setSelected(tp.isPlotUsers());
		plotActiveUsers.setBackground(Color.WHITE);
		plotActiveUsers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotUsers(plotActiveUsers.isSelected());
			}
		});
		this.add(plotActiveUsers);

		plotDroppedUsers.setSelected(tp.isPlotDroppedUsers());
		plotDroppedUsers.setBackground(Color.WHITE);
		plotDroppedUsers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotDroppedUsers(plotDroppedUsers.isSelected());
			}
		});
		this.add(plotDroppedUsers);

		plotConnectionLines.setSelected(tp.isPlotConnectionLines());
		plotConnectionLines.setBackground(Color.WHITE);
		plotConnectionLines.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotConnectionLines(plotConnectionLines.isSelected());
			}
		});
		this.add(plotConnectionLines);

		plotTransmitStats.setSelected(tp.isPlotTxStats());
		plotTransmitStats.setBackground(Color.WHITE);
		plotTransmitStats.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotTxStats(plotTransmitStats.isSelected());
			}
		});
		this.add(plotTransmitStats);

		plotAntennaPattern.setSelected(tp.isPlotAntennaPattern());
		plotAntennaPattern.setBackground(Color.WHITE);
		plotAntennaPattern.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotAntennaPattern(plotAntennaPattern.isSelected());
			}
		});
		this.add(plotAntennaPattern);

		plotSizeOfActivelist.setSelected(tp.isPlotSizeOfActiveList());
		plotSizeOfActivelist.setBackground(Color.WHITE);
		plotSizeOfActivelist.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotSizeOfActiveList(plotSizeOfActivelist.isSelected());
			}
		});
		this.add(plotSizeOfActivelist);

		final JCheckBox c7 = new JCheckBox("Cell Center");
		c7.setSelected(tp.isPlotCellCenter());
		c7.setBackground(Color.WHITE);
		c7.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotCellCenter(c7.isSelected());
			}
		});
		this.add(c7);

		plotExternalInterferers.setSelected(tp.isPlotExternalInterferers());
		plotExternalInterferers.setBackground(Color.WHITE);
		plotExternalInterferers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotExternalInterferers(plotExternalInterferers.isSelected());
			}
		});
		this.add(plotExternalInterferers);

		plotCellID.setSelected(tp.isPlotCellid());
		plotCellID.setBackground(Color.WHITE);
		plotCellID.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotCellid(plotCellID.isSelected());
			}
		});
		this.add(plotCellID);

		plotLegend.setSelected(tp.isPlotLegend());
		plotLegend.setBackground(Color.WHITE);
		plotLegend.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotLegend(plotLegend.isSelected());
			}
		});
		this.add(plotLegend);

		plotTips.setSelected(tp.isPlotHelp());
		plotTips.setBackground(Color.WHITE);
		plotTips.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tp.setPlotHelp(plotTips.isSelected());
			}
		});
		this.add(plotTips);

	}

	@Override
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
	}

	public void updateCheckBoxes() {
		if (tp != null) {
			plotActiveUsers.setSelected(tp.isPlotUsers());
			plotDroppedUsers.setSelected(tp.isPlotDroppedUsers());
			plotAntennaPattern.setSelected(tp.isPlotAntennaPattern());
			plotCellID.setSelected(tp.isPlotCellid());
			plotConnectionLines.setSelected(tp.isPlotConnectionLines());
			plotLegend.setSelected(tp.isPlotLegend());
			plotTips.setSelected(tp.isPlotHelp());
			plotTransmitStats.setSelected(tp.isPlotTxStats());

		}
	}

    public void addRemoveBehaviour(final Runnable runnable) {
        bClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        });
    }
}
