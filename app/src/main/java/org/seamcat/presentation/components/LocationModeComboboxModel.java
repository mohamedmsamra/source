package org.seamcat.presentation.components;

import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.presentation.systems.generic.CorrelationModesForUI;

import javax.swing.*;
import java.util.List;

public class LocationModeComboboxModel extends DefaultComboBoxModel {

    private List<InterferingLinkRelativePosition.CorrelationMode> values = CorrelationModesForUI.getGroup(false, false);

    @Override
    public Object getElementAt(int index) {
        return CorrelationModesForUI.name.get(values.get(index));
    }

    @Override
    public int getIndexOf(Object anObject) {
        return values.indexOf(CorrelationModesForUI.mode.get(anObject)) ;
    }

    @Override
    public int getSize() {
        return values.size();
    }

    public void setValues( List<InterferingLinkRelativePosition.CorrelationMode> modes ) {
        this.values = modes;
    }

    public InterferingLinkRelativePosition.CorrelationMode getValue( int index) {
        return values.get(index );
    }

}