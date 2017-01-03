package org.seamcat.presentation.genericgui.item;

import org.seamcat.presentation.genericgui.WidgetAndKind;

import java.util.List;


public interface Item<ValueType> {

	/** Invoking multiple times must return the same widget instances.
	 */
	List<WidgetAndKind> getWidgets();
	
	/** Returns the model value represented in the UI by the item. This value
	 * may not yet be stored in the model object.
	 */
	ValueType getValue();

	/** Sets the model value represented in the UI by the item. This value
	 * may not yet be stored in the model object.
	 */
	void setValue(ValueType value);
	
	void setRelevant(boolean relevant);
	boolean isRelevant();
}
