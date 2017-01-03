package org.seamcat.presentation.genericgui;


public class ClampMapper implements ValueMapper<Double, Double> {
	
	double minimumValue;
	double maximumValue;

	
   public ClampMapper(double minimumValue, double maximumValue) {
   	this.minimumValue = minimumValue;
   	this.maximumValue = maximumValue;
   }
	
	@Override
   public Double mapToModelValue(Double widgetValue) {
		if (widgetValue.doubleValue() > maximumValue) {
			return maximumValue;
		}
		if (widgetValue.doubleValue() < minimumValue) {
			return minimumValue;
		}
	   return widgetValue;
   }

	@Override
   public Double mapToWidgetValue(Double modelValue) {
	   return modelValue;
   } 
}
