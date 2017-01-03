package org.seamcat.model.scenariocheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScenarioCheckResult {

	public enum Outcome {
		FAILED, OK
	}

	private String checkName;
	private final List<String> messageList = new ArrayList<String>();
	private Outcome outcome = Outcome.OK;

	public ScenarioCheckResult() {
	}

	public ScenarioCheckResult(Outcome out, String name, List<String> messages) {
		outcome = out;
		checkName = name;
		messageList.addAll(messages);
	}

	public void addMessage(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message cant be null");
		}
		messageList.add(message);
	}

	public void clearMessages() {
		messageList.clear();
	}

	public String getCheckName() {
		return checkName != null ? checkName : "Undefined checkName";
	}

	public List<String> getMessages() {
		return Collections.unmodifiableList(messageList);
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public void removeMessage(String message) {
		messageList.remove(message);
	}

	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	public void setOutcome(Outcome outcome) {
		if (outcome == null) {
			throw new IllegalArgumentException("Outcome cant be null");
		}
		this.outcome = outcome;
	}
}