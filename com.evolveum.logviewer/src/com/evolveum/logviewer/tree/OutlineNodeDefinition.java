package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public abstract class OutlineNodeDefinition<C extends OutlineNodeContent> {

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
	
	public abstract C recognize(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException;

	public OutlineNodeDefinition<? extends OutlineNodeContent> getNextDefinition() {
		return nextDefinition;
	}

	public void setNextDefinition(OutlineNodeDefinition<? extends OutlineNodeContent> nextDefinition) {
		this.nextDefinition = nextDefinition;
	}
	
	protected OutlineNodeDefinition<?> parseFromLine(String line) {
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
		this.physicalLevel = level;
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
