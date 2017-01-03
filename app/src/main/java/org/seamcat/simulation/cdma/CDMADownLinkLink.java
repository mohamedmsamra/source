package org.seamcat.simulation.cdma;

import org.seamcat.model.functions.Point2D;

import java.util.Comparator;

// fast access class representing a link in an CDMADownLink system
class CDMADownLinkLink {
    /**
     * compare the pathloss of 2 CDMA links. It is used to sort them at a later stage.
     *<p></p>
     * <ol>
     *     <li>if( pathloss1 > pathloss2) return 1;</li>
     *     <li>if( pathloss1 < pathloss2) return -1;</li>
     *     <li>return 0;</li>
     *</ol>
     * <p></p>
     * <code>pathloss = txRxPathLoss - BsAntGain() - UserAntGain();</code>
     */
    public static Comparator<CDMADownLinkLink> LinkPathLossComparator = new Comparator<CDMADownLinkLink>() {

        public int compare(CDMADownLinkLink l1, CDMADownLinkLink l2) {
            if (l1 == null && l2 == null) {
                return 0;
            }
            if (l1 == null) {
                return -1;
            }
            if (l2 == null) {
                return 1;
            }

            double l1Result = l1.loss - l1.txGain - l1.rxGain;
            double l2Result = l2.loss - l2.txGain - l2.rxGain;

            if( l1Result > l2Result) return 1;
            if( l1Result < l2Result) return -1;
            return 0;

        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    };

    CDMADownLinkMobileStation ms;
    CDMADownLinkBaseStation bs;
    double distance;
    double txAzimuth;
    double txElevation;
    double txGain;
    double rxGain;
    Point2D txPosition;

    double loss;
    double effectiveLoss;

    double transmittedTrafficChannelPowerdBm;
    double receivedTrafficChannelPowerdBm;
    double totalReceivedPowerdBm;
    boolean powerScaledDownToMax;
}
