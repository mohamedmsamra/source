package org.seamcat.model.propagation;

import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.HataInput;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

/**
 * Extended Hata - SRD
 * <p></p>
 * Frequency range: 30 MHz - 3 GHz<br>
 * Distance range: up to 300 m<br>
 * Typical application area: Short range links under direct-LOS assumption,<br>
 * Important: antenna heights up to 3 m.<br>
 * Information: Note that the Hata model assumes that the specified antenna heights of transmitter and receiver are <br>
 *     heights above ground.
 *<p></p>
 * This model is a modified version of the Extended Hata model which was developed in CEPT for studies of Short Range <br>
 *     Devices (SRD). The basis for modification is an assumption, that although SRD devices are usually operated at <br>
 *         low antenna heights (typically person-carried devices, i.e. with antenna height of about 1.5 m), but the interference<br>
 * would usually occur at relatively short distances (up to 100 m or so) when direct- or near-LOS might be assumed.
 *<p></p>
 *  The expression of b(Hb) parameter in the standard Hata model, giving large extra losses for transmitter antenna <br>
 *      heights below 30 m, was considered to be unnecessarily severe. Therefore the only difference  between Hata-SRD <br>
 *          and Hata model lies in the new expression for the antenna  gain factor b(Hb), which for Hata model is expressed as
 *<p></p>
 *  b = min(0, 20 log(Hb/30));
 *  <p></p>
 *  to be replaced in Hata-SRD model by :
 *  <p></p>
 *  b = ( 1.1 log( f ) - 0.7 ) * min(10, Hb )  - ( 1.56 log(f ) - 0.8 ) + max( 0, 20 log( Hb / 10 ) );
 *  <p></p>
 *  Note: This expression assumes that antenna heights should not exceed 1.5-3 m.
 *
 */
