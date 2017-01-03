package org.seamcat.model.propagation;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.P452ver14Input;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;


public class P452ver14PropagationModel implements PropagationModelPlugin<P452ver14Input> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, P452ver14Input input, Validator<P452ver14Input> validator) {
        Distribution frequency = HataSE21PropagationModel.findFrequency(scenario, path);
        if ( frequency != null ) {
            Bounds bounds = frequency.getBounds();
            if (bounds.getMin() < 700 || bounds.getMax() > 50000) {
                validator.error("P452-14 model applicable for frequencies in the range 700 MHz to 50 GHz");
            }
        }

		if (path.size() > 0){
			if (PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,path) > 0.3){
				String errorMessage = "Distances above 300 m are not supported by the Extended Hata propagation SRD model";
				if (!PluginCheckUtilsToBeRemoved.getDeltaPosition().isEmpty()) errorMessage += PluginCheckUtilsToBeRemoved.getDeltaPosition();
				if (!PluginCheckUtilsToBeRemoved.getRelativePosition().isEmpty()) errorMessage += PluginCheckUtilsToBeRemoved.getRelativePosition();
				validator.error(errorMessage + PluginCheckUtilsToBeRemoved.getExceptionHint());
			}

			if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(path) < 10 || PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) < 10
					|| PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(path) > 200 || PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) > 200){
				validator.error(PluginCheckUtilsToBeRemoved.getManualReferene("e.g. antenna heights"));
			}


		}

    }

    private static final double EARTHRADIUS_IN_KM = 6371.0; //see p.7 of ITU-R P.452-14
    private Double rPressure = 1013.25;
    private Double refrIndexGradient = 40.0;
    Double rTemperature = 15.0;
    Double rLat = 45.0; // path centre latitude (degrees).
    Double rAht = 0.0; // additional losses to account for clutter shielding the transmitter, to be set to zero if there is no such shielding
    Double rAhr = 0.0; // additional losses to account for clutter shielding the receiever, to be set to zero if there is no such shielding
    Double rGTx = 0.0; // Antenna gain to the tropospheric model rGRx
    Double rGRx = 0.0;// Antenna gain to the tropospheric model rGRx

    Double rNo = 325.0; // Sea Level Surface Refractivity: Figure 13 of ITU-R P.452-14 provides a map of average sea-level surface refractivity, N0, for the troposcatter model
    // overall value used in various method defined at the highest level
	public double rBeta0 = 0;
    double rLd50 = 0;
    double rLdBeta = 0;
    double rLB0p = 0;
    double rLb0Beta = 0;
    double rLdp = 0;
    double rLbd50 = 0;
    double rDistIm50 = 0;
    double rDistIt50 = 0;
    double rDistIr50 = 0;
    double rLt50 = 0;
    double rLr50 = 0;
    double rHm = 0; 	// terrain roughness set to zero due to the flat earth assumption.

	double rW = 0; // fraction of the total path over water. Set to zero as sea path not simulated

    @Override
	public double evaluate(LinkResult linkResult, boolean variations, P452ver14Input input) {
        double rFreq = linkResult.getFrequency();
        double rDist = linkResult.getTxRxDistance();
        double rHTx = linkResult.txAntenna().getHeight();
        double rHRx = linkResult.rxAntenna().getHeight();

        double rL = 0.0;
		double rStdDev = 0.0; // Initial loss set to zero (what about variations
		// included in diffraction loss ???)
		double rP;

        rPressure = input.surfacePressure();
        refrIndexGradient = input.refractionIndex();
        rTemperature = input.surfaceTemperature();
        rLat = input.latitude();
        rAht = input.clutterLossTx();
        rAhr = input.clutterLossRx();
        rGTx = input.antennaGainTx();
        rGRx = input.antennaGainRx();
        rNo  = input.seaLevelSurfaceRefractivity();

		double rGRx = getAntennaGainReceiverForTroposphericModel();
		double rGTx = getAntennaGainTransmitterForTroposphericModel();
		double rNo = getSeaLevelSurfaceRefractivity();
		double rRho1 = input.waterConcentration(); // water vapour density for tropospheric study only
		double rRho2 = 7.5 + 2.5 * rW; // water vapour density for ducting study and Line of Sight propagation

		// general variable
        rP = input.timePercentage().trial();
		double rAe = medianEffectiveEarthRadiusCalculation(getRefrIndexGradient());
		double rABeta = betaPercentageEffectiveEarthRadiusCalculation();

		//Path classification parameters (Section 4, P.50 of the ITU-R P.452)
		double rDistt = 0; // interfering antenna horizon distance, dlt (see p.51 of ITU-R P.452-14)
		double rDistr = 0;// interfering-with antenna horizon distance, dlr (see p.51 of ITU-R P.452-14)
		double rhi = 0; // no terrain data are used in SEAMCAT, so it is assumed flat
		double rhj = rhi;
		double rhts = rhi + rHTx;
		double rhrs = rhi + rHRx;

		double rStep = 0.01; // step of the evaluation of di every 0.01 km
		double [] rDisti;
		int rTotalLength = Math.max(1, (int) Math.round( rDist/ rStep));
		rDisti = new double [rTotalLength];
		
		rDisti[0]=0;
		for (int i=1; i < rDisti.length ; i++){
			rDisti[i] = rDisti[i-1] + rStep;
		}
		
		double rThetaiMax = 0;
		double rThetajMax = 0;
		double rTemp1 = 0;
		double rTemp2 = 0;
		int rthetaiIndex = 0;
		int rthetajIndex = 0;
		// rDist should vary from 0 to the rDist
		for (int i = 1; i < rDisti.length ; i++){// Start at index 1 to avoid division by zero
			// interfering antenna horizon elevation angle, Theta_t
			rTemp1 = ((rhi - rhts)/rDisti[i]) - (1000*rDisti[i])/(2*rAe);
			
			// Initialise the rThetaiMax value as we do not know whether it is > or < than zero
			if (i == 1){ // initialise the rNu50 to the first value calculated, i.e. one can not be sure that the lowest value is 0
				rThetaiMax = rTemp1;
			}
			// Store the max value of rTemp1
			if (rTemp1 > rThetaiMax ){
				rThetaiMax  = rTemp1;
				rthetaiIndex = i;
			}
			// interfered-with antenna horizon elevation angle, Theta_t
			rTemp2 = ((rhj - rhrs)/(rDist -rDisti[i]) ) - ((1000 * (rDist - rDisti[i]))/(2 * rAe));

			// Initialise the rThetajMax value as we do not know whether it is > or < than zero
			if (i == 1){ // initialise the rNu50 to the first value calculated, i.e. one can not be sure that the lowest value is 0
				rThetajMax = rTemp2;
			}
			// Store the max value of rTemp2
			if (rTemp2 > rThetajMax ){
				rThetajMax  = rTemp2;
				rthetajIndex = i;
			}

		}
		double rThetat = 0;
		double rThetar = 0;

		double rThetatd = ((rhrs - rhts)/rDist ) - ((1000 * rDist)/(2 * rAe));
		double rThetard = ((rhts - rhrs)/rDist ) - ((1000 * rDist)/(2 * rAe));

		double rDistMedianDiffractionLoss = calculateDistMedianDiffractionLoss(rDist, rDisti, rhrs, rhts, rFreq / 1000, rhi, rAe);

		// A path is trans-horizon if the physical horizon elevation angle as seen by
		// the interfering antenna (relative to the local horizontal) is greater than 
		// the angle (again relative to the interferers local horizontal) subtended by the interfered with antenna.
		if ( rThetaiMax> rThetatd){
			//trans-horizon
			// For a trans-horizon path, d_lt and d_lr are the distance from the transmit 
			// and receive antennas to their respective horizons (km) (Table 3 of ITU-R P.452-14)
			rThetat = rThetaiMax;
			rThetar = rThetajMax;

			rDistt = rDisti[rthetaiIndex];
			rDistr = rDist - rDisti[rthetajIndex];
		}else{
			//line of sight
			//  For a LoS path, each d_lt and d_lr is set to the distance from the terminal to the profile 
			// point identified as the principal edge in the diffraction method for 50% time (Table 3 of ITU-R P.452-14 )

			rThetat = rThetatd;
			rThetar = rThetard;

			rDistt = rDistMedianDiffractionLoss;
			rDistr = rDist - rDistMedianDiffractionLoss;
		}

		// Corrections of median loss and standard deviation due to local
		// environment*/
		double rLbfsg = 92.5 + 20.0 * Math.log10(rFreq / 1000) + 20.0 * Math.log10(rDist) + attenuationByAtmosphericGasesP676ver8(rFreq / 1000, rRho2, rPressure, rTemperature); 
		double rLbs = troposhericScatter(rFreq / 1000, rDist, rhts, rhrs, rP, rGRx, rGTx, rNo,rThetat, rThetar, rAe, rRho1, rPressure, rTemperature);
		double rLba = ductingLayerReflection(rFreq / 1000, rDistt, rDistr, rP, rThetat, rThetar, rhts, rhrs, rDist, rAe, rRho2, rPressure, rTemperature);
		double rLbd = diffractionLossP452(rFreq / 1000, rDist, rP, rDistt, rDistr, rLbfsg, rDisti, rhrs, rhts, rhi, rAe, rABeta, rBeta0);

		rAht = getAdditionalClutterLossesTransmitter();
		rAhr = getAdditionalClutterLossesReceiver();
		
		if (input.diffraction() && !input.troposphericScatter() && !input.layerReflection()){
		// only consider the diffraction
			rL = rLbd + rAht + rAhr;
		}else if (input.diffraction() && input.troposphericScatter() && !input.layerReflection()){
			//consider diffraction and troposcatter only
			rL = Math.min(rLbd, rLbs) + rAht + rAhr;
		}else if (input.diffraction() && !input.troposphericScatter() && input.layerReflection()){
			//consider diffraction and ducting only
			rL = Math.min(rLbd, rLba) + rAht + rAhr;
		}else if (!input.diffraction() && input.troposphericScatter() && !input.layerReflection()){
			//only consider troposcatter
			rL = rLbs + rAht + rAhr;
		}else if (!input.diffraction() && input.troposphericScatter() && input.layerReflection()){
			//consider troposcatter and ducting only
			rL = Math.min(rLbs, rLba) + rAht + rAhr;
		}else if (!input.diffraction() && !input.troposphericScatter() && input.layerReflection()){
			//consider ducting only
			rL = rLba + rAht + rAhr;
		}else{
			rL = calculateOverallPrediction(rLbfsg, rLbd, rLba, rLbs, rAht, rAhr, rBeta0, rDisti, rDist, rDistt, rDistr, 
					rP, rAe, rThetat, rThetar, rW, rhrs, rhts, rFreq / 1000, rhi, rLB0p, rLdp, rLb0Beta, rLbd50);
		}

		// calculation of the standard deviation
		if (variations) {
			rL += Factory.distributionFactory().getGaussianDistribution(0, rStdDev).trial();
		}
		
		if (Double.isInfinite(rL)) {
			rL = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm 
		}
		return rL;
	}

	public double angularDistance(double rDist, double rAe, double rThetat, double rThetar){
		double rTheta = 0; 

		rTheta = ((1000 * rDist)/rAe) + rThetat + rThetar;

		return rTheta;
	}

	/**
	 * <p> time percentage and angular-distance dependent losses within the 
	 * anomalous propagation mechanism </p>
	 * 
	 * <p> Eq. 40 of ITU-R P.452</p>
	 * 
	 * @return Ad(p) = gamma_d * thetaPrime + A(p)
	 */
	public double angularDistanceDependentLoss(double rFreq, double rDist, double rDistt, double rDistr, double rTimePercentage, double rThetat, double rThetar, double rAe, double rHTx, double rHRx){
		double rAd = 0;
		double rGammaD = 0;
		double rThetaPrime = 0;	
		double rThetatPrime = 0;
		double rThetarPrime = 0;
		double rAp = 0;
		double rEpsilon = 3.5;
		double rTau = 0;
		double rAlpha = 0;
		double rNu2 = 0;
		double rNu3 = 0;	
		double rNu1 = 0;
		double rNu4 = 0;
		double rBeta = 0;
		double rGammaCapital = 0;
		
		double rDI = Math.min(rDist - rDistt - rDistr, 40);
		if(rHm <= 10){
			rNu3 = 1;
		}else{
			rNu3 = Math.exp(-4.6 * Math.pow(10, -5) * (rHm - 10) * (43 + 6 *rDI));
		}
		/*
		 * Beta0 (%), the time percentage for which refractive index lapse-rates exceeding 
		 * 100 N units/km can be expected in the first 100 m of the lower atmosphere, 
		 * is used to estimate the relative incidence of fully developed anomalous propagation 
		 * at the latitude under consideration. The value of Beta0 to be used is that appropriate 
		 * to the path centre latitude.
		 * 
		 * We assume rPhi = path centre latitude (degrees) to less or equal to 70 degrees.
		 * 
		 * */

		rTau = 1 - Math.exp(-(4.12 * Math.pow(10, -4) * Math.pow(rDist, 2.41))) ;// eq. 3a

		double rValue1 = Math.pow(10, (-rDist/(16 - 6.6 * rTau)));
		double rValue3 = Math.pow(10, -(0.496 + 0.354 * rTau));
		double rValue2 = Math.pow(rValue3, 5);
		rNu1 = Math.pow(rValue1 + rValue2, 0.2);
		
		rLat = getLatitude();
		if (Math.abs(rLat) <= 70){
			rNu4 = Math.pow(10, (-0.935 + 0.0176 * Math.abs(rLat))* Math.log10(rNu1));
		}else{
			rNu4 = Math.pow(10, 0.3 * Math.log10(rNu1));
		}

		if (Math.abs(rLat) <= 70){
			rBeta0 = Math.pow(10, (-0.015 * Math.abs(rLat))+1.67) * rNu1 * rNu4;
		}else{
			rBeta0 = 4.17 * rNu1 * rNu4;
		}

		//rGammaD: specific attenuation (eq. 41)
		rGammaD = 5 * Math.pow(10, -5) * rAe * Math.pow(rFreq, 1.0/3.0);

		// angular distance (eq. 42)
		if (rThetat <= 0.1 * rDistt){
			rThetatPrime = rThetat;
		}else if (rThetat > 0.1 * rDistt){
			rThetatPrime = 0.1 * rDistt;
		}
		if(rThetar <= 0.1 * rDistr){
			rThetarPrime = rThetar;
		}else if (rThetar > 0.1 * rDistr){
			rThetarPrime = 0.1 * rDistr;
		}
		rThetaPrime = (1000 * rDist / rAe) + rThetatPrime + rThetarPrime;

		//time percentage variability (cumulative distribution) (eq. 43)

		rAlpha = -0.6 - (rEpsilon * Math.pow(10, -9) * Math.pow(rDist, 3.1) * rTau);
		rAlpha = Math.max(rAlpha, -3.4);

		rNu2 = Math.pow((500.0/rAe)*((rDist*rDist)/Math.pow(Math.sqrt(rHTx) + Math.sqrt(rHTx), 2.0)), rAlpha);
		rNu2 = Math.min(rNu2, 1);


		rBeta = rBeta0 * rNu2 * rNu3; // eq.44

		rGammaCapital = (1.076/Math.pow(2.0058 - Math.log10(rBeta), 1.012)) * Math.exp(-(9.51 - (4.8 * Math.log10(rBeta)) + (0.198 * Math.pow(Math.log10(rBeta), 2)))* Math.pow(10, -6) * Math.pow(rDist, 1.13));

		rAp = -12 + ((1.2 + (3.7 * Math.pow(10, -3) * rDist)) * Math.log10(rTimePercentage / rBeta)) + 12 * Math.pow(rTimePercentage / rBeta, rGammaCapital); 

		rAd = rGammaD * rThetaPrime + rAp;

		return rAd;
	}
	/**
	 * <p>total of fixed coupling losses (except for local clutter losses) between the 
	 * antennas and the anomalous propagation structure within the atmosphere </p>
	 * 
	 * <p> Eq. 37 of ITU-R P.452</p>
	 * 
	 * @param rFreq (in GHz)
	 * @return Lba = Af + Ad (p) + Ag
	 */
	public double totalFixedCoulingLosses(double rFreq, double rDistt, double rDistr, double rThetat, double rThetar){
		double rAf = 0;
		double rAlf = 0;
		double rAst = 0;
		double rAsr = 0;
		double rAct = 0;
		double rAcr = 0;
		double rThetatSecond = rThetat - (0.1 * rDistt);
		double rThetarSecond = rThetar - (0.1 * rDistr);

		if (rFreq < 0.5){
			rAlf = 45.375 - (137.0 * rFreq) + (92.5 * rFreq * rFreq);
		}else{
			rAlf = 0;
		}

		if (rThetatSecond > 0){
			rAst = 20 * Math.log10(1 + (0.361 * rThetatSecond * Math.pow(rFreq * rDistt, 0.5))) + (0.264 * rThetatSecond * Math.pow(rFreq, 1/3));
		}else if (rThetatSecond <= 0){
			rAst = 0;
		}
		if (rThetarSecond > 0){
			rAsr = 20 * Math.log10(1 + (0.361 * rThetarSecond * Math.pow(rFreq * rDistr, 0.5))) + (0.264 * rThetarSecond * Math.pow(rFreq, 1/3));
		}else if (rThetarSecond <= 0){
			rAsr = 0;
		}

		// sea land path are not considered
		rAct = 0;
		rAcr = 0;

		rAf = 102.45 + 20*Math.log10(rFreq) + 20 * Math.log10(rDistt + rDistr) + rAlf + rAst + rAsr + rAct + rAcr;
		return rAf;
	}
	public double ductingLayerReflection(double rFreq, double rDistt, double rDistr, double rTimePercentage, 
			double rThetat, double rThetar, double rHTx, double rHRx, double rDist, double rAe, double rRho, double rPressure, double rTemperature){
		double rDucting = 0;
		double rAf = 0;
		double rAd = 0;
		double rAg = 0;

		rAf = totalFixedCoulingLosses(rFreq, rDistt, rDistr, rThetat, rThetar);
		rAd = angularDistanceDependentLoss(rFreq, rDist, rDistt, rDistr, rTimePercentage, rThetat, rThetar, rAe, rHTx, rHRx);
		rAg = rDist * attenuationByAtmosphericGasesP676ver8(rFreq, rRho, rPressure, rTemperature);
		rDucting = rAf + rAd + rAg;
		return rDucting;
	}
	public double troposhericScatter(double rFreq, double rDist, double rHTx,
			double rHRx, double rTimePercentage, double rGRx, double rGTx, double rSeaLevelSurfaceRefractivity, double rThetat, 
			double rThetar, double rAe, double rRho, double rPressure, double rTemperature) {
		double rTroposphericScatterLoss = 0;
		double rLf = 0;
		double rLc = 0;
		double rNo = 0;
		double rAg = 0;
		double rTheta = 0;

		rLf = 25 * Math.log10(rFreq) - 2.5 * Math.pow(Math.log10(rFreq/2), 2);
		rLc = 0.051 * Math.exp(0.055 * (rGTx + rGRx));
		rNo = rSeaLevelSurfaceRefractivity;
		rAg = attenuationByAtmosphericGasesP676ver8(rFreq, rRho, rPressure, rTemperature);
		rTheta = angularDistance(rDist, rAe, rThetat, rThetar);
		rTroposphericScatterLoss = 190 + rLf + 20*Math.log10(rDist) + 0.573*rTheta - 0.15*rNo + rLc + rAg - 10.1* Math.pow(- Math.log10(rTimePercentage/50), 0.7);

		return rTroposphericScatterLoss;
	}
