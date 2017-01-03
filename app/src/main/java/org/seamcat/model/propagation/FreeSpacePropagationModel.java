package org.seamcat.model.propagation;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.Scenario;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.FreespaceInput;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

/**
 * method that calculates the free space propagation model (ITU-R P.525)
 * <p></p>
 * Frequency range: Above 30 MHz<br>
 * Distance range: LOS-limited<br>
 * Typical application area: This model is appropriate to use on paths were unobstructed direct-Line-of-Sight propagation <br>
 *     (with no reflection) could be expected (e.g. point-to-point fixed service links, links over short distances in <br>
 *         open areas, etc).
 *<p></p>
 * <code>rL = 32.44 + 10log10((Tx_Height - Rx_Height)^2) / 1e6 + TxRxDistance^2) + 20log10(Frequency);</code>
 *<p></p>
 * if rL is infinitely large in magnitude then it is replaced<br>
 * by <code>rL = 20 * Math.log10(Frequency) - 100</code> <br>
 *     which is representing a symbolic distance of about 0.3 mm<br>
 */
public class FreeSpacePropagationModel implements PropagationModelPlugin<FreespaceInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, FreespaceInput input, Validator<FreespaceInput> validator) {
    }

    @Override
    public double evaluate(LinkResult linkResult, boolean variations, FreespaceInput input) {
        double rL = 0.0; // Median loss (deterministic component);

        if (linkResult.getFrequency() < 30){
            throw new RuntimeException("Frequencies below 30 MHz are not supported by the Free Space model");
        }
        rL = 32.44
                + 10
                * Math.log10((linkResult.txAntenna().getHeight() - linkResult.rxAntenna().getHeight()) * (linkResult.txAntenna().getHeight() - linkResult.rxAntenna().getHeight()) / 1e6 + linkResult.getTxRxDistance() * linkResult.getTxRxDistance())
                + 20 * Math.log10(linkResult.getFrequency());

        if (Double.isInfinite(rL)) {
            rL = 20 * Math.log10(linkResult.getFrequency()) - 100;//represents a symbolic distance of about 0.3 mm
        }
        if (variations) {
            rL += Factory.distributionFactory().getGaussianDistribution(0, input.stdDev()).trial();
        }
        return rL;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Free Space (ITU-R P.525)", "<html><body><b><u>Frequency range:</u></b><br>Above 30 MHz<br><b><u>Distance range:</u></b><br>LOS-limited<br><b><u>" +
                "Typical application area:</u></b><br>" +
                "This model is appropriate to use on paths were <br> unobstructed direct-Line-of-Sight <br>" +
                "propagation (with no reflection) could be expected <br>(e.g. point-to-point fixed service links, <br>" +
                "links over short distances in open areas, etc)</body></html>");
	}
}
