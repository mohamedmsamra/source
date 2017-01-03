package org.seamcat;

import org.apache.log4j.Logger;
import org.seamcat.loadsave.WorkspaceSaver;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.Model;
import org.seamcat.model.Workspace;
import org.seamcat.model.engines.InterferenceSimulationEngine;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.result.SingleValueTypes;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.plugin.SandboxInitializer;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.Simulation;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLine {

    private static DecimalFormat numberFormat = new DecimalFormat("0.0##");
    private static final Logger LOG = Logger.getLogger(CommandLine.class);

    private static final String WS = "Workspace";
    private static final String OUTPUT = "result";
    private static final String EVENTS = "events";

    public static void main(String[] args) throws Exception {
        Logging.initialize();
        Logging.initializeFromHomeDir(Model.getSeamcatHomeDir());
        SandboxInitializer.initializeSandbox();

        Map<String, String> commands = new HashMap<>();
        if ( args.length == 0 ) {
            LOG.error("Please provide a workspace file as argument");
            return;
        }
        for (String arg : args) {
            if ( arg.contains("=")) {
                String[] split = arg.split("=");
                commands.put( split[0], split[1]);
            } else {
                commands.put(WS, arg);
            }
        }

        File file = new File(commands.get(WS));
        if ( !file.exists() ) {
            LOG.error("Could not find workspace file: " + file.getAbsolutePath() );
            return;
        }
        try {
            Workspace workspace = Model.openWorkspace(file);
            if ( workspace != null ) {
                if ( commands.containsKey(EVENTS)) {
                    SimulationControl prototype = Factory.prototype(SimulationControl.class, workspace.getSimulationControl());
                    Factory.when(prototype.numberOfEvents()).thenReturn(Integer.parseInt(commands.get(EVENTS)));
                    workspace.setSimulationControl(Factory.build(prototype));
                }

                simulate(workspace, true);

                File result;
                if ( commands.containsKey( OUTPUT )) {
                    result = new File( fileWithExtension(commands.get(OUTPUT)));
                } else {
                    result = new File( fileWithExtension(workspace.getName()));
                }

                WorkspaceSaver workspaceSaver = new WorkspaceSaver(workspace, workspace);
                workspaceSaver.saveToFile(result);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fileWithExtension( String name ) {
        if ( name.endsWith(".swr")) {
            return name;
        }
        if ( name.endsWith(".sws")) {
            return name.substring(0, name.length()-4) + ".swr";
        }

        return name + ".swr";
    }

    private static void simulate(Workspace workspace, boolean logTime ) {

        WorkspaceScenario scenario = new WorkspaceScenario(workspace);
        workspace.prune();
        workspace.setScenario( scenario );
        workspace.prepareSimulate();
        Simulation simulation = new Simulation(workspace, scenario);

        new InterferenceSimulationEngine().simulateInterference( simulation, Model.getSimulationPool() );

        Model.getSimulationPool().getPool().shutdown();
        if ( logTime ) {
            LOG.info("Simulated " + scenario.numberOfEvents()+ " events");
            SimulationResultGroup stats = workspace.getSimulationResults().getSeamcatResult(InterferenceSimulationEngine.STATISTICS);
            List<SingleValueTypes<?>> types = stats.getResultTypes().getSingleValueTypes();
            for (SingleValueTypes<?> type : types) {
                LOG.info( type.toString());
            }
        }
    }

}
