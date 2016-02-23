package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.tree.GeneralLevelDefinition;
import com.evolveum.logviewer.tree.OutlineLevelDefinition;
import com.evolveum.logviewer.tree.OutlineNodeContent;

public class EditorConfiguration {

	public Boolean componentNames = null;			// null = auto detect
	public boolean skipThreadProcessing = false;
	private Integer errorIfDelay;
	private Integer warningIfDelay;
	private Integer infoIfDelay;
	
	private List<ErrorMarkingInstruction> errorInstructions = new ArrayList<>();
	private List<OutlineLevelDefinition<? extends OutlineNodeContent>> outlineLevelDefinitions = new ArrayList<>();
	private OutlineLevelDefinition<? extends OutlineNodeContent> rootOutlineLevelDefinition;

	public List<ErrorMarkingInstruction> getErrorInstructions() {
		return errorInstructions;
	}
	
	public void addErrorInstruction(ErrorMarkingInstruction errorInstruction) {
		errorInstructions.add(errorInstruction);
	}
	
	public void addOutlineInstruction(OutlineLevelDefinition<? extends OutlineNodeContent> outlineInstruction) {
		if (outlineInstruction != null) {
			outlineLevelDefinitions.add(outlineInstruction);
		}
	}
	
	public void sortOutlineLevelDefinitions() {
		int remaining = outlineLevelDefinitions.size();
		int levelNumber = 0;
		OutlineLevelDefinition<? extends OutlineNodeContent> lastDefinition = null; 		
		while (remaining > 0) {
			for (OutlineLevelDefinition<? extends OutlineNodeContent> definition : outlineLevelDefinitions) {
				if (definition.getLevel() < 0) {
					System.err.println("Level less than zero: " + definition);
					return;
				}
				if (definition.getLevel() == levelNumber) {
					if (lastDefinition == null) {
						rootOutlineLevelDefinition = definition;
						lastDefinition = definition;
					} else {
						lastDefinition.setNextLevelDefinition(definition);
						lastDefinition = definition;
					}
					remaining--;
				}
			}
			levelNumber++;
		}
	}

	public OutlineLevelDefinition<? extends OutlineNodeContent> getRootOutlineInstruction() {
		return rootOutlineLevelDefinition;
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
