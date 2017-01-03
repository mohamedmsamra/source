package org.seamcat.model.propagation;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.FreespaceInput;
import org.seamcat.model.plugin.propagation.JTG56Input;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.*;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.model.types.result.LinkResultImpl;

import java.util.List;
import java.util.Random;

/**
 * Implementation of propagation model specified by Joint Task Group 5 an 6<br>
 * March 08 - 2009
 */

public class JTG56PropagationModel implements PropagationModelPlugin<JTG56Input> {

    private Random random = new Random();
    private Integer generalEnv;
    private Boolean variationsSelected = true;
    private Boolean medianSelected = true;

    private Distribution timePercentage = Factory.distributionFactory().getUniformDistribution(50., 50.);
    private Integer timePercIndex;
    private Double userSpecifiedLocalClutterHeight = 0.0;
    private Boolean useUserSpecifiedLocalClutterHeight = false;

    private final int BELOW_1MHZ = 1;

    private Boolean useSE42approach;
    private Boolean useReciprocity;

    private enum GenEnv {URBAN, SUBURBAN, RURAL}

    ;
    private String[] GenEnvString = {"Urban", "Suburban", "Rural"};


    Double rStdDev, distanceCutOff, timeprobability, localClutterHeight; // added due to new format of evaluate

    @Override
    public double evaluate(LinkResult linkResult, boolean variation, JTG56Input input) {

        double loss = 0, distance, frequency, txHeight = 0, rxHeight = 0;

        frequency = linkResult.getFrequency();
        distance = linkResult.getTxRxDistance();
        txHeight = linkResult.txAntenna().getHeight();
        rxHeight = linkResult.rxAntenna().getHeight();
        LinkResultImpl localLinkResult = new LinkResultImpl();
        localLinkResult.setRxAntenna(linkResult.rxAntenna());
        localLinkResult.setTxAntenna(linkResult.txAntenna());
        localLinkResult.setFrequency(linkResult.getFrequency());

        PropagationModel<FreespaceInput> freespace = Factory.propagationModelFactory().getFreeSpace();

        initPlugin(input, variation);
        if (distanceCutOff == 0.0) {
            distanceCutOff = 0.04; //Default to 40 meters
        } else if (distanceCutOff < 0) {
            distanceCutOff = 0.0;
        } else if (distanceCutOff >= 0.1) {
            throw new IllegalArgumentException("CutOff distance cannot exceed 100 meters ");
        }

        configure(generalEnv, timeprobability, localClutterHeight);

        //Use distance to determine loss formula
        if (distance <= distanceCutOff) { //From cutoff and below
            //Loss defined by A.2.7
            loss = freespace.evaluate(linkResult, false);

        } else if (distance < 0.1 && distance > distanceCutOff) {//Between cutoff and - 100 meters
            //Loss defined by A.2.8
            //Interpolate between Freespace at cutoff distance and Hata at 100 meters
            localLinkResult.setTxRxDistance(distanceCutOff);
            double freespace_cutoff = freespace.evaluate(localLinkResult, false);
            double hata_100 = localHata(frequency, 0.1, txHeight, rxHeight);


            double interpolation = (Math.log10(distance) - Math.log10(distanceCutOff))
                    / (Math.log10(0.1) - Math.log10(distanceCutOff));
            interpolation *= (hata_100 - freespace_cutoff);

            loss = freespace_cutoff + interpolation;

            if (generalEnv == GenEnv.SUBURBAN.ordinal()) {
                loss = subUrban(loss, frequency);
            }
            if (generalEnv == GenEnv.RURAL.ordinal()) {
                loss = Rural(loss, frequency);
            }
            loss = Math.max(loss, freespace.evaluate(linkResult, false));

        } else if (round(distance) == 0.1) { // Distance is 100 meters (rounded to 3 digits)
            //Hata at 100 meters
            loss = localHata(frequency, distance, txHeight, rxHeight);
            if (generalEnv == GenEnv.SUBURBAN.ordinal()) {
                loss = subUrban(loss, frequency);
            }
            if (generalEnv == GenEnv.RURAL.ordinal()) {
                loss = Rural(loss, frequency);
            }
            loss = Math.max(loss, freespace.evaluate(linkResult, false));

        } else if (distance < 1 && distance > 0.1) { //Between 100 and 1000 meters
            //Interpolation between Hata at 100 meters and ITU 1546 at 1000 meters
            double hata_100 = localHata(frequency, 0.1, txHeight, rxHeight);

            double itu1546_1000 = localITUP1546_evaluate(frequency, 1.0, txHeight, rxHeight, timeprobability);

            double interpolation = (Math.log10(distance) - Math.log10(0.1)) / (Math.log10(1) - Math.log10(0.1));

            // interpolation as agreed in SE42

            if (generalEnv == GenEnv.SUBURBAN.ordinal()) {
                hata_100 = subUrban(hata_100, frequency);
            }
            if (generalEnv == GenEnv.RURAL.ordinal()) {
                hata_100 = Rural(hata_100, frequency);
            }
            if (useSE42approach)
            hata_100 = Math.max(hata_100, freespace.evaluate(linkResult, false)); // value at 100 m

            interpolation *= (itu1546_1000 - hata_100);

            loss = hata_100 + interpolation;

            loss = Math.max(loss, freespace.evaluate(linkResult, false));

        } else { //Above 1000 meters
            loss = localITUP1546_evaluate(frequency, distance, txHeight, rxHeight, timeprobability);
            loss = Math.max(loss, freespace.evaluate(linkResult, false));
        }

        // Std calculation
        double rR0;
        // Introduce the clutter height in the determination of the standard deviation
        if (useUserSpecifiedLocalClutterHeight) {
            rR0 = userSpecifiedLocalClutterHeight;
        } else {
            // If local enviroment is Rural or SubUrban local clutter height equals 10. Urban equals 20
            rR0 = generalEnv == GenEnv.URBAN.ordinal() ? 20 : 10;
        }
        if (rxHeight > rR0) {
            rStdDev = 5.5;
        } else {
            if (txHeight > rR0) {
                rStdDev = 5.5;
            } else {
                rStdDev = 7.0;
            }
        }
        // change std dev and perform trial
        if (getVariationsSelected()) {
            loss += rStdDev * random.nextGaussian();
        }

        return loss;
    }

    /**
     * assign the values of the user defined parameters accordingly
     *
     * @param params    List of parameter definitions
     * @param variation whether to apply standard deviation
     */
    private void initPlugin(JTG56Input params, boolean variation) {
        variationsSelected = variation;
        setVariationsSelected(variationsSelected);
        useReciprocity = params.allowReciprocity();
        useSE42approach = params.useSE42();

        String environment = params.generalEnv();
        if (environment.contains("RURAL")) {
            generalEnv = GenEnv.RURAL.ordinal();
        } else if (environment.contains("SUBURBAN")) {
            generalEnv = GenEnv.SUBURBAN.ordinal();
        } else {
            generalEnv = GenEnv.URBAN.ordinal();
        }

        timeprobability = params.time().trial();
        distanceCutOff = params.cutOff();
        distanceCutOff /= 1000; // in km

        if (params.userClutter().isRelevant()) {
            useUserSpecifiedLocalClutterHeight = params.userClutter().isRelevant();
            setUseUserSpecifiedLocalClutterHeight(useUserSpecifiedLocalClutterHeight);
            userSpecifiedLocalClutterHeight = params.userClutter().getValue();
            localClutterHeight = userSpecifiedLocalClutterHeight;
        } else {
            localClutterHeight = generalEnv == GenEnv.URBAN.ordinal() ? 20. : 10.;
        }
    }

    private void configure(int generalEnv, double timeprobability, double localClutterHeight) {

        setGeneralEnv(generalEnv);

        if (useUserSpecifiedLocalClutterHeight) {
            setUserSpecifiedLocalClutterHeight(localClutterHeight);
            setUseUserSpecifiedLocalClutterHeight(true); //Simulate DenseUrban
        }
        if (timeprobability != 1 && timeprobability != 50) {
            //FIXME JPK 7.04.2009: Handle Time probability limitation
            throw new IllegalArgumentException(
                    "Time probability interpolation between 1% and 50% is not yet supported. You should enter either 1 (for 1%) or 50 (for 50%)");
        }
        /*
         * if (timeprobability == 0) { timeprobability = 50; }
		 */
//		setTimePercentage(new UniformDistribution(timeprobability, timeprobability)); //Fake constant value from distribution

    }

