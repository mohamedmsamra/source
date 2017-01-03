package org.seamcat.presentation.components;

import org.jfree.data.general.DatasetChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class DiscreteFunctionPlot extends JPanel implements DatasetChangeListener {

    public abstract void setAxisNames(String xAxis, String yAxis);

    public abstract DiscreteFunctionTableModelAdapter getDataSet();

    public abstract void saveChartImage();

    public abstract void drawGraphToGraphics(Graphics2D g, Rectangle2D r);
}
