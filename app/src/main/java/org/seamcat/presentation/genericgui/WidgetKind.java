package org.seamcat.presentation.genericgui;


public enum WidgetKind {
	/** Widgets serving the purpose of identifying the value to the user.
	 * Typically a JLabel.
	 */
	LABEL,

	/** Widgets with which the user may interact to modify the item value.
	 * Typically entry fields, checkboxes, combos, buttons.
	 */
	VALUE,
	
	/** Widgets which indicate the unit of the value. Typically a JLabel.
	 */
	UNIT,
	
	/** Widgets with allow the user to preview the value, when the value widget
	 * does not allow the user to observe the actual value. Used in connection with
	 * values edited by clicking a button to open a sub-dialog, such as functions, 
	 * or distributions.
	 */
	VALUE_PREVIEW, 
	
	/** Widgets which are not of any other kind, and which should be treated
	 * in any special way. The layout mechanism should give such widgets the 
	 * widest most spacious layout.
	 */
	NONE
}
