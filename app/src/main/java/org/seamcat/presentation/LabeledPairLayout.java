package org.seamcat.presentation;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;

public class LabeledPairLayout implements LayoutManager {

	public static final String FIELD = "field";
	public static final String LABEL = "label";

	protected static final int maxWidth(Collection<JComponent> comps) {
		int width = 0;
		for (JComponent comp : comps) {
			int localWidth = (int) comp.getPreferredSize().getWidth();
			width = Math.max(width, localWidth);
		}
		return width;
	}

	protected Vector<JComponent> fields = new Vector<JComponent>();
	protected Vector<JComponent> labels = new Vector<JComponent>();
	protected int xGap = 2;

	protected int yGap = 2;

	public void addLayoutComponent(String s, Component c) {
		if (!(c instanceof JComponent)) {
			throw new IllegalArgumentException(
			      "Layoutcomponents must be of type JComponent");
		}
		if (s.equals(LABEL)) {
			labels.addElement((JComponent) c);
		} else if (s.equals(FIELD)) {
			fields.addElement((JComponent) c);
		}
	}

	public int getXGap() {
		return xGap;
	}

	public int getYGap() {
		return yGap;
	}

	public void layoutContainer(Container c) {
		Insets insets = c.getInsets();
		int labelWidth = LabeledPairLayout.maxWidth(labels);

		int yPos = insets.top;

		for (Iterator<JComponent> lIt = labels.iterator(), fIt = fields
		      .iterator(); lIt.hasNext() && fIt.hasNext();) {
			JComponent label = lIt.next();
			JComponent field = fIt.next();
			int height = Math.max(label.getPreferredSize().height, field
			      .getPreferredSize().height);
			label.setBounds(insets.left, yPos, labelWidth, height);
			field
			      .setBounds(insets.left + labelWidth + xGap, yPos,
			            c.getSize().width
			                  - (labelWidth + xGap + insets.left + insets.right),
			            height);
			yPos += height + yGap;
		}
	}

	public Dimension minimumLayoutSize(Container c) {
		Insets insets = c.getInsets();
		int labelWidth = LabeledPairLayout.maxWidth(labels);
		int fieldWidth = LabeledPairLayout.maxWidth(fields);
		int yPos = insets.top;

		for (Iterator<JComponent> lIt = labels.iterator(), fIt = fields
		      .iterator(); lIt.hasNext() && fIt.hasNext();) {
			JComponent label = lIt.next();
			JComponent field = fIt.next();
			int height = Math.max(label.getPreferredSize().height, field
			      .getPreferredSize().height);
			yPos += height + yGap;
		}
		yPos += 2 * yGap;
		return new Dimension(labelWidth + fieldWidth, yPos);
	}

	public Dimension preferredLayoutSize(Container c) {
		return minimumLayoutSize(c);
	}

	public void removeLayoutComponent(Component c) {
		labels.remove(c);
		fields.remove(c);
	}

	public void setXGap(int xGap) {
		this.xGap = xGap;
	}

	public void setYGap(int yGap) {
		this.yGap = yGap;
	}
}