package org.seamcat.presentation.propagationtest;

import org.jfree.data.xy.XYSeries;
import org.seamcat.presentation.DensitySeries;

public class PropagationHolder {

   private double average;
   private double standardDeviation;
   private double variance;
   private double median;
   private String title;
   private double[] data;
   private double[] sortedDistributions;
   private String sortedTitle;
   private double min;
   private double max;
   private double minAxis;
   private double maxAxis;
   private XYSeries vectorDataSeries;
   private XYSeries cumulativeDataSeries;
   public int binCount;


   private DensitySeries densityDataSeries;
   private double[] displayData;


   public double getAverage() {
      return average;
   }

   public void setAverage(double average) {
      this.average = average;
   }

   public double getStandardDeviation() {
      return standardDeviation;
   }

   public void setStandardDeviation(double standardDeviation) {
      this.standardDeviation = standardDeviation;
   }

   public double getMedian() {
      return median;
   }

   public void setMedian(double median) {
      this.median = median;
   }

   public double[] getData() {
      return data;
   }

   public void setData(double[] data) {
      this.data = data;
   }

   public double[] getSortedDistributions() {
      return sortedDistributions;
   }

   public void setSortedDistributions(double[] sortedDistributions) {
      this.sortedDistributions = sortedDistributions;
   }

   public String getTitle() {
      return title;
   }


   public void setTitle(String title) {
      this.title = title;
       if ( vectorDataSeries != null ) {
           vectorDataSeries.setKey( title );
       }
       if ( cumulativeDataSeries != null ) {
           cumulativeDataSeries.setKey( title );
       }
   }


   public double getMin() {
      return min;
   }

   public double getMinAxis() {
      return minAxis;
   }

   public void setMin(double min) {
      this.min = min;
   }

   public void setMinAxis(double minAxis) {
      this.minAxis = minAxis;
   }


   public double getMax() {
      return max;
   }

   public double getMaxAxis() {
      return maxAxis;
   }

   public void setMax(double max) {
      this.max = max;
   }

   public void setMaxAxis(double maxAxis) {
      this.maxAxis = maxAxis;
   }


   public double[] getDisplayData() {
      return displayData;
   }


   public void setDisplayData(double[] displayData) {
      this.displayData = displayData;
   }


   public XYSeries getVectorDataSeries() {
      return vectorDataSeries;
   }


   public void setVectorDataSeries(XYSeries dataSeries) {
      this.vectorDataSeries = dataSeries;
   }



   public XYSeries getCumulativeDataSeries() {
      return cumulativeDataSeries;
   }


   public void setCumulativeDataSeries(XYSeries cumulativeDataSeries) {
      this.cumulativeDataSeries = cumulativeDataSeries;
   }


   public DensitySeries getDensityDataSeries() {
      return densityDataSeries;
   }


   public void setDensityDataSeries(DensitySeries densityDataSeries) {
      this.densityDataSeries = densityDataSeries;
   }

   @Override
   public String toString() {
      return title;
   }

   public void setSortedTitle(String sortedTitle) {
      this.sortedTitle = sortedTitle;
   }

   public String getSortedTitle() {
      return sortedTitle;
   }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }
}