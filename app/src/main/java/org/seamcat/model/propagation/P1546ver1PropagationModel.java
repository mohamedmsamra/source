package org.seamcat.model.propagation;

import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.OptionalDoubleValue;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.P1546ver1Input;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class P1546ver1PropagationModel implements PropagationModelPlugin<P1546ver1Input> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, P1546ver1Input input, Validator<P1546ver1Input> validator) {
        Distribution frequency = HataSE21PropagationModel.findFrequency(scenario, path);
        if ( frequency != null ) {
            Bounds bounds = frequency.getBounds();
            if (bounds.getMin() < 30 || bounds.getMax() > 3000) {
                validator.error("Frequencies below 30 MHz or above 3000 MHz are not supported by the ITU-R P.1546-1 Recommendation." +
				PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
        }
		if (input.timePercentage().getBounds().getMin() < 1. || input.timePercentage().getBounds().getMin() > 50.)
			validator.error("P1546-1 is not valid for fields strengths exceeded for percentage times outside the range from 1% to 50%" +
			PluginCheckUtilsToBeRemoved.getExceptionHint());

		if (path.size() > 0){
			if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmin(path) < 1.)
				validator.error("Height of receiver is less than one meter (1.0) and is not supported by the ITU-R P.1546-1 Recommendation" +
				PluginCheckUtilsToBeRemoved.getExceptionHint());

			if (PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) > 1000)
				validator.error("Antenna height higher than 3000 m are not supported by the ITU-R P.1546-1 Recommendation." +
				PluginCheckUtilsToBeRemoved.getExceptionHint());

		}
    }

    private static double a0[] = { 0.0814, 0.0814, 0.0776, 0.0946, 0.0913, 0.0870, 0.0946, 0.0941, 0.0918 };

	private static double a1[] = { 0.761, 0.761, 0.726, 0.8849, 0.8539, 0.8141, 0.8849, 0.8805, 0.8584 };
	private static double a2[] = { -30.444, -30.444, -29.028, -35.399, -34.160,  -32.567, -35.399, -35.222, -34.337 };

	private static double a3[] = { 90.226, 90.226, 90.226, 92.778, 92.778,
		92.778, 94.493, 94.493, 94.493 };
	private static double b0[] = { 33.6238, 40.4554, 45.577, 51.6386, 35.3453,
		36.8836, 30.0051, 25.0641, 31.3878 };
	private static double b1[] = { 10.8917, 12.8206, 14.6752, 10.9877, 15.7595,
		13.8843, 15.4202, 22.1011, 15.6683 };

	private static double b2[] = { 2.3311, 2.2048, 2.2333, 2.2113, 2.2252,
		2.3469, 2.2978, 2.3183, 2.3941 };
	private static double b3[] = { 0.4427, 0.4761, 0.5439, 0.5384, 0.5285,
		0.5246, 0.4971, 0.5636, 0.5633 };
	private static double b4[] = { 1.256E-7, 7.788E-7, 1.050E-6, 4.323E-6,
		1.704E-7, 5.169E-7, 1.677E-7, 3.126E-8, 1.439E-7 };

	private static double b5[] = { 1.775, 1.68, 1.65, 1.52, 1.76, 1.69, 1.762,
		1.86, 1.77 };
	private static double b6[] = { 49.39, 41.78, 38.02, 49.52, 49.06, 46.5,
		55.21, 54.39, 49.18 };
	private static double b7[] = { 103.01, 94.3, 91.77, 97.28, 98.93, 101.59,
		101.89, 101.39, 100.39 };
	private static double c0[] = { 5.4419, 5.4877, 4.7697, 6.4701, 5.8636,
		4.7453, 6.9657, 6.5809, 6.0398 };
	private static double c1[] = { 3.7364, 2.4673, 2.7487, 2.9820, 3.0122,
		2.9581, 3.6532, 3.547, 2.5951 };
	private static double c2[] = { 1.9457, 1.7566, 1.6797, 1.7604, 1.7335,
		1.9286, 1.7658, 1.7750, 1.9153 };
	private static double c3[] = { 1.845, 1.9104, 1.8793, 1.7508, 1.7452,
		1.7378, 1.6268, 1.7321, 1.6542 };
	private static double c4[] = { 415.91, 510.08, 343.24, 198.33, 216.91,
		247.68, 114.39, 219.54, 186.67 };
	private static double c5[] = { 0.1128, 0.1622, 0.2642, 0.1432, 0.1690,
		0.1842, 0.1309, 0.1704, 0.1019 };
	private static double c6[] = { 2.3538, 2.1963, 1.9549, 2.2690, 2.1985,
		2.0873, 2.3286, 2.1977, 2.3954 };
	private static double d0[] = { 10, 5.5, 3, 5, 5, 8, 8, 8, 8 };
	private static double d1[] = { -1, 1, 2, 1.2, 1.2, 0, 0, 0, 0 };
	private static final double NB_TIME_PERC_INDEX = 3;
	private static final int P1 = 2;
	private static final int P10 = 1;
	private static final int P50 = 0;

	private OptionalDoubleValue clutterHeight;

	private double clutterCorrection(String environment, double rFreq, double rDist, double rHTx, double rHRx) {
		double rCorr;

		if (rHRx < 1.0) {
			throw new RuntimeException("Height of receiver is less than one meter (1.0) and is not supported by the ITU-R P.1546-1 Recommendation");
		}

		double rR0;
		if (clutterHeight.isRelevant()) {
			rR0 = clutterHeight.getValue();
		} else {
			// If local enviroment is Rural or SubUrban local clutter height equals 10. Urban equals 20
			rR0 = environment.equals("Urban") ? 20 : 10;
		}

		double rR = (1000 * rDist * rR0 - 15.0 * rHTx) / (1000 * rDist - 15.0);

		// not less than one meter
		if (rR < 1.0) {
			rR = 1.0;
		}

		double rKHRx = 3.2 + 6.2 * Math.log10(rFreq);

		if ( environment.equals("Urban")|| environment.equals("Suburban")) {
            if (rHRx < rR) {
                double rHdif = rR - rHRx;
                double rKnu = 0.0108 * Math.sqrt(rFreq);
                double rTheta = Mathematics.atanD(rHdif / 27);
                double rNu = rKnu * Math.sqrt(rHdif * rTheta);
                rCorr = 6.03 - j(rNu);
            } else { // rHRx >= rR
                rCorr = rKHRx * Math.log10(rHRx / rR);
            }
        } else {
            rCorr = rKHRx * Math.log10(rHRx / rR);
        }

		// Paragraf 7:
		if ((environment.equals("Urban") || environment.equals("Suburban")) && rDist < 15.0
				&& rHTx - rR0 < 150 && rHTx > rR0) {
			rCorr += -3.3 * Math.log10(rFreq) * (1 - 0.85 * Math.log10(rDist))
			* (1 - 0.46 * Math.log10(1 + rHTx - rR0));
		}


		return rCorr;
	}

	private double dh(double rHTx)  {
		double rDH = 0;

		rDH = 4.1 * Math.sqrt(rHTx);

		return rDH;
	}

	private double e(double rFreq, double rDist, double rHTx, double rHRx, double rTimePerc) {
		int eFreqInf; // TFreqIndex
		int eFreqSup; // TFreqIndex
		int eTimePercInf; // TTimePercIndex
		int eTimePercSup; // TTimePercIndex

		double rFreqInf;
		double rFreqSup;
		double rTimePercInf;
		double rTimePercSup;

		double rEInf;
		double rESup;
		double rE1;
		double rE2;
		double rQ;
		double rQ1;
		double rQ2;
		double rE;
		double rEfs = 0.;

		if (rFreq < 30 || rFreq > 3000){
			throw new RuntimeException("Frequencies below 30 MHz or above 3000 MHz are not supported by the ITU-R P.1546-1 Recommendation ");
		}
		if (rDist > 1000){
			throw new RuntimeException("Distances above 1000 km are not supported by the ITU-R P.1546-1 Recommendation ");
		}
		if (rHTx > 3000) {
			throw new RuntimeException("Antenna height higher than 3000 m are not supported by the ITU-R P.1546-1 Recommendation ");
		}

		if (rHTx >= 10.0) {
			if (rFreq < 600) {
				eFreqInf = 0;
				rFreqInf = 100;
				eFreqSup = 1;
				rFreqSup = 600;
			} else {
				eFreqInf = 1;
				rFreqInf = 600;
				eFreqSup = 2;
				rFreqSup = 2000;
			}
			if (rTimePerc < 10) {
				eTimePercInf = P1;
				rTimePercInf = 1;
				eTimePercSup = P10;
				rTimePercSup = 10;
			} else {
				eTimePercInf = P10;
				rTimePercInf = 10;
				eTimePercSup = P50;
				rTimePercSup = 50;
			}

			// Free space loss
			rEfs = efs(rDist, rHTx, rHRx);

			rEInf = eb(eFreqInf, rDist, rHTx, rHRx, eTimePercInf);
			rESup = eb(eFreqSup, rDist, rHTx, rHRx, eTimePercInf);
			rE1 = rEInf + (rESup - rEInf) * Math.log10(rFreq / rFreqInf)
			/ Math.log10(rFreqSup / rFreqInf);

			// limit field strength to free space loss
			if (rE1 > rEfs) {
				rE1 = rEfs;
			}

			rEInf = eb(eFreqInf, rDist, rHTx, rHRx, eTimePercSup);
			rESup = eb(eFreqSup, rDist, rHTx, rHRx, eTimePercSup);
			rE2 = rEInf + (rESup - rEInf) * Math.log10(rFreq / rFreqInf)
			/ Math.log10(rFreqSup / rFreqInf);

			// limit field strength to free space loss
			if (rE1 > rEfs) {
				rE1 = rEfs;
			}

			rQ = Stats.qi(rTimePerc / 100);
			rQ1 = Stats.qi(rTimePercInf / 100);
			rQ2 = Stats.qi(rTimePercSup / 100);
			rE = (rE2 * (rQ1 - rQ) + rE1 * (rQ - rQ2)) / (rQ1 - rQ2);
		} else {
			if (rDist < dh(rHTx)) {
				rE = e(rFreq, dh(10), 10.0, rHRx, rTimePerc)
				+ e(rFreq, rDist, 10.0, rHRx, rTimePerc)
				- e(rFreq, dh(rHTx), 10.0, rHRx, rTimePerc);
			} else {
				rE = e(rFreq, dh(10) + rDist - dh(rHTx), 10.0, rHRx, rTimePerc);
			}
		}

		return rE;

	}

	private double eb(int eFreqIndex, double rDist, double rHTx, double rHRx, int eTimePercIndex)  {
		double rK = 0;
		double rDzeta;
		double rEu = 0;
		double rEref = 0;
		double rEoff = 0;
		double rEfs = 0;
		double rE1 = 0;
		double rE2 = 0;
		double rPb;
		double rPbb;
		double rEb;

		double rA0 = a0[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rA1 = a1[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rA2 = a2[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rA3 = a3[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB0 = b0[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB1 = b1[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB2 = b2[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB3 = b3[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB4 = b4[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB5 = b5[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB6 = b6[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rB7 = b7[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC0 = c0[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC1 = c1[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC2 = c2[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC3 = c3[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC4 = c4[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC5 = c5[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rC6 = c6[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rD0 = d0[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];
		double rD1 = d1[(int) (eTimePercIndex + NB_TIME_PERC_INDEX * eFreqIndex)];

		rK = Math.log10(rHTx / 9.375) / Math.log10(2);

		rE1 = (rA0 * rK * rK + rA1 * rK + rA2) * Math.log10(rDist) + 0.1995
		* rK * rK + 1.8671 * rK + rA3;

		rDzeta = Math.pow(Math.log10(rDist), rB5);
		rEref = rB0
		* (Math.exp(-rB4 * Math.pow(10.0, rDzeta)) - 1)
		+ rB1
		* Math.exp(-(Math.log10(rDist) - rB2)
				* (Math.log10(rDist) - rB2) / (rB3 * rB3)) - rB6
				* Math.log10(rDist) + rB7;
		rEoff = rC0
		/ 2
		* rK
		* (1.0 - Mathematics.tanh(rC1
				* (Math.log10(rDist) - rC2 - Math.pow(rC3, rK) / rC4)))
				+ rC5 * Math.pow(rK, rC6);
		rE2 = rEref + rEoff;

		rPb = rD0 + rD1 * Math.sqrt(rK);
		rEu = rPb
		* Math.log10(Math.pow(10.0, (rE1 + rE2) / rPb)
				/ (Math.pow(10.0, rE1 / rPb) + Math.pow(10.0, rE2 / rPb)));

		rPbb = 8;
		rEfs = efs(rDist, rHTx, rHRx);
		rEb = rPbb
		* Math.log10(Math.pow(10.0, (rEu + rEfs) / rPbb)
				/ (Math.pow(10.0, rEu / rPbb) + Math
						.pow(10.0, rEfs / rPbb)));

		return rEb;

	}

	private double efs(double rDist, double rHTx, double rHRx) {
		double rEfs = 0;

		// rEfs = 106.9 - 10 * Math.log10(rDist * rDist +
		// (rHTx-rHRx)*(rHTx-rHRx));
		rEfs = 106.9 - 20 * Math.log10(rDist);
		return rEfs;

	}

    @Override
    public double evaluate(LinkResult linkResult, boolean variations, P1546ver1Input input) {
        double rFreq = linkResult.getFrequency();
        double rDist = linkResult.getTxRxDistance();
        double rHTx = linkResult.txAntenna().getHeight();
        double rHRx = linkResult.rxAntenna().getHeight();

        double rE = 0.;
        double rEfs = 0.;
        double rL = 0.;
        double rPt = 0.;

        double rStdDev = 0.;
        double rK;
        String environment = input.generalEnvironment();
        String system = input.system();
        clutterHeight = input.clutterHeight();

        if (rDist < 1.0) {
            // If Distance is less than one centimeter from 1km we round up
            // (assuming that difference
            // is due to a rounding error.
            if (1 - rDist < 0.0001) {
                rDist = 1.0;
            }
        }

        // Median loss component
        // Trial on time percentage
        Distribution timePercentage = input.timePercentage();
        rPt = timePercentage.trial();
        if (rPt < 1.0 || rPt > 50) {
            throw new RuntimeException("P1546-1 is not valid for fields strengths exceeded for percentage times outside the range from 1% to 50%");
        }

        // Field strength
        rE = e(rFreq, rDist, rHTx, rHRx, rPt);

        // Clutter correction
        rE += clutterCorrection(environment, rFreq, rDist, rHTx, rHRx);

        // limit field strength to free space loss
        rEfs = efs(rDist, rHTx, rHRx);
        if (rE > rEfs) {
            rE = rEfs;
        }

        // Field strength to path loss conversion
        rL = 139 - rE + 20 * Math.log10(rFreq);

        // Variable component due to variations in locations
        if (variations) {
            if (system.equals("Digital (Bw < 1MHz)")) {
                if (environment.equals("Urban")) { // RURAL and SUBURBAN
                    rK = 2.1;
                } else {
                    rK = 3.8;
                }
                rStdDev = rK + 1.6 * Math.log10(rFreq);
            } else if (system.equals("Analogue")) {
                rK = 5.1;
                rStdDev = rK + 1.6 * Math.log10(rFreq);
            } else { // DIGITAL_WIDE
                rStdDev = 5.5;
            }

            // change std dev and perform trial
            rL += Factory.distributionFactory().getGaussianDistribution(0, rStdDev).trial();
        }

        if (Double.isInfinite(rL)) {
            rL = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
        }
        return rL;

    }

	public double j(double v) {
		double rJ = 0;

		rJ = 6.9 + 20 * Math.log10(Math.sqrt((v - 0.1) * (v - 0.1) + 1) + v
				- 0.1);
		return rJ;
	}

    @Override
    public Description description() {
        return new DescriptionImpl("ITU-R P.1546-1 Annex 8","<html><body><b><u>Frequency range:</u></b><br>30 MHz - 3 GHz<br><b><u>Distance range:</u></b><br>1-1000 km<br><b><u>Typical application area:</u></b><br>Broadcasting and other terrestrial services, <br>typically considered in cases with high <br>mounted transmitter antenna (e.g. above<br> 50-60 m).<br><b><u>Information:</u></b><br>Note that the P.1546 model assumes that<br> the specified height of transmitting antenna <br>is height above local clutter (effective height<br> of antenna). <br>The receiver antenna is above ground and <br>the correction for local clutter will be applied<br> by the model.<br><b><u>Note:</u></b><br>The use of the ITU-R P.1546-1 is under <br>the responsibility of the user as it is <br>superseded by ITU-R P.1546-4.</body></html>");
	}
}
