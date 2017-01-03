package org.seamcat.presentation.valuepreview;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class ValuePreviewPopupHandler {

    private ValuePreviewable previewable;
    private Popup popup;
    private JComponent component;
    private MouseListener listener;

    ValuePreviewPopupHandler(JComponent component) {
        this.component = component;
        listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hidePopup();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hidePopup();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (ValuePreviewPopupHandler.this.component.isEnabled()) {
                    showPopup(e);
                }
            }
        };
        component.addMouseListener(listener);
    }

    void setPreviewable(ValuePreviewable previewable) {
        this.previewable = previewable;
    }

    void showPopup(final MouseEvent e) {
        hidePopup();
        if (previewable != null) {
            Point popupLocation = new Point(e.getX()+10, component.getHeight()/2);
            SwingUtilities.convertPointToScreen(popupLocation, component);
            Component tooltipComponent = new ValuePreviewPopup(previewable);
            popup = PopupFactory.getSharedInstance().getPopup(component, tooltipComponent, popupLocation.x, popupLocation.y);
            tooltipComponent.setVisible(true);
            tooltipComponent.setSize(tooltipComponent.getPreferredSize());
            popup.show();
        }
    }

    void hidePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    public void dispose() {
        component.removeMouseListener(listener);
    }
}
