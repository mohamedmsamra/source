package org.seamcat.util;


public class Assert {
	public static void notNull(String message, Object object) {
		if (object == null) {
			throw new AssertionException(message);
		}
	}

	public static void isTrue(String message, boolean condition) {
		if (!condition) {
			throw new AssertionException(message);
		}
   }
}
