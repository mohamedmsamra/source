package org.seamcat.presentation.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class ValidatorDocument extends PlainDocument {

	public static enum Type {
		CHARACTERS, FLOAT_DELIMITERS, INTEGERS, MATHS, NEGATE, NEWLINE, PUNCTUATION
	}

	protected final static char[] CHARACTERS = new char[] { ' ', 'a', 'b', 'c',
	      'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
	      'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
	      'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
	      'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '@' };
	protected final static char[] FLOAT_DELIMITERS = new char[] { '.', ',' };
	protected final static char[] INTEGERS = new char[] { '0', '1', '2', '3',
	      '4', '5', '6', '7', '8', '9' };
	protected final static char[] MATHS = new char[] { '=', '+', '-', '*', '/',
	      '^' };
	protected final static char[] NEGATE = new char[] { '-' };
	protected final static char[] NEWLINE = new char[] { '\n' };
	protected final static char[] PUNCTUATION = new char[] { '.', ',', ';', ':',
	      '!', '"', '#', '$', '%', '&', '/', '(', ')', '?', };

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static boolean validateString(String s, char[] allowedChars) {
		if (s.length() > 0) {
			char[] chars = s.toCharArray();
			int y;
			for (int x = 0; x < chars.length; x++) {
				searchChar: {
					for (y = 0; y < allowedChars.length; y++) {
						if (allowedChars[y] == chars[x]) {
							break searchChar;
						}
					}
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private final Type[] allowedTypes;

	// Override rules
	private boolean overriding = false;

	ValidatorDocument(Type... allowedTypes) {
		this.allowedTypes = allowedTypes;
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a)
	      throws BadLocationException {
		if (validateString(str)) {
			super.insertString(offs, str, a);
		}
	}

	public boolean isOverriding() {
		return overriding;
	}

	public void setOverriding(boolean overriding) {
		this.overriding = overriding;
	}

	private boolean validateString(String s) {
		boolean ok = false;

		if (!overriding) {
			for (Type type : allowedTypes) {
				switch (type) {
					case INTEGERS: {
						ok = ValidatorDocument.validateString(s, INTEGERS);
						break;
					}
					case FLOAT_DELIMITERS: {
						ok = ValidatorDocument.validateString(s, FLOAT_DELIMITERS);
						break;
					}
					case CHARACTERS: {
						ok = ValidatorDocument.validateString(s, CHARACTERS);
						break;
					}
					case NEWLINE: {
						ok = ValidatorDocument.validateString(s, NEWLINE);
						break;
					}
					case PUNCTUATION: {
						ok = ValidatorDocument.validateString(s, PUNCTUATION);
						break;
					}
					case MATHS: {
						ok = ValidatorDocument.validateString(s, MATHS);
						break;
					}
					case NEGATE: {
						ok = ValidatorDocument.validateString(s, NEGATE);
						break;
					}
					default: {
						throw new IllegalStateException(
						      "Unsupported validation type encountered: " + type);
					}
				}
				if (ok) {
					break;
				}
			}
		} else {
			ok = true;
		}
		return ok;
	}
}