package org.seamcat.presentation.components;

import org.seamcat.events.RSSEvent;
import org.seamcat.model.Workspace;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class EventStatisticsPanel extends JPanel {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
	private static final String unitdBm = " dBm";

	private JLabel blockingMedianValue,blockingMeanDBmValue,blockingStdDValue,dRSSMedianValue,dRSSMeanDBmValue,dRSSStdDValue;
	private JLabel unwantedEmissionMedianValue,unwantedEmissionMeanDBmValue,unwantedEmissionStdDValue;
    private JLabel dRSSTitle, unwantedTitle, blockingTitle;

    private Workspace workspace;

	public EventStatisticsPanel( Workspace workspace ) {
		this.workspace = workspace;
        this.setLayout(new GridLayout(5, 4));

		dRSSMedianValue = new JLabel("0.0" + unitdBm, SwingConstants.RIGHT);
		dRSSMeanDBmValue = new JLabel("0.0" + unitdBm, SwingConstants.RIGHT);
		dRSSStdDValue = new JLabel("0.0", SwingConstants.RIGHT);

        unwantedEmissionMedianValue = new JLabel("0.0" + unitdBm, SwingConstants.RIGHT);
		unwantedEmissionMeanDBmValue = new JLabel("0.0" + unitdBm, SwingConstants.RIGHT);
		unwantedEmissionStdDValue = new JLabel("0.0", SwingConstants.RIGHT);

		blockingMedianValue = new JLabel("0.0" + unitdBm, SwingConstants.RIGHT);
		blockingMeanDBmValue = new JLabel("0.0" + unitdBm, SwingConstants.RIGHT);
		blockingStdDValue = new JLabel("0.0", SwingConstants.RIGHT);

		JLabel lbMeanDBm = new JLabel("Mean", SwingConstants.RIGHT);
		JLabel lbMedian = new JLabel("Median", SwingConstants.RIGHT);
		JLabel lbStddev = new JLabel("StdDev", SwingConstants.RIGHT);
		
		lbMeanDBm.setFont(dRSSMedianValue.getFont().deriveFont(Font.BOLD));
		dRSSMeanDBmValue.setFont(dRSSMedianValue.getFont().deriveFont(Font.BOLD));
		unwantedEmissionMeanDBmValue.setFont(dRSSMedianValue.getFont().deriveFont(Font.BOLD));
		blockingMeanDBmValue.setFont(dRSSMedianValue.getFont().deriveFont(Font.BOLD));

		add(Box.createGlue());
		add(lbMeanDBm);
		add(lbMedian);
		add(lbStddev);

        dRSSTitle = new JLabel(STRINGLIST.getString("SIMULATION_STATUS_DRSS"));
		add(dRSSTitle);
		add(dRSSMeanDBmValue);
		add(dRSSMedianValue);
		add(dRSSStdDValue);

        unwantedTitle = new JLabel(STRINGLIST.getString("SIMULATION_STATUS_IRSS_UNWANTED"));
        add(unwantedTitle);
		add(unwantedEmissionMeanDBmValue);
		add(unwantedEmissionMedianValue);
		add(unwantedEmissionStdDValue);

        blockingTitle = new JLabel(STRINGLIST.getString("SIMULATION_STATUS_IRSS_BLOCKING"));
        add(blockingTitle);
		add(blockingMeanDBmValue);
		add(blockingMedianValue);
		add(blockingStdDValue);
	}

	public void startingEventGeneration(Workspace _workspace) {
		this.workspace = _workspace;
        if(!workspace.getVictimSystemLink().isDMASystem()){
            dRSSTitle.setText(STRINGLIST.getString("SIMULATION_STATUS_DRSS"));
            unwantedTitle.setText(STRINGLIST.getString("SIMULATION_STATUS_IRSS_UNWANTED"));
            blockingTitle.setText(STRINGLIST.getString("SIMULATION_STATUS_IRSS_BLOCKING"));
        }else{
            dRSSTitle.setText(STRINGLIST.getString("SIMULATION_STATUS_DRSS_DMA"));
            unwantedTitle.setText(STRINGLIST.getString("SIMULATION_STATUS_IRSS_UNWANTED_DMA"));
            blockingTitle.setText(STRINGLIST.getString("SIMULATION_STATUS_IRSS_BLOCKING_DMA"));
        }
    }

    public void handle(RSSEvent event) {
        if ( event.getContext() == workspace ) {
            dRSSMedianValue.setText(event.getRss().getMedian());
            dRSSMeanDBmValue.setText(event.getRss().getMean());
            dRSSStdDValue.setText(event.getRss().getStdDev());

            unwantedEmissionMedianValue.setText(event.getIrssU().getMedian());
            unwantedEmissionMeanDBmValue.setText(event.getIrssU().getMean());
            unwantedEmissionStdDValue.setText(event.getIrssU().getStdDev());

            blockingMedianValue.setText(event.getIrssB().getMedian());
            blockingMeanDBmValue.setText(event.getIrssB().getMean());
            blockingStdDValue.setText(event.getIrssB().getStdDev());
        }
    }
}