    public void setVariationsSelected(boolean variationsSelected) {
        this.variationsSelected = variationsSelected;
    }

    public boolean getVariationsSelected() {
        return variationsSelected;
    }

    public int getGeneralEnv() {
        return generalEnv;
    }

    public void setGeneralEnv(int generalEnv) {
        this.generalEnv = generalEnv;
    }

    public boolean getMedianSelected() {
        return medianSelected;
    }

    private double localHata(double frequency, double distance, double Txheight, double Rxheight) {
        double rHm = Math.min(Txheight, Rxheight);
        double rHb = Math.max(Txheight, Rxheight);

        double rCorr = Math.log10(Math
                .sqrt(((distance * distance) + (((Txheight - Rxheight) * (Txheight - Rxheight)) / 1e6))));

        double rA = (1.1 * Math.log10(frequency) - 0.7) * Math.min(10, rHm) - (1.56 * Math.log10(frequency) - 0.8)
                + Math.max(0, 20 * Math.log10(rHm / 10));
        double rB = Math.min(0, 20 * Math.log10(rHb / 30));

        double loss = 69.6 + 26.2 * Math.log10(frequency) - 13.82 * Math.log10(Math.max(30, rHb))
                + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA - rB;

        return loss;
    }

    private double subUrban(double urbanLoss, double frequency) {
        double adjustedFrequency = Math.log10(Math.min(Math.max(150, frequency), 2000) / 28);

        return urbanLoss - 2 * (adjustedFrequency * adjustedFrequency) - 5.4;
    }

    private double Rural(double urbanLoss, double frequency) {
        double adjustedFrequency = Math.log10(Math.min(Math.max(150, frequency), 2000));

        return urbanLoss - 4.78 * (adjustedFrequency * adjustedFrequency) + 18.33 * adjustedFrequency - 40.94;
    }

    public static final double round(double d) {
        if (d == 0) {
            return 0.0d;
        }
        return Math.rint(d * 1000) / 1000;

    }

    private double clutterCorrection(double rFreq, double rDist, double rHTx, double rHRx) {
        int eGeneralEnv = getGeneralEnv();
        double rCorr;

        if (rHRx < 1.0) {
            throw new IllegalArgumentException("Height of receiver is less than one meter (1.0)");
        }

        double rR0;
        if (useUserSpecifiedLocalClutterHeight) {
            rR0 = userSpecifiedLocalClutterHeight;
        } else {
            // If local enviroment is Rural or SubUrban local clutter height equals 10. Urban equals 20
            rR0 = eGeneralEnv == GenEnv.URBAN.ordinal() ? 20 : 10;
        }

        double rR = (1000 * rDist * rR0 - 15.0 * rHTx) / (1000 * rDist - 15.0); //A.2.17

        double h2 = (1000 * rDist * rHRx - 15 * rHTx) / (1000 * rDist - 15); //A.2.17`

        // not less than one meter
        if (rR < 1.0) {
            rR = 1.0;
        }

        double rKHRx = 3.2 + 6.2 * Math.log10(rFreq); //A.2.22

        switch (eGeneralEnv) {
            case 0:
            case 1: {
                if (h2 < rR) {
                    double rHdif = rR - h2; //A.2.18
                    double rTheta = Mathematics.atanD(rHdif / 27); //A.2.19
                    double rKnu = 0.0108 * Math.sqrt(rFreq); //A.2.20

                    double rNu = rKnu * Math.sqrt(rHdif * rTheta); //A.2.21

                    rCorr = 6.03 - j(rNu); //A.2.24 (function j is A.2.23)
                } else { // rHRx >= rR
                    rCorr = rKHRx * Math.log10(h2 / rR); //A.2.25
                }
                break;
            }
            default: {// RURAL
                rR = 10;
                //rCorr = rKHRx * Math.log10(h2 / rR);//A.2.25
                rCorr = rKHRx * Math.log10(rHRx / rR);//as in P1546-3 27b.
                break;
            }
        }

        //Section v:
        if (rR < 10) {
            if (rR0 == 20) {
                rCorr = rCorr - rKHRx * Math.log10(10 / rR); //A.2.26
            }
        }

        // Paragraf 7:
        if ((eGeneralEnv == GenEnv.URBAN.ordinal() || eGeneralEnv == GenEnv.SUBURBAN.ordinal()) && rDist < 15.0 && rHTx - rR0 < 150 && rHTx > rR0) {
            rCorr += -3.3 * Math.log10(rFreq) * (1 - 0.85 * Math.log10(rDist))
                    * (1 - 0.46 * Math.log10(1 + rHTx - rR0));
        }

        return rCorr;
    }

    private double efs(double rDist, double rHTx, double rHRx) {
        double rEfs = 0;

        // rEfs = 106.9 - 10 * Math.log10(rDist * rDist +
        // (rHTx-rHRx)*(rHTx-rHRx));
        rEfs = 106.9 - 20 * Math.log10(rDist);
        return rEfs;

    }

    public double localITUP1546_evaluate(double rFreq, double rDist, double rHTxIntermediate, double rHRxIntermediate, double timeprobability) {
        double rE = 0.;
        double rEfs = 0.;
        double rL = 0.;
        double rPt = 0.;
        double rHTx = 0;
        double rHRx = 0;

        if (useReciprocity) {
            rHTx = Math.max(rHTxIntermediate, rHRxIntermediate);
            rHRx = Math.min(rHTxIntermediate, rHRxIntermediate);
        } else {
            rHTx = rHTxIntermediate;
            rHRx = rHRxIntermediate;
        }


        if (rDist < 1.0) {
            // If Distance is less than one centimeter from 1km we round up
            // (assuming that difference
            // is due to a rounding error.
            if (1 - rDist < 0.0001) {
                rDist = 1.0;
            }
        }

        // Median loss component
        if (getMedianSelected()) {
            // Trial on time percentage
            rPt = getTimePercentage().trial();

            // Field strength
            rE = calculateFieldStrength(rFreq, rDist, rHTx, timeprobability);

            // Clutter correction
            rE += clutterCorrection(rFreq, rDist, rHTx, rHRx);

            // limit field strength to free space loss
            rEfs = efs(rDist, rHTx, rHRx);
            if (rE > rEfs) {
                rE = rEfs;
            }

            // Field strength to path loss conversion
            rL = 139.3 - rE + 20 * Math.log10(rFreq);
        }
        return rL;
    }

    /**
     * A.2.14
     */
    private double calculateFieldStrength(double frequency, double distance, double heightTX, double timeprobability) {
        double value = 0;
        double e_600 = 0;
        double e_2000 = 0;

        if (timeprobability == 50) {
            e_600 = calculateFieldStrengthForAntennaHeight(FieldStrength_50pctLand_600MHz, distance, heightTX);
            e_2000 = calculateFieldStrengthForAntennaHeight(FieldStrength_50pctLand_2000MHz, distance, heightTX);
        }
        if (timeprobability == 1) {
            e_600 = calculateFieldStrengthForAntennaHeight(FieldStrength_1pctLand_600MHz, distance, heightTX);
            e_2000 = calculateFieldStrengthForAntennaHeight(FieldStrength_1pctLand_2000MHz, distance, heightTX);
        }
        if (timeprobability != 1 && timeprobability != 50) {
            //FIXME JPK 7.04.2009: Handle Time probability limitation
            throw new IllegalArgumentException(
                    "Time probability interpolation between 1% and 50% is not yet supported. You should enter either 1 (for 1%) or 50 (for 50%)");
        }

        if (frequency == 600) {
            value = e_600;
        } else if (frequency == 2000) {
            value = e_2000;
        } else {
            //Interpolation

            value = e_600 + (e_2000 - e_600) * (Math.log10((frequency / 600)) / Math.log10((2000 / 600))); //A.2.14

        }

        return value;
    }

