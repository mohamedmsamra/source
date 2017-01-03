package org.seamcat.calculator;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;

public class ImageIconBorder extends AbstractBorder {

	private class ButtonBorder extends AbstractBorder {

		private ImageIcon disabledIcon;
		private ImageIcon icon;

		private ButtonBorder(ImageIcon _icon, ImageIcon _disabledIcon) {
			super();
			icon = _icon;
			if (_disabledIcon == null) {
				disabledIcon = icon;
			} else {
				disabledIcon = _disabledIcon;
			}
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 0, icon.getIconWidth());
		}

		@Override
		public Insets getBorderInsets(Component c, Insets insets) {
			insets.set(0, 0, 0, 0);
			return insets;
		}

		@Override
		public Rectangle getInteriorRectangle(Component c, int x, int y,
		      int width, int height) {
			return super.getInteriorRectangle(c, x, y, (width - icon
			      .getIconWidth()), height);
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width,
		      int height) {
			Graphics2D gr = (Graphics2D) g;
			ImageIcon img;
			if (c.isEnabled()) {
				img = icon;
			} else {
				img = disabledIcon;
			}
			gr.drawImage(img.getImage(), x + width - icon.getIconWidth() + 2, y,
			      null);

		}

	}

	private CompoundBorder border;

    public ImageIconBorder(JComponent owner, ImageIcon icon,
	      ImageIcon disabledIcon) {
		super();
        ButtonBorder buttonBorder = new ButtonBorder(icon, disabledIcon);
		border = new CompoundBorder(owner.getBorder(), buttonBorder);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return border.getBorderInsets(c);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		return border.getBorderInsets(c, insets);
	}

	@Override
	public Rectangle getInteriorRectangle(Component c, int x, int y, int width,
	      int height) {
		return border.getInteriorRectangle(c, x, y, width, height);
	}

	@Override
	public boolean isBorderOpaque() {
		return border.isBorderOpaque();
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
	      int height) {
		border.paintBorder(c, g, x, y, width, height);
	}

}
