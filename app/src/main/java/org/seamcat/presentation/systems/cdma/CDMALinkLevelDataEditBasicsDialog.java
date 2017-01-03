package org.seamcat.presentation.systems.cdma;

import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.cdma.CDMALinkLevelData.LinkType;
import org.seamcat.cdma.CDMALinkLevelData.TargetERType;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.SeamcatTextFieldFormats;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.components.SpringUtilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ResourceBundle;

public class CDMALinkLevelDataEditBasicsDialog extends EscapeDialog {

	private class IdentificationPanel extends JPanel {
		private final JComboBox cbTargetType = new JComboBox();
		private final JFormattedTextField tfFrequency = new CalculatorInputField();
		private final JTextField tfSource = new JTextField(30);
		private final JTextField tfSystem = new JTextField(30);
		private final JTextField tfTargetPct = new JTextField(5);

		IdentificationPanel() {
			super(new SpringLayout());

			tfSystem.addFocusListener(SeamcatTextFieldFormats.SELECTALL_FOCUSHANDLER);
			tfSource.addFocusListener(SeamcatTextFieldFormats.SELECTALL_FOCUSHANDLER);
			tfTargetPct.addFocusListener(SeamcatTextFieldFormats.SELECTALL_FOCUSHANDLER);

			// Fill target type combobox
			for (TargetERType t : CDMALinkLevelData.TargetERType.values()) {
				cbTargetType.addItem(t);
			}

			// "All values are derived for a X% target (FER/BLER)
			JPanel targetTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			targetTypePanel.add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_TARGETTYPE_1")));
			targetTypePanel.add(tfTargetPct);
			targetTypePanel.add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_TARGETTYPE_2")));
			targetTypePanel.add(cbTargetType);

			add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_SYSTEM")));
			add(tfSystem);
			add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_SOURCE")));
			add(tfSource);
			add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_FREQUENCY")));
			add(tfFrequency);
			add(targetTypePanel);

			setBorder(new TitledBorder(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_IDENTIFICATION")));
			SpringUtilities.makeCompactGrid(this, 7, 1, 0, 0, 0, 0);
		}

		private void getFields(CDMALinkLevelData data) {
			data.setSystem(tfSystem.getText());
			data.setSource(tfSource.getText());
			data.setFrequency(((Number) tfFrequency.getValue()).doubleValue());
			data.setTargetERpct(tfTargetPct.getText());
			data.setTargetERType((TargetERType) cbTargetType.getSelectedItem());
		}

		private void setFields(CDMALinkLevelData data) {
			tfSystem.setText(data.getSystem());
			tfSource.setText(data.getSource());
			tfFrequency.setValue(data.getFrequency());
			tfTargetPct.setText(data.getTargetERpct());
			cbTargetType.setSelectedItem(data.getTargetERType());
		}
	}

	private static class LinkTypePanel extends JPanel {
		private final JRadioButton jbDownlink = new JRadioButton(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_DOWNLOAD"));
		private final JRadioButton jbUplink = new JRadioButton(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_UPLINK"));

		LinkTypePanel() {
			super(new GridLayout(2, 1));

			ButtonGroup bgType = new ButtonGroup();
			bgType.add(jbUplink);
			bgType.add(jbDownlink);

			add(jbUplink);
			add(jbDownlink);

			setBorder(new TitledBorder(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_LINKTYPE")));
		}

		private void getFields(CDMALinkLevelData data) {
			data.setLinkType(jbUplink.isSelected() ? LinkType.UPLINK : LinkType.DOWNLINK);
		}

		private void setFields(CDMALinkLevelData data) {
			switch (data.getLinkType()) {
				case UPLINK: {
					jbUplink.doClick();
					break;
				}
				case DOWNLINK: {
					jbDownlink.doClick();
					break;
				}
				default: {
					throw new IllegalStateException("Unknown link type");
				}
			}
		}
	}

	private static class PathCaptionPanel extends JPanel {
		private final JTextField tf1Path = new JTextField(30);
		private final JTextField tf2Path = new JTextField(30);

		PathCaptionPanel() {
			super(new SpringLayout());

			add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_1PATH")));
			add(tf1Path);
			add(new JLabel(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_2PATH")));
			add(tf2Path);

			setBorder(new TitledBorder(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_PATHCAPTIONS")));
			SpringUtilities.makeCompactGrid(this, 4, 1, 0, 0, 0, 0);
		}

		private void getFields(CDMALinkLevelData data) {
			data.setPathDescription(1, tf1Path.getText());
			data.setPathDescription(2, tf2Path.getText());
		}

		private void setFields(CDMALinkLevelData data) {
			String path1 = data.getPathDescription(1);
			String path2 = data.getPathDescription(2);
			tf1Path.setText(path1 != null ? path1 : "");
			tf2Path.setText(path2 != null ? path2 : "");
		}
	}

	protected static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

	private final IdentificationPanel idPanel = new IdentificationPanel();

	private final LinkTypePanel linkTypePanel = new LinkTypePanel();

	private final JFrame owner;

	private final PathCaptionPanel pathCaptionPanel = new PathCaptionPanel();

	public CDMALinkLevelDataEditBasicsDialog(JFrame owner) {
		super(owner, true);
		this.owner = owner;

		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS));
		dialogPanel.add(linkTypePanel);
		dialogPanel.add(idPanel);
		dialogPanel.add(pathCaptionPanel);

		getContentPane().add(dialogPanel, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this), BorderLayout.SOUTH);

		setTitle(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_WINDOWTITLE"));
		pack();
		setResizable(false);
	}

	private void getFields(CDMALinkLevelData data) {
		linkTypePanel.getFields(data);
		idPanel.getFields(data);
		pathCaptionPanel.getFields(data);
	}

	private void setFields(CDMALinkLevelData data) {
		linkTypePanel.setFields(data);
		idPanel.setFields(data);
		pathCaptionPanel.setFields(data);
	}

	public boolean showDialog(CDMALinkLevelData data) {
		setLocationRelativeTo(owner);
		setAccept( false );

		setFields(data);
		setVisible(true);

		if (isAccept()) {
			getFields(data);
		}
		return isAccept();
	}
}