    private int findUpperAntennaHeightIndex(double value) {
        for (int i = 1; i < specifiedAntennaHeights.length; i++) {
            if (specifiedAntennaHeights[i] >= value) {
                return i;
            }
        }
        //FIXME CP 31.03.2009: Handle Transmitter heights above 1200 meters
        throw new IllegalArgumentException("Antenna heights above "
                + specifiedAntennaHeights[specifiedAntennaHeights.length - 1]
                + " m is not yet supported - this is a temporary limitation");
    }

    private int findUpperDistanceIndex(double[][] table, double value) {
        if (value < table[0][0]) {
            //FIXME CP 31.03.2009: Handle Transmitter heights below 10 meters
            throw new IllegalArgumentException("Distance below " + table[0][0]
                    + " km is not yet supported - this is a temporary limitation");
        }
        for (int i = 0; i < table.length; i++) {
            if (table[i][0] >= value) {
                return i;
            }
        }
        //FIXME CP 31.03.2009: Handle Transmitter heights above 1200 meters
        throw new IllegalArgumentException("Distance above " + table[table.length - 1][0]
                + " km is not yet supported - this is a temporary limitation");
    }

    /**
     * A.2.16 Note: This method should not be called directly - call
     * calculateFieldStrengthForAntennaHeight instead
     */
    private double interploateFieldStrengthForDistance(double[][] fieldStrengths, double distance,
                                                       int antennaHeightIndex) {
        double value = 0;

        int upperDistanceIndex = findUpperDistanceIndex(fieldStrengths, distance);
        if (upperDistanceIndex == 0) {
            //Distance is 1km - no interpolation
            value = fieldStrengths[0][antennaHeightIndex];
        } else {
            int lowerDistanceIndex = upperDistanceIndex - 1;

            double e_lower = fieldStrengths[lowerDistanceIndex][antennaHeightIndex];
            double e_upper = fieldStrengths[upperDistanceIndex][antennaHeightIndex];

            double d_lower = fieldStrengths[lowerDistanceIndex][0];
            double d_upper = fieldStrengths[upperDistanceIndex][0];

            value = e_lower + (e_upper - e_lower)
                    * (Math.log10((distance / d_lower)) / Math.log10((d_upper / d_lower))); //A.2.16
        }

        return value;
    }

    /**
     * A.2.15
     */
    private double calculateFieldStrengthForAntennaHeight(double[][] fieldStrengths, double distance,
                                                          double antennaHeight) {
        double value = 0;

        int upperAntennaIndex = findUpperAntennaHeightIndex(antennaHeight);
        int lowerAntennaIndex = upperAntennaIndex - 1;

        if (specifiedAntennaHeights[lowerAntennaIndex] == antennaHeight) {
            //No interpolation needed on antenna height
            value = interploateFieldStrengthForDistance(fieldStrengths, distance, lowerAntennaIndex);
        } else {

            double e_lower = interploateFieldStrengthForDistance(fieldStrengths, distance, lowerAntennaIndex);
            double e_upper = interploateFieldStrengthForDistance(fieldStrengths, distance, upperAntennaIndex);

            double h_lower = specifiedAntennaHeights[lowerAntennaIndex];
            double h_upper = specifiedAntennaHeights[upperAntennaIndex];

            value = e_lower + (e_upper - e_lower)
                    * (Math.log10((antennaHeight / h_lower)) / Math.log10((h_upper / h_lower))); //A.2.15
        }

        return value;
    }

    public void setUserSpecifiedLocalClutterHeight(double userSpecifiedLocalClutterHeight) {
        this.userSpecifiedLocalClutterHeight = userSpecifiedLocalClutterHeight;
    }

    public void setUseUserSpecifiedLocalClutterHeight(boolean useUserSpecifiedLocalClutterHeight) {
        this.useUserSpecifiedLocalClutterHeight = useUserSpecifiedLocalClutterHeight;
    }


    public Distribution getTimePercentage() {
        return timePercentage;
    }

    public double j(double v) {
        double rJ = 0;

        rJ = 6.9 + 20 * Math.log10(Math.sqrt((v - 0.1) * (v - 0.1) + 1) + v - 0.1);
        return rJ;
    }

	/*public GaussianDistribution getVariationsDistrib() {
		return variationsDistrib;
	}*/

    //
    private static final double[] specifiedAntennaHeights = {1, 10, 20, 37.5, 75, 150, 300, 600, 1200};

