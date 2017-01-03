package org.seamcat.presentation.components.interferencecalc;

import org.seamcat.model.Workspace;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.presentation.components.InterferenceCalculationsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ICEControlPanel extends JPanel {

	private static final String imageUrlPrefix = "/org/seamcat/presentation/resources/";

	private int currentIndex;
	private JButton deleteButton = new JButton("Delete");
	private JButton firstButton = new JButton("First");
	private JButton lastButton = new JButton("Last");
	private JButton nextButton = new JButton("Next");
	private InterferenceCalculationsPanel parent;
	private JButton previousButton = new JButton("Previous");

	private JButton startButton = new JButton("Start");

	private JLabel statusText = new JLabel("No stored calculations");
	private JButton stopButton = new JButton("Stop");
	private Workspace workspace;

	public ICEControlPanel(InterferenceCalculationsPanel _parent) {
		super();
		parent = _parent;
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		startButton.setEnabled(true);
		stopButton.setEnabled(false);

		startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "Play16.gif")));
		stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "Stop.gif")));

		firstButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "First.gif")));
		previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "Previous.gif")));
		nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "Next.gif")));
		lastButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "Last.gif")));
		deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		      imageUrlPrefix + "Delete.gif")));

		add(startButton);
		add(stopButton);
		add(new JSeparator(SwingConstants.VERTICAL));

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(firstButton);
		p.add(previousButton);
		p.add(nextButton);
		p.add(lastButton);
		p.add(deleteButton);

		add(p);
		add(new JSeparator(SwingConstants.VERTICAL));
		add(statusText);

		addActionListenerFirst(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentIndex = 0;
				updateIceConf();
			}
		});
		addActionListenerLast(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentIndex = workspace.getIceConfigurations().size() - 1;
				updateIceConf();
			}
		});
		addActionListenerNext(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentIndex++;
				if (currentIndex == workspace.getIceConfigurations().size()) {
					parent.updateModel();
					ICEConfiguration iceconf = workspace.getIceConfigurations().get(currentIndex - 1);
					ICEConfiguration ic = new ICEConfiguration(iceconf);
					ic.setNumberOfSamples(workspace.getScenario().numberOfEvents());

					workspace.addIceConfiguration(ic);

				}
				updateIceConf();
			}
		});
		addActionListenerPrevious(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentIndex--;
				updateIceConf();
			}
		});
		addActionListenerDelete(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				workspace.removeIceConfiguration(workspace.getIceConfigurations().get(currentIndex));
				if (workspace.getIceConfigurations().size() < 1) {
					workspace.addIceConfiguration(new ICEConfiguration());
					currentIndex = 0;
				}
				updateIceConf();
			}
		});
	}

	public void addActionListenerDelete(ActionListener acc) {
		deleteButton.addActionListener(acc);
	}

	public void addActionListenerFirst(ActionListener acc) {
		firstButton.addActionListener(acc);
	}

	public void addActionListenerLast(ActionListener acc) {
		lastButton.addActionListener(acc);
	}

	public void addActionListenerNext(ActionListener acc) {
		nextButton.addActionListener(acc);
	}

	public void addActionListenerPrevious(ActionListener acc) {
		previousButton.addActionListener(acc);
	}

	public void addActionListenerStart(ActionListener acc) {
		startButton.addActionListener(acc);
	}

	public void addActionListenerStop(ActionListener acc) {
		stopButton.addActionListener(acc);
	}

	public void init(Workspace _workspace) {
		workspace = _workspace;
		currentIndex = 0;
		updateIceConf();
	}

	public void setStatusText(String text) {
		statusText.setText(text);
	}

	public void updateIceConf() {
		try {
			parent.updateModel();
			if (currentIndex == workspace.getIceConfigurations().size()) {
				currentIndex--;
			}
			ICEConfiguration ice = workspace.getIceConfigurations().get(currentIndex);
			ice.updateConf(workspace);
          
         parent.init(ice); 
			
			previousButton.setEnabled(currentIndex > 0);
			firstButton.setEnabled(currentIndex > 0);
			lastButton.setEnabled(currentIndex < workspace.getIceConfigurations().size() - 1);
			deleteButton.setEnabled(workspace.getIceConfigurations().get(currentIndex) != null);
			startButton.setEnabled(!workspace.getIceConfigurations().get(currentIndex).getHasBeenCalculated());

			statusText.setText("ICEConfiguration " + (currentIndex + 1) + " of " + workspace.getIceConfigurations().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		workspace = null;
	}
}
