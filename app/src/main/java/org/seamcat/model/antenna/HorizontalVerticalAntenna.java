package org.seamcat.model.antenna;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.plugin.antenna.HorizontalVerticalInput;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.simulation.result.SensingLinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class HorizontalVerticalAntenna implements AntennaGainPlugin<HorizontalVerticalInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, HorizontalVerticalInput input, Validator<HorizontalVerticalInput> validator) {
        OptionalFunction function = input.horizontal();
        if ( function.isRelevant() ) {
            Bounds bounds = function.getFunction().getBounds();
            if ( bounds.getMin() != 0.0 || bounds.getMax() != 360.0 ) {
                validator.error( "Antenna pattern must be specified from point 0.0 to 360.0").horizontal();
            }
        }
    }

    private static final Logger LOG = Logger.getLogger(HorizontalVerticalAntenna.class);

    @Override
    public double evaluate(LinkResult link, AntennaResult antenna, double peakGain, HorizontalVerticalInput input) {
        OptionalFunction horizontal = input.horizontal();
        OptionalFunction vertical = input.vertical();
        double gain = 0, phi = antenna.getAzimuth(), theta = antenna.getElevation(), beta = 0;
        //TODO F.1336 mechanical tilt formulas added by KK
        double thetaH = theta,  phiH = phi;
        if (!Mathematics.equals(antenna.getTilt(), 0, 1e-5)) {
            beta = -antenna.getTilt(); //TODO F.1336 takes down tilt as  positive angle
            if (!Mathematics.equals(antenna.getElevationCompensation(), 0, 1e-5) &&
                    link instanceof InterferenceLinkResult || link instanceof SensingLinkResult) {
                beta -= antenna.getElevationCompensation(); //TODO correction if antenna is pointing
            }
            theta = Mathematics.asinD(Mathematics.sinD(thetaH) * Mathematics.cosD(beta) +
                    Mathematics.cosD(thetaH) * Mathematics.cosD(antenna.getAzimuth()) * Mathematics.sinD(beta));

            if (phiH > 180)
                phiH = -360 + phiH;//REVISIT: to fit the range -180 ... +180 of F.1336 due to the arcos function

            phi = Mathematics.acosD(Math.min(1.0,
                    Math.max(-1.0,
                            (-Mathematics.sinD(thetaH) * Mathematics.sinD(beta) + Mathematics.cosD(thetaH) * Mathematics.cosD(phiH) * Mathematics.cosD(beta))
                                    / Mathematics.cosD(theta))));
            phi = (antenna.getAzimuth() > 180) ? 360 - phi : phi;
        }

        {
            // Vr Hor Pattern
            double horiGain = 0, vertiGain = 0;

            if (horizontal.isRelevant()) {
                horiGain = horizontal.getFunction().evaluate(phi);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using horizontal pattern:");
                    LOG.debug("horizontalGain = gainH(horizontalAngle) -> " + horiGain + " = gainH(" + phi
                            + ")");
                }
            }

            // Vr Vert Pattern
            if (vertical.isRelevant()) {
                vertiGain = vertical.getFunction().evaluate(theta);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using vertical pattern:");
                    LOG.debug("verticalGain = gainV(verticalAngle) -> " + vertiGain + " = gainV(" + theta + ")");
                }
            }

            if (horizontal.isRelevant() && vertical.isRelevant()) {
                if (Math.abs(horiGain - vertiGain) < 3) { //3 is the value decided by STG18 (April 2009)
                    double G_horiz = Mathematics.dB2Linear(horiGain);
                    double G_vert = Mathematics.dB2Linear(vertiGain);
                    gain = Mathematics.dB2Linear(peakGain) * (Math.sqrt(((G_horiz * G_horiz) + (G_vert * G_vert)) / 2));
                    gain = Mathematics.linear2dB(gain);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("gain = G_max * (Math.sqrt(((G_horiz*G_horiz) + (G_vert*G_vert))/2)");
                    }
                } else {
                    gain = peakGain + Math.min(horiGain, vertiGain);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("gain = G_max * Math.min(G_horiz,G_vert)");
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Antenna Gain  = " + gain);
                }
            } else {
                gain = peakGain + horiGain + vertiGain;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Antenna Gain  = " + gain + " =getPeakGain() (" + peakGain + ") + horiGain (" + horiGain + ") + vertiGain (" + vertiGain + ");");
                }
            }
        }
        return gain;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Horizontal-Vertical Gain",
                "<html>Based on the direction of the antenna uses the horizontal <br>" +
                        "and/or vertical pattern to find the peak gain</html>");
    }
}
