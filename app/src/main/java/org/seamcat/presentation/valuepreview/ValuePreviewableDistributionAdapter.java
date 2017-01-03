package org.seamcat.presentation.valuepreview;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.seamcat.model.distributions.UserDefinedDistributionImpl;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.StairDistributionImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.presentation.components.DiscreteFunctionGraph;
import org.seamcat.presentation.components.StairDistributionGraph;
import org.seamcat.presentation.components.DiscreteFunctionTableModelAdapter;
import org.seamcat.presentation.components.StairDistributionTableModelAdapter;


public class ValuePreviewableDistributionAdapter implements ValuePreviewable {
	
	AbstractDistribution distribution;

	public ValuePreviewableDistributionAdapter(AbstractDistribution distribution) {
		this.distribution = distribution;
   }

	@Override
	public boolean isDrawable() {
		return distribution instanceof UserDefinedDistributionImpl
				|| distribution instanceof StairDistributionImpl;
	}

	@Override
   public Dimension getDrawablePreviewPreferredSize() {
		return new Dimension(300, 300);
   }

	@Override
	public void drawValuePreview(Graphics2D g, Rectangle r) {
		if (distribution instanceof UserDefinedDistributionImpl) {
			drawContinuousDistribution((UserDefinedDistributionImpl) distribution, g, r);
		}
		else if (distribution instanceof StairDistributionImpl) {
			drawStairDistribution((StairDistributionImpl) distribution, g, r);
		}
		else {
			throw new RuntimeException("Distribution not drawable: " + distribution.getClass().getName());
		}
	}

	private void drawStairDistribution(StairDistributionImpl distribution, Graphics2D g, Rectangle r) {
	   StairDistributionTableModelAdapter dataset = new StairDistributionTableModelAdapter();
	   dataset.setPoints(distribution);
	   new StairDistributionGraph(dataset).drawGraphToGraphics(g, r);
   }

	private void drawContinuousDistribution(UserDefinedDistributionImpl distribution, Graphics2D g, Rectangle r) {
	   DiscreteFunctionTableModelAdapter dataset = new DiscreteFunctionTableModelAdapter();
	   dataset.setDiscreteFunction((DiscreteFunction) distribution.getCdf());
	   new DiscreteFunctionGraph(dataset, "Value", "Probability").drawGraphToGraphics(g, r) ;
   }

	@Override
	public String getValuePreviewText() {
		return distribution.toString();
	}	
}
