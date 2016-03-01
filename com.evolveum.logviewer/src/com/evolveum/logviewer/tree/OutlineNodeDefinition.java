package com.evolveum.logviewer.tree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.config.Instruction;

public abstract class OutlineNodeDefinition<C extends OutlineNodeContent> implements Instruction {

	protected EditorConfiguration editorConfiguration;
	protected int physicalLevel;
	protected int normalizedLevel;
	protected OutlineNodeDefinition<? extends OutlineNodeContent> nextDefinition;
	
	public int getLevel() {
		return normalizedLevel;
	}
	
	public int getPhysicalLevel() {
		return physicalLevel;
	}

	public void setNormalizedLevel(int normalizedLevel) {
		this.normalizedLevel = normalizedLevel;
	}

	public OutlineNodeDefinition(EditorConfiguration editorConfiguration) {
		this.editorConfiguration = editorConfiguration;
	}
	
	public abstract ContentSelectionStrategy getContentSelectionStrategy();
	
	public abstract C recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) throws BadLocationException;

	public OutlineNodeDefinition<? extends OutlineNodeContent> getNextDefinition() {
		return nextDefinition;
	}

	public void setNextDefinition(OutlineNodeDefinition<? extends OutlineNodeContent> nextDefinition) {
		this.nextDefinition = nextDefinition;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration config, String line) {
		if (line.startsWith("%outline startup")) {
			return GenericNodeDefinition.parseFromLineAsStartupDefinition(config, line);
		} else if (line.startsWith("%outline test-part")) {
			return GenericNodeDefinition.parseFromLineAsTestPartDefinition(config, line);
		} else if (line.startsWith("%outline test")) {
			return GenericNodeDefinition.parseFromLineAsTestDefinition(config, line);
		} else if (line.startsWith("%outline operation-summary")) {
			return SummaryNodeDefinition.parseFromLine(config, line);
		} else if (line.startsWith("%outline operation-context")) {
			return ContextNodeDefinition.parseFromLine(config, line);
		} else if (line.startsWith("%outline projection-context")) {
			return ProjectionContextNodeDefinition.parseFromLine(config, line);
		} else if (line.startsWith("%outline mapping")) {
			return MappingNodeDefinition.parseFromLine(config, line);
		} else if (line.startsWith("%outline expression")) {
			return ExpressionNodeDefinition.parseFromLine(config, line);
		} else if (line.startsWith("%outline script")) {
			return ScriptNodeDefinition.parseFromLine(config, line);
		} else if (line.startsWith("%outline execution")) {
			return ExecutionNodeDefinition.parseFromLine(config, line);
		} else {
			return null;
		}
	}
	
	private static final Pattern PATTERN = Pattern.compile("\\%outline\\s+\\S+\\s+(?<level>\\d+)\\s*(#.*)?");
	
	protected static Integer getLevel(String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			throw new IllegalStateException("This is not an outline definition: " + line);
		}
		return Integer.parseInt(matcher.group("level"));
	}
	
	protected OutlineNodeDefinition<?> parseFromLine(String line) {
		this.physicalLevel = getLevel(line);
		return this;
	}
	
	public void dumpAll() {
		for (int i = 0; i < normalizedLevel; i++) {
			System.out.print("  ");
		}
		System.out.println(toString());
		if (nextDefinition != null) {
			nextDefinition.dumpAll();
		}
	}
	
	public String toString() {
		return "L" + physicalLevel + " (" + normalizedLevel + "): " + getClass().getSimpleName();
	}

	public EditorConfiguration getEditorConfiguration() {
		return editorConfiguration;
	}
	
}
