package org.seamcat.presentation.genericgui;


public class IdentityMapper<T> implements ValueMapper<T, T> {

	@Override
   public T mapToModelValue(T widgetValue) {
	   return widgetValue;
   }

	@Override
   public T mapToWidgetValue(T modelValue) {
	   return modelValue;
   }
}
