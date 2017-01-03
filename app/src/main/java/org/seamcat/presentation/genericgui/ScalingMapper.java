package org.seamcat.presentation.genericgui;


public class ScalingMapper implements ValueMapper<Double, Double> {

	private double displayScale;
	
	public static ScalingMapper PERCENT = new ScalingMapper(100.0);
	
	public ScalingMapper(double displayScale) {
		this.displayScale = displayScale;
	}
	
	@Override
   public Double mapToModelValue(Double widgetValue) {
      return widgetValue/displayScale;
   }

	@Override
   public Double mapToWidgetValue(Double modelValue) {
      return modelValue*displayScale;
   }

}
