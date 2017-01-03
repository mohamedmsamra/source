package org.seamcat.model.core;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.xy.XYSeriesCollection;
import org.seamcat.presentation.Argument;
import org.seamcat.presentation.ExtendableXYSeries;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ScenarioOutlineModel extends XYSeriesCollection{

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
	private XYSeriesCollection dataset = new XYSeriesCollection();

	private ExtendableXYSeries itSeries;
	private ExtendableXYSeries wrSeries;
	private ExtendableXYSeries wtSeries;
	private ExtendableXYSeries vrSeries;

	private double lower = Double.MAX_VALUE;
	private double upper = Double.MIN_VALUE;

	private void init() {
		itSeries = new ExtendableXYSeries(STRINGLIST.getString("INTERFERING_TRANSMITTER_SERIES_TITLE"));
		itSeries.setType(STRINGLIST.getString("INTERFERING_TRANSMITTER_SERIES_TITLE"));
		wrSeries = new ExtendableXYSeries(STRINGLIST.getString("WANTED_RECEIVER_SERIES_TITLE"));
		wrSeries.setType(STRINGLIST.getString("WANTED_RECEIVER_SERIES_TITLE"));
		wtSeries = new ExtendableXYSeries(STRINGLIST.getString("WANTED_TRANSMITTER_SERIES_TITLE"));
		wtSeries.setType(STRINGLIST.getString("WANTED_TRANSMITTER_SERIES_TITLE"));
		vrSeries = new ExtendableXYSeries(STRINGLIST.getString("VICTIM_RECEIVER_SERIES_TITLE"));
		vrSeries.setType(STRINGLIST.getString("VICTIM_RECEIVER_SERIES_TITLE"));
	}

    public ScenarioOutlineModel( List<ExtendableXYSeries> seriesList ){
        for (ExtendableXYSeries series : seriesList ) {
            setSeries( series );
        }
        setDataSeries();
    }

	public ScenarioOutlineModel() {
		init();
		setDataSeries();
	}

	public void setInterferingTransmitterTitle(String title) {
		itSeries.setKey(title);
	}

	public void setInterferingReceiverTitle(String title) {
		wrSeries.setKey(title);
	}

	public void setVictimTransmitterTitle(String title) {
		wtSeries.setKey(title);
	}

	public void setVictimReceiverTitle(String title) {
		vrSeries.setKey(title);

	}

	private void setDataSeries() {
		addSeries(itSeries);
		addSeries(wtSeries);
		addSeries(wrSeries);
		addSeries(vrSeries);
	}

	private void setUpperAndLower(double x, double y) {
		if (x < lower) {
			lower = x;
		}
		if (x > upper) {
			upper = x;
		}

		if (y < lower) {
			lower = y;
		}
		if (y > upper) {
			upper = y;
		}
	}

	public void clearAllElements() {
		itSeries.clear();
		dataset.seriesChanged(new SeriesChangeEvent(itSeries));
		vrSeries.clear();
		dataset.seriesChanged(new SeriesChangeEvent(vrSeries));
		wrSeries.clear();
		dataset.seriesChanged(new SeriesChangeEvent(wrSeries));
		wtSeries.clear();
		dataset.seriesChanged(new SeriesChangeEvent(wtSeries));
	}

	public int getWantedTransmitterItemCount() {
		return wtSeries.getItemCount();
	}

	public int getWantedReceiverItemCount() {
		return wrSeries.getItemCount();
	}

	public int getInterferingTransmitterItemCount() {
		return itSeries.getItemCount();
	}

	public int getVictimReceiverItemCount() {
		return vrSeries.getItemCount();
	}

	public void addToWantedTransmitterSeries(double x, double y, Argument... args) {
		wtSeries.add(x, y, args);
		setUpperAndLower(x, y);
	}

	public void addToWantedReceiverSeries(double x, double y, Argument... args) {
		wrSeries.add(x, y, args);
		setUpperAndLower(x, y);
	}

	public void addToInterferingTransmitterSeries(double x, double y, Argument... args) {
		itSeries.add(x, y, args);
		setUpperAndLower(x, y);
	}

	public void addToVictimReceiverSeries(double x, double y, Argument... args) {
		vrSeries.add(x, y, args);
		setUpperAndLower(x, y);
	}

	private void setSeries(ExtendableXYSeries series) {
		String key = series.getType();
		if (STRINGLIST.getString("INTERFERING_TRANSMITTER_SERIES_TITLE").equals(key)) {
			itSeries = series;
		} else if (STRINGLIST.getString("WANTED_RECEIVER_SERIES_TITLE").equals(key)) {
			wrSeries = series;
		} else if (STRINGLIST.getString("WANTED_TRANSMITTER_SERIES_TITLE").equals(key)) {
			wtSeries = series;
		} else if (STRINGLIST.getString("VICTIM_RECEIVER_SERIES_TITLE").equals(key)) {
			vrSeries = series;
		}
	}

    public ExtendableXYSeries getItSeries() {
        return itSeries;
    }

    public ExtendableXYSeries getWtSeries() {
        return wtSeries;
    }

    public ExtendableXYSeries getWrSeries() {
        return wrSeries;
    }

    public ExtendableXYSeries getVrSeries() {
        return vrSeries;
    }
}
