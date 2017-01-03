package org.seamcat.presentation.valuepreview;


public class ValuePreviewTextUtil {

	private static final int VALUE_PREVIEW_MAX_LENGTH = 16;
	private final static String DOTS = "...";

	public static String previewLabelText(String s) {
		if (s == null) {
			return s;
		}
		
		if (s.length() > VALUE_PREVIEW_MAX_LENGTH ) {
			return "[" + s.substring(0, VALUE_PREVIEW_MAX_LENGTH-DOTS.length())+DOTS + "]";
		}
		else {
			return "[" + s + "]";
		}		
	}
}
