package org.seamcat.model.engines;

public interface InterferenceCriterionType {
	public static final int CI = 1;
	public static final int CNI = 2;
	public static final int IN = 4;
	public static final int INI = 3;

	public int getInterferenceCriterionType();
}
