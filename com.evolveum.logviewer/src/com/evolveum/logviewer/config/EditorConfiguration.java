package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
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
	
	private final List<Instruction> instructions = new ArrayList<>();
	
	private final List<OutlineNodeDefinition<? extends OutlineNodeContent>> outlineNodeDefinitions = new ArrayList<>();
	private final List<OutlineNodeDefinition<? extends OutlineNodeContent>> outlineLevelDefinitionsSorted = new ArrayList<>();
	private int numberOfLevels;

	public List<Instruction> getInstructions() {
		return instructions;
	}
	
	public void addInstruction(Instruction instruction) {
		if (instruction == null) {		// just to simplify parsing
			return;
			
		}
		instructions.add(instruction);
		if (instruction instanceof MarkDelayInstruction) {
			MarkDelayInstruction mdi = (MarkDelayInstruction) instruction;
			switch (mdi.getSeverity()) {
			case IMarker.SEVERITY_ERROR: errorIfDelay = mdi.getMilliseconds(); break;
			case IMarker.SEVERITY_WARNING: warningIfDelay = mdi.getMilliseconds(); break;
			case IMarker.SEVERITY_INFO: infoIfDelay = mdi.getMilliseconds(); break;			
			}
		} else if (instruction instanceof OutlineNodeDefinition<?>) {
			outlineNodeDefinitions.add((OutlineNodeDefinition<?>) instruction);
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

	public Integer getWarningIfDelay() {
		return warningIfDelay;
	}

	public Integer getInfoIfDelay() {
		return infoIfDelay;
	}

	public String getSummary() {
		return instructions.size() + " instruction(s)";
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

	public <T extends Instruction> List<T> getInstructions(Class<T> clazz) {
		List<T> rv = new ArrayList<>();
		for (Instruction instruction : instructions) {
			if (clazz.isAssignableFrom(instruction.getClass())) {
				rv.add((T) instruction);
			}
		}
		return rv;
	}

}
