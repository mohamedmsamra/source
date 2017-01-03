package org.seamcat.presentation.valuepreview;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;


/** An object which can have its value previewed, either as text or graphics.
 * If the object can be displayed as graphics, it should return true from isDrawable(), and
 * drawValuePreview() will then be called upon to do the actual drawing. Otherwise, 
 * getValuePreviewText() will be used. 
 */
public interface ValuePreviewable {
	public boolean isDrawable();
	public Dimension getDrawablePreviewPreferredSize();
	public void drawValuePreview(Graphics2D g, Rectangle r);
	public String getValuePreviewText();
}
