package org.seamcat.model.engines;

import org.seamcat.model.functions.Point2D;

/**
 * InterferenceCalculationListener
 */
public interface InterferenceCalculationListener {

	public void addTranslationResult(Point2D point);

	public void calculationComplete();

	public void calculationStarted();

	public boolean confirmContinueOnWarning(String warning);

	public void incrementCurrentProcessCompletionPercentage(int value);

	public void parameters(int numberTotalEvents, double probabilityTotalN, double ciLevel, double cniLevel, double iniLevel, double niLevel, double sensitivity);

	public void setCurrentProcessCompletionPercentage(int value);

	public void warningMessage(String warning);
}
