package org.seamcat.presentation.valuepreview;

import javax.swing.*;


public class LabelWithValuePreviewTip extends JLabel {
	private ValuePreviewPopupHandler popupHandler = new ValuePreviewPopupHandler(this);
	
   public void setPreviewable(ValuePreviewable previewable) {
   	popupHandler.setPreviewable(previewable);
   }

    public void dispose() {
        popupHandler.dispose();
    }
}
