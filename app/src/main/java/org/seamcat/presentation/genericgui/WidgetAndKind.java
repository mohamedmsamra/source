package org.seamcat.presentation.genericgui;

import java.awt.*;


public class WidgetAndKind {
	private Component widget;
	private WidgetKind kind;
	
   public WidgetAndKind(Component widget, WidgetKind kind) {
   	this.widget = widget;
   	this.kind = kind;
   }
   
   public Component getWidget() {
   	return widget;
   }
	
   public void setWidget(Component widget) {
   	this.widget = widget;
   }

	public WidgetKind getKind() {
   	return kind;
   }
	
   public void setKind(WidgetKind kind) {
   	this.kind = kind;
   }

    public void dispose() {
        widget = null;
    }
}
