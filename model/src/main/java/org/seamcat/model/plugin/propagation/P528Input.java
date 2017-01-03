package org.seamcat.model.plugin.propagation;
/**
 * Created by T0068531 on 02/07/15.
 */
import org.seamcat.model.plugin.Config;

/*
		description = "<html>Values outside the range 1 ... 95 % cause an exception.</html>";
		parameters.add(new DoubleParameter("Location percentage").defaultValue(95.0).unit("%").description(description));
		parameters.add(new DoubleParameter("Standard deviation").defaultValue(0).description("Standard deviation"));

 */


public interface P528Input {

	@Config(order = 1, name = "Time percentage", unit = "%")
	double TimePercentage();
	double TimePercentage= 95;

	@Config(order = 2, name = "Variations std. dev.", unit = "dB", toolTip = "Variations standard deviation")
	double stdDev();
	double stdDev = 0;

	boolean variations = false;
}