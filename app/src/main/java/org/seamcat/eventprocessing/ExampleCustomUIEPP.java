package org.seamcat.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.eventprocessing.CustomUI;
import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.plugin.eventprocessing.Panels;
import org.seamcat.model.plugin.eventprocessing.PostProcessing;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.model.types.result.*;
import org.seamcat.presentation.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExampleCustomUIEPP implements CustomUI {

    private Panels panels;

    public interface Left {
        @Config(order = 1, name = "Value")
        double value();

        @Config(order = 2, name = "Function")
        Function function();

        @Config(order = 3, name = "Antenna")
        AntennaGain antenna();
    }

    public interface Right {
        @Config(order = 1, name = "Propagation")
        PropagationModel pm();
    }

    @Override
    public PanelDefinition[] panelDefinitions() {
        return new PanelDefinition[]{
                new PanelDefinition("Parameters", Left.class),
                new PanelDefinition("Propagation Model", Right.class)
        };
    }

    @Override
    public String getTitle() {
        return "Result types generator";
    }

    @Override
    public void buildUI(JPanel canvas, Panels panels) {
        this.panels = panels;
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.add(panels.get("Parameters").getPanel());
        split.add(panels.get("Propagation Model").getPanel());
        canvas.setLayout( new BorderLayout() );
        canvas.add( split, BorderLayout.CENTER );
    }

    private void popupDialog( String type ) {
        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                "Created random " + type + ". See the value in Results tab",
                "Result generated",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @PostProcessing(order = 1, name="Scatter")
    public void scatter(Scenario scenario, ResultTypes allResults) {
        ScatterDiagramResultType type = new ScatterDiagramResultType("Scatter", "x", "y");
        Random random = new Random();
        for ( int i=0; i<25; i++) {
            type.getScatterPoints().add(new Point2D(i, i*random.nextDouble()));
        }
        allResults.getScatterDiagramResultTypes().add(type);
        popupDialog("scatter plot");
    }

    @PostProcessing(order = 2, name = "Bar chart")
    public void bar(Scenario scenario, ResultTypes allResults) {
        BarChartResultType type = new BarChartResultType("Bar chart", "x", "y");
        Random random = new Random();
        for ( int i=0; i<10; i++) {
            type.getChartPoints().add(new BarChartValue(""+i, i*random.nextDouble()));
        }

        allResults.getBarChartResultTypes().add(type);
        popupDialog("bar chart");
    }

    @PostProcessing(order = 3, name = "Vector Group")
    public void vectorGroup(Scenario scenario, ResultTypes allResults) {
        VectorGroupResultType type = new VectorGroupResultType("Vector group", "dBm");

        Random random = new Random();
        for ( int i=0; i<4; i++) {
            List<Double> vector = new ArrayList<Double>();
            for ( int j=0; j<150; j++) {
                vector.add( random.nextDouble());
            }
            type.addVector(new NamedVectorResult("Vector " + (i + 1), new VectorResult(vector)));
        }

        allResults.getVectorGroupResultTypes().add(type);
        popupDialog("vector group");
    }

}
