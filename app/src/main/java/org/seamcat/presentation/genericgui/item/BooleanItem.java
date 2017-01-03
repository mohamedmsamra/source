package org.seamcat.presentation.genericgui.item;

import org.seamcat.presentation.SeamcatIcons;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.util.Assert;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class BooleanItem extends AbstractItem<Boolean, Object> {

    private JCheckBox valueWidget;

    public JCheckBox getValueWidget() {
        return valueWidget;
    }

    public BooleanItem() {
    }

    public BooleanItem label(String label) {
        super.label(label);
        return this;
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = new ArrayList<WidgetAndKind>();

        valueWidget = new JCheckBox(getLabel());
        if ( getToolTipText() != null ) {
            valueWidget.setToolTipText( getToolTipText() );
        }
        valueWidget.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireItemChanged();
            }
        });
        widgets.add(new WidgetAndKind(valueWidget, WidgetKind.LABEL));
        if ( getInformation() != null ) {
            JLabel info = new JLabel(SeamcatIcons.getImageIcon("SEAMCAT_ICON_INFORMATION", SeamcatIcons.IMAGE_SIZE_TOOLBAR));
            info.setToolTipText( getInformation() );
            widgets.add(new WidgetAndKind(info, WidgetKind.LABEL));
        }
        return widgets;
    }

    @Override
    public Boolean getValue() {
        return valueWidget.isSelected();
    }

    @Override
    public void setValue(Boolean value) {
        Assert.notNull("Item value is null", value);
        valueWidget.setSelected(value);
    }

    @Override
    public void updateLabel(String label) {
        super.updateLabel(label);
        valueWidget.setText( label );
    }
}
