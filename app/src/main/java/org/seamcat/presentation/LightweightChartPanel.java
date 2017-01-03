package org.seamcat.presentation;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
* Extends ChartPanel to provide extra or faster functionality.
* - Removes EntityCollection to save memory (especially on large datasets).
* - Dynamically determines the ChartEntity.
*/
public class LightweightChartPanel extends ChartPanel
{
   public LightweightChartPanel(JFreeChart chart)
   {
      this(chart,
             DEFAULT_WIDTH,
             DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH,
             DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH,
             DEFAULT_MAXIMUM_DRAW_HEIGHT,
             DEFAULT_BUFFER_USED,
             true,  // properties
             true,  // save
             true,  // print
             true,  // zoom
             true   // tooltips
       );
   }
   
   public LightweightChartPanel(JFreeChart chart, boolean useBuffer)
   {
      this(chart,
             DEFAULT_WIDTH,
             DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH,
             DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH,
             DEFAULT_MAXIMUM_DRAW_HEIGHT,
             useBuffer,
             true,  // properties
             true,  // save
             true,  // print
             true,  // zoom
             true   // tooltips
        );
   }
   
   public LightweightChartPanel(JFreeChart chart,
                  int width, int height,
                  int minimumDrawWidth, int minimumDrawHeight,
                  int maximumDrawWidth, int maximumDrawHeight,
                  boolean useBuffer,
                  boolean properties, boolean save, boolean print, boolean zoom,
                  boolean tooltips)
   {
      super(chart,
           width,
           height,
           minimumDrawWidth,
           minimumDrawHeight,
           maximumDrawWidth,
           maximumDrawHeight,
           useBuffer,
           properties,
           save,
           print,
           zoom,
           tooltips
      );
      
      // Disable inefficient EntityCollection
      this.getChartRenderingInfo().setEntityCollection(null);
   }

   /**
    * Gets the tooltip dynamically instead of from the EntityCollection.
    */
   public String getToolTipText(MouseEvent e)
   {
      ChartEntity entity = this.getChartEntityForPoint(e.getPoint());
      if (entity != null)
         return entity.getToolTipText();
      return null;
   }
   
   /**
     * - No longer setNotify(true) to force a redraw
     * - Dynamically creates a ChartEntity instead of using this.info
     */
   public void mouseClicked(MouseEvent event)
   {
      Insets insets = this.getInsets();
        int x = (int) ((event.getX() - insets.left) / this.getScaleX());
        int y = (int) ((event.getY() - insets.top) / this.getScaleY());

        this.setAnchor(new Point2D.Double(x, y));
        if (this.getChart() == null) {
            return;
        }

        Object[] listeners = this.getListeners(ChartMouseListener.class);
        if (listeners.length == 0) {
            return;
        }

        /* Create custom entity */
        ChartEntity entity = this.getChartEntityForPoint(event.getPoint());
        
        ChartMouseEvent chartEvent = new ChartMouseEvent(this.getChart(), event, entity);
        for (int i = listeners.length - 1; i >= 0; i -= 1) {
            ((ChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
        }
    }
   
    protected static int HOTSPOT_SIZE = 5;

    /**
     * Gets the ChartEntity for the corresponding point.
     * It converts the screen (X, Y) into chart area (X, Y)
     * and then looks for a data item that lies inside the hotspot.
     * 
     * Inspired by http://www.jfree.org/phpBB2/viewtopic.php?p=69588#69588
     */
    public ChartEntity getChartEntityForPoint(Point point)
    {
       XYPlot xyPlot = null;
        Rectangle2D screenArea = null;
       try
        {
          xyPlot = this.getChart().getXYPlot();
           screenArea = this.scale(this.getChartRenderingInfo().getPlotInfo().getDataArea());
        }
        catch (Exception e)
        {
           return null;
        }

        double hotspotSizeX = HOTSPOT_SIZE * this.getScaleX();
        double hotspotSizeY = HOTSPOT_SIZE * this.getScaleY();
        double x0 = point.getX();
        double y0 = point.getY();
        double x1 = x0 - hotspotSizeX;
        double y1 = y0 + hotspotSizeY;
        double x2 = x0 + hotspotSizeX;
        double y2 = y0 - hotspotSizeY;
        RectangleEdge xEdge = RectangleEdge.BOTTOM;
        RectangleEdge yEdge = RectangleEdge.LEFT;
        
        // Switch everything for horizontal charts
        if (xyPlot.getOrientation() == PlotOrientation.HORIZONTAL)
        {
            hotspotSizeX = HOTSPOT_SIZE * this.getScaleY();
            hotspotSizeY = HOTSPOT_SIZE * this.getScaleX();
            x0 = point.getY();
            y0 = point.getX();
            x1 = x0 + hotspotSizeX;
            y1 = y0 - hotspotSizeY;
            x2 = x0 - hotspotSizeX;
            y2 = y0 + hotspotSizeY;
            xEdge = RectangleEdge.LEFT;
            yEdge = RectangleEdge.BOTTOM;
        }

        // Loop through each dataset
        int datasetCount = xyPlot.getDatasetCount();
        for (int datasetIndex = 0; datasetIndex < datasetCount; datasetIndex++)
        {
           ValueAxis domainAxis = xyPlot.getDomainAxisForDataset(datasetIndex);
            ValueAxis rangeAxis  = xyPlot.getRangeAxisForDataset(datasetIndex);
            
            double tx1 = domainAxis.java2DToValue(x1, screenArea, xEdge);
            double ty1 = rangeAxis.java2DToValue(y1, screenArea, yEdge);
            double tx2 = domainAxis.java2DToValue(x2, screenArea, xEdge);
            double ty2 = rangeAxis.java2DToValue(y2, screenArea, yEdge);
           
           XYDataset dataset = xyPlot.getDataset(datasetIndex);
           if (dataset != null)
           {
              // Loop through each series
               int seriesCount = dataset.getSeriesCount();
               for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++)
               {
                   int itemCount = dataset.getItemCount(seriesIndex);
   
                   // Loop through each item
                   for (int item = 0; item < itemCount; item++)
                   {
                       double xValue = dataset.getXValue(seriesIndex, item);
                       double yValue = dataset.getYValue(seriesIndex, item);
                       
                       // Does the data point (X, Y) lie in the hotspot (tx1 <= xValue <= tx2) (ty1 <= yValue <= ty2)
                       if ((tx1 <= xValue) && (xValue <= tx2) && (ty1 <= yValue) && (yValue <= ty2))
                       {
                          String tooltip = null;
                          try
                          {
                             tooltip = xyPlot.getRenderer(datasetIndex).getToolTipGenerator(seriesIndex, item).generateToolTip(dataset, seriesIndex, item);
                          }
                          catch (Exception ignore) {}
                          
                          return new XYItemEntity(new Rectangle(), dataset, seriesIndex, item, tooltip, null);
                       }
                   }
               }
           }
        }

        return null;
    }
}
