package org.seamcat.model.propagation.p528;

/**
 *
 */
//---------------------------------------------------------------------------------------------------------------------------------------------------
//

import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.P528Input;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.propagation.PluginCheckUtilsToBeRemoved;
import org.seamcat.model.propagation.p528.impl.P528_Interpol;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

/**
 * ITU-R P.528 propagation model for AGA radio links<br>
 * Version for SEAMCAT V5.0.0 alpha 13, (new plugin interface)<br>
 * Provided by THALES Communications and Security France/Béatrice MARTIN<br>
 * <p></p>
 * Propagation between terminals whom at least one  is located above 1 000 m
 * <p></p>
 * Validity domain:<br>
 * Frequency range : 125 MHz - 15.5 GHz<br>
 * Distance range : 0 - 1 800 km<br>
 * Antenna Heights  :<br>
 * &nbsp&nbsp lowest altitude (ground terminal) : between 1.5 and 1 000 m<br>
 * &nbsp&nbsp highest altitude (aero terminal)    : between 1 000 and 20 000 m)<br>
 * Percentage of the time : 1 - 95 %<br>
 */
public class P528PropagationModel implements PropagationModelPlugin<P528Input> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, P528Input input, Validator<P528Input> validator) {

        if (path.size() > 0) {
            RadioSystem system = null;
            Bounds bounds = null, rB = null, tB = null;

            if (path.get(0) instanceof RadioSystem) {
                system = (RadioSystem) path.get(0);
            } else if (path.get(0) instanceof InterferenceLink) {
                system = ((InterferenceLink) path.get(0)).getInterferingSystem();
            }
            bounds = system.getFrequency().getBounds();
            rB = system.getReceiver().getHeight().getBounds();
            tB = system.getTransmitter().getHeight().getBounds();

            if (bounds.getMin() < 125) {
                validator.error("ITU-R P.528 model not applicable below 125 MHz" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }

            if (bounds.getMax() > 15500) {
                validator.error("ITU-R P.528 model not applicable above 15.5 GHz" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }

            double h = Math.min(rB.getMin(), tB.getMin());
            if (h < 1.5) {
                validator.error("ITU-R P.528 model not applicable for antenna height below 1.5 m" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
            if (h > 1000.0) {
                validator.error("ITU-R P.528 model not applicable for ground antenna height above 1 000 m" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }

            h = Math.max(rB.getMax(), tB.getMax());
            if (h > 20000.0) {
                validator.error("ITU-R P.528 model not applicable for aero antenna height above 20 000 m - Please make use of Free Space Model"
                        + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
            if (h < 1000.0) {
                validator.error("ITU-R P.528 model not applicable for aero antenna height below 1 000 m - Please make use of Longley Rice Model"
                        + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
            double distance = PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,path);
            if (distance > 1800)validator.error("Distance out of range : should be less than 1 800 km" + PluginCheckUtilsToBeRemoved.getExceptionHint());

        }
        double TimePercentage = input.TimePercentage();
        if (TimePercentage < 1 || TimePercentage > 95) {
            validator.error("outside the range 1 ... 95 %" + PluginCheckUtilsToBeRemoved.getExceptionHint()).TimePercentage();
        }

    }

    @Override
    public double evaluate(LinkResult linkResult, boolean variations, P528Input input) {
        double loss = 0.0;
        double frequency = linkResult.getFrequency();
        double distance = linkResult.getTxRxDistance();
        double HTx = linkResult.txAntenna().getHeight();
        double HRx = linkResult.rxAntenna().getHeight();

        if (frequency < 125) {
            throw new RuntimeException("ITU-R P.528 model not applicable below 125 MHz");
        }

        if (frequency > 15500) {
            throw new RuntimeException("ITU-R P.528 model not applicable above 15.5 GHz");
        }

        double h = Math.min(HTx, HRx);
        if (h < 1.5) {
            throw new RuntimeException("ITU-R P.528 model not applicable for antenna height below 1.5 m");
        }
        if (h > 1000.0) {
            throw new RuntimeException("ITU-R P.528 model not applicable for ground antenna height above 1 000 m");
        }

        h = Math.max(HTx, HRx);
        if (h > 20000.0) {
            throw new RuntimeException("ITU-R P.528 model not applicable for aero antenna height above 20 000 m - Please make use of Free Space Model");
        }
        if (h < 1000.0) {
            throw new RuntimeException("ITU-R P.528 model not applicable for aero antenna height below 1 000 m - Please make use of Longley Rice Model");
        }

        double TimePercentage = input.TimePercentage();
        if (TimePercentage < 1 || TimePercentage > 95) {
            throw new IllegalArgumentException("Time percentage outside the range 1 ... 95 %");
        }
        if (distance > 1800) {
            throw new IllegalArgumentException("Distance out of range : should be less than 1 800 km");
        }
//essayer de déplacer les tests au-dessus
        loss = P528_Interpol.PerteP528(HTx, HRx, distance, frequency, TimePercentage);

        if (variations) {
            loss += Factory.distributionFactory().getGaussianDistribution(0, input.stdDev()).trial();
        }
        return loss;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("ITU-R P.528", "ITU-R P.528 - Qt in the range [1-95]%");
    }

}


