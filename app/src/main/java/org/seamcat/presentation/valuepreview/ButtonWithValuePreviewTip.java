package org.seamcat.presentation.valuepreview;

import javax.swing.*;


public class ButtonWithValuePreviewTip extends JButton {

	private ValuePreviewPopupHandler popupHandler = new ValuePreviewPopupHandler(this);

	public ButtonWithValuePreviewTip() {
   }   

	public ButtonWithValuePreviewTip(String text) {
		super(text);
   }   

	public void setPreviewable(ValuePreviewable previewable) {
		popupHandler.setPreviewable(previewable);
	}

    public void dispose() {
        popupHandler.dispose();
    }
}
