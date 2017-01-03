package org.seamcat.presentation.report;

import org.seamcat.events.VectorValues;
import org.seamcat.model.IdElement;
import org.seamcat.model.InterferenceLinkElement;
import org.seamcat.model.Library;
import org.seamcat.model.Workspace;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.types.result.*;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;
import org.seamcat.simulation.generic.GenericVictimSystemSimulation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    public static List<ReportSection> generate( boolean withScenario, boolean withPre, boolean withResults, boolean expand, Workspace workspace ) {
        List<ReportSection> sections = new ArrayList<>();

        if ( withScenario ) {
            ReportSection scenario = new ReportSection("Scenario");
            sections.add(scenario);
            scenario.addGroup( group("Simulation control", SimulationControl.class, workspace.getSimulationControl()));
            Class<SystemModel> vClass = Library.getSystemModelClass(workspace.getVictimSystem());
            ReportGroup victimGroup = new ReportGroup(sysName("Victim System", workspace.getVictimSystem()));
            scenario.addGroup( victimGroup );
            compositeGroup(victimGroup, vClass, workspace.getVictimSystem());
            for (IdElement<SystemModel> element : workspace.getSystemModels()) {
                if ( !workspace.getVictimSystemId().equals(element.getId())) {
                    ReportGroup system = new ReportGroup(sysName("Interfering System", element.getElement()));
                    scenario.addGroup( system );
                    compositeGroup( system, vClass, workspace.getVictimSystem());
                }
            }
            for (InterferenceLinkElement element : workspace.getInterferenceLinkUIs()) {
                ReportGroup iGroup = new ReportGroup(element.getName());
                scenario.addGroup( iGroup );
                compositeGroup( iGroup, InterferenceLinkUI.class, element.getSettings());
            }
            for (EventProcessingConfiguration epp : workspace.getEventProcessingList()) {
                ReportGroup eGroup = new ReportGroup(epp.getPlugin().description().name());
                scenario.addGroup( eGroup );
                group(eGroup, epp.getModelClass(), epp.getModel());
            }
        }

        if ( withPre ) {
            ReportSection pre = new ReportSection("Pre-simulation Results");
            sections.add(pre);
            results(expand, pre, workspace.getSimulationResults().getSystemPreSimulationResults());
        }

        if ( withResults ) {
            ReportSection results = new ReportSection("Simulation Results");
            sections.add(results);
            results(expand, results, workspace.getSimulationResults().getSeamcatResults());

            List<SimulationResultGroup> epps = workspace.getSimulationResults().getEventProcessingResults();
            if ( epps != null && epps.size() > 0 ) {
                ReportSection eppResults = new ReportSection("Event Processing Results");
                sections.add(eppResults);
                results(expand, eppResults, epps);
            }
        }

        return sections;
    }


    private static void results( boolean expand, ReportSection section, List<SimulationResultGroup> groups ) {
        for (SimulationResultGroup group : groups) {
            ReportGroup g = new ReportGroup(group.getName());
            section.addGroup( g );
            for (SingleValueTypes<?> value : group.getResultTypes().getSingleValueTypes()) {
                g.addValue( value.getName(), value.getValue(), value.getUnit());
            }
            for (VectorResultType type : group.getResultTypes().getVectorResultTypes()) {
                vector(g, expand, type);
            }
            for (VectorGroupResultType type : group.getResultTypes().getVectorGroupResultTypes()) {
                g.addValue(type.getName(), "Vector group["+ type.size()+"]", type.getUnit());
                for (NamedVectorResult result : type.getVectorGroup()) {
                    vector(g, expand, new VectorResultType(result.getName(), type.getUnit(), result.getVector().asArray()));
                }
            }
            for (BarChartResultType type : group.getResultTypes().getBarChartResultTypes()) {
                g.addValue(type.getTitle(), "Bar chart["+type.getChartPoints().size()+"]", type.getxLabel()+"/"+type.getyLabel());
                if ( expand ) {
                    for (BarChartValue value : type.getChartPoints()) {
                        g.addValue("", value.getName(), ""+value.getValue());
                    }
                }
            }
            for (ScatterDiagramResultType type : group.getResultTypes().getScatterDiagramResultTypes()) {
                g.addValue(type.getTitle(), "Scatter["+type.getScatterPoints().size()+"]", type.getxLabel()+"/"+type.getyLabel());
                if ( expand ) {
                    for (Point2D point : type.getScatterPoints()) {
                        g.addValue("", point.getX(), ""+point.getY());
                    }
                }
            }
        }
    }


    private static void vector( ReportGroup g, boolean expandVectors, VectorResultType type) {
        VectorValues values = GenericVictimSystemSimulation.calculate(type.getValue().asArray());
        if ( values != null ) {
            g.addValue( type.getName(), "Vector[" + type.getValue().size()+"]", type.getUnit());
            g.addValue( "Average", values.getMean());
            g.addValue( "Median", values.getMedian());
            g.addValue( "Std.dev.", values.getStdDev());
            if ( expandVectors ) {
                g.addValue( "Vector values", "");
                double[] asArray = type.getValue().asArray();
                for (int i = 0; i < asArray.length; i++) {
                    g.addValue("", ""+i, ""+asArray[i]);
                }
            }
        }
    }

    private static String sysName( String type, SystemModel model) {
        return type + " (" + model.description().name() + ")";
    }

    private static void compositeGroup( ReportGroup group, Class<?> composite, Object instance) {
        try {
            for (Method m : Cache.ordered(composite)) {
                UITab tab = m.getAnnotation(UITab.class);
                if (tab != null) {
                    group.addValue(tab.value(), "");
                    compositeGroup(group, m.getReturnType(), m.invoke(instance));
                }

                UIPosition pos = m.getAnnotation(UIPosition.class);
                if ( pos != null) {
                    Object invoke = m.invoke(instance);
                    if ( invoke instanceof PluginConfiguration ) {
                        PluginConfiguration conf = (PluginConfiguration) invoke;
                        group.addValue(pos.name(), conf.description().name());
                        group(group, conf.getModelClass(), conf.getModel());
                    } else {
                        group.addValue( pos.name(), "");
                        group( group, m.getReturnType(), invoke);
                    }
                }
            }
        } catch (Exception e) {
            //
        }

    }

    private static ReportGroup group( String title, Class<?> clazz, Object instance ) {
        ReportGroup group = new ReportGroup(title);
        return group( group, clazz, instance );
    }

    private static ReportGroup group( ReportGroup group, Class<?> clazz, Object instance ) {
        for ( Method m : Cache.orderedConfig(clazz)) {
            try {
                Config config = m.getAnnotation(Config.class);
                Object invoke = m.invoke(instance);
                if ( invoke instanceof PluginConfiguration) {
                    PluginConfiguration conf = (PluginConfiguration) invoke;
                    group.addValue(Library.name(conf), conf.description().name());
                    group(group, conf.getModelClass(), conf.getModel());
                } else {
                    group.addValue( config.name(), invoke, config.unit());
                }
            } catch (Exception e) {
                // skip
            }
        }

        return group;
    }
}
