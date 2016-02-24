package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;

public abstract class OutlineLevelDefinition<C extends OutlineNodeContent> {

	protected EditorConfiguration editorConfiguration;
	protected int level;
	protected OutlineLevelDefinition<? extends OutlineNodeContent> nextLevelDefinition;
	
	public int getLevel() {
		return level;
	}
	
	public OutlineLevelDefinition(EditorConfiguration editorConfiguration) {
		this.editorConfiguration = editorConfiguration;
	}

	public abstract boolean isHeaderLast();
	
	public abstract MatchResult<C> matches(OutlineNode<C> outlineItem, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException;

	public OutlineLevelDefinition<? extends OutlineNodeContent> getNextLevelDefinition() {
		return nextLevelDefinition;
	}

	public void setNextLevelDefinition(OutlineLevelDefinition<? extends OutlineNodeContent> nextLevelDefinition) {
		this.nextLevelDefinition = nextLevelDefinition;
	}
	
	protected OutlineLevelDefinition<?> parseLevelFromLine(String line) {
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.out.println("Couldn't parse outline level definition: " + line);
		}
		int space2 = line.indexOf(' ', space1+1);
		if (space2 < 0) {
			System.out.println("Couldn't parse outline level definition: " + line);
		}
		int level;
		try {
			level = Integer.parseInt(line.substring(space2+1));
		} catch (NumberFormatException e) {
			System.out.println("Couldn't parse outline level definition: " + line + ": " + e);
			return null;
		}
		this.level = level;
		return this;
	}
	
	public void dumpAll() {
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
		System.out.println(toString());
		if (nextLevelDefinition != null) {
			nextLevelDefinition.dumpAll();
		}
	}
	
	public String toString() {
		return "L" + level + ": " + getClass().getSimpleName();
	}

	public EditorConfiguration getEditorConfiguration() {
		return editorConfiguration;
	}
	
	protected MatchResult<C> createMatchResult(OutlineNode<C> existingNode,
			C content, IRegion region, int lineNumber, String line, IDocument document) {
		if (isHeaderLast()) {
			return createMatchResultHeaderLast(existingNode, content, region, lineNumber, line, document);
		} else {
			return createMatchResultHeaderFirst(existingNode, content, region, lineNumber, line, document);
		}
	}
	
	protected MatchResult<C> createMatchResultHeaderLast(OutlineNode<C> existingNode,
			C content, IRegion region, int lineNumber, String line, IDocument document) {
		if (existingNode.getContent() == null) {
			existingNode.setContent(content);
			existingNode.setCoordinates(region, lineNumber, line, document);
			OutlineNode<C> node = new OutlineNode<C>(editorConfiguration, this.getLevel());
			return new MatchResult<>(node);
		} else {
			OutlineNode<C> node0 = new OutlineNode<C>(editorConfiguration, this.getLevel());
			node0.setContent(content);
			node0.setCoordinates(region, lineNumber, line, document);
			OutlineNode<C> node = new OutlineNode<C>(editorConfiguration, this.getLevel());
			return new MatchResult<>(node0, node);
		}
	}
	
	protected MatchResult<C> createMatchResultHeaderFirst(OutlineNode<C> existingNode,
			C content, IRegion region, int lineNumber, String line, IDocument document) {
		OutlineNode<C> node = new OutlineNode<>(this, content, region, lineNumber, line, document);
		return new MatchResult<>(node);
	}

}
