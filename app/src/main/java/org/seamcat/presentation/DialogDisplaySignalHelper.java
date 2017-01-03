package org.seamcat.presentation;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.Range;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.propagationtest.PropagationHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogDisplaySignalHelper {

	/**
	 * Compiles and sets the different types of data series for each chart type
	 */
	public static void setDataSets(List<PropagationHolder> propagations, boolean signalIsConstant) {
		for (PropagationHolder propagationHolder : propagations) {
			setXYSeries(propagationHolder);
			setCumulativeSeries(propagationHolder, signalIsConstant);
			setDesityDataSet(propagationHolder, signalIsConstant);
		}

	}

    public static HistogramDataset getDensityHistogram(List<PropagationHolder> propagations, int bin) {
        HistogramDataset collection = new HistogramDataset();
        DensitySeries densitySeries;
        for (PropagationHolder propagationHolder : propagations) {
            densitySeries = propagationHolder.getDensityDataSeries();
            if (densitySeries != null && propagationHolder.getData().length > 0) {
                collection.addSeries(densitySeries.comparableKey,propagationHolder.getData(), bin);
            }
        }
        collection.setType(HistogramType.RELATIVE_FREQUENCY);
        return collection;
    }

	public static HistogramDataset getDensityHistogram(List<PropagationHolder> propagations) {
		HistogramDataset collection = new HistogramDataset();
		DensitySeries densitySeries;
		for (PropagationHolder propagationHolder : propagations) {
			densitySeries = propagationHolder.getDensityDataSeries();
			if (densitySeries != null && propagationHolder.getData().length > 0) {
				collection.addSeries(densitySeries.comparableKey,propagationHolder.getData(), densitySeries.bin);
			}
		}
		collection.setType(HistogramType.RELATIVE_FREQUENCY);
		return collection;
	}

	private static double[] getMinAndMaxSortedDistribution(
	      List<PropagationHolder> propagations) {
		double[] sortedDistributions;
		double[] values = new double[2];
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (PropagationHolder propagationHolder : propagations) {
			sortedDistributions = propagationHolder.getSortedDistributions();
			min = (sortedDistributions[0] < min) ? sortedDistributions[0] : min;
			max = (sortedDistributions[sortedDistributions.length - 1] > max) ? sortedDistributions[sortedDistributions.length - 1]
			      : max;
		}
		values[0] = min;
		values[1] = max;
		return values;
	}

	private static int getLongestDisplayData(List<PropagationHolder> propagations) {
		int lenght = Integer.MIN_VALUE;
		for (PropagationHolder propagationHolder : propagations) {
			lenght = (propagationHolder.getDisplayData().length > lenght) ? propagationHolder
			      .getDisplayData().length : lenght;
		}
		return lenght;
	}

	private static double getMinumimLogDomainValue(
	      List<PropagationHolder> propagations, double minDataValue) {
		double rangeStart = Double.MAX_VALUE;
		double[] sortedDistributions;
		double tmp;
		for (PropagationHolder propagationHolder : propagations) {
			sortedDistributions = propagationHolder.getSortedDistributions();
			tmp = Mathematics.getMinimumLogDomainValue(sortedDistributions[0],
			      minDataValue);
			rangeStart = (tmp < rangeStart) ? tmp : rangeStart;
		}
		return rangeStart;
	}

	private static double[] getMinAndMax(List<PropagationHolder> propagations) {
		double max = Integer.MIN_VALUE;
		double min = Integer.MAX_VALUE;
		double[] minAndMax = new double[2];

		for (PropagationHolder propagationHolder : propagations) {
			max = (max > propagationHolder.getMaxAxis()) ? max : propagationHolder.getMaxAxis();
			min = (min < propagationHolder.getMinAxis()) ? min : propagationHolder.getMinAxis();

		}
		minAndMax[0] = min;
		minAndMax[1] = max;
		return minAndMax;
	}

	private static void setCumulativeSeries(PropagationHolder propagationHolder,
	      boolean signalIsConstant) {
		XYSeries series;
		double[] displayData = propagationHolder.getDisplayData();
		double[] sortedDisplayData;

		series = new XYSeries(propagationHolder.getTitle());

		if (displayData.length > 0) {
			if (signalIsConstant) {
				series.add(displayData[0], 0);
				series.add(displayData[0], 1);
			} else {
				sortedDisplayData = new double[displayData.length];
				System.arraycopy(displayData, 0, sortedDisplayData, 0,
				      displayData.length);

				Arrays.sort(sortedDisplayData);
				for (int j = 0; j < sortedDisplayData.length; j++) {
					series.add(sortedDisplayData[j], new Double((double) j
					      / (double) sortedDisplayData.length));
				}

			}
		}
		propagationHolder.setCumulativeDataSeries(series);
	}

	private static void setDesityDataSet(PropagationHolder propagationHolder, boolean signalIsConstant) {

		if (!signalIsConstant) {
			double[] data = propagationHolder.getData();
			DensitySeries densitySeries = new DensitySeries();
			densitySeries.bin = (int) Math.sqrt(data.length);
			densitySeries.comparableKey = propagationHolder.getTitle();
			propagationHolder.setDensityDataSeries(densitySeries);
		}

	}

	private static void setXYSeries(PropagationHolder propagationHolder) {
		XYSeries series;
		double[] displayData;
		double[] sortedDistributions;

		series = new XYSeries(propagationHolder.getTitle());
		displayData = propagationHolder.getDisplayData();

		sortedDistributions = propagationHolder.getSortedDistributions();
		for (int j = 0; j < displayData.length; j++) {
			if (sortedDistributions == null) {
				series.add(j, displayData[j]);
			} else {
				series.add(sortedDistributions[j], displayData[j]);
			}
		}
		propagationHolder.setVectorDataSeries(series);
	}

	public static XYSeriesCollection getVectorSeriesCollection( List<PropagationHolder> propagations) {
		XYSeriesCollection collection = new XYSeriesCollection();
		Map<String, Integer> unique = new HashMap<>();
        for (PropagationHolder propagationHolder : propagations) {
			String prefix = "";
            if (unique.containsKey(propagationHolder.getTitle())) {
                Integer counter = unique.get(propagationHolder.getTitle());
                counter++;
                prefix = " " + counter;
                unique.put( propagationHolder.getTitle(), counter);
            } else {
                unique.put( propagationHolder.getTitle(), 1);
            }
            propagationHolder.setTitle( propagationHolder.getTitle() + prefix );
            collection.addSeries(propagationHolder.getVectorDataSeries());
		}
		return collection;
	}

	public static XYSeriesCollection getCumulativeSeriesCollection(
	      List<PropagationHolder> propagations) {
		XYSeriesCollection collection = new XYSeriesCollection();
		for (PropagationHolder propagationHolder : propagations) {
			collection.addSeries(propagationHolder.getCumulativeDataSeries());
		}

		return collection;
	}

	public static void setVectorDataRange(List<PropagationHolder> propagations,
	      boolean signalIsConstant, ChartPanel vectorChart) {
		NumberAxis x = (NumberAxis) vectorChart.getChart().getXYPlot()
		      .getDomainAxis();
		NumberAxis y = (NumberAxis) vectorChart.getChart().getXYPlot()
		      .getRangeAxis();
		x.setAutoRange(false);

		Range xRange;
		Range yRange;

		if (propagations.get(0).getSortedDistributions() == null) {
			xRange = new Range(0, getLongestDisplayData(propagations));
		} else {
			double[] minAndMax = getMinAndMaxSortedDistribution(propagations);
			xRange = new Range(minAndMax[0], minAndMax[1]);

		}

		x.setRange(xRange);
        if ( getLongestDisplayData(propagations) == 1) {
            signalIsConstant = true;
        }

		if (!signalIsConstant) {
			y.setAutoRange(false);
			double[] minAndMax = getMinAndMax(propagations);
			yRange = new Range(minAndMax[0], minAndMax[1]);
			if(Math.abs(minAndMax[0] - minAndMax[1])<0.001){
				y.setRange(minAndMax[0] - 0.1, minAndMax[1] + 0.1);
			} else {
				y.setRange(yRange);
			}
		} else {
			y.setAutoRange(true);
		}
	}

	public static void setLogDataRange(List<PropagationHolder> propagations,
	      boolean isUniform, double minDataValue, double maxDataValue,
	      ChartPanel vectorLogChart, boolean hasSortedDistributions) {
		LogAxis xLog = (LogAxis) vectorLogChart.getChart().getXYPlot()
		      .getDomainAxis();
		xLog.setAutoRange(false);
		double[] sortedDistributions, displayData;
		Range range;
		if (hasSortedDistributions) {

			if (isUniform) {
				double rangeStart = getMinumimLogDomainValue(propagations, minDataValue);
				xLog.setRange(rangeStart, maxDataValue);
			} else {
				double[] minAndMax = getMinAndMaxSortedDistribution(propagations);
				range = new Range(minAndMax[0], minAndMax[1]);
			}
		}

        for (PropagationHolder propagation : propagations) {
            sortedDistributions = propagation.getSortedDistributions();
            displayData = propagation.getDisplayData();
            if (sortedDistributions == null) {
                range = new Range(0, displayData.length);
                xLog.setRange(range);
            } else {
                xLog.setRange(new Range(sortedDistributions[0],
                        sortedDistributions[sortedDistributions.length - 1]));
            }
        }
	}

	public static void setDataLimit(int limit,
	      List<PropagationHolder> propagations) {

		double[] data, displayData;

		for (PropagationHolder holder : propagations) {
			data = holder.getData();

			if (data.length >= limit) {
				int factor = data.length / limit;
				displayData = new double[Math.min(data.length, limit)];
				for (int i = 0, j = 0; j < displayData.length; j++, i += factor) {
					displayData[j] = data[i];
				}
			} else {
				displayData = data;
			}

			holder.setDisplayData(displayData);
		}

	}

	public static void setPropagationStatistics(
	      List<PropagationHolder> propagations, double minDataValue,
	      double maxDataValue, boolean isUniform) {

		boolean useDataValueBounds = (minDataValue != maxDataValue) && !isUniform;
		double[] data, displayData;
		double stddev;

		for (PropagationHolder propagation : propagations) {
			if ( propagation == null ) continue;
            data = propagation.getData();

			if (data == null) {
				return;
			}
			propagation.setMedian(getMedian(data));

			if (useDataValueBounds) {
				propagation.setAverage(getMeanWithBounds(data, minDataValue, maxDataValue));
				propagation.setStandardDeviation(getStandardDeviationWithBounds(data, propagation.getAverage(), minDataValue, maxDataValue));
			} else {
				propagation.setAverage(Mathematics.getAverage(data, data.length));
				propagation.setStandardDeviation(Mathematics.getStdDev(data,propagation.getAverage()));
			}
			displayData = propagation.getDisplayData();
            stddev = propagation.getStandardDeviation();
            propagation.setVariance( stddev * stddev );
            propagation.setMin(Mathematics.min(displayData));
			propagation.setMax(Mathematics.max(displayData));
			propagation.setMinAxis(Mathematics.min(displayData) - stddev);// only for display purpose
			propagation.setMaxAxis(Mathematics.max(displayData) + stddev);
		}
	}

	private static double getMeanWithBounds(double[] data, double minDataValue,
	      double maxDataValue) {
		return Mathematics.getAverage(data, data.length, minDataValue,
		      maxDataValue);
	}

	private static double getStandardDeviationWithBounds(double[] data,
	      double mean, double minDataValue, double maxDataValue) {
		return Mathematics.getStdDev(data, mean, data.length, minDataValue,
		      maxDataValue);
	}

	private static double getMedian(double[] data) {
		return Mathematics.getMedian(data, data.length, false);
	}
}
