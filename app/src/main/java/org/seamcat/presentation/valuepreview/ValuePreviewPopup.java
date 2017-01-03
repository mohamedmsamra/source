package org.seamcat.presentation.valuepreview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;


class ValuePreviewPopup extends JComponent {
	private static final Insets INSETS = new Insets(3, 2, 3, 2);
	private static final Border BORDER = new LineBorder(new Color(128, 128,128));
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 220);
	private static final Color TEXT_COLOR = Color.black;
	
	private ValuePreviewable previewable;
	
   public ValuePreviewPopup(ValuePreviewable previewable) {
   	this.previewable = previewable;  	
   	setBorder(BORDER);
   }

	@Override
	public void paintComponent(Graphics gPlain) {
		Graphics2D g = (Graphics2D) gPlain;
	   if (previewable.isDrawable()) {
	   	drawBackground(g);
	   	previewable.drawValuePreview(g,subtractInsets(new Rectangle(getSize()), INSETS));
	   }
	   else {
	   	drawBackground(g);
	   	drawPreviewText(g);
	   }
	}

	private Rectangle subtractInsets(Rectangle r, Insets i) {
	   return new Rectangle(
	   		r.x + i.left, 
	   		r.y + i.top, 
	   		r.width - i.left - i.right, 
	   		r.height - i.top - i.bottom);
   }

	private void drawPreviewText(Graphics2D g) {
	   setUpPlatformFontRenderingHints(g);
	   g.setColor(TEXT_COLOR);
	   g.drawString(previewable.getValuePreviewText(), INSETS.left, INSETS.top + getFontMetrics(getFont()).getAscent());
   }

	/** Sets up rendering hints to make text appear like on the rest of the application
	 * (antialiasing and subpixel rendering)
	 */
	private void setUpPlatformFontRenderingHints(Graphics2D g) {
	   Toolkit tk = Toolkit.getDefaultToolkit();
	   Map map = (Map)(tk.getDesktopProperty("awt.font.desktophints"));
	   if (map != null) {
	   	g.addRenderingHints(map);
	   }
   }
			
	private void drawBackground(Graphics2D g) {
		g.setColor(BACKGROUND_COLOR);
      g.fillRect(0, 0, getWidth(), getHeight());
   }

	@Override
	public Dimension getMinimumSize() {
	   if (previewable.isDrawable()) {
	   	return addInsets(previewable.getDrawablePreviewPreferredSize(), INSETS);
	   }
	   else {
	   	FontMetrics fontMetrics = getFontMetrics(getFont());
			return new Dimension(
					SwingUtilities.computeStringWidth(fontMetrics, previewable.getValuePreviewText()) + INSETS.left + INSETS.right,
					fontMetrics.getAscent() + fontMetrics.getDescent() + INSETS.top + INSETS.bottom);
	   }
	}
	
	private static Dimension addInsets(Dimension dimension, Insets insets) {
		return new Dimension(
				dimension.width + insets.left + insets.right,
				dimension.height + insets.top + insets.bottom
				);
   }

	@Override
	public Dimension getPreferredSize() {
	   return getMinimumSize();
	}
}
