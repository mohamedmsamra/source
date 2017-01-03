package org.seamcat.presentation;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SeamcatIcons {

	public static final int IMAGE_SIZE_16x16 = 0;
	public static final int IMAGE_SIZE_32x32 = 1;
	public static final int IMAGE_SIZE_CUSTOM = 2;
	public static final int IMAGE_SIZE_16x16_DISABLED = 3;
	public static final int IMAGE_SIZE_TOOLBAR = 0;

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
	private static final String IMAGE_URL_PREFIX = STRINGLIST.getString("GENERAL_IMAGE_LOCATION");

	private static Map<String, ImageIcon> images = new HashMap<String, ImageIcon>();

	private static final Logger LOG = Logger.getLogger(SeamcatIcons.class);
	
	private static final String POSTFIX_16x16 = STRINGLIST.getString("GENERAL_POSTFIX_16x16_ICONS");
	private static final String POSTFIX_16x16_DISABLED = STRINGLIST.getString("GENERAL_POSTFIX_16x16_DISABLED_ICONS");
	private static final String POSTFIX_32x32 = STRINGLIST.getString("GENERAL_POSTFIX_32x32_ICONS");

    public static Image getImage(String identifier, int imageSize) {
		try {
			return SeamcatIcons.getImageIcon(identifier, imageSize).getImage();
		} catch (RuntimeException ex) {
			return null;
		}
	}

	public static ImageIcon getImageIcon(String identifier) {
		return SeamcatIcons.getImageIcon(identifier, IMAGE_SIZE_16x16);
	}

	public static ImageIcon getImageIcon(String identifier, int imageSize) {
		String postfix = null;
		switch (imageSize) {
			case IMAGE_SIZE_16x16: {
				postfix = POSTFIX_16x16;
				break;
			}
			case IMAGE_SIZE_32x32: {
				postfix = POSTFIX_32x32;
				break;
			}
			case IMAGE_SIZE_CUSTOM: {
				postfix = "";
				break;
			}
			case IMAGE_SIZE_16x16_DISABLED: {
				postfix = POSTFIX_16x16_DISABLED;
				break;
			}
			default: {
				throw new IllegalArgumentException("Invalid Image Size");
			}
		}
		ImageIcon icon = images.get(identifier + postfix);
		if (icon == null) {
			try {
				icon = new ImageIcon(SeamcatIcons.class.getResource(IMAGE_URL_PREFIX + Icons.getString(identifier) + postfix));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Loaded image: " + IMAGE_URL_PREFIX + Icons.getString(identifier) + postfix);
                }
			} catch (Exception e) {
				LOG.error("Unable to load image: " + identifier + " [" + IMAGE_URL_PREFIX + Icons.getString(identifier) + postfix + "] due to " + e);
			}
			images.put(identifier + postfix, icon);
		}
		return icon;
	}

	private SeamcatIcons() {

	}
}
