package org.seamcat.presentation;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Icons {

	private static final String BUNDLE_NAME = "org.seamcat.presentation.seamcat_icons";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
	      .getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	private Icons() {
	}
}
