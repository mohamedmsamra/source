package org.seamcat.presentation;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.IntervalXYDelegate;
import org.jfree.data.xy.XYDataset;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.FunctionException;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.components.DiscreteFunction2TableModelAdapter;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.seamcat.model.mathematics.Mathematics.dB2Linear;
import static org.seamcat.model.mathematics.Mathematics.linear2dB;

public class AclrDatasetWrapper extends DiscreteFunction2TableModelAdapter implements IntervalXYDataset, XYItemLabelGenerator {

	public static final double DEFAULT_ADJACENT_CHANNEL = 1d; // MHz
	protected int numberOfChannels;
	private double min;
	private double max;
	
	private IntervalXYDelegate delegate;

	private double interfererBandwidth = 1.25;
	private double adjacentChannel = DEFAULT_ADJACENT_CHANNEL;
	
	// Datamodel
	private final List<Channel> channels = new LinkedList<Channel>();

	public AclrDatasetWrapper(DiscreteFunction2TableModelAdapter origData) {
		super(origData.getFunction());
		delegate = new IntervalXYDelegate(this, false);
	}

	protected void update() {
		setDiscreteFunction2((EmissionMaskImpl) getFunction().normalize());
		min = getFunction().getBounds().getMin();
		max = getFunction().getBounds().getMax();

		double p0;
        p0 = linear2dB(dB2Linear(function.integrate(0, interfererBandwidth)) * interfererBandwidth);

      double halfIBw = interfererBandwidth / 2;
      
      channels.clear();
      try {
			if (adjacentChannel == 0d) {
				throw new IllegalArgumentException("AdjacentChannel is zero");
			}
			else if (adjacentChannel > 0) {
				double numberOfChannelsD = (max - adjacentChannel) / interfererBandwidth;
				numberOfChannels = (int)numberOfChannelsD;
				
				int channelno = 1;
				for (double x = adjacentChannel + halfIBw; x <= max; x += interfererBandwidth, channelno++) {
			      	addChannel(channelno, p0, x);
				}
			}
			else {
				numberOfChannels = (int)(abs((min - adjacentChannel)) / interfererBandwidth);
				int channelno = 1;
				for (double x = adjacentChannel - halfIBw; x >= min; x -= interfererBandwidth, channelno++) {
		      	addChannel(channelno, p0, x);
				}
			}
      } catch (FunctionException ex) {
      	throw new IllegalStateException("Trying to integrate x outside function bounds");
      }
	}
	
	private void addChannel(int channelno, double p0, double x) throws FunctionException {
   	double y = p0 - linear2dB(
            dB2Linear(
                    function.integrate(x, interfererBandwidth)
            ) * interfererBandwidth
    );
		channels.add(new Channel(channelno, x, y));
	}
	
	@Override
   public int getItemCount(int series) {
	   return numberOfChannels;
   }

	@Override
   public int getSeriesCount() {
	   return 1;
   }

	@Override
   public Comparable getSeriesKey(int arg0) {
	   return "ACLR";
   }

	private Channel getChannel(int channelno) {
		return channels.get(channelno);
	}
	
	private boolean hasChannel(int channelno) {
		return channelno < channels.size();
	}
	
	@Override
   public double getXValue(int series, int item) {
	   return hasChannel(item) ? getChannel(item).x : 0d;
   }

	@Override
   public double getYValue(int series, int item) {
		return hasChannel(item) ? getChannel(item).y : 0d;
   }

	@Override
   public Number getEndX(int series, int item) {
	   return delegate.getEndX(series, item);
   }

	@Override
   public double getEndXValue(int series, int item) {
		try {
			return delegate.getEndXValue(series, item);
		 } catch (Exception e) {
		   	return 0;
		   }
   }

	@Override
   public Number getEndY(int series, int item) {
		return getY(series, item);
	}

	@Override
   public double getEndYValue(int series, int item) {
	   try {
	   	return getYValue(series, item);
	   } catch (Exception e) {
	   	return 0;
	   }
   }

	@Override
   public Number getStartX(int series, int item) {
		return delegate.getStartX(series, item);
	}

	@Override
   public double getStartXValue(int series, int item) {
		return delegate.getStartXValue(series, item);
	}

	@Override
   public Number getStartY(int series, int item) {
	   return getY(series, item);
   }

	@Override
   public double getStartYValue(int series, int item) {
	   try {
	   	return getYValue(series, item);
	   } catch (Exception e) {
	   	return 0;
	   }
   }
	
	@Override
   public String generateLabel(XYDataset dataset, int series, int item) {
		Channel c = getChannel(item);
		return format("%d. Adjacent Channel (%10.3f dB)", c.channelNo, Mathematics.round(c.y));
   }

	public void setInterfererBandwidth(double valueInMHz) {
		interfererBandwidth = valueInMHz;
		delegate.setFixedIntervalWidth(interfererBandwidth);
		update();
   }

	public void setAdjacentChannel(double valueInMHz) {
		adjacentChannel = valueInMHz;
		update();
	}
	
	private static class Channel {
		public final int channelNo;
		public final double x;
		public final double y;
		
		private Channel(int channelNo, double x, double y) {
			this.channelNo = channelNo;
			this.x = x;
			this.y = y;
		}
		
	}
}
