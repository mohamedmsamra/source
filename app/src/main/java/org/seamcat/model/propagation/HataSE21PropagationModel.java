package org.seamcat.model.propagation;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.HataInput;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Extended Hata propagation model
 *
 * Frequency range: 30 MHz - 3 GHz
 * Distance range: up to 40 km
 * Typical application area: Mobile services and other services working in non-LOS/cluttered environment.
 * Note that in theory, the model can go up to 100 km since the curvature of the earth is included,
 * but in practice it is recommended to use it up to 40 km.
 *
 * Information: the Hata model assumes that the specified antenna heights of transmitter
 * and receiver are heights above ground.
 *
 */
public class HataSE21PropagationModel implements PropagationModelPlugin<HataInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, HataInput input, Validator<HataInput> validator) {
		List<String> info = new ArrayList<>();
		Distribution frequency = findFrequency(scenario, path);
        if ( frequency != null ) {
            Bounds bounds = frequency.getBounds();
            if (bounds.getMin() < 30) {
                validator.error("Hata model not applicable below 30 MHz" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
            if (bounds.getMax() > 3000) {
                validator.error("Hata model not applicable above 3 GHz"+ PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
        }

		if (PluginCheckUtilsToBeRemoved.getMaxDistance(scenario, path) > 100) {
			String msg = "Extended Hata model not applicable for distances larger than 100 km" +
					PluginCheckUtilsToBeRemoved.getExceptionHint() + PluginCheckUtilsToBeRemoved.getRelativePosition();
			if (!PluginCheckUtilsToBeRemoved.getDeltaPosition().isEmpty()) msg += PluginCheckUtilsToBeRemoved.getDeltaPosition();
			validator.error(msg);
		}
		if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(path) > 200 || PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) > 200){
			info.add( "antenna heights > 200 m");
		}

		  if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(path) < 30 && PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) < 30){
			  info.add("both antenna heights below 30 m ");
        }