    private static final double[][] FieldStrength_50pctLand_600MHz = { //Figure 9 - Last columns is freespace field strength (which we do not use for now)
            {1, 92.681, 94.868, 97.072, 99.699, 102.345, 104.591, 106.007, 106.629, 106.900},
            {2, 81.108, 84.291, 87.092, 90.356, 93.803, 97.071, 99.417, 100.484, 100.879},
            {3, 73.480, 77.690, 81.046, 84.741, 88.624, 92.462, 95.443, 96.866, 97.358},
            {4, 67.693, 72.675, 76.575, 80.667, 84.877, 89.107, 92.562, 94.285, 94.859},
            {5, 63.064, 68.556, 72.942, 77.421, 81.920, 86.457, 90.290, 92.275, 92.921},
            {6, 59.229, 65.047, 69.834, 74.687, 79.459, 84.256, 88.406, 90.626, 91.337},
            {7, 55.965, 61.992, 67.096, 72.296, 77.333, 82.365, 86.792, 89.227, 89.998},
            {8, 53.130, 59.293, 64.640, 70.152, 75.447, 80.700, 85.376, 88.010, 88.838},
            {9, 50.628, 56.879, 62.410, 68.195, 73.739, 79.204, 84.110, 86.933, 87.815},
            {10, 48.393, 54.701, 60.370, 66.387, 72.167, 77.839, 82.961, 85.965, 86.900},
            {11, 46.377, 52.719, 58.489, 64.702, 70.703, 76.576, 81.907, 85.085, 86.072},
            {12, 44.542, 50.904, 56.748, 63.122, 69.327, 75.396, 80.928, 84.279, 85.316},
            {13, 42.862, 49.230, 55.127, 61.633, 68.022, 74.282, 80.013, 83.533, 84.621},
            {14, 41.315, 47.680, 53.613, 60.224, 66.780, 73.223, 79.148, 82.838, 83.977},
            {15, 39.883, 46.238, 52.192, 58.888, 65.590, 72.209, 78.327, 82.187, 83.378},
            {16, 38.553, 44.890, 50.856, 57.617, 64.447, 71.233, 77.541, 81.574, 82.818},
            {17, 37.312, 43.626, 49.594, 56.404, 63.345, 70.289, 76.786, 80.993, 82.291},
            {18, 36.151, 42.437, 48.399, 55.244, 62.280, 69.373, 76.056, 80.441, 81.795},
            {19, 35.062, 41.315, 47.265, 54.133, 61.250, 68.480, 75.346, 79.914, 81.325},
            {20, 34.038, 40.254, 46.185, 53.066, 60.250, 67.607, 74.655, 79.408, 80.879},
            {25, 29.704, 35.679, 41.448, 48.276, 55.634, 63.479, 71.375, 77.129, 78.941},
            {30, 26.339, 31.999, 37.521, 44.162, 51.501, 59.617, 68.237, 75.108, 77.358},
            {35, 23.638, 28.930, 34.148, 40.517, 47.713, 55.935, 65.125, 73.200, 76.019},
            {40, 21.411, 26.304, 31.182, 37.224, 44.194, 52.395, 61.999, 71.296, 74.859},
            {45, 19.531, 24.013, 28.535, 34.219, 40.906, 48.992, 58.862, 69.318, 73.836},
            {50, 17.910, 21.986, 26.151, 31.464, 37.834, 45.734, 55.739, 67.213, 72.921},
            {55, 16.485, 20.173, 23.991, 28.936, 34.972, 42.632, 52.661, 64.966, 72.093},
            {60, 15.211, 18.536, 22.027, 26.616, 32.314, 39.698, 49.656, 62.591, 71.337},
            {65, 14.051, 17.044, 20.233, 24.486, 29.852, 36.938, 46.748, 60.122, 70.642},
            {70, 12.982, 15.675, 18.588, 22.530, 27.578, 34.354, 43.955, 57.601, 69.998},
            {75, 11.982, 14.407, 17.071, 20.730, 25.477, 31.941, 41.287, 55.065, 69.399},
            {80, 11.037, 13.223, 15.666, 19.068, 23.536, 29.694, 38.752, 52.542, 68.838},
            {85, 10.136, 12.111, 14.357, 17.527, 21.739, 27.602, 36.351, 50.056, 68.312},
            {90, 9.269, 11.059, 13.129, 16.093, 20.070, 25.654, 34.083, 47.624, 67.815},
            {95, 8.429, 10.056, 11.972, 14.751, 18.515, 23.837, 31.944, 45.257, 67.346},
            {100, 7.612, 9.095, 10.874, 13.489, 17.061, 22.138, 29.928, 42.964, 66.900},
            {110, 6.030, 7.273, 8.825, 11.164, 14.407, 19.050, 26.235, 38.617, 66.072},
            {120, 4.498, 5.556, 6.929, 9.049, 12.026, 16.304, 22.941, 34.601, 65.316},
            {130, 3.004, 3.915, 5.147, 7.093, 9.855, 13.830, 19.982, 30.910, 64.621},
            {140, 1.541, 2.336, 3.455, 5.261, 7.848, 11.571, 17.302, 27.523, 63.977},
            {150, 0.103, 0.805, 1.834, 3.528, 5.972, 9.484, 14.854, 24.413, 63.378},
            {160, -1.311, -0.684, 0.272, 1.873, 4.200, 7.538, 12.597, 21.550, 62.818},
            {170, -2.702, -2.137, -1.241, 0.285, 2.516, 5.707, 10.500, 18.905, 62.291},
            {180, -4.070, -3.557, -2.710, -1.246, 0.904, 3.972, 8.537, 16.452, 61.795},
            {190, -5.417, -4.945, -4.140, -2.728, -0.646, 2.319, 6.689, 14.166, 61.325},
            {200, -6.741, -6.305, -5.534, -4.166, -2.141, 0.736, 4.938, 12.027, 60.879},
            {225, -9.955, -9.585, -8.880, -7.594, -5.677, -2.969, 0.905, 7.208, 59.856},
            {250, -13.033, -12.709, -12.047, -10.819, -8.976, -6.385, -2.743, 2.977, 58.941},
            {275, -15.981, -15.689, -15.059, -13.871, -12.081, -9.573, -6.099, -0.816, 58.113},
            {300, -18.809, -18.541, -17.934, -16.774, -15.023, -12.577, -9.227, -4.275, 57.358},
            {325, -21.529, -21.277, -20.688, -19.550, -17.827, -15.427, -12.172, -7.473, 56.662},
            {350, -24.151, -23.913, -23.336, -22.214, -20.514, -18.150, -14.966, -10.466, 56.019},
            {375, -26.687, -26.459, -25.893, -24.784, -23.101, -20.764, -17.637, -13.294, 55.419},
            {400, -29.150, -28.930, -28.371, -27.273, -25.603, -23.288, -20.207, -15.988, 54.859},
            {425, -31.550, -31.336, -30.784, -29.694, -28.035, -25.737, -22.692, -18.575, 54.332},
            {450, -33.896, -33.688, -33.141, -32.057, -30.407, -28.124, -25.108, -21.074, 53.836},
            {475, -36.198, -35.995, -35.452, -34.374, -32.730, -30.459, -27.467, -23.501, 53.366},
            {500, -38.464, -38.264, -37.724, -36.651, -35.013, -32.752, -29.780, -25.872, 52.921},
            {525, -40.700, -40.503, -39.966, -38.896, -37.264, -35.010, -32.055, -28.195, 52.497},
            {550, -42.911, -42.717, -42.183, -41.116, -39.488, -37.241, -34.301, -30.481, 52.093},
            {575, -45.104, -44.912, -44.379, -43.315, -41.691, -39.450, -36.522, -32.737, 51.707},
            {600, -47.281, -47.090, -46.560, -45.498, -43.877, -41.641, -38.723, -34.968, 51.337},
            {625, -49.445, -49.256, -48.727, -47.667, -46.049, -43.817, -40.908, -37.178, 50.982},
            {650, -51.598, -51.411, -50.883, -49.825, -48.209, -45.981, -43.080, -39.373, 50.642},
            {675, -53.743, -53.556, -53.030, -51.974, -50.359, -48.135, -45.240, -41.552, 50.314},
            {700, -55.878, -55.693, -55.168, -54.113, -52.500, -50.278, -47.390, -43.719, 49.998},
            {725, -58.005, -57.821, -57.297, -56.243, -54.632, -52.412, -49.529, -45.873, 49.693},
            {750, -60.123, -59.939, -59.416, -58.363, -56.753, -54.536, -51.657, -48.015, 49.399},
            {775, -62.230, -62.047, -61.524, -60.472, -58.864, -56.649, -53.774, -50.144, 49.114},
            {800, -64.325, -64.143, -63.620, -62.569, -60.962, -58.748, -55.877, -52.258, 48.838},
            {825, -66.405, -66.224, -65.702, -64.652, -63.045, -60.834, -57.966, -54.355, 48.571},
            {850, -68.469, -68.288, -67.767, -66.717, -65.112, -62.901, -60.037, -56.435, 48.312},
            {875, -70.514, -70.334, -69.813, -68.764, -67.159, -64.950, -62.088, -58.493, 48.060},
            {900, -72.537, -72.356, -71.836, -70.787, -69.183, -66.975, -64.116, -60.528, 47.815},
            {925, -74.534, -74.354, -73.834, -72.786, -71.182, -68.975, -66.118, -62.537, 47.577},
            {950, -76.502, -76.323, -75.803, -74.755, -73.152, -70.946, -68.091, -64.515, 47.346},
            {975, -78.439, -78.259, -77.740, -76.693, -75.090, -72.885, -70.031, -66.461, 47.120},
            {1000, -80.340, -80.161, -79.642, -78.595, -76.993, -74.789, -71.937, -68.371, 46.900}

    };

