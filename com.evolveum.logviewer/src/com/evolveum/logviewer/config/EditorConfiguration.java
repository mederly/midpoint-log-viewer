package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.tree.GenericNodeDefinition;
import com.evolveum.logviewer.tree.OutlineNodeDefinition;
import com.evolveum.logviewer.tree.OutlineNodeContent;

public class EditorConfiguration {

	public Boolean componentNames = null;			// null = auto detect
	public boolean skipThreadProcessing = false;
	private Integer errorIfDelay;
	private Integer warningIfDelay;
	private Integer infoIfDelay;
	
	private final List<ErrorMarkingInstruction> errorInstructions = new ArrayList<>();
	private final List<OutlineNodeDefinition<? extends OutlineNodeContent>> outlineNodeDefinitions = new ArrayList<>();
	private final List<OutlineNodeDefinition<? extends OutlineNodeContent>> outlineLevelDefinitionsSorted = new ArrayList<>();
	private int numberOfLevels;

	public List<ErrorMarkingInstruction> getErrorInstructions() {
		return errorInstructions;
	}
	
	public void addErrorInstruction(ErrorMarkingInstruction errorInstruction) {
		errorInstructions.add(errorInstruction);
	}
	
	public void addOutlineInstruction(OutlineNodeDefinition<? extends OutlineNodeContent> outlineInstruction) {
		if (outlineInstruction != null) {
			outlineNodeDefinitions.add(outlineInstruction);
		}
	}
	
	public void sortOutlineLevelDefinitions() {
		
		if (outlineNodeDefinitions.isEmpty()) {
			return;
		}
		
		int levelNumber = getLowestPhysicalLevel();
		
		int remaining = outlineNodeDefinitions.size();
		
		OutlineNodeDefinition<? extends OutlineNodeContent> lastDefinition = null;
		int normalizedLevelNumber = 1;
		
		while (remaining > 0) {
			boolean thisLevelEmpty = true;
			for (OutlineNodeDefinition<? extends OutlineNodeContent> definition : outlineNodeDefinitions) {
				if (definition.getPhysicalLevel() == levelNumber) {
					thisLevelEmpty = false;
					if (lastDefinition == null) {
						lastDefinition = definition;
					} else {
						lastDefinition.setNextDefinition(definition);
						lastDefinition = definition;
					}
					definition.setNormalizedLevel(normalizedLevelNumber);
					outlineLevelDefinitionsSorted.add(definition);
					remaining--;
				}
			}
			levelNumber++;
			if (!thisLevelEmpty) {
				normalizedLevelNumber++;
			}
		}
		numberOfLevels = normalizedLevelNumber-1;
		System.out.println("Outline level definitions (" + outlineNodeDefinitions.size() + "):");
		getRootOutlineInstruction().dumpAll();
	}

	private Integer getLowestPhysicalLevel() {
		int levelNumber = -1;
		
		for (OutlineNodeDefinition<? extends OutlineNodeContent> definition : outlineNodeDefinitions) {
			if (levelNumber < 0 || definition.getPhysicalLevel() < levelNumber) {
				levelNumber = definition.getPhysicalLevel();
			}
		}
		return levelNumber;
	}

	public OutlineNodeDefinition<? extends OutlineNodeContent> getRootOutlineInstruction() {
		return outlineLevelDefinitionsSorted.isEmpty() ? null : outlineLevelDefinitionsSorted.get(0);
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

	public List<OutlineNodeDefinition<? extends OutlineNodeContent>> getOutlineLevelDefinitions(int level) {
		List<OutlineNodeDefinition<? extends OutlineNodeContent>> rv = new ArrayList<>();
		for (OutlineNodeDefinition<? extends OutlineNodeContent> def : outlineNodeDefinitions) {
			if (def.getLevel() == level) {
				rv.add(def);
			}
		}
		return rv;
	}

	public Integer getNextOutlineLevel(int level) {
		
		OutlineNodeDefinition<? extends OutlineNodeContent> def = getRootOutlineInstruction();
		while (def != null) {
			if (def.getLevel() > level) {
				return def.getLevel();
			}
			def = def.getNextDefinition();
		}
		return null;
		
	}

	public List<OutlineNodeDefinition<?>> getAllOutlineLevelDefinitions() {
		return outlineLevelDefinitionsSorted;
	}

	public int getNumberOfLevels() {
		return numberOfLevels;
	}

}
