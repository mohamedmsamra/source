package org.seamcat.presentation.genericgui.item;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;


public class ComponentWrapperItem implements Item<Void> {
	
	private List<WidgetAndKind> widgets;
	private boolean relevant;
	
   public ComponentWrapperItem(Component c) {
   	widgets = new ArrayList<WidgetAndKind>();
   	widgets.add(new WidgetAndKind(c, WidgetKind.NONE));   	
   }
	
	public ComponentWrapperItem(WidgetAndKind... widgetAndKinds) {
		widgets = new ArrayList<WidgetAndKind>();
		Collections.addAll(widgets, widgetAndKinds);
	}

	protected void addWidgetAndKind( WidgetAndKind wak) {
		widgets.add( wak );
	}

	@Override
   public List<WidgetAndKind> getWidgets() {
	   return widgets;
   }

	@Override
   public Void getValue() {
	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public void setValue(Void value) {
		// TODO: Refactor Item hierarchy so we can have 
		// value-less items.
		throw new UnsupportedOperationException();
   }

	@Override
   public void setRelevant(boolean relevant) {
		this.relevant = relevant;
		for (WidgetAndKind wiak: widgets) {
			wiak.getWidget().setEnabled(relevant);
		}
   }

	@Override
   public boolean isRelevant() {
	   // TODO Auto-generated method stub
	   return relevant;
   }

}
