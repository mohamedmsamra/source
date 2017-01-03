package org.seamcat.presentation.components;

import org.seamcat.commands.CalculateInterferenceCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.ICEProbabilityResultEvent;
import org.seamcat.model.Workspace;
import org.seamcat.model.engines.ICE;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.engines.InterferenceCalculationListener;
import org.seamcat.model.functions.Point2D;
import org.seamcat.presentation.components.interferencecalc.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class InterferenceCalculationsPanel extends JPanel implements InterferenceCalculationListener {
	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

	private AlgorithmSamplesPanel algorithmSamples = new AlgorithmSamplesPanel();
	private CalculationModePanel calculationMode = new CalculationModePanel();

	private ICE ice;
	private ICEConfiguration iceconf;
	private final ICEControlPanel iceControl = new ICEControlPanel(this);
	private final ParametersPanel interferenceCriterion;

	private final LayoutManager layout = new BorderLayout();
	private final ICEResultPanel resultsPanel = new ICEResultPanel();
	private final SignalTypePanel signalType = new SignalTypePanel();

	private TranslationParametersPanel translationParameters = new TranslationParametersPanel();
	private Workspace workspace;

	public InterferenceCalculationsPanel( Workspace workspace ) {
		this.workspace = workspace;
        if (workspace.getIceConfigurations().size() == 0) {
            workspace.addIceConfiguration(new ICEConfiguration());
        }
        setLayout(layout);

		interferenceCriterion = new ParametersPanel();
		translationParameters.setElementStatusEnabled(false);
		
		calculationMode.addModeListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				translationParameters.setElementStatusEnabled(calculationMode.modeIsTranslation());
				resultsPanel.setElementStatusEnabled(!calculationMode.modeIsTranslation());
				signalType.init(iceconf);
			}
		});

		iceControl.addActionListenerStart(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				EventBusFactory.getEventBus().publish( new CalculateInterferenceCommand( iceconf ));
			}
		});
		
		// First row
		JPanel firstRow = new JPanel(new GridLayout(1, 4));
		firstRow.add(new BorderPanel(calculationMode,STRINGLIST.getString("ICECONFIG_CALCULATION_MODE_TITLE")));
		firstRow.add(new BorderPanel(signalType,STRINGLIST.getString("ICECONFIG_SIGNALTYPE_TITLE")));
		firstRow.add(new BorderPanel(interferenceCriterion,STRINGLIST.getString("ICECONFIG_INTERFERENCE_CRITERION_TITLE")));
		firstRow.add(new BorderPanel(algorithmSamples,STRINGLIST.getString("ICECONFIG_GENERAL_TITLE")));

		add(firstRow, BorderLayout.NORTH);

		JPanel secondRow = new JPanel(new BorderLayout());
		secondRow.add(new BorderPanel(iceControl,STRINGLIST.getString("ICECONFIG_INTERFERENCE_CALCULATION_ENGINE_CONTROL_TITLE")), BorderLayout.NORTH);
		
		JPanel secondMiddle = new JPanel(new BorderLayout());
		secondMiddle.add(new BorderPanel(translationParameters,STRINGLIST.getString("ICECONFIG_TRANSLATION_PARAMETERS_TITLE")), BorderLayout.WEST);
		secondMiddle.add(new BorderPanel(resultsPanel,STRINGLIST.getString("ICECONFIG_RESULTS_TITLE")), BorderLayout.CENTER);

		secondRow.add(secondMiddle, BorderLayout.CENTER);
		JScrollPane sp = new JScrollPane(secondRow);    
		sp.setBorder(BorderFactory.createEmptyBorder());     
		
		add(sp, BorderLayout.CENTER);
        iceControl.init(workspace);
        Subscriber.subscribe(this);
    }

	public void addTranslationResult(Point2D point) {
		resultsPanel.addTranslationResult(point);
		iceconf.addTranslationPoint(point);
	}

	public void calculationComplete() {
		iceControl.updateIceConf();
	}

	public void calculationStarted() {
		iceControl.updateIceConf();
	}

	public boolean confirmContinueOnWarning(String warning) {
		return JOptionPane.showConfirmDialog(this, warning, "ICE Warning",
		      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}

	public void incrementCurrentProcessCompletionPercentage(int value) {
	}

	public void init(ICEConfiguration iceconf) {
		this.iceconf = iceconf;
		calculationMode.init(iceconf);
		signalType.init(iceconf);
		interferenceCriterion.init(iceconf);
		algorithmSamples.init(iceconf);
		translationParameters.init(iceconf, workspace.getScenario());
		resultsPanel.init(iceconf);
	}

    @UIEventHandler
    public void handle( CalculateInterferenceCommand command ) {
        if ( iceconf != command.getIce() ) return;
        ice = new ICE(workspace.getScenario(), workspace.getSimulationResults());
        ice.addIceListener(InterferenceCalculationsPanel.this);
        updateModel();
        ice.calculateInterference(iceconf);
    }

    @UIEventHandler
	public void handle( ICEProbabilityResultEvent event ) {
		if ( iceconf != event.getIce() ) return;

        resultsPanel.setProbabilityResult( iceconf.getPropabilityResult() );
    }

	public void parameters(int numberTotalEvents, double probabilityTotalN, double ciLevel, double cniLevel, double iniLevel, double niLevel, double sensitivity) {
		iceconf.setnumberOfTotalEvents(numberTotalEvents);
		iceconf.setProbabilityTotalN(probabilityTotalN);
		iceconf.setCiLevel(ciLevel);
		iceconf.setCniLevel(cniLevel);
		iceconf.setIniLevel(iniLevel);
		iceconf.setNiLevel(niLevel);
		iceconf.setSensitivity(sensitivity);
		interferenceCriterion.init(iceconf);
		algorithmSamples.init(iceconf);
	}

	public void setCurrentProcessCompletionPercentage(int value) {
	}

    public void init() {
        iceControl.init( workspace );
    }

	public void close() {
		this.workspace = null;
		iceControl.close();
	}

	public void updateModel() {
		translationParameters.updateModel();
	}

	public void warningMessage(String warning) {
		JOptionPane.showMessageDialog(this, warning, "ICE Error",
		      JOptionPane.ERROR_MESSAGE);
	}

}
