package org.seamcat.model.propagation;
//---------------------------------------------------------------------------------------------------------------------------------------------------
//
//           ITU-R P.1411 propagation model for both Tx and Rx antennas at low height
//           Tested for SEAMCAT V5.0.0 (Alpha 15, 16 and next ones if no change of plugin interface)
//           Provided by THALES Communications and Security France/Béatrice MARTIN
//
//           Propagation between terminals located below roof-top height and near to the ground at UHF.
//           Ref :  ITU-R P.1411-7, §4.3
//           Applicability : 300 MHz - 3 GHz, d < 3 km, Tx and Rx antenna heights between 1.9 and 3 m

//           frequency    : carrier frequency (MHz) --> Range = [300 MHz;3 GHz]
//           distance     : distance (km)   --> Max distance limited to 3 km

//           percentLoc   : percentage of the locations --> used for setting corrections
//           witdh : typical value is 15 m


import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.P1411LowAntennaHeightInput;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.*;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class P1411LowAntennaHeight implements PropagationModelPlugin<P1411LowAntennaHeightInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> list, P1411LowAntennaHeightInput input, Validator<P1411LowAntennaHeightInput> validator) {
        Object coverage;
        InterferenceLink interferenceLink = null;
        SensingLink sensingLink = null;
        RadioSystem singleSystem = null, victimSystem = null, interferenceSystem = null;
        Distribution hTX = null, hRX = null, frequency = null;
        double maxDistance = 0;

        String msgBelow = "", msgAbove = "", msgDistance = "", linkInformation = "";

        if (list.size() > 0) {
            if (list.get(0) instanceof RadioSystem) {
                // generic or cellular
                singleSystem = (RadioSystem) list.get(0);
            } else if (list.get(0) instanceof InterferenceLink) {
                //interferer
                interferenceLink = (InterferenceLink) list.get(0);
                victimSystem = interferenceLink.getVictimSystem();
                interferenceSystem = interferenceLink.getInterferingSystem();
                linkInformation = "InterferenceLink";
            }
            // TODO handle EPP nested situation

        } else if (list.size() > 1 && list.get(0) instanceof InterferenceLink && list.get(1) instanceof SensingLink) {
            // sensing link
            linkInformation = "Sensing";
        }

        if (interferenceLink != null) {
            boolean isIndoor = false;
            List<LocalEnvironment> envRX = victimSystem.getReceiver().getLocalEnvironments();
            for (LocalEnvironment le : envRX) {
                if (le.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
            }
            List<LocalEnvironment> envTX = interferenceSystem.getTransmitter().getLocalEnvironments();
            for (LocalEnvironment le : envTX) {
                if (le.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
            }
            if (isIndoor) validator.error("This model ignores local environment distributions 'indoor'.");
            maxDistance = interferenceLink.getInterferingLinkRelativePosition().getSimulationRadius() * interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
            msgDistance = " -> Interfering Link transmitter to victim link receiver path (takes account of the path distance factor)";
        }
        if (singleSystem != null && !(singleSystem instanceof CellularSystem)) { // generic system
            boolean isIndoor = false;
            List<LocalEnvironment> envRX = singleSystem.getReceiver().getLocalEnvironments();
            for (LocalEnvironment le : envRX) {
                if (le.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
            }
            List<LocalEnvironment> envTX = singleSystem.getTransmitter().getLocalEnvironments();
            for (LocalEnvironment le : envTX) {
                if (le.getEnvironment() == LocalEnvironment.Environment.Indoor) isIndoor = true;
            }
            if (isIndoor) validator.error("This model ignores local environment distributions 'indoor'.");
            hRX = singleSystem.getReceiver().getHeight();
            hTX = singleSystem.getTransmitter().getHeight();
            frequency = singleSystem.getFrequency();
            coverage = ((GenericSystem) list.get(0)).getLink().getCoverageRadius();
            if (coverage != null) {
                maxDistance = ((CoverageRadius) coverage).evaluate(((GenericSystem) list.get(0)));
                maxDistance *= ((GenericSystem) list.get(0)).getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                msgDistance = " -> coverage radius";
            }
            if (hRX.getBounds().getMin() < 1.9) msgBelow = " -> RX";
            if (hTX.getBounds().getMin() < 1.9) msgBelow += " -> TX";
            if (hRX.getBounds().getMax() > 3.0) msgAbove = " -> RX";
            if (hTX.getBounds().getMax() > 3.0) msgAbove += " -> TX";
        } else if (singleSystem != null) {// cellular system
            CellularSystem cellSystem = ((CellularSystem) singleSystem);
            hRX = cellSystem.getLink().getMobileStation().getAntennaHeight();
            hTX = cellSystem.getLink().getBaseStation().getHeight();
            if (hRX.getBounds().getMin() < 1.9) msgBelow = " -> MS";
            if (hTX.getBounds().getMin() < 1.9) msgBelow += " -> BS";
            if (hRX.getBounds().getMax() > 3.0) msgAbove = " -> MS";
            if (hTX.getBounds().getMax() > 3.0) msgAbove += " -> BS";
            frequency = cellSystem.getFrequency();
            maxDistance = cellSystem.getLayout().getCellRadius();
            msgDistance = " -> cell radius";
        } else if ((victimSystem != null) && !(victimSystem instanceof CellularSystem)) {// victim generic
            if (linkInformation.contains("Sensing")) {
                hTX = victimSystem.getTransmitter().getHeight();
                hRX = interferenceSystem.getReceiver().getHeight();
                if (hRX.getBounds().getMin() < 1.9) msgBelow = " -> ILR";
                if (hTX.getBounds().getMin() < 1.9) msgBelow = msgBelow.isEmpty() ? " -> VLT" : msgBelow + " and VLT";
                if (hRX.getBounds().getMax() > 3.0) msgAbove = " -> ILR";
                if (hTX.getBounds().getMax() > 3.0) msgAbove = msgAbove.isEmpty() ? " -> VLT" : msgBelow + " and VLT";
                frequency = victimSystem.getFrequency();
            } else if (!(interferenceSystem instanceof CellularSystem)) {// interferer generic
                boolean isIndoor = false;
                String msg = "";
                List<LocalEnvironment> envRX = victimSystem.getReceiver().getLocalEnvironments();
                for (LocalEnvironment le : envRX) {
                    if (le.getEnvironment().equals(LocalEnvironment.Environment.Indoor)) {
                        isIndoor = true;
                        msg = " on the victim";
                    }
                }
                List<LocalEnvironment> envTX = interferenceSystem.getTransmitter().getLocalEnvironments();
                for (LocalEnvironment le : envTX) {
                    if (le.getEnvironment().equals(LocalEnvironment.Environment.Indoor)) {
                        isIndoor = true;
                        if (msg.isEmpty()) msg = " on the interferer";
                        else msg += " and the interferer";
                    }
                }
                if (isIndoor) validator.error("This model ignores local environment distributions 'indoor'" + msg);
                hTX = interferenceSystem.getTransmitter().getHeight();
                hRX = victimSystem.getReceiver().getHeight();
                if (hRX.getBounds().getMin() < 1.9) msgBelow = " -> VLR";
                if (hTX.getBounds().getMin() < 1.9) msgBelow = msgBelow.isEmpty() ? " -> ILT" : msgBelow + " and ILT";
                if (hRX.getBounds().getMax() > 3.0) msgAbove = " -> VLR";
                if (hTX.getBounds().getMax() > 3.0) msgAbove = msgAbove.isEmpty() ? " -> ILT" : msgBelow + " and ILT";
                frequency = interferenceSystem.getFrequency();
            } else {// interferer cellular
                validator.error("ITU-R P.1411 model should not be used for the interference link in case the interferer is <strong>NOT</strong> of type Generic <br/>" +
                        "due to the distances to the VLR might exceed the valid range of 3 km " +
                        "which causes runtime exceptions. <em>-> " + ((CellularSystem) list.get(0)).getName() + "</em>");
                return;
            }
        } else {// victim is cellular system
            CellularSystem cellSystem = (CellularSystem) victimSystem;
            if (interferenceSystem instanceof CellularSystem) {
                validator.error("ITU-R P.1411 model should not be used for the interference link in case the interferer is <strong>NOT</strong> of type Generic <br/>" +
                        "due to the distances to the VLR might exceed the valid range of 3 km " +
                        "which causes runtime exceptions. <em>-> " + cellSystem.getName() + "</em>");
                return;
            } else if (interferenceSystem != null) {// interferer is generic
                frequency = interferenceSystem.getFrequency();
                hTX = interferenceSystem.getTransmitter().getHeight();
                if (cellSystem.isUpLink()) {
                    hRX = cellSystem.getLink().getBaseStation().getHeight();
                    if (hRX.getBounds().getMin() < 1.9) msgBelow = " -> BS";
                    if (hRX.getBounds().getMax() > 3.0) msgAbove = " -> BS";
                } else {// down link
                    hRX = cellSystem.getLink().getMobileStation().getAntennaHeight();
                    if (hRX.getBounds().getMin() < 1.9) msgBelow = " -> MS";
                    if (hRX.getBounds().getMax() > 3.0) msgAbove = " -> MS";
                }
            }
            if (hTX != null) {
                if (hTX.getBounds().getMin() < 1.9) msgBelow = msgBelow.isEmpty() ? " -> ILT" : msgBelow + " and ILT";
                if (hTX.getBounds().getMax() > 3.0) msgAbove = msgAbove.isEmpty() ? " -> ILT" : msgAbove + " and ILT";
            }
        }
        String info = "";
        if (!msgBelow.isEmpty())
            validator.error("ITU-R P.1411 model not applicable with Antenna heights below 1.9 m" + msgBelow + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if (!msgAbove.isEmpty())
            validator.error("ITU-R P.1411 model not applicable with Antenna heights above 3 m" + msgAbove + msgBelow + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if ((frequency != null ? frequency.getBounds().getMin() : 0) < 300.)
            validator.error("ITU-R P.1411 model not applicable below 300 MHz" + msgBelow + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if ((frequency != null ? frequency.getBounds().getMax() : 0) > 100000.)
            validator.error("ITU-R P.1411 model not applicable above 100 GHz" + msgBelow + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if (maxDistance > 3.0)
            info += ("ITU-R P.1411 model not applicable for distances larger than 3 km" + msgDistance + msgBelow + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if (input.LocationPercentage() < 1 || input.LocationPercentage() > 99)
            info += ("Location percentage outside the range 1 ... 99%" + msgBelow + PluginCheckUtilsToBeRemoved.getExceptionHint());
        if (input.WidthTransitionRegion() > 25)
            info += ("Width for transition region should not exceed 25 m");

        if (!info.isEmpty())validator.error(PluginCheckUtilsToBeRemoved.getManualReferene("e.g. distance, location percentage or width of transistion region"));
    }

    @Override
    public double evaluate(LinkResult linkResult, boolean variations, P1411LowAntennaHeightInput input) {
        double loss = 0.0;
        double frequency = linkResult.getFrequency();  //or getTrialFrequency() ?
        double distance = linkResult.getTxRxDistance();
        double HTx = linkResult.txAntenna().getHeight();
        double HRx = linkResult.rxAntenna().getHeight();

        if (frequency < 300) {
            throw new RuntimeException("ITU-R P.1411 model not applicable below 300 MHz");
        }

        if (frequency > 100000) {
            throw new RuntimeException("ITU-R P.1411 model not applicable above 100 GHz");
        }

        if (HTx > 3 || HRx > 3) {
            throw new RuntimeException("ITU-R P.1411 model not applicable with Tx Antenna above 3 m");
        }
        if (HTx < 1.9 || HRx < 1.9) {
            throw new RuntimeException("ITU-R P.1411 model not applicable with Tx Antenna below 1.9 m");
        }

        String environment = input.generalEnvironment();
        double percentLoc = input.LocationPercentage() / 100.0;
        double street_width = input.WidthTransitionRegion();

        loss = perteP1411(distance, frequency, environment, percentLoc, street_width);

        if (variations) {
            loss += Factory.distributionFactory().getGaussianDistribution(0, input.stdDev()).trial();
        }
        return loss;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("ITU-R P.1411 Low Antenna Height", "ITU-R P.1411 §4.3");
    }

    public static double perteP1411(double distance, double frequency, String environment, double pLoc, double street_width) {
        double Lurban, deltaL_los, deltaL_Nlos, dist_los, Llos, Lnlos;
        double loss = 0;
        // deltaL_los     : LOS location correction (dB)
        //'deltaL_Nlos    : NLOS location correction (dB)
        //'dist_los       : distance for which the LOS fraction FLOS equals the location percentage
        //'Lurban         : 0 dB for suburban, 6.8 dB for urban, 2.3 dB for dense urban
        //'p              : percent location as a fraction i.e. percentLoc/100 --> this variable is introduced to repeat 6 times the division by 100 (percentLoc/1)

        //Convert distances into meters in order to fasten calculation time
        double distance_m = distance * 1000.0;

        if (environment.contains("Suburban")) Lurban = 0;
        else if (environment.contains("Urban")) Lurban = 6.8;
        else if (environment.contains("Dense Urban/High-Rise")) Lurban = 2.3;
        else
            Lurban = 0;   //should never occur since the user has a limited choice in the menu list (no possibility to enter another value)

        deltaL_los = 1.5624 * 7.0 * (Math.sqrt(-2.0 * Math.log(1.0 - pLoc)) - 1.1774);   //Sigma = 7 dB
        deltaL_Nlos = -7.0 * Mathematics.Qi(pLoc);    //TODO modified by KK							//Sigma = 7 dB and a minus sign is added since there is probably a bug in ITU P.1411

        if (pLoc < 0.45) dist_los = 212.0 * Math.pow(Math.log10(pLoc), 2.0) - 64.0 * Math.log10(pLoc);
        else dist_los = 79.2 - 70.0 * pLoc;

        if (distance_m < dist_los)
            loss = 32.45 + 20.0 * Math.log10(frequency) + 20.0 * Math.log10(distance) + deltaL_los;
        else if (distance_m > (dist_los + street_width))
            loss = 9.5 + 45.0 * Math.log10(frequency) + 40.0 * Math.log10(distance) + Lurban + deltaL_Nlos;
        else //linear interpolation between LOS and NLOS
        {
            Llos = 32.45 + 20.0 * Math.log10(frequency) + 20.0 * Math.log10(dist_los / 1000) + deltaL_los;
            Lnlos = 9.5 + 45.0 * Math.log10(frequency) + 40.0 * Math.log10((dist_los + street_width) / 1000.0) + Lurban + deltaL_Nlos;
            loss = Llos + (Lnlos - Llos) * (distance_m - dist_los) / street_width;
        }
        return loss;

    }

    //the code below has been copied from the SEAMCAT source code file P1546ver3PropagationModel.java which contains public Qi(x)
    //The method is integrated here in order to reduce execution time and a bug on original public Qi(x) has been corrected
    //it is renamed Qi_1411 in order to guarantee no confusion, and kept private to accelerate execution time
    private static double Qi_1411(double x) {
        double Q, t, xi;
        double c0, c1, c2, d1, d2, d3;

        try {
            c0 = 2.515517f;
            c1 = 0.802853f;
            c2 = 0.010328f;
            d1 = 1.432788f;
            d2 = 0.189269f;
            d3 = 0.001308f;

            if (x <= 0.5) {
                t = Math.sqrt(-2.0 * Math.log(x));
            } else {
                t = Math.sqrt(-2.0 * Math.log(1 - x));
            }

            xi = (((c2 * t + c1) * t) + c0) / (((d3 * t + d2) * t + d1) * t + 1);
            if (x <= 0.5) {
                Q = t - xi;
            } else {
                Q = -(t - xi);
            }
            return Q;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}