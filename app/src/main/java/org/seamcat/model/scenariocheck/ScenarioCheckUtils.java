package org.seamcat.model.scenariocheck;

import org.apache.log4j.Logger;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.Workspace;
import org.seamcat.model.generic.GenericLink;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.types.*;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.ValidationResult;
import org.seamcat.scenario.WorkspaceScenario;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScenarioCheckUtils {

	private static final Logger LOG = Logger.getLogger(Workspace.class);

	/**
	 * Performs an array of checks (defined by STD_CHECKS) on the given workspace
	 * and returns a list of ScenarioCheckResult's
	 */
	public static List<ScenarioCheckResult> checkWorkspace(Workspace workspace, boolean wsPrefix) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking workspace consistency");
        }
        String prefix = "";
        if ( wsPrefix ) {
            prefix = workspace.getName()+"->";
        }

        List<ScenarioCheckResult> results = new ArrayList<ScenarioCheckResult>();

        // loop and evaluate all plugins

        WorkspaceScenario scenario = workspace.getScenario();
        checkSystem(scenario, results, scenario.getVictimSystem(), prefix);
        int i=1;
        List<InterferenceLink> interferenceLinks = scenario.getInterferenceLinks();
        for (InterferenceLink link : interferenceLinks) {
            checkSystem(scenario, results, link.getInterferingSystem(), prefix);
            check(scenario, path(link), results, (PluginConfiguration) link.getPropagationModel(), prefix+"InterferenceLink(" + i + ")");
            Transmitter transmitter = link.getInterferingSystem().getTransmitter();
            if ( transmitter instanceof GenericTransmitter && ((GenericTransmitter) transmitter).isInterfererCognitiveRadio()) {
                SensingLink sensingLink = ((GenericTransmitter) transmitter).getSensingLink();
                check(scenario, path(link, sensingLink), results, (PluginConfiguration) sensingLink.getPropagationModel(), prefix + "InterferenceLink(" + i + ")->Sensing Link");
            }

            i++;
        }

        List<EventProcessing> eventProcessingList = scenario.getEventProcessingList();
        for (EventProcessing processing : eventProcessingList) {
            PluginConfiguration conf = (PluginConfiguration) processing;
            check( scenario, path(), results, conf, prefix+conf.description().name());
        }

        // The standard tests performed by checkWorkspace(Workspace)
        VictimLinkCheck victimLinkCheck = new VictimLinkCheck(prefix);
        final ScenarioCheck[] checks = new ScenarioCheck[] {
                victimLinkCheck, new InterferingLinkCheck(prefix), new CDMACheck(prefix),
		      new GeneralScenarioCheck(prefix) };

        if ( workspace.getVictimSystem() instanceof GenericSystem) {
            if ( workspace.isUseUserDefinedDRSS() ) {
                victimLinkCheck.checkDistribution(workspace.getUserDefinedDRSS(), prefix+"Scenario", "drss");
            }
        }
		for (ScenarioCheck sCheck : checks) {
			results.add(sCheck.check(workspace));
		}
		return results;
	}

    private static void checkSystem(Scenario scenario, List<ScenarioCheckResult> results, RadioSystem system, String prefix ) {
        PluginConfiguration conf  = (PluginConfiguration) system.getLink().getPropagationModel();
        check(scenario, path(system), results, conf, prefix+system.getName() + "->Propagation Model");
        conf = (PluginConfiguration) system.getReceiver().getAntennaGain();
        check(scenario, path(system, system.getReceiver()), results, conf, prefix+system.getName() + "->Receiver->Antenna Gain" + conf.description().name() + "->");
        conf = (PluginConfiguration) system.getTransmitter().getAntennaGain();
        check(scenario, path(system, system.getTransmitter()), results, conf, prefix+system.getName() + "->Transmitter->Antenna Gain" + conf.description().name() + "->");

        if ( system instanceof GenericSystem) {
            conf = (PluginConfiguration) ((GenericLink) system.getLink()).getCoverageRadius();
            check(scenario, path(system), results, conf, prefix+system.getName() +"->Coverage radius"+conf.description().name()+"->");
        }
    }

    private static List<Object> path( Object... elms ) {
        List<Object> path = new ArrayList<Object>();
        Collections.addAll(path, elms);
        return path;
    }

    public static void check(Scenario scenario, List<Object> path2, List<ScenarioCheckResult> results, PluginConfiguration conf, String path ){
        ValidationResult result = conf.consistencyCheck(scenario, path2);
        for (String message : result.getModelErrors()) {
            ScenarioCheckResult error = new ScenarioCheckResult();
            error.setCheckName(path );
            error.setOutcome( ScenarioCheckResult.Outcome.FAILED );
            error.addMessage( message );
            results.add( error );
        }
        for (Map.Entry<Method, String> entry : result.getFieldError().entrySet()) {
            ScenarioCheckResult error = new ScenarioCheckResult();
            Config config = entry.getKey().getAnnotation(Config.class);
            error.setCheckName(path + "->" + config.name());
            error.setOutcome( ScenarioCheckResult.Outcome.FAILED );
            error.addMessage( entry.getValue());
            results.add( error );
        }
        // search for other plugin elements
        Class modelClass = conf.getModelClass();
        for (Method method : modelClass.getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            if ( returnType.isAssignableFrom(AntennaGain.class) ||
                    returnType.isAssignableFrom(PropagationModel.class)) {
                Config annotation = method.getAnnotation(Config.class);
                if ( annotation != null ) {
                    try {
                        PluginConfiguration nestedConf = (PluginConfiguration) method.invoke( conf.getModel() );
                        path2.add( conf );
                        check(scenario, path2, results, nestedConf, path + "->" + annotation.name());
                        path2.remove(conf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}