    private static final double[][] FieldStrength_50pctLand_2000MHz = {//Figure  17 - Last columns is freespace field strength (which we do not use for now)
            {1, 94.233, 96.509, 98.662, 101.148, 103.509, 105.319, 106.328, 106.732, 106.900},
            {2, 82.427, 85.910, 88.758, 91.971, 95.244, 98.116, 99.916, 100.632, 100.879},
            {3, 74.501, 79.135, 82.671, 86.395, 90.171, 93.677, 96.070, 97.049, 97.358},
            {4, 68.368, 73.847, 78.078, 82.308, 86.474, 90.429, 93.289, 94.498, 94.859},
            {5, 63.385, 69.412, 74.253, 79.006, 83.536, 87.851, 91.099, 92.513, 92.921},
            {6, 59.209, 65.580, 70.908, 76.172, 81.068, 85.701, 89.284, 90.887, 91.337},
            {7, 55.628, 62.216, 67.909, 73.643, 78.912, 83.845, 87.729, 89.509, 89.998},
            {8, 52.499, 59.227, 65.186, 71.329, 76.970, 82.198, 86.365, 88.312, 88.838},
            {9, 49.725, 56.544, 62.696, 69.181, 75.181, 80.707, 85.144, 87.253, 87.815},
            {10, 47.236, 54.116, 60.406, 67.169, 73.507, 79.331, 84.035, 86.304, 86.900},
            {11, 44.981, 51.901, 58.291, 65.277, 71.922, 78.043, 83.013, 85.442, 86.072},
            {12, 42.922, 49.867, 56.329, 63.490, 70.408, 76.822, 82.061, 84.652, 85.316},
            {13, 41.029, 47.989, 54.502, 61.800, 68.956, 75.653, 81.165, 83.922, 84.621},
            {14, 39.279, 46.245, 52.794, 60.198, 67.558, 74.524, 80.313, 83.243, 83.977},
            {15, 37.653, 44.619, 51.191, 58.677, 66.210, 73.429, 79.497, 82.608, 83.378},
            {16, 36.137, 43.096, 49.682, 57.231, 64.909, 72.361, 78.708, 82.009, 82.818},
            {17, 34.717, 41.665, 48.257, 55.853, 63.652, 71.316, 77.942, 81.442, 82.291},
            {18, 33.384, 40.316, 46.908, 54.537, 62.437, 70.293, 77.192, 80.903, 81.795},
            {19, 32.129, 39.041, 45.627, 53.278, 61.260, 69.289, 76.455, 80.387, 81.325},
            {20, 30.945, 37.832, 44.407, 52.072, 60.121, 68.303, 75.728, 79.893, 80.879},
            {25, 25.889, 32.596, 39.051, 46.684, 54.906, 63.628, 72.179, 77.642, 78.941},
            {30, 21.921, 28.355, 34.599, 42.081, 50.306, 59.317, 68.706, 75.604, 77.358},
            {35, 18.729, 24.802, 30.756, 37.994, 46.119, 55.280, 65.291, 73.633, 76.019},
            {40, 16.114, 21.754, 27.352, 34.269, 42.210, 51.427, 61.921, 71.641, 74.859},
            {45, 13.939, 19.099, 24.292, 30.826, 38.510, 47.703, 58.580, 69.571, 73.836},
            {50, 12.108, 16.766, 21.526, 27.634, 34.999, 44.085, 55.252, 67.388, 72.921},
            {55, 10.548, 14.707, 19.028, 24.685, 31.681, 40.581, 51.937, 65.076, 72.093},
            {60, 9.201, 12.884, 16.779, 21.982, 28.576, 37.214, 48.650, 62.636, 71.337},
            {65, 8.025, 11.268, 14.761, 19.525, 25.699, 34.012, 45.413, 60.085, 70.642},
            {70, 6.984, 9.831, 12.957, 17.305, 23.060, 31.001, 42.256, 57.448, 69.998},
            {75, 6.050, 8.546, 11.343, 15.308, 20.658, 28.196, 39.207, 54.753, 69.399},
            {80, 5.200, 7.390, 9.896, 13.516, 18.482, 25.607, 36.292, 52.029, 68.838},
            {85, 4.416, 6.342, 8.594, 11.905, 16.516, 23.230, 33.529, 49.305, 68.312},
            {90, 3.683, 5.381, 7.413, 10.452, 14.740, 21.057, 30.929, 46.606, 67.815},
            {95, 2.990, 4.493, 6.334, 9.135, 13.131, 19.074, 28.496, 43.955, 67.346},
            {100, 2.327, 3.662, 5.339, 7.933, 11.669, 17.262, 26.230, 41.369, 66.900},
            {110, 1.061, 2.131, 3.544, 5.800, 9.102, 14.082, 22.170, 36.454, 66.072},
            {120, -0.158, 0.716, 1.934, 3.934, 6.899, 11.377, 18.668, 31.936, 65.316},
            {130, -1.356, -0.628, 0.443, 2.249, 4.953, 9.027, 15.628, 27.838, 64.621},
            {140, -2.550, -1.932, -0.972, 0.686, 3.187, 6.939, 12.958, 24.145, 63.977},
            {150, -3.746, -3.213, -2.338, -0.794, 1.548, 5.042, 10.577, 20.821, 63.378},
            {160, -4.950, -4.482, -3.674, -2.219, -0.002, 3.287, 8.421, 17.823, 62.818},
            {170, -6.161, -5.745, -4.989, -3.604, -1.486, 1.637, 6.440, 15.102, 62.291},
            {180, -7.380, -7.004, -6.290, -4.962, -2.923, 0.066, 4.597, 12.617, 61.795},
            {190, -8.604, -8.261, -7.581, -6.297, -4.322, -1.442, 2.863, 10.329, 61.325},
            {200, -9.831, -9.515, -8.861, -7.614, -5.692, -2.901, 1.216, 8.208, 60.879},
            {225, -12.895, -12.626, -12.020, -10.839, -9.011, -6.382, -2.610, 3.469, 59.856},
            {250, -15.923, -15.685, -15.110, -13.970, -12.202, -9.678, -6.132, -0.679, 58.941},
            {275, -18.888, -18.670, -18.115, -17.003, -15.275, -12.821, -9.429, -4.412, 58.113},
            {300, -21.771, -21.567, -21.026, -19.933, -18.232, -15.826, -12.542, -7.834, 57.358},
            {325, -24.562, -24.368, -23.837, -22.758, -21.077, -18.705, -15.497, -11.013, 56.662},
            {350, -27.260, -27.073, -26.549, -25.479, -23.813, -21.466, -18.314, -13.996, 56.019},
            {375, -29.866, -29.684, -29.166, -28.103, -26.447, -24.120, -21.009, -16.816, 55.419},
            {400, -32.387, -32.209, -31.695, -30.638, -28.989, -26.676, -23.597, -19.500, 54.859},
            {425, -34.830, -34.656, -34.144, -33.092, -31.450, -29.147, -26.093, -22.070, 54.332},
            {450, -37.206, -37.034, -36.525, -35.476, -33.839, -31.545, -28.510, -24.545, 53.836},
            {475, -39.525, -39.355, -38.848, -37.801, -36.168, -33.881, -30.861, -26.943, 53.366},
            {500, -41.798, -41.629, -41.123, -40.079, -38.449, -36.167, -33.160, -29.279, 52.921},
            {525, -44.034, -43.866, -43.362, -42.319, -40.691, -38.414, -35.417, -31.566, 52.497},
            {550, -46.243, -46.077, -45.574, -44.532, -42.906, -40.633, -37.644, -33.818, 52.093},
            {575, -48.435, -48.269, -47.767, -46.727, -45.103, -42.832, -39.850, -36.045, 51.707},
            {600, -50.617, -50.452, -49.951, -48.911, -47.289, -45.021, -42.044, -38.257, 51.337},
            {625, -52.796, -52.631, -52.131, -51.092, -49.471, -47.205, -44.233, -40.460, 50.982},
            {650, -54.976, -54.812, -54.312, -53.274, -51.654, -49.389, -46.422, -42.661, 50.642},
            {675, -57.162, -56.998, -56.498, -55.461, -53.841, -51.579, -48.614, -44.864, 50.314},
            {700, -59.354, -59.190, -58.691, -57.654, -56.035, -53.774, -50.812, -47.072, 49.998},
            {725, -61.552, -61.389, -60.890, -59.854, -58.236, -55.975, -53.016, -49.284, 49.693},
            {750, -63.756, -63.593, -63.094, -62.058, -60.440, -58.181, -55.224, -51.498, 49.399},
            {775, -65.960, -65.797, -65.298, -64.263, -62.646, -60.387, -57.433, -53.712, 49.114},
            {800, -68.159, -67.997, -67.498, -66.463, -64.846, -62.589, -59.636, -55.920, 48.838},
            {825, -70.347, -70.184, -69.686, -68.651, -67.035, -64.778, -61.826, -58.115, 48.571},
            {850, -72.513, -72.351, -71.853, -70.818, -69.202, -66.946, -63.995, -60.289, 48.312},
            {875, -74.649, -74.487, -73.989, -72.954, -71.339, -69.083, -66.134, -62.431, 48.060},
            {900, -76.744, -76.582, -76.084, -75.049, -73.434, -71.179, -68.230, -64.530, 47.815},
            {925, -78.785, -78.624, -78.126, -77.092, -75.476, -73.221, -70.274, -66.577, 47.577},
            {950, -80.763, -80.602, -80.104, -79.070, -77.455, -75.200, -72.254, -68.559, 47.346},
            {975, -82.667, -82.505, -82.008, -80.973, -79.359, -77.104, -74.159, -70.466, 47.120},
            {1000, -84.485, -84.324, -83.826, -82.792, -81.178, -78.924, -75.979, -72.288, 46.900}};

