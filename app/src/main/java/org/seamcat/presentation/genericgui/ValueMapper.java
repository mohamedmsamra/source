package org.seamcat.presentation.genericgui;


public interface ValueMapper<ModelType, WidgetType> {
	public ModelType mapToModelValue(WidgetType widgetValue);
	public WidgetType mapToWidgetValue(ModelType modelValue);
}
