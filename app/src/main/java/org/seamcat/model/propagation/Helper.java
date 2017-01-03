package org.seamcat.model.propagation;

import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.simulation.result.LocalEnvironmentResult;

import static org.seamcat.model.types.LocalEnvironment.Environment.Indoor;
import static org.seamcat.model.types.LocalEnvironment.Environment.Outdoor;


public class Helper {

    /**
     * Method that is called by HataSE21, HataSE24 and SD propagation model provides local environment correction to the <br>
     *     medianLoss and the variation in the path loss for<br>
     *     <ol>
     *       <li>indoor-outdoor</li>
     *       <li>indoor-indoor</li>
     *          <ol>
     *              <li>Transmitter and receiver in same building</li>
     *              <li>Transmitter and receiver in different buildings</li>
     *          </ol>
     *     </ol>
     * there is no correction for outdoor-outdoor
     *
     * @param localEnvironmentCorrections
     * @param linkResult
     * @param floorHeight
     * @param roomSize
     * @param wiLoss
     * @param floorLoss
     * @param empiricalParam
     * @param wiStdDev
     * @return localEnvironmentCorrections medianLoss and variation
     */
    protected static LocalEnvCorrections localEnvCorrections(LocalEnvCorrections localEnvironmentCorrections,LinkResult linkResult,
            double floorHeight,double roomSize, double wiLoss, double floorLoss, double empiricalParam, double wiStdDev) {

        LocalEnvironmentResult rxEnv = linkResult.rxAntenna().getLocalEnvironment();
        LocalEnvironmentResult txEnv = linkResult.txAntenna().getLocalEnvironment();
        // CAUTION : do NOT permute the order of the tests
        if ( rxEnv.getEnvironment() == Indoor && txEnv.getEnvironment() == Indoor) {
            if ( linkResult.isTxRxInSameBuilding() ) {
                // Transmitter and receiver are located in the same building
                // specific calculation : replaces standard calculation
                double rK;

                rK = Math.abs(
                        Math.floor(linkResult.txAntenna().getHeight()/ floorHeight) -
                        Math.floor(linkResult.rxAntenna().getHeight()/ floorHeight)
                );

                double d1 = linkResult.txAntenna().getHeight() - linkResult.rxAntenna().getHeight();
                double realDistance = Math.sqrt( (d1*d1) + (linkResult.getTxRxDistance()*linkResult.getTxRxDistance()) );

                 localEnvironmentCorrections.rMedianLoss = -27.6
                        + 20.0
                        * Math.log10(1000 * realDistance)
                         +  20.0
                        * Math.log10(linkResult.getFrequency())
                        + Math.floor(1000 * linkResult.getTxRxDistance() / roomSize)
                        * wiLoss
                        + Math.pow(rK,
                        ((rK + 2.0) / (rK + 1.0) - empiricalParam))
                        * floorLoss;
                localEnvironmentCorrections.rStdDev = wiStdDev;
            } else {
                // Transmitter and receiver are located in different buildings
                // Calculation is similar to indoor-outdoor case with doubled
                // corrections
                localEnvironmentCorrections.rMedianLoss += rxEnv.getWallLoss() + txEnv.getWallLoss();
                localEnvironmentCorrections.rStdDev = Math
                        .sqrt(localEnvironmentCorrections.rStdDev * localEnvironmentCorrections.rStdDev
                                + ((txEnv.getWallLossStdDev() * txEnv.getWallLossStdDev()) + (rxEnv.getWallLossStdDev() * rxEnv.getWallLossStdDev())));
            }
        } else if (rxEnv.getEnvironment() == Indoor && txEnv.getEnvironment() == Outdoor) {
            localEnvironmentCorrections.rMedianLoss += rxEnv.getWallLoss();
            localEnvironmentCorrections.rStdDev = Math.sqrt(localEnvironmentCorrections.rStdDev * localEnvironmentCorrections.rStdDev +
                    rxEnv.getWallLossStdDev() * rxEnv.getWallLossStdDev());
        } else if (rxEnv.getEnvironment() == Outdoor && txEnv.getEnvironment() == Indoor) {
            localEnvironmentCorrections.rMedianLoss += txEnv.getWallLoss();
            localEnvironmentCorrections.rStdDev = Math.sqrt(localEnvironmentCorrections.rStdDev * localEnvironmentCorrections.rStdDev +
                    txEnv.getWallLossStdDev()*txEnv.getWallLossStdDev());
        }
        // outdoor outdoor => no correction
        return localEnvironmentCorrections;
    }

    public static double generalEnvCorrections( double rMedianLoss, double frequency,double rDist,String gEnv) {
        if (rDist >= 0.1) {
            if (gEnv.equals("Suburban")) {
                rMedianLoss += -2
                        * Math.pow(Math.log10(Math.min(Math.max(150.0, frequency),
                        2000) / 28), 2) - 5.4;
            } else if (gEnv.equals("Rural")) {
                double rural_correction = -4.78
                        * Math.pow(Math.log10(Math.min(Math.max(150.0, frequency), 2000)), 2) + 18.33
                        * Math.log10(Math.min(Math.max(150.0, frequency), 2000))
                        - 40.94;
                rMedianLoss += rural_correction;
            }
        }
        return rMedianLoss;
    }

    protected static double variationsStdDev(double rDist, String pEnv) {
        double rStdDev = 0.0;

        if (rDist <= 0.04) {
            rStdDev = 3.5;
        } else if (rDist <= .1) {
            if (pEnv.equals("Above roof")) {
                rStdDev = 3.5 + (12 - 3.5) / (0.1 - 0.04) * (rDist - 0.04);
            } else  {
                rStdDev = 3.5 + (17 - 3.5) / (0.1 - 0.04) * (rDist - 0.04);
            }
        } else if (rDist <= .2) {
            if (pEnv.equals("Above roof")) {
                rStdDev = 12;
            } else {
                rStdDev = 17;
            }
        } else if (rDist <= .6) {
            if (pEnv.equals("Above roof")) {
                rStdDev = 12.0 + (9.0 - 12.0) / (0.6 - 0.2) * (rDist - 0.2);
            } else  {
                rStdDev = 17.0 + (9.0 - 17.0) / (0.6 - 0.2) * (rDist - 0.2);
            }
        } else {
            rStdDev = 9;
        }
        return rStdDev;
    }
}