    private static final double[][] FieldStrength_1pctLand_600MHz = { //Figure 11 - Last columns is freespace field strength (which we do not use for now)
            {1, 92.788, 94.892, 97.076, 99.699, 102.345, 104.591, 106.007, 106.629, 106.900},
            {2, 82.390, 85.130, 87.816, 91.033, 94.401, 97.503, 99.622, 100.542, 100.879},
            {3, 76.031, 79.199, 82.230, 85.816, 89.602, 93.221, 95.829, 96.974, 97.358},
            {4, 71.287, 74.801, 78.119, 82.004, 86.105, 90.104, 93.101, 94.437, 94.859},
            {5, 67.459, 71.245, 74.808, 78.950, 83.315, 87.625, 90.956, 92.465, 92.921},
            {6, 64.233, 68.233, 72.001, 76.367, 80.963, 85.543, 89.173, 90.848, 91.337},
            {7, 61.442, 65.608, 69.545, 74.107, 78.907, 83.727, 87.637, 89.475, 89.998},
            {8, 58.981, 63.277, 67.353, 72.083, 77.064, 82.101, 86.274, 88.279, 88.838},
            {9, 56.781, 61.178, 65.368, 70.241, 75.382, 80.614, 85.040, 87.217, 87.815},
            {10, 54.794, 59.270, 63.553, 68.547, 73.827, 79.235, 83.902, 86.259, 86.900},
            {11, 52.982, 57.520, 61.879, 66.974, 72.376, 77.942, 82.838, 85.382, 86.072},
            {12, 51.320, 55.907, 60.326, 65.506, 71.013, 76.718, 81.832, 84.572, 85.316},
            {13, 49.785, 54.410, 58.878, 64.129, 69.726, 75.555, 80.872, 83.816, 84.621},
            {14, 48.361, 53.016, 57.524, 62.833, 68.505, 74.443, 79.950, 83.103, 83.977},
            {15, 47.035, 51.712, 56.251, 61.608, 67.345, 73.377, 79.059, 82.428, 83.378},
            {16, 45.794, 50.488, 55.052, 60.449, 66.240, 72.354, 78.196, 81.783, 82.818},
            {17, 44.630, 49.336, 53.919, 59.348, 65.184, 71.369, 77.358, 81.193, 82.291},
            {18, 43.535, 48.249, 52.846, 58.300, 64.174, 70.420, 76.542, 80.637, 81.795},
            {19, 42.502, 47.220, 51.828, 57.302, 63.207, 69.506, 75.747, 80.104, 81.325},
            {20, 41.527, 46.246, 50.860, 56.349, 62.279, 68.623, 74.987, 79.591, 80.879},
            {25, 37.346, 42.033, 46.640, 52.150, 58.144, 64.629, 71.455, 77.239, 78.941},
            {30, 34.052, 38.661, 43.212, 48.682, 54.670, 61.210, 68.237, 75.108, 77.358},
            {35, 31.393, 35.890, 40.350, 45.739, 51.677, 58.225, 65.349, 73.200, 76.019},
            {40, 29.207, 33.564, 37.907, 43.185, 49.043, 55.568, 62.793, 71.296, 74.859},
            {45, 27.383, 31.579, 35.785, 40.927, 46.680, 53.158, 60.456, 69.318, 73.836},
            {50, 25.843, 29.860, 33.912, 38.900, 44.527, 50.940, 58.287, 67.213, 72.921},
            {55, 24.524, 28.352, 32.239, 37.057, 42.543, 48.871, 56.248, 64.966, 72.093},
            {60, 23.382, 27.014, 30.728, 35.366, 40.697, 46.924, 54.311, 62.835, 71.337},
            {65, 22.380, 25.814, 29.351, 33.803, 38.970, 45.081, 52.456, 61.203, 70.642},
            {70, 21.491, 24.729, 28.089, 32.352, 37.348, 43.329, 50.672, 59.607, 69.998},
            {75, 20.692, 23.738, 26.924, 30.999, 35.819, 41.661, 48.950, 58.041, 69.399},
            {80, 19.965, 22.826, 25.843, 29.733, 34.377, 40.071, 47.286, 56.497, 68.838},
            {85, 19.295, 21.981, 24.836, 28.546, 33.016, 38.554, 45.677, 54.975, 68.312},
            {90, 18.671, 21.192, 23.893, 27.430, 31.728, 37.108, 44.122, 53.471, 67.815},
            {95, 18.085, 20.450, 23.005, 26.378, 30.510, 35.730, 42.620, 51.988, 67.346},
            {100, 17.527, 19.748, 22.167, 25.384, 29.356, 34.416, 41.172, 50.527, 66.900},
            {110, 16.476, 18.440, 20.613, 23.546, 27.220, 31.969, 38.431, 47.675, 66.072},
            {120, 15.482, 17.228, 19.189, 21.876, 25.284, 29.739, 35.891, 44.932, 65.316},
            {130, 14.522, 16.084, 17.865, 20.338, 23.512, 27.699, 33.539, 42.310, 64.621},
            {140, 13.581, 14.987, 16.614, 18.906, 21.876, 25.820, 31.358, 39.815, 63.977},
            {150, 12.649, 13.922, 15.419, 17.555, 20.349, 24.076, 29.331, 37.449, 63.378},
            {160, 11.721, 12.880, 14.265, 16.269, 18.911, 22.447, 27.438, 35.210, 62.818},
            {170, 10.792, 11.853, 13.143, 15.033, 17.544, 20.911, 25.663, 33.091, 62.291},
            {180, 9.862, 10.837, 12.045, 13.837, 16.235, 19.455, 23.989, 31.086, 61.795},
            {190, 8.930, 9.828, 10.965, 12.674, 14.974, 18.065, 22.404, 29.185, 61.325},
            {200, 7.994, 8.825, 9.901, 11.538, 13.753, 16.731, 20.895, 27.380, 60.879},
            {225, 5.644, 6.338, 7.292, 8.788, 10.837, 13.589, 17.392, 23.230, 59.856},
            {250, 3.288, 3.878, 4.744, 6.139, 8.069, 10.656, 14.188, 19.503, 58.941},
            {275, 0.937, 1.448, 2.250, 3.572, 5.415, 7.880, 11.207, 16.105, 58.113},
            {300, -1.396, -0.946, -0.192, 1.075, 2.853, 5.227, 8.396, 12.965, 57.358},
            {325, -3.705, -3.301, -2.583, -1.357, 0.372, 2.676, 5.723, 10.029, 56.662},
            {350, -5.985, -5.616, -4.926, -3.731, -2.040, 0.209, 3.160, 7.256, 56.019},
            {375, -8.235, -7.892, -7.224, -6.054, -4.393, -2.187, 0.688, 4.614, 55.419},
            {400, -10.456, -10.134, -9.483, -8.332, -6.695, -4.523, -1.709, 2.079, 54.859},
            {425, -12.651, -12.345, -11.708, -10.572, -8.954, -6.810, -4.046, -0.372, 54.332},
            {450, -14.825, -14.532, -13.905, -12.782, -11.180, -9.059, -6.335, -2.755, 53.836},
            {475, -16.983, -16.700, -16.082, -14.970, -13.380, -11.278, -8.588, -5.086, 53.366},
            {500, -19.130, -18.856, -18.245, -17.141, -15.562, -13.476, -10.814, -7.378, 52.921},
            {525, -21.273, -21.005, -20.400, -19.304, -17.734, -15.660, -13.022, -9.642, 52.497},
            {550, -23.415, -23.153, -22.553, -21.463, -19.900, -17.838, -15.220, -11.888, 52.093},
            {575, -25.562, -25.304, -24.708, -23.623, -22.068, -20.015, -17.414, -14.122, 51.707},
            {600, -27.716, -27.463, -26.871, -25.790, -24.240, -22.195, -19.610, -16.353, 51.337},
            {625, -29.882, -29.632, -29.043, -27.966, -26.420, -24.383, -21.810, -18.584, 50.982},
            {650, -32.059, -31.812, -31.226, -30.152, -28.611, -26.579, -24.018, -20.818, 50.642},
            {675, -34.249, -34.004, -33.420, -32.350, -30.812, -28.786, -26.234, -23.057, 50.314},
            {700, -36.450, -36.208, -35.626, -34.558, -33.023, -31.002, -28.459, -25.302, 49.998},
            {725, -38.661, -38.420, -37.840, -36.774, -35.243, -33.225, -30.690, -27.551, 49.693},
            {750, -40.877, -40.638, -40.059, -38.995, -37.466, -35.452, -32.924, -29.801, 49.399},
            {775, -43.093, -42.856, -42.279, -41.217, -39.690, -37.679, -35.157, -32.049, 49.114},
            {800, -45.305, -45.069, -44.493, -43.433, -41.908, -39.900, -37.383, -34.287, 48.838},
            {825, -47.505, -47.270, -46.695, -45.636, -44.113, -42.108, -39.595, -36.511, 48.571},
            {850, -49.684, -49.450, -48.877, -47.819, -46.297, -44.294, -41.786, -38.712, 48.312},
            {875, -51.835, -51.602, -51.029, -49.973, -48.452, -46.452, -43.947, -40.883, 48.060},
            {900, -53.948, -53.716, -53.144, -52.089, -50.570, -48.571, -46.070, -43.014, 47.815},
            {925, -56.015, -55.784, -55.213, -54.158, -52.640, -50.643, -48.145, -45.097, 47.577},
            {950, -58.026, -57.795, -57.225, -56.171, -54.654, -52.658, -50.163, -47.122, 47.346},
            {975, -59.971, -59.742, -59.172, -58.119, -56.603, -54.609, -52.116, -49.081, 47.120},
            {1000, -61.844, -61.615, -61.045, -59.993, -58.478, -56.485, -53.995, -50.966, 46.900},

    };
    private static final double[][] FieldStrength_1pctLand_2000MHz = {//Figure  19 - Last columns is freespace field strength (which we do not use for now)
            {1, 94.233, 96.509, 98.662, 101.148, 103.509, 105.319, 106.328, 106.732, 106.900},
            {2, 82.711, 86.063, 88.943, 92.187, 95.445, 98.251, 99.972, 100.647, 100.879},
            {3, 75.466, 79.573, 82.996, 86.732, 90.502, 93.925, 96.182, 97.077, 97.358},
            {4, 70.027, 74.676, 78.565, 82.726, 86.888, 90.763, 93.451, 94.539, 94.859},
            {5, 65.657, 70.683, 74.957, 79.501, 84.003, 88.248, 91.304, 92.565, 92.921},
            {6, 62.006, 67.294, 71.875, 76.760, 81.573, 86.141, 89.526, 90.949, 91.337},
            {7, 58.874, 64.345, 69.167, 74.349, 79.451, 84.314, 88.001, 89.580, 89.998},
            {8, 56.136, 61.735, 66.743, 72.180, 77.547, 82.686, 86.660, 88.391, 88.838},
            {9, 53.705, 59.397, 64.548, 70.199, 75.807, 81.206, 85.457, 87.339, 87.815},
            {10, 51.522, 57.280, 62.541, 68.369, 74.194, 79.839, 84.359, 86.395, 86.900},
            {11, 49.543, 55.348, 60.693, 66.667, 72.684, 78.558, 83.344, 85.538, 86.072},
            {12, 47.736, 53.573, 58.982, 65.075, 71.260, 77.348, 82.394, 84.751, 85.316},
            {13, 46.073, 51.933, 57.390, 63.580, 69.909, 76.194, 81.495, 84.023, 84.621},
            {14, 44.536, 50.409, 55.903, 62.170, 68.623, 75.088, 80.638, 83.345, 83.977},
            {15, 43.109, 48.989, 54.508, 60.836, 67.396, 74.022, 79.815, 82.708, 83.378},
            {16, 41.777, 47.658, 53.196, 59.572, 66.220, 72.993, 79.018, 82.107, 82.818},
            {17, 40.531, 46.409, 51.957, 58.371, 65.094, 71.996, 78.243, 81.536, 82.291},
            {18, 39.362, 45.232, 50.786, 57.227, 64.011, 71.028, 77.487, 80.991, 81.795},
            {19, 38.261, 44.120, 49.675, 56.136, 62.971, 70.088, 76.745, 80.469, 81.325},
            {20, 37.223, 43.068, 48.619, 55.093, 61.969, 69.173, 76.017, 79.966, 80.879},
            {25, 32.791, 38.529, 44.017, 50.483, 57.457, 64.947, 72.530, 77.659, 78.941},
            {30, 29.313, 34.900, 40.271, 46.649, 53.606, 61.212, 69.260, 75.604, 77.358},
            {35, 26.509, 31.910, 37.130, 43.372, 50.246, 57.871, 66.197, 73.633, 76.019},
            {40, 24.203, 29.394, 34.437, 40.508, 47.258, 54.843, 63.329, 71.641, 74.859},
            {45, 22.275, 27.239, 32.087, 37.965, 44.561, 52.066, 60.636, 69.571, 73.836},
            {50, 20.641, 25.367, 30.009, 35.677, 42.098, 49.494, 58.096, 67.447, 72.921},
            {55, 19.239, 23.722, 28.150, 33.599, 39.830, 47.095, 55.688, 65.425, 72.093},
            {60, 18.021, 22.260, 26.473, 31.696, 37.727, 44.844, 53.398, 63.427, 71.337},
            {65, 16.951, 20.949, 24.948, 29.945, 35.771, 42.726, 51.214, 61.454, 70.642},
            {70, 16.000, 19.764, 23.554, 28.327, 33.944, 40.727, 49.126, 59.511, 69.998},
            {75, 15.145, 18.684, 22.271, 26.825, 32.235, 38.839, 47.129, 57.605, 69.399},
            {80, 14.368, 17.692, 21.086, 25.427, 30.632, 37.054, 45.218, 55.738, 68.838},
            {85, 13.655, 16.776, 19.985, 24.122, 29.128, 35.364, 43.389, 53.914, 68.312},
            {90, 12.994, 15.924, 18.957, 22.901, 27.713, 33.764, 41.639, 52.135, 67.815},
            {95, 12.375, 15.126, 17.995, 21.754, 26.380, 32.249, 39.964, 50.402, 67.346},
            {100, 11.791, 14.375, 17.089, 20.675, 25.123, 30.812, 38.362, 48.717, 66.900},
            {110, 10.700, 12.985, 15.421, 18.690, 22.808, 28.153, 35.364, 45.491, 66.072},
            {120, 9.683, 11.711, 13.906, 16.898, 20.722, 25.747, 32.617, 42.457, 65.316},
            {130, 8.715, 10.523, 12.510, 15.262, 18.825, 23.557, 30.096, 39.612, 64.621},
            {140, 7.777, 9.397, 11.205, 13.748, 17.082, 21.550, 27.774, 36.945, 63.977},
            {150, 6.859, 8.317, 9.971, 12.333, 15.466, 19.698, 25.627, 34.447, 63.378},
            {160, 5.951, 7.270, 8.792, 10.997, 13.955, 17.974, 23.632, 32.104, 62.818},
            {170, 5.051, 6.249, 7.656, 9.725, 12.529, 16.360, 21.768, 29.903, 62.291},
            {180, 4.153, 5.247, 6.554, 8.505, 11.173, 14.837, 20.017, 27.831, 61.795},
            {190, 3.257, 4.260, 5.480, 7.328, 9.877, 13.392, 18.366, 25.876, 61.325},
            {200, 2.362, 3.284, 4.428, 6.186, 8.631, 12.013, 16.800, 24.025, 60.879},
            {225, 0.125, 0.885, 1.876, 3.452, 5.686, 8.798, 13.191, 19.788, 59.856},
            {250, -2.108, -1.470, -0.591, 0.851, 2.928, 5.835, 9.922, 16.003, 58.941},
            {275, -4.330, -3.785, -2.991, -1.650, 0.309, 3.058, 6.905, 12.567, 58.113},
            {300, -6.534, -6.061, -5.332, -4.069, -2.202, 0.425, 4.084, 9.403, 57.358},
            {325, -8.715, -8.299, -7.621, -6.417, -4.622, -2.092, 1.415, 6.456, 56.662},
            {350, -10.870, -10.499, -9.861, -8.705, -6.967, -4.514, -1.129, 3.683, 56.019},
            {375, -12.999, -12.663, -12.058, -10.941, -9.248, -6.858, -3.572, 1.050, 55.419},
            {400, -15.104, -14.797, -14.217, -13.131, -11.476, -9.137, -5.933, -1.470, 54.859},
            {425, -17.187, -16.903, -16.345, -15.285, -13.661, -11.364, -8.229, -3.897, 54.332},
            {450, -19.253, -18.989, -18.449, -17.410, -15.811, -13.549, -10.471, -6.252, 53.836},
            {475, -21.307, -21.060, -20.534, -19.513, -17.936, -15.704, -12.675, -8.551, 53.366},
            {500, -23.356, -23.122, -22.609, -21.603, -20.045, -17.838, -14.850, -10.807, 52.921},
            {525, -25.405, -25.183, -24.680, -23.687, -22.144, -19.959, -17.006, -13.034, 52.497},
            {550, -27.461, -27.248, -26.754, -25.772, -24.243, -22.077, -19.154, -15.243, 52.093},
            {575, -29.527, -29.323, -28.837, -27.865, -26.347, -24.197, -21.301, -17.443, 51.707},
            {600, -31.610, -31.413, -30.934, -29.970, -28.463, -26.327, -23.454, -19.642, 51.337},
            {625, -33.714, -33.523, -33.050, -32.093, -30.595, -28.471, -25.618, -21.848, 50.982},
            {650, -35.840, -35.654, -35.187, -34.237, -32.746, -30.633, -27.798, -24.064, 50.642},
            {675, -37.990, -37.810, -37.347, -36.402, -34.919, -32.815, -29.996, -26.294, 50.314},
            {700, -40.165, -39.989, -39.530, -38.591, -37.113, -35.018, -32.213, -28.540, 49.998},
            {725, -42.363, -42.190, -41.735, -40.800, -39.328, -37.241, -34.449, -30.801, 49.693},
            {750, -44.579, -44.410, -43.959, -43.027, -41.560, -39.480, -36.699, -33.074, 49.399},
            {775, -46.810, -46.644, -46.195, -45.268, -43.805, -41.731, -38.960, -35.356, 49.114},
            {800, -49.047, -48.884, -48.438, -47.514, -46.055, -43.986, -41.225, -37.639, 48.838},
            {825, -51.283, -51.122, -50.679, -49.758, -48.302, -46.239, -43.485, -39.917, 48.571},
            {850, -53.507, -53.349, -52.907, -51.989, -50.537, -48.478, -45.732, -42.179, 48.312},
            {875, -55.708, -55.552, -55.113, -54.196, -52.748, -50.693, -47.954, -44.414, 48.060},
            {900, -57.874, -57.720, -57.282, -56.368, -54.922, -52.871, -50.138, -46.612, 47.815},
            {925, -59.992, -59.839, -59.403, -58.491, -57.048, -55.000, -52.273, -48.758, 47.577},
            {950, -62.049, -61.898, -61.463, -60.553, -59.112, -57.067, -54.345, -50.841, 47.346},
            {975, -64.033, -63.883, -63.450, -62.541, -61.102, -59.060, -56.343, -52.849, 47.120},
            {1000, -65.931, -65.783, -65.351, -64.444, -63.007, -60.968, -58.255, -54.770, 46.900},};


