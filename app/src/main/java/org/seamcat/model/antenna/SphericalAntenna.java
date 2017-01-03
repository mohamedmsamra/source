package org.seamcat.model.antenna;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.plugin.antenna.SphericalInput;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.simulation.result.SensingLinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class SphericalAntenna implements AntennaGainPlugin<SphericalInput> {

    private static final Logger LOG = Logger.getLogger(HorizontalVerticalAntenna.class);

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, SphericalInput input, Validator<SphericalInput> validator) {
    }

    @Override
    public double evaluate(LinkResult link, AntennaResult antenna, double peakGain, SphericalInput input) {
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

        double sphericalAngle = Mathematics.acosD(Mathematics.cosD(theta)
                * Mathematics.cosD(phi));
        double sphericalGain = input.spherical().evaluate(sphericalAngle);
        gain = peakGain + sphericalGain;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Using spherical pattern");
            LOG.debug("Spherical angle (rSAng) = " + sphericalAngle + " = acosD(cosD(RxTXElev [" + theta
                    + "]) * cosD(RxTxAzi [" + phi + "]))");
            LOG.debug("VrSResult = getVictimLinkReceiver().getAntenna().gainS(" + sphericalGain + ") = "
                    + sphericalGain);
            LOG.debug("Antenna Gain = " + gain);
        }
        return gain;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Spherical Antenna",
                "<html>The antenna gain is calculated using a spherical <br>antenna pattern</html>");
    }
}
