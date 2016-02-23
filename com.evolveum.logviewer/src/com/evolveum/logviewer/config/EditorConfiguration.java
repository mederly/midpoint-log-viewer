package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

public class EditorConfiguration {

	public Boolean componentNames = null;			// null = auto detect
	public boolean skipThreadProcessing = false;
	private Integer errorIfDelay;
	private Integer warningIfDelay;
	private Integer infoIfDelay;
	
	private List<ErrorMarkingInstruction> errorInstructions = new ArrayList<>();
	private List<OutlineInstruction> outlineInstructions = new ArrayList<>();

	public List<ErrorMarkingInstruction> getErrorInstructions() {
		return errorInstructions;
	}
	
	public void addErrorInstruction(ErrorMarkingInstruction errorInstruction) {
		errorInstructions.add(errorInstruction);
	}
	
	public List<OutlineInstruction> getOutlineInstructions() {
		return outlineInstructions;
	}
	
	public void addOutlineInstruction(OutlineInstruction outlineInstruction) {
		outlineInstructions.add(outlineInstruction);
	}
	
	public OutlineInstruction getRootOutlineInstruction() {
		OutlineInstruction root = null;
		for (OutlineInstruction oi : outlineInstructions) {
			if (root == null || oi.getLevel() < root.getLevel()) {
				root = oi;
			}
		}
		return root;
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
