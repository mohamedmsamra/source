package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.types.InterferenceLink;

public interface InterferingLinkRelativePosition {

    enum CorrelationMode {
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT,
        VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR,
        VICTIM_DMA_INTERFERER_DMA_COR,
        VICTIM_DMA_INTERFERER_DMA_DYN,
        VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT,
        VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT,
        VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR,
        VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR,
        VICTIM_DMA_INTERFERER_CLASSICAL_NONE,
        VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM,
        VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST,
        VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT,
        VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT,
        VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR,
        VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR,;


        @Override
        public String toString() {
            switch (this) {
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR: return "None VR relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR: return "Uniform density VR relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR: return "Closest interferer VR relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT: return "None WT relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT: return "Uniform density WT relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT: return "Closest interferer WT relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR: return "Corr (Victim link -> ILTx) VR relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT: return "Corr (Victim link -> ILTx) WT relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT: return "Corr (Victim link -> ILRx) WT relative";
                case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR: return "Corr (Victim link -> ILRx) VR relative";
                case VICTIM_DMA_INTERFERER_DMA_COR: return "Cor. (victim BS ref. cell -> interfering BS ref. cell)";
                case VICTIM_DMA_INTERFERER_DMA_DYN: return "Dyn. (victim BS ref. cell -> interfering BS ref. cell)";
                case VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT: return "Cor. (Interfering BS ref. cell) WT relative";
                case VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT: return "Dyn. (Interfering BS ref. cell) WT relative";
                case VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR: return "Cor. (Interfering BS ref. cell) VR relative";
                case VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR: return "Dyn. (Interfering BS ref. cell) VR relative";
                case VICTIM_DMA_INTERFERER_CLASSICAL_NONE: return "None (origin: victim BS ref. cell)";
                case VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM: return "Uniform density (origin: victim BS ref. cell)";
                case VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST: return "Closest interferer (origin: victim BS ref. cell)";
                case VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT: return "Cor. (victim BS ref. cell -> ILTx)";
                case VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT: return "Dyn. (victim BS ref. cell -> ILTx)";
                case VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR: return "Cor. (victim BS ref. cell -> ILRx)";
                default /*VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR*/: return "Dyn. (victim BS ref. cell -> ILRx)";
            }
        }
    }

    CorrelationMode getCorrelationMode();

    boolean isWrCenterOfItDistribution();

    double getSimulationRadius();

    int getNumberOfActiveTransmitters();

    boolean useCoLocatedWith();

    InterferenceLink getCoLocatedWith();

    Point2D getCoLocationDeltaPosition();

    Distribution getMinimumCouplingLoss();

    Distribution getProtectionDistance();

    RelativeLocation getRelativeLocation();

}
