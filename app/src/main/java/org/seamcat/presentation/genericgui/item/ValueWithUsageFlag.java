package org.seamcat.presentation.genericgui.item;


/** Represents a value with a related "use" flag. This is for values whose use are optional, 
 * but which are stored even when not used. 
 */
public class ValueWithUsageFlag<T> {
	public ValueWithUsageFlag(boolean useValue, T value) {
		this.useValue = useValue;
		this.value = value;
	}
	
	public boolean useValue;
	public T value;
}

