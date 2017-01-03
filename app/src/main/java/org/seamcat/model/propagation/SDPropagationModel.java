package org.seamcat.model.propagation;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.plugin.propagation.SphericalDiffractionInput;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class SDPropagationModel implements PropagationModelPlugin<SphericalDiffractionInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, SphericalDiffractionInput input, Validator<SphericalDiffractionInput> validator) {
        Distribution frequency = HataSE21PropagationModel.findFrequency(scenario, path);
        if ( frequency != null ) {
            Bounds bounds = frequency.getBounds();
            if (bounds.getMax() < 3000) {
                validator.error("Frequencies below 3 GHz are not supported by the Spherical Diffraction model" +
                PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
        }
    }

    private static final double EARTHRADIUS_IN_KM = 6371.0; //see p.7 of ITU-R P.452-14

    private double rBeta;

    public double f(double rX) {
        if (rX < 1.6){
            return -20.0 * Math.log10(rX) - 5.6488 * Math.pow(rX, 1.425);
        } else {
            return 11.0 + Math.log10(rX) - 17.6 * rX;
        }
    }

    /**
     * Calculate the Effective Earth radius
     * It uses the median effective Earth radius factor k50
     *
     * It assumes a true Earth radius of 6 371 km
     *
     * @return ae = 6371 * kKe
     */
    public double EffectiveEarthRadiusCalculation(double rRefrIndexGradient, double rRefrLayerProb, double rTimePercentage){
        double rAe = 0;
        double rRe = EARTHRADIUS_IN_KM; // earth radius in Km
        double rKe;
        double rKe50; // multiplicative factor for earth radius for time percentage = 50 %
        // Equivalent earth radius (dependent on time percentage) : reflects the
        // vairations in path loss

        rKe50 = 157.0 / (157.0 - rRefrIndexGradient);
        if (rTimePercentage > 50){
            rAe = rKe50 * rRe;
        }else{
            rKe = rKe50 + (5 - rKe50) * ((1.7 - Math.log10(rTimePercentage))/(1.7 - Math.log10(rRefrLayerProb)));
            rAe = rKe * rRe;
        }


        return rAe;
    }

    public double diffractionLoss(double rFreq, double rDist, double rHTx,
                                  double rHRx, double rTimePercentage) {
        double rDiffractionLoss;

        // model fixed parameters
        double rK = getEarthSurfaceAdmittance(); // Geoclimatic factor : should be calculated

        // normalized variables
        double rX; // normalized path between Tx and Rx
        double rYRx; // normalized antenna height for receiver
        double rYTx; // normalized antenna height for transceiver

        // intermediate parameters

        double rAe;
        double rK2 = rK * rK;
        double rK4 = rK2 * rK2;
        double dmin = 0.0;
        double rXmin = 0.0;
        double rXlim = 0.0;

        rAe = EffectiveEarthRadiusCalculation(getRefrIndexGradient(),getRefrLayerProb(),rTimePercentage);

        if (rFreq < 20) {//20 MHz
            rBeta = (1 + 1.6 * rK2 + 0.67 * rK4) / (1.0 + 4.5 * rK2 + 1.53 * rK4);
        } else {
            rBeta = 1;
        }

        rX = 2.188 * rBeta * Math.pow(rFreq, 1.0 / 3.0) * Math.pow(rAe, -2.0 / 3.0) * rDist;
        rYTx = 9.575 * 1e-3 * rBeta * Math.pow(rFreq, 2.0 / 3.0)/ Math.pow(rAe, 1.0 / 3.0) * rHTx;
        rYRx = 9.575 * 1e-3 * rBeta * Math.pow(rFreq, 2.0 / 3.0)/ Math.pow(rAe, 1.0 / 3.0) * rHRx;
        rXlim = 1.096 - 1.280 * (1 - rBeta);
        rXmin = rXlim + Math.pow(rBeta * rYTx, 1.0/2.0) * delta(rYTx , rBeta) + Math.pow(rBeta * rYRx, 1.0/2.0) * delta(rYRx , rBeta);
        dmin = rXmin / (2.188 * rBeta * Math.pow(rFreq, 1.0 / 3.0) * Math.pow(rAe, -2.0 / 3.0));

        rDiffractionLoss = f(rX) + g(rYTx,rBeta * rYTx) + g(rYRx,rBeta * rYRx);

        if(rDiffractionLoss > 0){
            rDiffractionLoss = 0.0;
        }
        return -rDiffractionLoss;
    }

    private Double wiLoss = 5.0;
    private Double wiStdDev = 10.0;
    private Double floorLoss = 18.3;
    private Double empiricalParameter = 0.46;
    private Double roomSize = 4.0;
    private Double floorHeight = 3.0;
    private Double waterCtr = 3.0;
    private Double earthSurfaceAdmittance = 1e-5;
    private Double refrIndexGradient = 40.0;
    private Double refrLayerProb = 1.0;

    public void setEarthSurfaceAdmittance( double esa ) {
        earthSurfaceAdmittance = esa;
    }

    @Override
    public double evaluate(LinkResult linkResult, boolean variations, SphericalDiffractionInput input) {
        double rFreq = linkResult.getFrequency();
        double rDist = linkResult.getTxRxDistance();
        double rHTx = linkResult.txAntenna().getHeight();
        double rHRx = linkResult.rxAntenna().getHeight();

        double rL = 0.0;
        double rStdDev = 0.0;
        wiLoss = input.wallLossInIn();
        wiStdDev = input.wallLossStdDev();
        floorLoss = input.adjacentFloorLoss();
        empiricalParameter = input.empiricalParameters();
        roomSize = input.sizeOfRoom();
        floorHeight = input.floorHeight();
        waterCtr = input.waterConcentration();
        earthSurfaceAdmittance = input.earthSurfaceAdm();
        refrIndexGradient = input.refractionGradient();
        refrLayerProb = input.refractionProb();

        // Initial loss set to zero (what about variations
        // included in diffraction loss ???)
        double rP;

        double rW = 0; // fraction of the total path over water. Setto zero as sea path not simulated
        double rRho2 = 7.5 + 2.5 * rW; // water vapour density for ducting study and Line of Sight propagation

        // general variable
        Distribution timePercentage = input.timePercentage();
        rP = timePercentage.trial();

		/* need the following parameter as input*/
        double rPressure = 1013;
        double rTemperature = 10;

        // consistency check
        if (rFreq < 3000){
            throw new RuntimeException("Frequencies below 3 GHz are not supported by the Spherical Diffraction model");
        }

        // calculation of the median loss
		/*rL = 92.5 + 20.0 * Math.log10(rFreq / 1000) + 20.0 * Math.log10(rDist) 
			+ gazLoss(rFreq, rDist) 
			+ diffractionLoss(rFreq, rDist, rHTx, rHRx, rP);*/

        rL = 92.5 + 20.0 * Math.log10(rFreq / 1000) + 20.0 * Math.log10(rDist)
                + attenuationByAtmosphericGasesP676ver8(rFreq / 1000, rRho2, rPressure, rTemperature)
                + diffractionLoss(rFreq, rDist, rHTx, rHRx, rP);

        // Corrections of median loss and standard deviation due to local
        // environment
        LocalEnvCorrections lec = Helper.localEnvCorrections(new LocalEnvCorrections(rL, rStdDev), linkResult,
                floorHeight, roomSize, wiLoss, floorLoss, empiricalParameter, wiStdDev);
        rL = lec.rMedianLoss;
        rStdDev = lec.rStdDev;

        // calculation of the standard deviation
        if (variations) {
            rL += Factory.distributionFactory().getGaussianDistribution(0, rStdDev).trial();
        }

        if (Double.isInfinite(rL)) {
            rL = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
        }
        return rL;
    }

    public double delta(double rY, double rBeta) {
        double rDelta = 0.0;
        double rDeltaZero = 0.0;
        double rDeltaInfinity = 0.0;

        rDeltaZero = 0.5 * (1 + Math.tanh((-0.255 + 0.5 * Math.log10(rBeta * rY))/0.3));
        rDeltaInfinity = 0.5 * (1 + Math.tanh((0.255 + 0.5 * Math.log10(rBeta * rY))/0.3));

        rDelta = rDeltaZero + 1.779 * (1- rBeta) * ( rDeltaInfinity - rDeltaZero);

        return rDelta;
    }
    public double g(double rY, double rB) {
        double rK = getEarthSurfaceAdmittance(); // geoclimatic factor K
        double rG;

        double rK1 = rK * 10.0;
        double rK2 = rK / 10.0;

        if (rB > 2) {
            rG = 17.6 * Math.sqrt(rB - 1.1) - 5.0 * Math.log10(rB - 1.1) - 8.0;
        } else if (rB > rK1) {
            rG = 20.0 * Math.log10(rB + 0.1 * rB * rB * rB);
        } else if (rY > rK2) {
            rG = 2.0 + 20.0 * Math.log10(rK) + 9 * Math.log10(rB/rK) * (Math.log10(rB / rK) + 1);
        } else {
            rG = 2.0 + 20.0 * Math.log10(rK);
        }
        return rG;
    }

    /**
     * <p>Recommendation ITU-R P.676 provides methods to estimate
     * the attenuation of atmospheric gases on terrestrial and
     * slant paths using a simplified approximate method to estimate gaseous
     * attenuation that is applicable in the frequency range 1 350 GHz.</p>
     * <p>The below code gives an approximate estimation of gaseous
     * attenuation in the frequency range 1-350 GHz. It is a simplified
     * algorithms for quick, approximate estimation of gaseous attenuation
     * for a limited range of meteorological conditions and a limited
     * variety of geometrical configurations.</p>
     *
     * <p>Note: the Frequency is in GHz</p>
     *
     * @param rFreq
     * @return gaseous attenuation = gammaDryAir + gammaWaterVapour
     */
    public double attenuationByAtmosphericGasesP676ver8(double rFreq, double rRho, double rPressure, double rTemperature) {
        double rGamma = 0.0;

        rPressure = rPressure / 1013;
        rTemperature = 288/(273 + rTemperature);

        double gammaDryAir = attenuationDueToDryAir(rFreq, rPressure, rTemperature);
        double gammaWaterVapour = attenuationDueToWaterVapour(rFreq, rPressure, rTemperature, rRho);
        rGamma = gammaDryAir + gammaWaterVapour;

        return rGamma;
    }

    public double functionPhi(double rPressure, double rTemperature, double a, double b, double c, double d){
        double rValue = 0;

        double rTemp = (c * (1 - rPressure)) + (d * (1 - rTemperature));

        rValue = Math.pow(rPressure, a) * Math.pow(rTemperature, b) * Math.exp(rTemp);
        return rValue;
    }

    public double functionG(double rFreq, double rFreqi){
        double rValue =0;

        rValue = 1 + Math.pow((rFreq - rFreqi)/(rFreq + rFreqi), 2);

        return rValue;
    }

    public double attenuationDueToDryAir(double rFreq, double rPressure, double rTemperature){
        double rGammaDryAir = 0;

        double rZeta1 = functionPhi(rPressure, rTemperature, 0.0717, -1.8132, 0.0156, -1.6515);
        double rZeta2 = functionPhi(rPressure, rTemperature, 0.5146, -4.6368, -0.1921, -5.7416);
        double rZeta3 = functionPhi(rPressure, rTemperature, 0.3414, -6.5851, 0.2130, -8.5854);
        double rZeta4 = functionPhi(rPressure, rTemperature, -0.0112, 0.0092, -0.1033, -0.0009);
        double rZeta5 = functionPhi(rPressure, rTemperature, 0.2705, -2.7192, -0.3016, -4.1033);
        double rZeta6 = functionPhi(rPressure, rTemperature, 0.2445, -5.9191, 0.0422, -8.0719);
        double rZeta7 = functionPhi(rPressure, rTemperature, -0.1833, 6.5589, -0.2402, 6.131);
        double rGamma54 = 2.192 * functionPhi(rPressure, rTemperature, 1.8286, -1.9487, 0.4051, -2.8509);
        double rGamma58 = 12.59 * functionPhi(rPressure, rTemperature, 1.0045, 3.5610, 0.1588, 1.2834);
        double rGamma60 = 15.0 * functionPhi(rPressure, rTemperature, 0.9003, 4.1335, 0.0427, 1.6088);
        double rGamma62 = 14.28 * functionPhi(rPressure, rTemperature, 0.9886, 3.4176, 0.1827, 1.3429);
        double rGamma64 = 6.819 * functionPhi(rPressure, rTemperature, 1.4320, 0.6258, 0.3177, -0.5914);
        double rGamma66 = 1.908 * functionPhi(rPressure, rTemperature, 2.0717, -4.1404, 0.4910, -4.8718);
        double rDelta = -0.00306 * functionPhi(rPressure, rTemperature, 3.211, -14.94, 1.583, -16.37);

        if (rFreq <= 54){
            rGammaDryAir = ((7.2 * Math.pow(rTemperature, 2.8)/(Math.pow(rFreq, 2)
                    + (0.34 * Math.pow(rPressure, 2)* Math.pow(rTemperature, 1.6))))
                    +((0.62 * rZeta3)/(Math.pow(54-rFreq, 1.16 * rZeta1) + 0.083 * rZeta2)))
                    * Math.pow(rFreq, 2)
                    * Math.pow(rPressure, 2)
                    * Math.pow(10, -3);
        }else if (rFreq <= 60){
            rGammaDryAir = Math.exp(((rFreq - 58) * (rFreq -60) * Math.log(rGamma54) / 24)
                    -((rFreq - 54) * (rFreq - 60) * Math.log(rGamma58) / 8)
                    +((rFreq - 54) * (rFreq -58) * Math.log(rGamma60) / 12));
        }else if (rFreq <= 62){
            rGammaDryAir = rGamma60 + ((rGamma62 - rGamma60) * (rFreq - 60)) / 2;
        }else if (rFreq <= 66){
            rGammaDryAir = Math.exp(((rFreq - 64) * (rFreq - 66) * Math.log(rGamma62) / 8)
                    -((rFreq - 62) * (rFreq - 66) * Math.log(rGamma64) / 4)
                    +((rFreq - 62) * (rFreq - 64) * Math.log(rGamma66) / 8));
        }else if (rFreq <= 120){
            rGammaDryAir = ((3.02 * Math.pow(10, -4) * Math.pow(rTemperature, 3.5))
                    + ((0.283 * Math.pow(rTemperature, 3.8))/(Math.pow(rFreq - 118.75, 2) + (2.91 * Math.pow(rPressure, 2) * Math.pow(rTemperature, 1.6))))
                    + ((0.502 * rZeta6 * (1 - (0.0163 * rZeta7 * (rFreq - 66))))/(Math.pow(rFreq - 66, 1.4346 * rZeta4) + (1.15 * rZeta5))))
                    * Math.pow(rFreq, 2)
                    * Math.pow(rPressure, 2)
                    * Math.pow(10, -3);
        }else if (rFreq <= 350){
            rGammaDryAir = (((3.02 * Math.pow(10, -4))/(1 + (1.9 * Math.pow(10, -5) * Math.pow(rFreq, 1.5))))
                    + ((0.283 * Math.pow(rTemperature, 0.3))/(Math.pow(rFreq - 118.75, 2) + (2.91 * Math.pow(rPressure, 2) * Math.pow(rTemperature, 1.6)))))
                    * Math.pow(rFreq, 2)
                    * Math.pow(rPressure, 2)
                    * Math.pow(rTemperature, 3.5)
                    * Math.pow(10, -3)
                    + rDelta;
        }
        return rGammaDryAir;
    }
    public double attenuationDueToWaterVapour(double rFreq, double rPressure, double rTemperature, double rRho){
        double rGammaWaterVapour = 0;

        double rEta1 = (0.955 * rPressure * Math.pow(rTemperature, 0.68)) + (0.006 * rRho);
        double rEta2 = (0.735 * rPressure * Math.pow(rTemperature, 0.5)) + (0.0353 * Math.pow(rTemperature, 4)* rRho);

        double rValue1 = (3.98 * rEta1 * Math.exp(2.23*(1-rTemperature)))/(Math.pow((rFreq - 22.235), 2) + 9.42 * Math.pow(rEta1, 2)) * functionG(rFreq, 22);
        double rValue2 = (11.96 * rEta1 * Math.exp(0.7*(1-rTemperature)))/(Math.pow(rFreq - 183.31, 2) + (11.14 * Math.pow(rEta1, 2)));
        double rValue3 = (0.081 * rEta1 * Math.exp(6.44 * (1 - rTemperature)))/(Math.pow(rFreq - 321.226, 2) + (6.29 * Math.pow(rEta1, 2)));
        double rValue4 = (3.66 * rEta1 * Math.exp(1.6*(1-rTemperature)))/(Math.pow(rFreq - 321.153, 2) + 9.22 * Math.pow(rEta1, 2));
        double rValue5 = (25.37 * rEta1 * Math.exp(1.09 * (1 - rTemperature)))/(Math.pow(rFreq - 380, 2));
        double rValue6 = (17.4 * rEta1 * Math.exp(1.46 * (1 - rTemperature)))/(Math.pow(rFreq - 448, 2));
        double rValue7 = (844.6 * rEta1 * Math.exp(0.17 * (1- rTemperature)))/(Math.pow(rFreq - 557, 2)) * functionG(rFreq, 557);
        double rValue8 = (290 * rEta1 * Math.exp(0.41 * (1- rTemperature)))/(Math.pow(rFreq - 752, 2)) * functionG(rFreq, 752);
        double rValue9 = (8.3328 * Math.pow(10, 4) * rEta2 * Math.exp(0.99 * (1 - rTemperature)) )/(Math.pow(rFreq - 1780, 2)) * functionG(rFreq, 1780);
        rGammaWaterVapour = (rValue1
                + rValue2
                + rValue3
                + rValue4
                + rValue5
                + rValue6
                + rValue7
                + rValue8
                + rValue9) * Math.pow(rFreq, 2) * Math.pow(rTemperature, 2.5) * rRho * Math.pow(10, -4);

        return rGammaWaterVapour;
    }
    public double getEarthSurfaceAdmittance() {
        return earthSurfaceAdmittance;
    }

    public double getRefrIndexGradient() {
        return refrIndexGradient;
    }

    public double getRefrLayerProb() {
        return refrLayerProb;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Spherical Diffraction (ITU-R P.526-2)", "<html><body><b><u>Frequency range:</u></b><br>above 3 GHz<br><b><u>Distance range:</u></b><br>Up to and beyond radio horizon<br><b><u>Typical application area:</u></b><br>Interference on terrestrial paths in <br>predominantly open (e.g. rural) areas.</body></html>");
    }

    public void setRefrIndexGradient(double refrIndexGradient) {
        this.refrIndexGradient = refrIndexGradient;
    }

    public void setRefrLayerProb(double refrLayerProb) {
        this.refrLayerProb = refrLayerProb;
    }
}
