package org.seamcat.model.propagation;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.Model_C_IEEE_802_11_rev3_Input;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;
import java.util.Random;

/**
 * Created by Karl Koch (adhoc@heiseka.de) on 06.10.2015.<br/>
 * Project: STG 17a plugin<br/>
 * <br/><br/>
 */

public class Model_C_IEEE_802_11_rev3 implements PropagationModelPlugin<Model_C_IEEE_802_11_rev3_Input> {
    private String warningMessage;
    private static final Logger LOG = Logger.getLogger(Model_C_IEEE_802_11_rev3.class);

    @Override
    public double evaluate(LinkResult linkResult, boolean variation, Model_C_IEEE_802_11_rev3_Input input) {
        Random random = new Random();
        double pathLoss = 0;
        double pathLossToBP = 0, pathLossAfter = 0;
        double distance = linkResult.getTxRxDistance(), distanceToBP = input.distanceToBP() / 1000.; // in km

//        if (null != getWarningMessage() && !getWarningMessage().isEmpty())throw new RuntimeException(getWarningMessage());

        if (Mathematics.equals(input.distanceToBP(),0,1e-5)){
            throw new RuntimeException("distance to break point (BP) equal to zero not allowed.");
        }

        double freeSpaceReference = 32.44 + 20 * Math.log10(linkResult.getFrequency()) +
                10 * Math.log10(distance * distance +
                        (linkResult.rxAntenna().getHeight() - linkResult.txAntenna().getHeight())
                                * (linkResult.rxAntenna().getHeight() - linkResult.txAntenna().getHeight()) / 1000000.);

        if (distance <= distanceToBP) {
            pathLoss = freeSpaceReference;
            if (variation) pathLoss += input.logNormBefore() * random.nextGaussian();
        } else {
            pathLossToBP = 32.44 + 20 * Math.log10(linkResult.getFrequency()) +
                    10 * Math.log10(distanceToBP * distanceToBP +
                            (linkResult.rxAntenna().getHeight() - linkResult.txAntenna().getHeight())
                                    * (linkResult.rxAntenna().getHeight() - linkResult.txAntenna().getHeight()) / 1000000.);
            pathLossAfter = 35 * Math.log10(distance / distanceToBP);
            if (variation) {
                pathLoss = pathLossToBP + input.logNormBefore() * random.nextGaussian()
                        + pathLossAfter + input.logNormAfter() * random.nextGaussian();
            } else {
                pathLoss = pathLossToBP + pathLossAfter;
            }
        }
        if (LOG.isDebugEnabled()){
            LOG.debug("distance to break point = " + distanceToBP + " km; "
                    + "LOG-NORMAL distribution before BP = " + input.logNormBefore() + " dB; "
                    + "LOG-NORMAL distribution after BP = " + input.logNormAfter() + " dB");
        }

        return pathLoss;
    }

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> list, Model_C_IEEE_802_11_rev3_Input input, Validator<Model_C_IEEE_802_11_rev3_Input> validator) {
        if (input.logNormAfter() < input.logNormBefore())
            validator.error("LOG-NORMAL distribution after BP shall not be less than LOG-NORMAL distribution before BP");
        if (Mathematics.equals(input.distanceToBP(), 0., 0.0001))
            validator.error("distance to break point (BP) must be greater than zero" + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if (list.size() > 0) {
            Distribution frequency = HataSE21PropagationModel.findFrequency(scenario,list);
            if (frequency.getBounds().isBounded()){
                if (frequency.getBounds().getMin() < 1800)validator.error("This model is designed for the 2 GHz and 5 GHz ranges. 1800 MHz is assumed as lowest valid frequency.");
            }
            if (PluginCheckUtilsToBeRemoved.getMinDistance(scenario,list)> input.distanceToBP() / 1000.)
                validator.error("distance to break point (BP) must be greater than " + PluginCheckUtilsToBeRemoved.getMinDistance(scenario,list)* 1000 + "m" +
                PluginCheckUtilsToBeRemoved.getExceptionHint());
            else if (PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,list) < input.distanceToBP() / 1000.){
                setWarningMessage("distance to break point (BP) must be less than " + PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,list) * 1000 + "m");
                validator.error("distance to break point (BP) must be less than " + PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,list) * 1000 + "m" +
                PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
        }
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Model C IEEE 802.11 rev3", "Calculate the IEEE 802.11 Model C");
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public String getWarningMessage() {
        return warningMessage;
    }}