package org.seamcat.presentation.systems.generic;

import org.seamcat.model.generic.InterferingLinkRelativePosition.CorrelationMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.seamcat.model.generic.InterferingLinkRelativePosition.CorrelationMode.*;

public class CorrelationModesForUI {

    public static final Map<CorrelationMode, String> name;
    public static final Map<String,CorrelationMode> mode;

    static {
        name = new HashMap<>();
        mode = new HashMap<>();
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR, "None");
        mode.put("None", VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR);
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT, "None");
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR, "Uniform density");
        mode.put("Uniform density", VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR);
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT, "Uniform density");
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR, "Closest interferer");
        mode.put("Closest interferer", VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR);
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT, "Closest interferer");
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR, "Corr (victim link -> ILTx)");
        mode.put("Corr (victim link -> ILTx)", VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR);
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT, "Corr (victim link -> ILTx)");
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT, "Corr (victim link -> ILRx)");
        mode.put("Corr (victim link -> ILRx)", VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT);
        name.put(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR, "Corr (victim link -> ILRx)");
        name.put(VICTIM_DMA_INTERFERER_DMA_COR, "Cor. (victim BS ref.cell -> interfering BS ref. cell)");
        mode.put("Cor. (victim BS ref.cell -> interfering BS ref. cell)", VICTIM_DMA_INTERFERER_DMA_COR);
        name.put(VICTIM_DMA_INTERFERER_DMA_DYN, "Dyn. (victim BS ref.cell -> interfering BS ref.cell)");
        mode.put("Dyn. (victim BS ref.cell -> interfering BS ref.cell)", VICTIM_DMA_INTERFERER_DMA_DYN);
        name.put(VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT, "Cor. (interfering BS ref.cell)");
        mode.put("Cor. (interfering BS ref.cell)", VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT);
        name.put(VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR, "Cor. (interfering BS ref.cell)");
        name.put(VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT, "Dyn. (interfering BS ref.cell)");
        mode.put("Dyn. (interfering BS ref.cell)", VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT);
        name.put(VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR, "Dyn. (interfering BS ref.cell)");
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_NONE, "None (origin: victim BS ref.cell)");
        mode.put("None (origin: victim BS ref.cell)", VICTIM_DMA_INTERFERER_CLASSICAL_NONE);
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM, "Uniform density (origin: victim BS ref.cell)");
        mode.put("Uniform density (origin: victim BS ref.cell)", VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM);
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST, "Closest interferer (origin: victim BS ref.cell)");
        mode.put("Closest interferer (origin: victim BS ref.cell)", VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST);
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT, "Cor. (victim BS ref.cell -> ILTx)");
        mode.put("Cor. (victim BS ref.cell -> ILTx)", VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT);
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT, "Dyn. (victim BS ref.cell -> ILTx)");
        mode.put("Dyn. (victim BS ref.cell -> ILTx)", VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT);
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR, "Cor. (victim BS ref.cell -> ILRx)");
        mode.put("Cor. (victim BS ref.cell -> ILRx)", VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR);
        name.put(VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR, "Dyn. (victim BS ref.cell -> ILRx)");
        mode.put("Dyn. (victim BS ref.cell -> ILRx)", VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR);

    }

    public static List<CorrelationMode> getGroup( boolean victimIsDMA, boolean interfererIsDMA ) {
        List<CorrelationMode> modes = new ArrayList<>();
        if ( victimIsDMA ) {
            if ( interfererIsDMA ) {
                modes.add(VICTIM_DMA_INTERFERER_DMA_COR);
                modes.add(VICTIM_DMA_INTERFERER_DMA_DYN);
            } else {
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_NONE);
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM);
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST);
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT);
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT);
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR);
                modes.add(VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR);
            }
        } else {
            if ( interfererIsDMA ) {
                modes.add(VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR);
                modes.add(VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR);
            } else {
                modes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR);
                modes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR);
                modes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR);
                modes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR);
                modes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR);

            }
        }
        return modes;
    }

    public static boolean alignedSelection(boolean victimIsDMA, boolean interfererIsDMA, CorrelationMode selected) {
        List<CorrelationMode> group = getGroup(victimIsDMA, interfererIsDMA);
        return group.contains(align(selected));
    }

    public static int getIndex( boolean victimIsDMA, boolean interfererIsDMA, CorrelationMode selected ) {
        CorrelationMode align = align(selected);
        List<CorrelationMode> group = getGroup(victimIsDMA, interfererIsDMA);
        if ( group.contains( align )) {
            return group.indexOf(align);
        } else {
            return 0;
        }
    }

    private static CorrelationMode align( CorrelationMode selected ) {
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT ) {
            return VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT) {
            return VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR;
        } else
        if (selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT) {
            return VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT) {
            return VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT) {
            return VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT ) {
            return VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT) {
            return VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR;
        } else {
            return selected;
        }
    }

    public static CorrelationMode getMode( CorrelationMode selected, boolean vrRelative) {
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR ) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR : VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR : VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT;
        } else
        if (selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR : VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR : VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR : VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR ) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR : VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT;
        } else
        if ( selected == VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR) {
            return vrRelative ? VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR : VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT;
        } else {
            return selected;
        }
    }
}
