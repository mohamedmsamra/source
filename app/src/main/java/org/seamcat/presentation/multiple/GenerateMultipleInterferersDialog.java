package org.seamcat.presentation.multiple;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.log4j.Logger;
import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.InterferenceLinkElement;
import org.seamcat.model.Workspace;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.generic.RelativeLocationUI;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.systems.generic.SystemModelGeneric;

import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.model.workspace.InterferenceLinkPathUI;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.RelativeLocationInterferenceUI;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.multiple.MultipleInterferersPreviewPanel.LayoutType;
import org.seamcat.presentation.systems.CellularPosition;
import org.seamcat.presentation.systems.generic.RelativeLocationInterferingLinkPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GenerateMultipleInterferersDialog extends EscapeDialog {

	private static final Logger LOG = Logger.getLogger(GenerateMultipleInterferersDialog.class);
	private int numberOfGeneratedLinks;
	private static ResourceBundle resources = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
	private List<InterferenceLinkElement> added;
	private Workspace workspace;

	public GenerateMultipleInterferersDialog(Frame owner) {
		super(owner);
		initComponents();
		updateGUI();
		registerWindowListeners();
		registerHelp();
	}

	private void registerWindowListeners() {
		// Window resize listener (forces update of previewpanel)
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				multipleInterferersPreviewPanel.repaint();
			}
		});
	}

	private void registerHelp() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SeamcatHelpResolver.showHelp(this);
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}


	private void helpButtonActionPerformed() {
		SeamcatHelpResolver.showHelp(this);
	}

	private void interferingLinkSelectionActionPerformed() {
		int index = interferingLinkSelection.getSelectedIndex();
		setSelectedInterferer(workspace.getInterferenceLinkUIs().get(index));
	}

	private void setSelectedInterferer(InterferenceLinkElement link) {
		if (link != null) {
			//First see if link has fixed location
			RelativeLocationInterferenceUI location = link.getSettings().path().relativeLocation();
			if (!(location.mode() == InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR)
					&& !(location.mode() == InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR)
					&& !(location.mode() == InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR)
					) {
				positionX.setValue(location.deltaX().trial());
				positionY.setValue(location.deltaY().trial());
			} else {
				positionX.setValue(0.0);
				positionY.setValue(0.0);
			}
			positionX.setEnabled(true);
			positionY.setEnabled(true);
			updateGUI();
		}
	}

	public void setModel(Workspace workspace) {
		this.workspace = workspace;
		SystemModel victim = workspace.getVictimSystem();
		List<InterferenceLinkElement> links = workspace.getInterferenceLinkUIs();
		added = null;
		multipleInterferersPreviewPanel.reset();
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (InterferenceLinkElement i : links) {
			model.addElement(i.getName());
		}
		interferingLinkSelection.setModel(model);

		positionX.setValue(0.0);
		positionY.setValue(0.0);
		positionX.setEnabled(false);
		positionY.setEnabled(false);
		interferingLinkSelection.setSelectedItem(model.getElementAt(0));

		// Determine if victim can be drawn:
		if (!(victim instanceof SystemModelGeneric)) {

			
			if (victim instanceof SystemModelCDMAUpLink || victim instanceof SystemModelOFDMAUpLink) {
				//Victim Receiver is BaseStations
				multipleInterferersPreviewPanel.setDmaVictim(true, true, getInterCellDistance(victim));
			} else {
				//Victim is UE's - WT is Basestations
				multipleInterferersPreviewPanel.setDmaVictim(true, false, getInterCellDistance(victim));
			}
		} else {
			//Victim is not DMA -> WT is at 0,0:
			SystemModelGeneric genVictim = (SystemModelGeneric) victim;
			if (genVictim.path().relativeLocation().useCorrelatedDistance()) {
				//Victim is at fixed position - easy to plot
				RelativeLocationUI pos = genVictim.path().relativeLocation();
				multipleInterferersPreviewPanel.addVictimReceiver("VR", pos.deltaX().trial(), pos.deltaY().trial());

			} else {
				//TODO 04.06.2009 CP: Find a way to draw victim location when it is dynamic
			}
		}
		updateGUI();
		this.setVisible(true);
	}

	private double getInterCellDistance(SystemModel systemModel) {
		CellularPosition position = null;
		if (systemModel instanceof SystemModelCDMAUpLink) {
			position = ((SystemModelCDMAUpLink) systemModel).positioning().position();
		} else if (systemModel instanceof SystemModelCDMADownLink) {
			position = ((SystemModelCDMADownLink) systemModel).positioning().position();
		} else if (systemModel instanceof SystemModelOFDMAUpLink) {
			position = ((SystemModelOFDMAUpLink) systemModel).positioning().position();
		} else if (systemModel instanceof SystemModelOFDMADownLink) {
			position = ((SystemModelOFDMADownLink) systemModel).positioning().position();
		}

		double interCellDistance = 0.0;

		if (!position.sectorType().equals(CellularLayout.SectorSetup.TriSector3GPP)) {
			interCellDistance = position.cellRadius() * Math.sqrt(3);
		} else {
			interCellDistance = position.cellRadius() * 3;
		}
		return interCellDistance;
	}


	private void updateGUI() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (interferingLinkSelection.getSelectedItem() != null) {
					multipleInterferersPreviewPanel.setPlotGenerated(true);
				} else {
					multipleInterferersPreviewPanel.setPlotGenerated(false);
				}

				if (layoutTabs.getSelectedIndex() == 0) { //Circular
					numberOfGeneratedLinks = calculateNumberOfGeneratedLinksInTier((Integer) circularFirstTier.getValue(), (Integer) circularTiers.getValue());
					multipleInterferersPreviewPanel.setLayoutType(LayoutType.Circular);
					multipleInterferersPreviewPanel.setIntersiteDistance(circularRadius.getValueAsDouble());
					multipleInterferersPreviewPanel.setNumberOfTiers((Integer) circularTiers.getValue());
					multipleInterferersPreviewPanel.setSizeOfFirstTier((Integer) circularFirstTier.getValue());
					multipleInterferersPreviewPanel.setDisplacementAngle(displacementAngle.getValueAsDouble());
					multipleInterferersPreviewPanel.setOffsetAngle(angleOffset.getValueAsDouble());
				} else if (layoutTabs.getSelectedIndex() == 1) { //3GPP Hexagon
					multipleInterferersPreviewPanel.setLayoutType(LayoutType.Hexagon3GPP);
					multipleInterferersPreviewPanel.setIntersiteDistance(hexagonal3GPPIntersiteDistance.getValueAsDouble());
					if (hexagon3GPP_tiers_one.isSelected()) {
						multipleInterferersPreviewPanel.setNumberOfTiers(1);
						numberOfGeneratedLinks = 6;
					} else {
						multipleInterferersPreviewPanel.setNumberOfTiers(2);
						numberOfGeneratedLinks = 18;
					}

				}
				multipleInterferersPreviewPanel.setCenterX(positionX.getValueAsDouble());
				multipleInterferersPreviewPanel.setCenterY(positionY.getValueAsDouble());

				multipleInterferersPreviewPanel.repaint();

				statusLabel.setText("The current configuration will generate " + numberOfGeneratedLinks + " new Interfering Links");
			}
		});
	}

	private void genericInputFieldActionPerformed() {
		updateGUI();
	}

	private void cancelButtonActionPerformed() {
		multipleInterferersPreviewPanel.setPlotGenerated(false);
		setVisible(false);
	}

	public List<InterferenceLinkElement> getAdded() {
		return added;
	}

	private void okButtonActionPerformed() {
		// TODO 01.06.2009 CP: Generate links here
		added = new ArrayList<InterferenceLinkElement>();
		generateLinks();
	}

	private void generateLinks() {
		try {
			InterferenceLinkElement[] links = null;
			int index = interferingLinkSelection.getSelectedIndex();
			InterferenceLinkElement orig = workspace.getInterferenceLinkUIs().get(index);

			SystemModel interferingSystem = workspace.getSystem(orig.getInterferingSystemId());
			if ((interferingSystem instanceof SystemModelGeneric && workspace.getVictimSystem() instanceof SystemModelGeneric) ) {
				InterferingLinkRelativePosition.CorrelationMode mode = orig.getSettings().path().relativeLocation().mode();
				if (positionRelativeWT.isSelected() && RelativeLocationInterferingLinkPanel.positionToVR.contains(orig.getSettings().path().relativeLocation().mode())) {
					switch (mode) {
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT);
							break;
					}
				} else if (positionRelativeVR.isSelected() && RelativeLocationInterferingLinkPanel.positionToWT.contains(orig.getSettings().path().relativeLocation().mode())) {
					switch (mode) {
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR);
							break;
						case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT:
							setMode(orig, InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR);
							break;
					}
				}
			}
			setXY(orig, positionX.getValueAsDouble(), positionY.getValueAsDouble());

			if (layoutTabs.getSelectedIndex() == 0) { //Circular
				numberOfGeneratedLinks = calculateNumberOfGeneratedLinksInTier((Integer) circularFirstTier.getValue(), (Integer) circularTiers.getValue());

				double radius = circularRadius.getValueAsDouble();
				double offset = angleOffset.getValueAsDouble();
				double displacement = displacementAngle.getValueAsDouble();
				int sizeOfFirst = (Integer) circularFirstTier.getValue();
				int numberOfTiers = (Integer) circularTiers.getValue();

				links = new InterferenceLinkElement[numberOfGeneratedLinks];
				generateCircularLayout(links, orig, 1, numberOfTiers, displacement, offset, sizeOfFirst, radius);
			} else if (layoutTabs.getSelectedIndex() == 1) { //3GPP Hexagon
				double d = hexagonal3GPPIntersiteDistance.getValueAsDouble();

				if (hexagon3GPP_tiers_one.isSelected()) {
					numberOfGeneratedLinks = 6;
				} else {
					numberOfGeneratedLinks = 18;
				}
				links = new InterferenceLinkElement[numberOfGeneratedLinks];
				generate3GPPLayout(links, orig, d);
			}

			if (links != null) {
				Collections.addAll(added, links);
				links = null;
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//Stop animation
					multipleInterferersPreviewPanel.setPlotGenerated(false);
					GenerateMultipleInterferersDialog.this.setVisible(false);
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			});
		} catch (Exception e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			JOptionPane.showMessageDialog(GenerateMultipleInterferersDialog.this, resources.getString("MULTI_GENERATION_ERROR_MESSAGE"), "Error Generating Links", JOptionPane.ERROR_MESSAGE);
			LOG.error("Error generating multiple interfering links", e);
		}
	}

	private void setRelativeLocation(InterferenceLinkElement orig, RelativeLocationInterferenceUI rel) {
		InterferenceLinkUI ilProto = Factory.prototype(InterferenceLinkUI.class, orig.getSettings());
        InterferenceLinkPathUI prototype = Factory.prototype(InterferenceLinkPathUI.class, orig.getSettings().path());
        Factory.when(prototype.relativeLocation()).thenReturn(rel);
		Factory.when(ilProto.path()).thenReturn(Factory.build(prototype));

		orig.setSettings( Factory.build(ilProto) );

	}

	private void setMode(InterferenceLinkElement orig, InterferingLinkRelativePosition.CorrelationMode mode) {
		RelativeLocationInterferenceUI prototype = Factory.prototype(RelativeLocationInterferenceUI.class, orig.getSettings().path().relativeLocation());
        Factory.when(prototype.mode()).thenReturn(mode);
		setRelativeLocation( orig, Factory.build(prototype) );
	}

	private void generate3GPPLayout(InterferenceLinkElement[] links, InterferenceLinkElement orig, double D) {
		double x = positionX.getValueAsDouble();
		double y = positionY.getValueAsDouble();
		int id = 0;

		links[id] = generateInteferenceLink(orig, x + 1.5 * D / Math.sqrt(3), y + D / 2, id++);
		links[id] = generateInteferenceLink(orig, x,y + D, id++);
		links[id] = generateInteferenceLink(orig, x - 1.5*D / Math.sqrt(3),y + D / 2, id++);
		links[id] = generateInteferenceLink(orig, x - 1.5*D / Math.sqrt(3),y - D / 2, id++);
		links[id] = generateInteferenceLink(orig, x,y - D, id++);
		links[id] = generateInteferenceLink(orig, x + 1.5*D/Math.sqrt(3),y - D / 2, id++);
		if (links.length > 6) {//Generate second tier
			links[id] = generateInteferenceLink(orig, x + 3*D / Math.sqrt(3),y, id++);
			links[id] = generateInteferenceLink(orig, x + 3*D / Math.sqrt(3),y + D, id++);
			links[id] = generateInteferenceLink(orig, x + 1.5*D / Math.sqrt(3),y + 1.5*D, id++);
			links[id] = generateInteferenceLink(orig, x,y + 2*D, id++);
			links[id] = generateInteferenceLink(orig, x - 1.5*D / Math.sqrt(3),y + 1.5*D, id++);
			links[id] = generateInteferenceLink(orig, x - 3*D / Math.sqrt(3),y + D, id++);
			links[id] = generateInteferenceLink(orig, x - 3*D / Math.sqrt(3),y, id++);
			links[id] = generateInteferenceLink(orig, x - 3*D / Math.sqrt(3),y - D, id++);
			links[id] = generateInteferenceLink(orig, x - 1.5*D / Math.sqrt(3),y - 1.5*D, id++);
			links[id] = generateInteferenceLink(orig, x,y - 2*D, id++);
			links[id] = generateInteferenceLink(orig, x + 1.5*D / Math.sqrt(3),y - 1.5*D, id++);
			links[id] = generateInteferenceLink(orig, x + 3*D / Math.sqrt(3),y - D, id++);
		}
	}

	private InterferenceLinkElement generateInteferenceLink(InterferenceLinkElement orig, double x, double y, int id) {
		InterferenceLinkUI settings = ProxyHelper.deepCloneComposite(InterferenceLinkUI.class, orig.getSettings());
		InterferenceLinkElement clone = new InterferenceLinkElement(orig.getInterferingSystemId(), "", settings);
		setXY(clone, x, y);
		return clone;
	}

	private void setXY( InterferenceLinkElement link, double x, double y) {
		RelativeLocationInterferenceUI prototype = Factory.prototype(RelativeLocationInterferenceUI.class, link.getSettings().path().relativeLocation());
        Factory.when( prototype.deltaX()).thenReturn(Factory.distributionFactory().getConstantDistribution(x));
		Factory.when( prototype.deltaY()).thenReturn(Factory.distributionFactory().getConstantDistribution(y));

		setRelativeLocation( link, Factory.build(prototype) );
	}

	private void generateCircularLayout(InterferenceLinkElement[] links, InterferenceLinkElement orig, int tierid, int numberOfTiers, double displacementAngle, double offsetAngle, int numberOfSitesInFirstTier, double radius) {

		if (tierid > numberOfTiers) {
			return; //Terminate recursiveLoop
		}
		int cellid;
		double angleFromCenter = displacementAngle / tierid;
		int numberOfSitesInThisTier = tierid * numberOfSitesInFirstTier;
		int cellsInnerRows = calculateNumberOfGeneratedLinksInTier(numberOfSitesInFirstTier, tierid - 1);

		for (int i = 0; i < numberOfSitesInThisTier; i++) {
			cellid = cellsInnerRows + i;

			double angle = i * angleFromCenter + offsetAngle;
			Point2D point = new Point2D(Mathematics.cosD(angle), Mathematics.sinD(angle)).scale(radius * tierid).add(relativeLocation(orig));

			if (links[cellid] == null) {
				links[cellid] = generateInteferenceLink(orig, point.getX(), point.getY(), cellid);
			}

		}
		//Generate next tier - Recursive call
		generateCircularLayout(links, orig, tierid + 1, numberOfTiers, displacementAngle, offsetAngle, numberOfSitesInFirstTier, radius);
	}

	private Point2D relativeLocation(InterferenceLinkElement element ){
		RelativeLocationInterferenceUI ui = element.getSettings().path().relativeLocation();
		return new Point2D(ui.deltaX().trial(), ui.deltaY().trial());
	}

	private void circularFirstTierStateChanged() {
		int sizeOfFirst = (Integer) circularFirstTier.getValue();
		displacementAngle.setValue(Mathematics.round(360.0 / sizeOfFirst));
		angleOffset.setValue(Mathematics.round(displacementAngle.getValueAsDouble()/2.0));


		updateGUI();
	}

	/**
	 * Recursive method used to calculate total number of generated Interfering Links
	 */
	private int calculateNumberOfGeneratedLinksInTier(int numberOfSitesInFirstTier, int tier) {
		if (tier == 0) {
			return 0;
		} else {
			return numberOfSitesInFirstTier * tier + calculateNumberOfGeneratedLinksInTier(numberOfSitesInFirstTier, (tier - 1));
		}
	}

	private void positionRelativeWTActionPerformed() {
		multipleInterferersPreviewPanel.setGenerateRelativeToVictim(positionRelativeVR.isSelected());
	}

	private void positionRelativeVRActionPerformed() {
		multipleInterferersPreviewPanel.setGenerateRelativeToVictim(positionRelativeVR.isSelected());
	}

	private void resetButtonActionPerformed(ActionEvent e) {
		circularTiers.setValue(1);
		circularFirstTier.setValue(6);
		circularRadius.setValue(0.433d);
		displacementAngle.setValue(60d);
		angleOffset.setValue(30d);

		hexagon3GPP_tiers_two.setSelected(true);
		hexagonal3GPPIntersiteDistance.setValue(0.433d);
		updateGUI();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("org.seamcat.presentation.localization");
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		separator1 = compFactory.createSeparator(bundle.getString("GenerateMultipleInterferersDialog.separator1.text"));
		label1 = new JLabel();
		interferingLinkSelection = new JComboBox();
		label2 = new JLabel();
		panel1 = new JPanel();
		panel2 = new JPanel();
		label3 = new JLabel();
		positionX = new CalculatorInputField();
		statusLabel = new JLabel();
		label4 = new JLabel();
		positionY = new CalculatorInputField();
		separator3 = compFactory.createSeparator(bundle.getString("GenerateMultipleInterferersDialog.separator3.text"));
		positionRelativeWT = new JRadioButton();
		positionRelativeVR = new JRadioButton();
		separator2 = compFactory.createSeparator(bundle.getString("GenerateMultipleInterferersDialog.separator2.text"));
		informationTextLabel = new JLabel();
		layoutTabs = new JTabbedPane();
		panel3 = new JPanel();
		label5 = new JLabel();
		circularTiers = new JSpinner();
		label6 = new JLabel();
		circularFirstTier = new JSpinner();
		label7 = new JLabel();
		circularRadius = new CalculatorInputField();
		label8 = new JLabel();
		label9 = new JLabel();
		displacementAngle = new CalculatorInputField();
		label11 = new JLabel();
		label10 = new JLabel();
		angleOffset = new CalculatorInputField();
		label12 = new JLabel();
		panel4 = new JPanel();
		label14 = new JLabel();
		panel6 = new JPanel();
		hexagon3GPP_tiers_one = new JRadioButton();
		hexagon3GPP_tiers_two = new JRadioButton();
		label15 = new JLabel();
		hexagonal3GPPIntersiteDistance = new CalculatorInputField();
		label16 = new JLabel();
		label13 = new JLabel();
		multipleInterferersPreviewPanel = new MultipleInterferersPreviewPanel();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		helpButton = new JButton();
		resetButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setModal(true);
		setTitle(bundle.getString("GenerateMultipleInterferersDialog.this.title"));
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setBorder(new TitledBorder(bundle.getString("GenerateMultipleInterferersDialog.contentPanel.border")));
				contentPanel.setLayout(new FormLayout(
						"default, $lcgap, 110dlu, $lcgap, default, $lcgap, default:grow",
						"4*(default, $lgap), [23dlu,pref], $lgap, default, $lgap, default:grow, $lgap, default"));
				contentPanel.add(separator1, cc.xywh(1, 1, 7, 1));

				//---- label1 ----
				label1.setText(bundle.getString("GenerateMultipleInterferersDialog.label1.text"));
				label1.setLabelFor(interferingLinkSelection);
				contentPanel.add(label1, cc.xy(1, 3));

				//---- interferingLinkSelection ----
				interferingLinkSelection.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						interferingLinkSelectionActionPerformed();
					}
				});
				contentPanel.add(interferingLinkSelection, cc.xywh(3, 3, 5, 1));

				//---- label2 ----
				label2.setText(bundle.getString("GenerateMultipleInterferersDialog.label2.text"));
				label2.setVerticalAlignment(SwingConstants.TOP);
				contentPanel.add(label2, cc.xywh(1, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.TOP));

				//======== panel1 ========
				{
					panel1.setLayout(new FormLayout(
							"default:grow",
							"default"));

					//======== panel2 ========
					{
						panel2.setLayout(new FormLayout(
								"default, right:default, default:grow",
								"4*(default, $lgap), default"));

						//---- label3 ----
						label3.setText(bundle.getString("GenerateMultipleInterferersDialog.label3.text"));
						label3.setLabelFor(positionX);
						panel2.add(label3, cc.xy(1, 1));

						//---- positionX ----
						positionX.setFocusLostBehavior(JFormattedTextField.COMMIT);
						positionX.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						positionX.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel2.add(positionX, cc.xy(2, 1));
						panel2.add(statusLabel, cc.xywh(3, 1, 1, 3, CellConstraints.DEFAULT, CellConstraints.TOP));

						//---- label4 ----
						label4.setText(bundle.getString("GenerateMultipleInterferersDialog.label4.text"));
						label4.setLabelFor(positionY);
						panel2.add(label4, cc.xy(1, 3));

						//---- positionY ----
						positionY.setFocusLostBehavior(JFormattedTextField.COMMIT);
						positionY.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						positionY.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel2.add(positionY, cc.xy(2, 3));
						panel2.add(separator3, cc.xywh(1, 5, 3, 1));

						//---- positionRelativeWT ----
						positionRelativeWT.setText(bundle.getString("GenerateMultipleInterferersDialog.positionRelativeWT.text"));
						positionRelativeWT.setSelected(true);
						positionRelativeWT.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								positionRelativeWTActionPerformed();
							}
						});
						panel2.add(positionRelativeWT, cc.xywh(2, 7, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

						//---- positionRelativeVR ----
						positionRelativeVR.setText(bundle.getString("GenerateMultipleInterferersDialog.positionRelativeVR.text"));
						positionRelativeVR.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								positionRelativeVRActionPerformed();
							}
						});
						panel2.add(positionRelativeVR, cc.xywh(2, 9, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
					}
					panel1.add(panel2, cc.xy(1, 1));
				}
				contentPanel.add(panel1, cc.xywh(3, 5, 5, 1));
				contentPanel.add(separator2, cc.xywh(1, 7, 7, 1));

				//---- informationTextLabel ----
				informationTextLabel.setText(bundle.getString("GenerateMultipleInterferersDialog.informationTextLabel.text"));
				contentPanel.add(informationTextLabel, cc.xywh(1, 9, 7, 1, CellConstraints.DEFAULT, CellConstraints.TOP));

				//======== layoutTabs ========
				{
					layoutTabs.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							genericInputFieldActionPerformed();
						}
					});

					//======== panel3 ========
					{
						panel3.setLayout(new FormLayout(
								"default:grow, 2*($lcgap, default)",
								"6*(default, $lgap), default"));

						//---- label5 ----
						label5.setText(bundle.getString("GenerateMultipleInterferersDialog.label5.text"));
						panel3.add(label5, cc.xy(1, 1));

						//---- circularTiers ----
						circularTiers.setModel(new SpinnerNumberModel(1, 1, 10, 1));
						circularTiers.addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						circularTiers.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel3.add(circularTiers, cc.xy(3, 1));

						//---- label6 ----
						label6.setText(bundle.getString("GenerateMultipleInterferersDialog.label6.text"));
						panel3.add(label6, cc.xy(1, 3));

						//---- circularFirstTier ----
						circularFirstTier.setModel(new SpinnerNumberModel(6, 1, 100, 1));
						circularFirstTier.addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								genericInputFieldActionPerformed();
								circularFirstTierStateChanged();
							}
						});
						circularFirstTier.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel3.add(circularFirstTier, cc.xy(3, 3));

						//---- label7 ----
						label7.setText(bundle.getString("GenerateMultipleInterferersDialog.label7.text"));
						panel3.add(label7, cc.xy(1, 5));

						//---- circularRadius ----
						circularRadius.setText(bundle.getString("GenerateMultipleInterferersDialog.circularRadius.text"));
						circularRadius.setAllowNegatives(false);
						circularRadius.setFocusLostBehavior(JFormattedTextField.COMMIT);
						circularRadius.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						circularRadius.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel3.add(circularRadius, cc.xy(3, 5));

						//---- label8 ----
						label8.setText(bundle.getString("GenerateMultipleInterferersDialog.label8.text"));
						panel3.add(label8, cc.xy(5, 5));

						//---- label9 ----
						label9.setText(bundle.getString("GenerateMultipleInterferersDialog.label9.text"));
						panel3.add(label9, cc.xy(1, 7));

						//---- displacementAngle ----
						displacementAngle.setText(bundle.getString("GenerateMultipleInterferersDialog.displacementAngle.text"));
						displacementAngle.setToolTipText(bundle.getString("GenerateMultipleInterferersDialog.displacementAngle.toolTipText"));
						displacementAngle.setFocusLostBehavior(JFormattedTextField.COMMIT);
						displacementAngle.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						displacementAngle.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel3.add(displacementAngle, cc.xy(3, 7));

						//---- label11 ----
						label11.setText(bundle.getString("GenerateMultipleInterferersDialog.label11.text"));
						panel3.add(label11, cc.xy(5, 7));

						//---- label10 ----
						label10.setText(bundle.getString("GenerateMultipleInterferersDialog.label10.text"));
						panel3.add(label10, cc.xy(1, 9));

						//---- angleOffset ----
						angleOffset.setText(bundle.getString("GenerateMultipleInterferersDialog.angleOffset.text"));
						angleOffset.setToolTipText(bundle.getString("GenerateMultipleInterferersDialog.angleOffset.toolTipText"));
						angleOffset.setFocusLostBehavior(JFormattedTextField.COMMIT);
						angleOffset.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						angleOffset.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel3.add(angleOffset, cc.xy(3, 9));

						//---- label12 ----
						label12.setText(bundle.getString("GenerateMultipleInterferersDialog.label12.text"));
						panel3.add(label12, cc.xy(5, 9));
					}
					layoutTabs.addTab(bundle.getString("GenerateMultipleInterferersDialog.panel3.tab.title"), panel3);


					//======== panel4 ========
					{
						panel4.setLayout(new FormLayout(
								"default, $lcgap, right:89dlu, $lcgap, default",
								"3*(default, $lgap), default"));

						//---- label14 ----
						label14.setText(bundle.getString("GenerateMultipleInterferersDialog.label14.text"));
						panel4.add(label14, cc.xy(1, 1));

						//======== panel6 ========
						{
							panel6.setLayout(new FormLayout(
									"default:grow",
									"default, $lgap, default"));

							//---- hexagon3GPP_tiers_one ----
							hexagon3GPP_tiers_one.setText(bundle.getString("GenerateMultipleInterferersDialog.hexagon3GPP_tiers_one.text"));
							hexagon3GPP_tiers_one.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									genericInputFieldActionPerformed();
								}
							});
							panel6.add(hexagon3GPP_tiers_one, cc.xy(1, 1));

							//---- hexagon3GPP_tiers_two ----
							hexagon3GPP_tiers_two.setText(bundle.getString("GenerateMultipleInterferersDialog.hexagon3GPP_tiers_two.text"));
							hexagon3GPP_tiers_two.setSelected(true);
							hexagon3GPP_tiers_two.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									genericInputFieldActionPerformed();
								}
							});
							panel6.add(hexagon3GPP_tiers_two, cc.xy(1, 3));
						}
						panel4.add(panel6, cc.xywh(3, 1, 3, 1));

						//---- label15 ----
						label15.setText(bundle.getString("GenerateMultipleInterferersDialog.label15.text"));
						panel4.add(label15, cc.xy(1, 3));

						//---- hexagonal3GPPIntersiteDistance ----
						hexagonal3GPPIntersiteDistance.setText(bundle.getString("GenerateMultipleInterferersDialog.hexagonal3GPPIntersiteDistance.text"));
						hexagonal3GPPIntersiteDistance.setAllowNegatives(false);
						hexagonal3GPPIntersiteDistance.setFocusLostBehavior(JFormattedTextField.PERSIST);
						hexagonal3GPPIntersiteDistance.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						hexagonal3GPPIntersiteDistance.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								genericInputFieldActionPerformed();
							}
						});
						panel4.add(hexagonal3GPPIntersiteDistance, cc.xy(3, 3));

						//---- label16 ----
						label16.setText(bundle.getString("GenerateMultipleInterferersDialog.label16.text"));
						panel4.add(label16, cc.xy(5, 3));
					}
					layoutTabs.addTab(bundle.getString("GenerateMultipleInterferersDialog.panel4.tab.title"), panel4);

				}
				contentPanel.add(layoutTabs, cc.xywh(1, 11, 4, 4));

				//---- label13 ----
				label13.setText(bundle.getString("GenerateMultipleInterferersDialog.label13.text"));
				label13.setFont(new Font("Dialog", Font.PLAIN, 18));
				contentPanel.add(label13, cc.xywh(7, 11, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
				contentPanel.add(multipleInterferersPreviewPanel, cc.xywh(7, 13, 1, 1, CellConstraints.FILL, CellConstraints.FILL));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(
						"$lcgap, default, $glue, 2*($button, $rgap), $button, $lcgap, default",
						"pref"));

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed();
					}
				});
				buttonBar.add(okButton, cc.xy(4, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed();
					}
				});
				buttonBar.add(cancelButton, cc.xy(6, 1));

				//---- helpButton ----
				helpButton.setText("Help");
				helpButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						helpButtonActionPerformed();
					}
				});
				buttonBar.add(helpButton, cc.xy(8, 1));

				//---- resetButton ----
				resetButton.setText(bundle.getString("GenerateMultipleInterferersDialog.resetButton"));
				resetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						resetButtonActionPerformed(e);
					}
				});
				buttonBar.add(resetButton, cc.xy(10, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(1000, 700);
		setLocationRelativeTo(getOwner());

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(positionRelativeWT);
		buttonGroup2.add(positionRelativeVR);

		//---- hexagonal3GPPNumberOfTiers ----
		ButtonGroup hexagonal3GPPNumberOfTiers = new ButtonGroup();
		hexagonal3GPPNumberOfTiers.add(hexagon3GPP_tiers_one);
		hexagonal3GPPNumberOfTiers.add(hexagon3GPP_tiers_two);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	private JPanel dialogPane;
	private JPanel contentPanel;
	private JComponent separator1;
	private JLabel label1;
	private JComboBox interferingLinkSelection;
	private JLabel label2;
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label3;
	private CalculatorInputField positionX;
	private JLabel statusLabel;
	private JLabel label4;
	private CalculatorInputField positionY;
	private JComponent separator3;
	private JRadioButton positionRelativeWT;
	private JRadioButton positionRelativeVR;
	private JComponent separator2;
	private JLabel informationTextLabel;
	private JTabbedPane layoutTabs;
	private JPanel panel3;
	private JLabel label5;
	private JSpinner circularTiers;
	private JLabel label6;
	private JSpinner circularFirstTier;
	private JLabel label7;
	private CalculatorInputField circularRadius;
	private JLabel label8;
	private JLabel label9;
	private CalculatorInputField displacementAngle;
	private JLabel label11;
	private JLabel label10;
	private CalculatorInputField angleOffset;
	private JLabel label12;
	private JPanel panel4;
	private JLabel label14;
	private JPanel panel6;
	private JRadioButton hexagon3GPP_tiers_one;
	private JRadioButton hexagon3GPP_tiers_two;
	private JLabel label15;
	private CalculatorInputField hexagonal3GPPIntersiteDistance;
	private JLabel label16;
	private JLabel label13;
	private MultipleInterferersPreviewPanel multipleInterferersPreviewPanel;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	private JButton helpButton;
	private JButton resetButton;

    public int getSelectedIndex() {
        return interferingLinkSelection.getSelectedIndex();
    }
}