		if (info.size() > 0)validator.error(PluginCheckUtilsToBeRemoved.getManualReferene("e.g. the antenna heights"));

    }

    public static Distribution findFrequency(Scenario scenario, List<Object> path) {
        if ( path.size() == 0 ) return null;

        Object obj = path.get(0);
        if ( obj instanceof RadioSystem ) {
            RadioSystem system = (RadioSystem) obj;
            if ( system.getTransmitter() instanceof GenericTransmitter && ((GenericTransmitter)system.getTransmitter()).isInterfererCognitiveRadio() ) {
                return scenario.getVictimSystem().getFrequency();
            } else {
                return system.getFrequency();
            }
        } else if ( obj instanceof InterferenceLink ) {
            return ((InterferenceLink) obj).getInterferingSystem().getFrequency();
        }

        return null;
    }

    protected double calculateMedianLoss(double rFreq, double rDist, double rHTx, double rHRx) {
		double rL = 0; // Median loss
		double rL0; // Free space attenuation
		double rHm;
		double rHb;
		double rA;
		double rB;
		double rAlpha;
		double rCorr = 0;

		final double rD1 = 0.04;
		final double rD2 = 0.1;

		rHm = Math.min(rHTx, rHRx);
		if (rHm < 1){
			rHm = 1;
		}
		rHb = Math.max(rHTx, rHRx);

		rL0 = 32.4 + 20 * Math.log10(rFreq) + 10
		      * Math.log10(rDist * rDist + (rHb - rHm) * (rHb - rHm) / 1e6);

		rA = (1.1 * Math.log10(rFreq) - 0.7) * Math.min(10, rHm)
				- (1.56 * Math.log10(rFreq) - 0.8)
		      + Math.max(0, 20 * Math.log10(rHm / 10));
		rB = Math.min(0, 20 * Math.log10(rHb / 30));

		if (rDist <= 20) {
			rAlpha = 1.0;
			rCorr = Math.log10(rDist);
		} else {
			rAlpha = 1.0 + (0.14 + 1.87e-4 * rFreq + 1.07e-3 * rHb)
			      * Math.pow(Math.log10(rDist / 20.0), 0.8);
			rCorr = Math.pow(Math.log10(rDist), rAlpha);
		}

		if (rDist <= rD1) {
			rL = rL0;
		} else if (rDist >= rD2) {
			if (rFreq > 30 && rFreq <= 150.0) {
				rL = 69.6 + 26.2 * Math.log10(150.0) - 20
				      * Math.log10(150.0 / rFreq) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			} else if (rFreq <= 1500.0) {
				rL = 69.6 + 26.2 * Math.log10(rFreq) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			} else if (rFreq <= 2000.0) {
				rL = 46.3 + 33.9 * Math.log10(rFreq) - 13.82
				      * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			} else { // if (rFreq <= 3000.0) { CHP: 10/03-2007: As agreed by STG in
						// 27. Feb 2007 meeting the 3GHz upper limit for Extended Hata
						// is removed
				rL = 46.3 + 33.9 * Math.log10(2000) + 10 * Math.log10(rFreq / 2000)
				      - 13.82 * Math.log10(Math.max(30, rHb))
				      + (44.9 - 6.55 * Math.log10(Math.max(30, rHb))) * rCorr - rA
				      - rB;
			}
		} else {

			double rL1;
			double rL2;

			rL1 = calculateMedianLoss(rFreq, rD1, rHTx, rHRx);
			rL1 = Helper.generalEnvCorrections(rL1, rFreq, rD1, input.generalEnvironment());

			rL2 = calculateMedianLoss(rFreq, rD2, rHTx, rHRx);
			rL2 = Helper.generalEnvCorrections(rL2, rFreq, rD2, input.generalEnvironment());

			rL = rL1 + (Math.log10(rDist) - Math.log10(rD1))
			      / (Math.log10(rD2) - Math.log10(rD1)) * (rL2 - rL1);
		}
		return rL;
	}

    private HataInput input;

    @Override
	public double evaluate(LinkResult linkResult, boolean variations, HataInput input) {
        double rFreq = linkResult.getFrequency();
        double rDist = linkResult.getTxRxDistance();
        double rHTx = linkResult.txAntenna().getHeight();
        double rHRx = linkResult.rxAntenna().getHeight();
        this.input = input;

        double medianLoss = 0.0; // Median loss (deterministic component);
		double standardDeviation = 0.0; // Standard deviation for variations in
		// path loss (static component)
		double freeSpaceAttenuation; // Free space attenuation
		double temp = 0;

		if (rFreq < 30 || rFreq > 3000 ){
			throw new RuntimeException("Frequencies below 30 MHz or above 3000 MHz are not supported by the Extended Hata propagation model");
		}
		if (rDist > 100){
			throw new RuntimeException("Distances above 100 km are not supported by the Extended Hata propagation model");
		}
		freeSpaceAttenuation = 32.44 + 20 * Math.log10(rFreq) + 10 * Math.log10(rDist * rDist + (rHTx - rHRx) * (rHTx - rHRx) / 1e6);

		if (Double.isInfinite(freeSpaceAttenuation)) {
			freeSpaceAttenuation = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
		}
		medianLoss = calculateMedianLoss(rFreq, rDist, rHTx, rHRx);
		standardDeviation = Helper.variationsStdDev(rDist, input.propagationEnvironment());
		medianLoss = Helper.generalEnvCorrections(medianLoss, rFreq, rDist, input.generalEnvironment());
		if (medianLoss < freeSpaceAttenuation) {
			medianLoss = freeSpaceAttenuation;
		}
		LocalEnvCorrections lec = Helper.localEnvCorrections(new LocalEnvCorrections(medianLoss, standardDeviation), linkResult,
                input.floorHeight(), input.sizeOfRoom(), input.wallLossInIn(), input.adjacentFloorLoss(), input.empiricalParameters(), input.wallLossStdDev());
		medianLoss = lec.rMedianLoss;
		standardDeviation = lec.rStdDev;

		if (variations) {
            medianLoss += Factory.distributionFactory().getGaussianDistribution(0, standardDeviation).trial();
		}

		if (Double.isInfinite(medianLoss)) {
			medianLoss = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
		}
		return medianLoss;
	}

    @Override
    public Description description() {
        return new DescriptionImpl("Extended Hata","<html><body><b><u>Frequency range:</u></b><br>30 MHz - 3 GHz<br><b><u>Distance range:</u></b><br>up to 40 km<br><b><u>Typical application area:</u></b><br>Mobile services and other services working<br> in non-LOS/cluttered environment. Note that <br>in theory, the model can go up to 100 km <br>since the curvature of the earth is included, <br>but in practice it is recommended to use it<br> up to 40 km. <br><b><u>Information:</u></b><br>Note that the Hata model assumes that the <br>specified antenna heights of transmitter <br>and receiver are heights above ground.</body></html>");
    }
}
