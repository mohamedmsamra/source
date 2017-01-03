package org.seamcat.presentation.valuepreview;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.functions.Function;
import org.seamcat.presentation.components.DiscreteFunctionGraph;
import org.seamcat.presentation.components.DiscreteFunctionTableModelAdapter;

import java.awt.*;


public class ValuePreviewableFunctionAdapter implements ValuePreviewable {
	
	private Function function;
	private String xAxisName;
	private String yAxisName;

	public ValuePreviewableFunctionAdapter(Function function) {
		this.function = function;
   }
	
	public ValuePreviewableFunctionAdapter axisNames(String xAxisName, String yAxisName) {
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
		return this;
	}

	@Override
	public boolean isDrawable() {
		return !function.isConstant();
	}

	@Override
   public Dimension getDrawablePreviewPreferredSize() {
		return new Dimension(300, 300);
   }

	@Override
	public void drawValuePreview(Graphics2D g, Rectangle r) {
		if (function instanceof DiscreteFunction) {
			drawDiscreteFunction((DiscreteFunction) function, g, r);			
		}
		else {
			throw new RuntimeException("Function not drawable: " + function.getClass().getName());
		}
	}

	private void drawDiscreteFunction(DiscreteFunction function, Graphics2D g, Rectangle r) {
	   DiscreteFunctionTableModelAdapter dataset = new DiscreteFunctionTableModelAdapter();
	   dataset.setDiscreteFunction(function);
	   String actualXAxisName = xAxisName != null ? xAxisName : "X";
		String actualYAxisName = yAxisName != null ? yAxisName : "Y";
		new DiscreteFunctionGraph(dataset, actualXAxisName, actualYAxisName).drawGraphToGraphics(g, r) ;
   }

	@Override
	public String getValuePreviewText() {
		return DiscreteFunction.pretty(function);
	}
}