/**
 * Calculate the median Effective Earth radius
 * It uses the median effective Earth radius factor k50
 * 
 * Assuming a true Earth radius of 6 371 km, the median 
 * value of effective Earth radius ae can be determined 
 * from:
 * 
 * ae = 6371 * k50
 * 
 * @param rRefrIndexGradient
 * @return ae = 6371 * k50
 */
	public double medianEffectiveEarthRadiusCalculation(double rRefrIndexGradient){
		double rAe = 0;
		double rRe = EARTHRADIUS_IN_KM; // earth radius in Km
		double rKe50; // multiplicative factor for earth radius for time percentage = 50 %
		// Equivalent earth radius (dependent on time percentage) : reflects the
		// vairations in path loss

		rKe50 = 157.0 / (157.0 - rRefrIndexGradient);
		rAe = rKe50 * rRe;

		return rAe;
	}
	/**
	 * Calculate the effective Earth radius exceeded for Beta0% time, 
	 * abeta such as
	 * 
	 * abeta = 6371 * kbeta
	 * 
	 * where rKbeta = 3 is an estimate of the effective Earth radius factor exceeded for Beta0% time
	 * @return abeta = 6371 * kbeta
	 */
		public double betaPercentageEffectiveEarthRadiusCalculation(){
			double rAbeta = 0;
			double rRe = EARTHRADIUS_IN_KM; // earth radius in Km
			double rKbeta = 3.0; // estimate of the effective Earth radius factor exceeded for Beta0% time

			rAbeta = rKbeta * rRe;

			return rAbeta;
		}
	
	public double diffractionLossP452(double rFreq, double rDist, double rP, double rDistt, double rDistr, double rLbfsg,
			double[] rDisti, double rhrs, double rhts, double rhi, double rAe, double rABeta, double rBeta0) {
		double rLbd = 0;
		//median diffraction loss (section 4.2.1) -> Ld50
		rLd50 = calculateMedianDiffractionLoss(rDist, rDisti, rhrs, rhts, rFreq, rhi, rAe);
		
		//The diffraction loss not exceeded for Beta0% of the time -> LdBeta
		rLdBeta = calculateBetaPercentageDiffractionLoss(rDist, rDisti, rhrs, rhts, rFreq, rhi, rABeta);
		
		//The diffraction loss not exceeded for p% of the time -> Lbd
		
		double rEsp = 2.6 * (1 - Math.exp(-0.1 * (rDistt + rDistr))) * Math.log10(rP / 50);
		double rEsbeta = 2.6 * (1 - Math.exp(-0.1 * (rDistt + rDistr))) * Math.log10(rBeta0 / 50);
		//notional line-of-sight basic transmission loss not exceeded for p% time, given by (eq.11)
		rLB0p = rLbfsg + rEsp;
		// notional LoS basic transmission loss not exceeded for Beta% time, given by (eq.12)
		rLb0Beta = rLbfsg + rEsbeta;
		
		rLdp = rLd50 + Fi(rP,rBeta0) * (rLdBeta - rLd50); // (eq. 32)

		rLbd50 = rLbfsg + rLd50; //Median basic transmission loss associated with diffraction (eq. 33)

		rLbd = rLB0p + rLdp;
		
		return rLbd;
	}
	
	/**
	 * Median Diffraction loss as in p.9 of ITU-R P.452-14
	 * 
	 * 	 * @return Median Diffraction loss as in p.9 of ITU-R P.452-14
	 */
	public double calculateDistMedianDiffractionLoss(double rDist, double[] rDisti, double rhrs, double rhts, double rFreq, double rhi, double rAe){
		double rDist50per = 0;
		double zeta_m = 0;
		double rHi = 0;
		double rLambda = 0.3 / rFreq;//rFreq should be in GHz

		zeta_m = Math.cos(Math.atan(0.001 * ((rhrs - rhts)/rDist)));

		double rTemp = 0; 
		double rNu50 = 0;
		int rNu50Index = 0;
		for (int i = 0; i < rDisti.length ; i++){
			rHi = rhi + (1000*(rDisti[i]*(rDist-rDisti[i]))/(2*rAe)) -	(((rhts*(rDist - rDisti[i]))+(rhrs*rDisti[i]))/rDist);
			rTemp = zeta_m * rHi * Math.sqrt((2 * rDist) / (1000 * rLambda * rDisti[i] * (rDist -rDisti[i])));

			// Store the max value of rTemp
			if (i == 0){ // initialise the rNu50 to the first value calculated, i.e. one can not be sure that the lowest value is 0
				rNu50 = rTemp;
			}
			if (rTemp > rNu50){
				rNu50 = rTemp;
				rNu50Index = i;
			}
		}
		rDist50per = rDisti[rNu50Index];

		return rDist50per;
	}

	public double calculateBetaPercentageDiffractionLoss(double rDist, double[] rDisti, double rhrs, double rhts, double rFreq, double rhi, double rABeta){
		double rLdBeta = 0;
		double rHimBeta = 0;
		double rHitBeta = 0;
		double rHirBeta = 0;
		double rLambda = 0.3 / rFreq;//rFreq should be in GHz
		double rLmBeta = 0;
		double rLtBeta = 0;
		double rLrBeta = 0;
		double rhim50 = rhi;
		double rhit50 = rhi;
		double rhir50 = rhi;
		double rZetam = 0;
		double rZetat = 0;
		double rZetar = 0;
		double rNumBeta = 0;
		double rNutBeta = 0;
		double rNurBeta = 0;

		//Principal edge diffraction loss not exceeded for Beta0% time
		// Find the main (i.e. principal) edge diffraction parameter, rNumBeta (eq. 24) 
		rZetam = Math.cos(Math.atan(Math.pow(10, -3) * (rhrs - rhts) / rDist));
		rHimBeta = rhim50 + (1000*(rDistIm50*(rDist-rDistIm50))/(2*rABeta)) -	(((rhts*(rDist - rDistIm50))+(rhrs*rDistIm50))/rDist);
		rNumBeta = rZetam * rHimBeta * Math.sqrt((2 * rDist) / (1000 * rLambda * rDistIm50 * (rDist - rDistIm50)));

		//	Calculate the knife-edge diffraction loss for the main edge, LmBeta (eq. 25)
		if (rNumBeta >= -0.78){
			rLmBeta = Jfunction(rNumBeta);
		}else{
			rLmBeta = 0;
		}

		// Transmitter-side secondary edge diffraction loss not exceeded for Beta0% time
		if (rLt50 == 0){
			rLtBeta = 0;
		}else{
			rZetat = Math.cos(Math.atan(Math.pow(10, -3) * (rhim50 - rhts) / rDistIm50));
			rHitBeta = rhit50 + (1000*(rDistIt50*(rDistIm50-rDistIt50))/(2*rABeta)) -	(((rhts*(rDistIm50 - rDistIt50))+(rhim50*rDistIt50))/rDistIm50);
			rNutBeta = rZetat * rHitBeta * Math.sqrt((2 * rDistIm50) / (1000 * rLambda * rDistIt50 * (rDistIm50 -rDistIt50)));
		}
		//	Calculate the knife-edge diffraction loss for the transmitter-side secondary edge, LtBeta
		if (rNutBeta >= -0.78){
			rLtBeta = Jfunction(rNutBeta);
		}else{
			rLtBeta = 0;
		}

		// Receiver-side secondary edge diffraction loss not exceeded for Beta0%
		if (rLr50 == 0){
			rLrBeta = 0;
		}else{
			rZetar = Math.cos(Math.atan(Math.pow(10, -3) * (rhrs - rhim50) / (rDist - rDistIm50)));
			rHirBeta = rhir50 + (1000*((rDistIr50 - rDistIm50)*(rDist-rDistIr50))/(2*rABeta)) - (((rhim50*(rDist - rDistIr50))+(rhrs*(rDistIr50 - rDistIm50)))/(rDist - rDistIm50));
			rNurBeta = rZetar * rHirBeta * Math.sqrt((2 * (rDist - rDistIm50)) / (1000 * rLambda * (rDistIr50 - rDistIm50) * (rDist - rDistIr50)));
		}
		//	Calculate the knife-edge diffraction loss for the receiver-side secondary edge, LrBeta
		if (rNurBeta >= -0.78){
			rLrBeta = Jfunction(rNurBeta);
		}else{
			rLrBeta = 0;
		}
		
		// Combination of the edge losses not exceeded for Beta0% time
		if (rNumBeta >= -0.78){
			rLdBeta = rLmBeta + (1 - Math.exp(-rLmBeta/6.0)) * (rLtBeta + rLrBeta + 10 + 0.04 *rDist);
		}else{
			rLdBeta = 0;
		}
		
		return rLdBeta;
	}
	
	/**
	 * The median diffraction loss, Ld50 (dB), is calculated using 
	 * the median value of the effective Earth radius, ae, 
	 * given by equation (6a).
	 */
	public double calculateMedianDiffractionLoss(double rDist, double[] rDisti, double rhrs, double rhts, double rFreq, double rhi, double rAe){
		double rHi = 0;
		double rLambda = 0.3 / rFreq;//rFreq should be in GHz
		
		//Median diffraction loss for the principal edge
		double rTempm = 0; 
		double rNum50 = 0;
		int rNu50mIndex = 0;
		double rZetam = 0;
		// Calculate a correction, rZetam, for overall path slope (eq.14)
		rZetam = Math.cos(Math.atan(Math.pow(10, -3) * (rhrs - rhts) / rDist));

		//Find the main (i.e. principal) edge, and calculate its diffraction parameter, rNum50 (eq. 15)
		for (int i = 0; i < rDisti.length ; i++){
			// vertical clearance
			rHi = rhi + (1000*(rDisti[i]*(rDist-rDisti[i]))/(2*rAe)) -	(((rhts*(rDist - rDisti[i]))+(rhrs*rDisti[i]))/rDist);
			rTempm = rZetam * rHi * Math.sqrt((2 * rDist) / (1000 * rLambda * rDisti[i] * (rDist -rDisti[i])));

			// Store the max value of rTemp
			if (i == 0){ // initialise the rNu50 to the first value calculated, i.e. one can not be sure that the lowest value is 0
				rNum50 = rTempm;
			}
			if (rTempm > rNum50){
				rNum50 = rTempm;
				rNu50mIndex = i;
			}
		}
		int rIndexm50 =  rNu50mIndex;
		
		double rLm50 = 0;
		//Calculate the median knife-edge diffraction loss for the main edge, Lm50 (eq. 16)
		if (rNum50 >= -0.78){
			rLm50 = Jfunction(rNum50);
		}else{
			rLm50 = 0;
		}
		
		double rhim50 = rhi;
		//rDistIm50 = calculateDistMedianDiffractionLoss(rDist, rDisti, rhrs, rhts, rFreq, rhim50, rAe);
		rDistIm50 = rDisti[rIndexm50];
		
		if(rLm50 == 0.0){
		/*	If Lm50 = 0, the median diffraction loss, Ld50, and the diffraction loss not 
		 * exceeded for rBeta0 % time, LdBeta, are both zero and no further diffraction 
		 * calculations are necessary.
		 * Otherwise possible additional losses due to secondary edges on the transmitter 
		 * and receiver sides of the principal edge should be investigated, as follows.*/
			rLd50 = 0;
			rLdBeta = 0;
		}else{
			//Median diffraction loss for transmitter-side secondary edge: rLt50
			if (rIndexm50 == 1){
				/* there is no transmitter-side secondary edge, and the associated 
				diffraction loss, Lt50, should be set to zero */
				rLt50 = 0;
			}else{
				double rTempt = 0; 
				double rNut50 = 0;
				int rNu50tIndex = 0;
				double rZetat = 0;
				// Calculate a correction, rZetat, for the slope of the path from the transmitter to the principal edge (eq. 17)
				rZetat = Math.cos(Math.atan(Math.pow(10, -3) * (rhim50 - rhts) / rDistIm50));
				
				//Find the transmitter-side secondary edge and calculate its diffraction parameter, rNut50 , 
				for (int i = 0; i < rIndexm50 ; i++){
					rHi = rhi + (1000*(rDisti[i]*(rDistIm50-rDisti[i]))/(2*rAe)) -	(((rhts*(rDistIm50 - rDisti[i]))+(rhim50*rDisti[i]))/rDistIm50);
					rTempt = rZetat * rHi * Math.sqrt((2 * rDistIm50) / (1000 * rLambda * rDisti[i] * (rDistIm50 -rDisti[i])));

					// Store the max value of rTemp
					if (i == 0){ // initialise the rNu50 to the first value calculated, i.e. one can not be sure that the lowest value is 0
						rNut50 = rTempt;
					}
					if (rTempt > rNut50){
						rNut50 = rTempt;
						rNu50tIndex = i;
					}
				}
				rDistIt50 = rDisti[rNu50tIndex];
				
				/* Set it50 to the index of the profile point for the transmitter-side secondary 
				 * edge (i.e. the index of the terrain height array element corresponding to the 
				 * value rNut50).
				 */
				int rIndext50 =  rNu50tIndex;
				
				//Calculate the median knife-edge diffraction loss for the transmitter-side secondary edge, Lt50 (eq. 19)
				if (rNut50 >= -0.78 && rIndexm50 >= 2){
					rLt50 = Jfunction(rNut50);
				}else{
					rLt50 = 0;
				}
			}
			
			//Median diffraction loss for the receiver-side secondary edge
			if (rIndexm50 == rDisti.length){ 
				/* there is no receiver-side secondary edge, and the associated diffraction loss, Lr50, 
				 * should be set to zero
				 */
				rLr50 = 0;
			}else{
				double rTempr = 0; 
				double rNur50 = 0;
				int rNu50rIndex = 0;
				double rZetar = 0;
				// Calculate a correction, rZetat, for the slope of the path from the transmitter to the principal edge (eq. 17)
				rZetar = Math.cos(Math.atan(Math.pow(10, -3) * (rhrs - rhim50) / (rDist - rDistIm50)));
				
				//Find the transmitter-side secondary edge and calculate its diffraction parameter, rNut50 , 
				for (int i = rIndexm50 + 1 ; i < rDisti.length ; i++){
					rHi = rhi + (1000*((rDisti[i] - rDistIm50)*(rDist-rDisti[i]))/(2*rAe)) - (((rhim50*(rDist - rDisti[i]))+(rhrs*(rDisti[i] - rDistIm50)))/(rDist - rDistIm50));
					rTempr = rZetar * rHi * Math.sqrt((2 * (rDist - rDistIm50)) / (1000 * rLambda * (rDisti[i] - rDistIm50) * (rDist - rDisti[i])));

					// Store the max value of rTemp
					if (i == rIndexm50 + 1){ // initialise the rNu50 to the first value calculated, i.e. one can not be sure that the lowest value is 0
						rNur50 = rTempr;
					}
					if (rTempr > rNur50){
						rNur50 = rTempr;
						rNu50rIndex = i;
					}
				}
				rDistIr50 = rDisti[rNu50rIndex];
					
				/* Set it50 to the index of the profile point for the receiver-side secondary 
				 * edge (i.e. the index of the terrain height array element corresponding to the 
				 * value rNur50).
				 */					
				int rIndexr50 =  rNu50rIndex;

				//Calculate the median knife-edge diffraction loss for the receiver-side secondary edge, Lt50 (eq. 2)
				if (rNur50 >= -0.78 && rIndexm50 < rDisti.length){
					rLr50 = Jfunction(rNur50);
				}else{
					rLr50 = 0;
				}
			}
		}
		// Combination of the edge losses for median Earth curvature
		// Calculate the median diffraction loss, rLd50 (eq.23)
		
		if (rNum50 > -0.78){
			rLd50 = rLm50 + (1 - Math.exp(-rLm50/6.0)) * (rLt50 + rLr50 + 10 + 0.04 *rDist);
		}else{
			rLd50 = 0;
		}
		
		return rLd50;
	}
	
	public double calculateOverallPrediction(double rLbfsg, double rLbd, double rLba, double rLbs, 
			double rAht, double rAhr, double rBeta0,
			double[] rDisti, double rDist, double rDistt, double rDistr, 
			double rP, double rAe, double rThetat, double rThetar, 
			double rW, double rhrs, double rhts, double rFreq, double rhi,
			double rLB0p, double rLdp, double rLb0Beta, double rLbd50){
		double rL = 0;
		// Calculate an interpolation factor, Fj, to take account of the path angular distance
		double rZeta = 0.8;
		double rThetaCapital = 0.3;
		double rTheta = angularDistance(rDist, rAe, rThetat, rThetar);
		double rFactorj = rZeta * (rTheta - rThetaCapital) / rThetaCapital;
		double rFj = 1.0 -0.5 * (1.0 + Math.tanh(3.0 * rFactorj));

		//Calculate an interpolation factor, Fk, to take account of the great circle path distance
		double rDsw = 20; // fixed parameter determining the distance range of the associated blending
		double rKappa = 0.5;// fixed parameter determining the blending slope at the ends of the range
		double rFactork = rKappa * (rDist -rDsw) / rDsw;
		double rFk = 1.0 -0.5 * (1.0 + Math.tanh(3.0* rFactork));

		/* Calculate a notional minimum basic transmission loss,rLminB0p (dB), associated with LoS 
		 * propagation and over-sea sub-path diffraction
		 */
		double rLminB0p = 0;
		if(rP < rBeta0){
			rLminB0p = rLB0p + (1 - rW) * rLdp;
		}else{
			rLminB0p = rLbd50 + Fi(rP,rBeta0) * (rLb0Beta + (1 - rW) * rLdp - rLbd50);
		}
		
		/* Calculate a notional minimum basic transmission loss, rLminbap (dB), associated with LoS 
		 * and transhorizon signal enhancements
		 */
		double rLminbap = 0;
		double rEta = 2.5;
		rLminbap = rEta * Math.log(Math.exp(rLba/rEta) + Math.exp(rLB0p/rEta));
		
		
		/* Calculate a notional basic transmission loss, rLbda (dB), associated with 
		 * diffraction and LoS or ducting/layer-reflection enhancements:
		 */
		double rLbda = 0;
		if (rLminbap > rLbd){
			rLbda = rLbd;
		}else{
			rLbda = rLminbap + rFk * (rLbd - rLminbap);
		}
		
		/* Calculate a modified basic transmission loss, rLbam (dB), which takes diffraction 
		 * and line-of-sight or ducting/layer-reflection enhancements into account:
		 */
		double rLbam = 0;
		rLbam = rLbda + rFj * (rLminB0p - rLbda);
		
		/* Calculate the final basic transmission loss not exceed for p% time, Lb (dB) (eq. 54)
		 */
		rL = -5.0 * Math.log10(Math.pow(10, -0.2 * rLbs) + Math.pow(10, -0.2 * rLbam)) + rAht + rAhr;
 
		return rL;
	}
	
	public double Jfunction(double rInput){
		double rValue = 0;
		
		rValue = 6.9 + 20 * Math.log10(Math.sqrt(Math.pow(rInput - 0.1, 2) + 1) + rInput - 0.1); 
		
		return rValue;
	}
	
	public double Fi(double rP, double rBeta0){
		double rValue = 0;
		if(rP == 50){
			rValue = 0;
		}else if (50 > rP && rP > rBeta0){
			rValue = Ifunction(rP/100) / Ifunction(rBeta0/100);
		}else if (rBeta0 >= rP ){
			rValue = 1;
		}
		return rValue;
	}

	public double Ifunction(double rInput){
		double rXi = 0;

		double rC0 = 2.515516698;
		double rC1 = 0.802853;
		double rC2 = 0.010328;
		double rD1 = 1.432788;
		double rD2 = 0.189269;
		double rD3 = 0.001308;

		double rTau = Math.sqrt(-2 * Math.log(rInput));
		rXi = (((rC1 + (rTau * rC2)) * rTau) + rC0) /((rD1 + rTau * (rD2 + (rD3 * rTau))) * rTau + 1);
		return rXi - rTau;
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
	/**
	 * function g(f,fi) equivalent to the equation 23d of ITU-R P.676-8
	 * 
	 * @param rFreq
	 * @param rFreqi
	 * 
	 * @return g(f,fi)
	 */
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
	
	public double getRefrIndexGradient() {
		return refrIndexGradient;
	}

	public double getLatitude() {
		return rLat;
	}

	public double getAdditionalClutterLossesTransmitter() {
		return rAht;
	}
	
	public double getAdditionalClutterLossesReceiver() {
		return rAhr;
	}
	
	public double getAntennaGainReceiverForTroposphericModel() {
		return rGRx;
	}

	public double getAntennaGainTransmitterForTroposphericModel() {
		return rGTx;
	}
	
	public double getSeaLevelSurfaceRefractivity() {
		return rNo;
	}

    @Override
    public Description description() {
        return new DescriptionImpl("ITU-R P.452-14","<html><body><b><u>Frequency range:</u></b><br>about 0.7 GHz to 50 GHz<br><b><u>Distance range:</u></b><br>up to a distance limit of 10 000 km<br><b><u>Typical application area:</u></b><br>Prediction method for the evaluation <br> of interference between stations on <br>the surface of the Earth at frequencies <br>above about 0.1 GHz, accounting for both <br> clear-air and hydrometeor scattering <br>interference mechanisms.</body></html>");
    }
}
