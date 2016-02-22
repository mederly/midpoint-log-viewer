package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.List;

public class EditorConfiguration {

	public Boolean componentNames = null;			// null = auto detect
	public boolean skipThreadProcessing = false;
	private Integer errorIfDelay;
	private Integer warningIfDelay;
	private Integer infoIfDelay;
	
	private List<ErrorMarkingInstruction> errorInstructions = new ArrayList<>();

	public List<ErrorMarkingInstruction> getErrorInstructions() {
		return errorInstructions;
	}
	
	public void addErrorInstruction(ErrorMarkingInstruction errorInstruction) {
		errorInstructions.add(errorInstruction);
	}
	
	public Integer getErrorIfDelay() {
		return errorIfDelay;
	}

	public void setErrorIfDelay(Integer errorIfDelay) {
		this.errorIfDelay = errorIfDelay;
	}

	public Integer getWarningIfDelay() {
		return warningIfDelay;
	}

	public void setWarningIfDelay(Integer warningIfDelay) {
		this.warningIfDelay = warningIfDelay;
	}
	
	public Integer getInfoIfDelay() {
		return infoIfDelay;
	}

	public void setInfoIfDelay(Integer infoIfDelay) {
		this.infoIfDelay = infoIfDelay;
	}

	public String getSummary() {
		return errorInstructions.size() + " error instruction(s)";
	}
}
