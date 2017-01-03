package org.seamcat.presentation.valuepreview;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.presentation.components.DiscreteFunction2TableModelAdapter;
import org.seamcat.presentation.components.UnwantedEmissionGraph2;
import org.seamcat.presentation.model.VictimCharacteristics;

import java.awt.*;


public class ValuePreviewableFunction2Adapter implements ValuePreviewable {
	
	private EmissionMask function;
	private String xAxisName;
	private String yAxisName;
	VictimCharacteristics victimCharacteristics;

	public ValuePreviewableFunction2Adapter(EmissionMask function) {
		this.function = function;
   }
	
	public ValuePreviewableFunction2Adapter axisNames(String xAxisName, String yAxisName) {
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
		return this;
	}

	public void setVictimCharacteristics(VictimCharacteristics victimCharacteristics) {
		this.victimCharacteristics = victimCharacteristics;
   }	

	@Override
	public boolean isDrawable() {
		return !function.isConstant();
	}

	@Override
   public Dimension getDrawablePreviewPreferredSize() {
		if (victimCharacteristics != null) {
			return new Dimension(350, 300);			
		}
		else {
			return new Dimension(300, 300);
		}
   }

	@Override
	public void drawValuePreview(Graphics2D g, Rectangle r) {
		if (function instanceof EmissionMaskImpl) {
			drawDiscreteFunction2((EmissionMaskImpl) function, g, r);
		}
		else {
			throw new RuntimeException("Function not drawable: " + function.getClass().getName());
		}
	}
	
	private void drawDiscreteFunction2(EmissionMaskImpl function, Graphics2D g, Rectangle r) {
	   DiscreteFunction2TableModelAdapter dataset = new DiscreteFunction2TableModelAdapter();
	   
		UnwantedEmissionGraph2 graph = new UnwantedEmissionGraph2(dataset);
		if (xAxisName != null && yAxisName != null) {
			graph.setLabels(xAxisName, yAxisName);			
		}
		
		if (victimCharacteristics != null) {
			graph.setVictimCharacteristics(victimCharacteristics.getVictimBandwidth(), victimCharacteristics.getFrequencyOffset(), victimCharacteristics.isShowACLR());
		}
		else {
			// Dont even ask... this is magical dust that must be applied to the graph.
			// Also, dataset.setDiscreteFunction2() apparently must be called *after* 
			// graph.setVictimCharacteristics().		
			graph.setVictimCharacteristics(-1, -1, false);
		}
	   dataset.setDiscreteFunction2(function);
		
	   graph.drawGraphToGraphics(g, r);
   }

	@Override
	public String getValuePreviewText() {
		return DiscreteFunction.pretty(function);
	}
}