    @Override
    public void consistencyCheck(Scenario scenario, List<Object> list, JTG56Input input, Validator<JTG56Input> validator) {
        //TODO consistency

        double tTime = input.time().getBounds().getMax();
        if (!Mathematics.equals(tTime, 1., 0.001)) if (!Mathematics.equals(tTime, 50., 0.001))
            validator.error("Time probability interpolation between 1% and 50% is not yet supported. You should enter either 1 (for 1%) or 50 (for 50%)" +
            PluginCheckUtilsToBeRemoved.getExceptionHint());
        if (input.cutOff() > 100.) validator.error("CutOff distance cannot exceed 100 meters" + PluginCheckUtilsToBeRemoved.getExceptionHint());

        Bounds frequency = null, hTX = null, hRX = null;
        double maxDistance = 0;
        boolean isIndoor = false;
        if (list.size() > 0) {
            maxDistance = PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,list);
            if (maxDistance > 1000)validator.error("Distance above 1000 km is not yet supported - this is a temporary limitation" + PluginCheckUtilsToBeRemoved.getExceptionHint());

            if (list.get(0) instanceof RadioSystem) {
                frequency = ((RadioSystem) list.get(0)).getFrequency().getBounds();
                hTX = ((RadioSystem) list.get(0)).getTransmitter().getHeight().getBounds();
                hRX = ((RadioSystem) list.get(0)).getReceiver().getHeight().getBounds();
                if (list.get(0) instanceof GenericSystem) {
                    GenericSystem system = (GenericSystem) list.get(0);
                    for (LocalEnvironment env : system.getReceiver().getLocalEnvironments()) {
                        if (env.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
                    }
                    for (LocalEnvironment env : system.getTransmitter().getLocalEnvironments()) {
                        if (env.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
                    }
                }
                if (list.get(0) instanceof CellularSystem){
                    CellularSystem system = (CellularSystem) list.get(0);
                   for (LocalEnvironment env : system.getReceiver().getLocalEnvironments()) {
                        if (env.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
                    }
                    for (LocalEnvironment env : system.getTransmitter().getLocalEnvironments()) {
                        if (env.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
                    }
                }
            } else if (list.get(0) instanceof InterferenceLink) {
                frequency = ((InterferenceLink) list.get(0)).getInterferingSystem().getFrequency().getBounds();
                hTX = ((InterferenceLink) list.get(0)).getInterferingSystem().getTransmitter().getHeight().getBounds();
                hRX = ((InterferenceLink) list.get(0)).getVictimSystem().getReceiver().getHeight().getBounds();
                InterferenceLink link = (InterferenceLink) list.get(0);
                for (LocalEnvironment env : link.getVictimSystem().getReceiver().getLocalEnvironments()) {
                    if (env.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
                }
                for (LocalEnvironment env : link.getInterferingSystem().getTransmitter().getLocalEnvironments()) {
                    if (env.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
                }
            }
            if (list.size() > 1 && list.get(0) instanceof InterferenceLink) {
                if (list.get(1) instanceof SensingLink) {
                    hRX = ((InterferenceLink) list.get(0)).getInterferingSystem().getTransmitter().getHeight().getBounds();
                    hTX = ((InterferenceLink) list.get(0)).getVictimSystem().getTransmitter().getHeight().getBounds();
                }
            }
            if (frequency != null && (frequency.getMax() > 2000 || frequency.getMin() < 600))
                validator.error(PluginCheckUtilsToBeRemoved.getManualReferene("Frequency : implemented to vary from 600 MHz to 2000 MHz as indicated in 3K/54"));
            if (hTX != null && (hTX.getMax() > 200. || hTX.getMin() < 30.))
                validator.error("<HtMl>TX antenna height should be in the range 30 m to 1200 m.<br/>" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(list) > 10 || PluginCheckUtilsToBeRemoved.getAntennaHeightRXmin(list) < 1.5)
                validator.error("RX antenna height should be in the range 1.5 m to 10 m" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            if (hRX != null && hTX != null && (hTX.getMin() < 5. && hRX.getMin() < 5. && input.allowReciprocity() && maxDistance > 1.))
                validator.error("<HtMl>Note that the combination of low antenna height (i.e. 1.5 m) " +
                        "for both Rx and Tx <br/>should be limited to small distances as this "  +
                        "combination is not valid for the P1546.");
            if (isIndoor || frequency != null && (frequency.getMax() > 2000 || frequency.getMin() < 600))
                validator.error(PluginCheckUtilsToBeRemoved.getManualReferene("e.g. indoor"));
        }
    }

    @Override
    public Description description() {
        return new DescriptionImpl("JTG56", "<html>Propagation model based on the WP3K/JTG 5-6/54 document<br>" +
                "with the option of applying the so called 'SE42 approach'</html>");
    }
}