public class HataSE24PropagationModel implements PropagationModelPlugin<HataInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, HataInput input, Validator<HataInput> validator) {
        Distribution frequency = HataSE21PropagationModel.findFrequency(scenario, path);
        if ( frequency != null ) {
            Bounds bounds = frequency.getBounds();
            if (bounds.getMin() < 30
				 ||bounds.getMax() > 3000) {
                validator.error("Frequencies below 30 MHz or above 3000 MHz are not supported by the Extended Hata propagation SRD model" +
				PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
        }
		if (path.size() > 0){
			if (PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,path) > 0.3){
				String errorMessage = "Distances above 300 m are not supported by the Extended Hata propagation SRD model";
				if (!PluginCheckUtilsToBeRemoved.getDeltaPosition().isEmpty()) errorMessage += PluginCheckUtilsToBeRemoved.getDeltaPosition();
				if (!PluginCheckUtilsToBeRemoved.getRelativePosition().isEmpty()) errorMessage += PluginCheckUtilsToBeRemoved.getRelativePosition();
				validator.error(errorMessage + PluginCheckUtilsToBeRemoved.getExceptionHint());
			}

			if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(path) > 3 || PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) > 3){
				validator.error(PluginCheckUtilsToBeRemoved.getManualReferene("e.g. antenna heights above 3 m"));
			}


		}
    }

    private double calculateMedianLoss(double frequency, double distance,
	      double heightOfTransmitter, double heightOfReceiver) {
		double medianLoss = 0; // Median loss
		double freeSpaceAttenuation; // Free space attenuation
		double rHm;
		double rHb;
		double rA;
		double rB;
		double rAlpha;
		double rCorr = 0;

		// constants
		final double firstLimitDistance = 0.04;
		final double secondLimitDistance = 0.1;

		rHm = Math.min(heightOfTransmitter, heightOfReceiver);
		if (rHm < 1){
			rHm = 1;
		}
		rHb = Math.max(heightOfTransmitter, heightOfReceiver);

		freeSpaceAttenuation = 32.4 + 20 * Math.log10(frequency) + 10
		      * Math.log10(distance * distance + (rHb - rHm) * (rHb - rHm) / 1e6);

		rA = (1.1 * Math.log10(frequency) - 0.7) * Math.min(10, rHm)
		      - (1.56 * Math.log10(frequency) - 0.8)
		      + Math.max(0, 20 * Math.log10(rHm / 10));

		rB = (1.1 * Math.log10(frequency) - 0.7) * Math.min(10, rHb)
		      - (1.56 * Math.log10(frequency) - 0.8)
		      + Math.max(0, 20 * Math.log10(rHb / 10));

		if (distance <= 20) {
			rAlpha = 1.0;
			rCorr = Math.log10(distance);
		} else {
			rAlpha = 1.0 + (0.14 + 1.87e-4 * frequency + 1.07e-3 * rHb)
			      * Math.pow(Math.log10(distance / 20.0), 0.8);
			rCorr = Math.pow(Math.log10(distance), rAlpha);
		}

		if (distance <= firstLimitDistance) {
			medianLoss = freeSpaceAttenuation;
		} else if (distance >= secondLimitDistance) {
			if (frequency > 30 && frequency <= 150.0) {
				medianLoss = 69.6 + 26.2 * Math.log10(150.0) - 20
				      * Math.log10(150.0 / frequency) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			} else if (frequency <= 1500.0) {
				medianLoss = 69.6 + 26.2 * Math.log10(frequency) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			} else if (frequency <= 2000.0) {
				medianLoss = 46.3 + 33.9 * Math.log10(frequency) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			} else if (frequency <= 3000.0) {
				medianLoss = 46.3 + 33.9 * Math.log10(2000) + 10
				      * Math.log10(frequency / 2000) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			}
		} else {
			// 40 < rDist < 100 (limits excluded !!!!)
			double lossForFirstLimitDistance;
			double lossForSecondLimitDistance;

			// calculation of median path loss L for d = 40km and d = 100km
			// CAUTION : risk of endless recursivity if tests
			// (rDist <= 40.0) and (rDist >= 100.0) do not include values 40 and
			// 100

			lossForFirstLimitDistance = calculateMedianLoss(frequency,firstLimitDistance, heightOfTransmitter, heightOfReceiver);
			lossForFirstLimitDistance = Helper.generalEnvCorrections(lossForFirstLimitDistance, frequency,firstLimitDistance, input.generalEnvironment());

			lossForSecondLimitDistance = calculateMedianLoss(frequency,secondLimitDistance, heightOfTransmitter, heightOfReceiver);
			lossForSecondLimitDistance = Helper.generalEnvCorrections(lossForSecondLimitDistance, frequency,secondLimitDistance, input.generalEnvironment());

			medianLoss = lossForFirstLimitDistance
			      + (Math.log10(distance) - Math.log10(firstLimitDistance))
			      / (Math.log10(firstLimitDistance) - Math
			            .log10(secondLimitDistance))
			      * (lossForFirstLimitDistance - lossForSecondLimitDistance);
		}
		return medianLoss;
	}

    private HataInput input;

    @Override
	public double evaluate(LinkResult linkResult, boolean variations, HataInput input) {
        double rFreq = linkResult.getFrequency();
        double rDist = linkResult.getTxRxDistance();
        double rHTx = linkResult.txAntenna().getHeight();
        double rHRx = linkResult.rxAntenna().getHeight();
        this.input = input;

        double medianLoss = 0.0; // Median loss (deterMath.ministic component);
		double standardDeviation = 0.0; // Standard deviation for variations in path
												// loss (static component)
		double freeSpaceAttenuation; // Free space attenuation
		double temp = 0;

		if (rFreq < 30 || rFreq > 3000 ){
			throw new RuntimeException("Frequencies below 30 MHz or above 3000 MHz are not supported by the Extended Hata propagation SRD model");
		}
		if (rDist > 0.3){
			throw new RuntimeException("Distances above 300 m are not supported by the Extended Hata propagation SRD model");
		}

		freeSpaceAttenuation = 32.4 + 20 * Math.log10(rFreq) + 10
		      * Math.log10(rDist * rDist + (rHTx - rHRx) * (rHTx - rHRx) / 1e6);

		if (Double.isInfinite(freeSpaceAttenuation)) {
			freeSpaceAttenuation = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
		}
		medianLoss = calculateMedianLoss(rFreq, rDist, rHTx, rHRx);
		standardDeviation = Helper.variationsStdDev(rDist, input.propagationEnvironment());
		medianLoss = Helper.generalEnvCorrections(medianLoss, rFreq, rDist, input.generalEnvironment());

		LocalEnvCorrections lec = Helper.localEnvCorrections(new LocalEnvCorrections(medianLoss, standardDeviation),
                linkResult, input.floorHeight(), input.sizeOfRoom(), input.wallLossInIn(), input.adjacentFloorLoss(), input.empiricalParameters(), input.wallLossStdDev());
		medianLoss = lec.rMedianLoss;
		standardDeviation = lec.rStdDev;

		if (variations) {
			medianLoss += Factory.distributionFactory().getGaussianDistribution(0, standardDeviation).trial();
			temp =  Factory.distributionFactory().getGaussianDistribution(0, 3.5).trial();
		}

		freeSpaceAttenuation += temp;
		if(standardDeviation > 3.5){ // this 3.5 dB condition is to consider the symetrie of the first 40 m of the path
			if (medianLoss < freeSpaceAttenuation) {
				medianLoss = freeSpaceAttenuation;
			}
		}

		if (Double.isInfinite(medianLoss)) {
			medianLoss = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
		}
		return medianLoss;
	}

    @Override
    public Description description() {
        return new DescriptionImpl("Extended Hata - SRD","<html><body><b><u>Frequency range:</u></b><br>30 MHz - 3 GHz<br><b><u>Distance range:</u></b><br>up to 300 m<br><b><u>Typical application area:</u></b><br>Short range links under direct-LOS <br> assumption,<br> <b>important: antenna heights up to 3 m </b>. <br><b><u>Information:</u></b><br>Note that the Hata model assumes that the <br>specified antenna heights of transmitter <br>and receiver are heights above ground.</body></html>");
	}
}
