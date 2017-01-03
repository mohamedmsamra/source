package org.seamcat.model.scenariocheck;

import org.seamcat.model.Workspace;

public class GeneralScenarioCheck extends AbstractCheck {

	public GeneralScenarioCheck(String prefix) {
		super();
		result.setCheckName(prefix+"General Scenario Check");
	}

	private int calculateMaxEvents(Workspace workspace) {
		int total = workspace.getInterferenceLinks().size();

		int vectorCount = (int) (3 * Math.pow(total, 2) + 3 * total + 4);
		final double byteConversionValue = 1024 * 1024; // Mb

		final Runtime r = Runtime.getRuntime();
		r.gc();
		final double memoryAvailable = (r.maxMemory() - (r.totalMemory() - r
		      .freeMemory()))
		      / byteConversionValue * 0.85d;

		return (int) (memoryAvailable / (vectorCount * 8.0d / byteConversionValue));
	}

	public ScenarioCheckResult check(Workspace workspace) {
        if (!checkMemory(workspace)) {
			addErrorMsg("SEAMCAT does not appear to have enough memory available to "
			      + "complete the requested simulation.<br>SEAMCAT estimates that it can only do around"
			      + " "
			      + calculateMaxEvents(workspace)
			      + " events of your scenario with the current "
			      + " available memory.");
		}
		return result;
	}

	private boolean checkMemory(Workspace workspace) {
		int total = workspace.getInterferenceLinks().size();

		int vectorCount = (int) (3 * Math.pow(total, 2) + 3 * total + 4);

		final double byteConversionValue = 1024 * 1024; // Mb

		double memoryNeeded = vectorCount
		      * workspace.getSimulationControl().numberOfEvents() * 8
		      / byteConversionValue;
		final Runtime r = Runtime.getRuntime();
		r.gc();
		final double memoryAvailable = (r.maxMemory() - (r.totalMemory() - r
		      .freeMemory()))
		      / byteConversionValue * 0.85d;
		if (memoryNeeded > memoryAvailable) {
			return false;
		} else {
			return true;
		}
	}
}
