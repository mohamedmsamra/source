package org.seamcat.model.scenariocheck;

import org.seamcat.model.Workspace;

public interface ScenarioCheck {

	ScenarioCheckResult check(Workspace workspace);
}