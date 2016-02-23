package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.parsing.MatchResult;

public abstract class OutlineLevelDefinition<C extends OutlineNodeContent> {

	protected int level;
	protected OutlineLevelDefinition<? extends OutlineNodeContent> nextLevelDefinition;
	
	public int getLevel() {
		return level;
	}
	
	public abstract boolean isHeaderLast();
	
	public abstract MatchResult<C> matches(OutlineNode<C> outlineItem, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException;

	public OutlineLevelDefinition<? extends OutlineNodeContent> getNextLevelDefinition() {
		return nextLevelDefinition;
	}

	public void setNextLevelDefinition(OutlineLevelDefinition<? extends OutlineNodeContent> nextLevelDefinition) {
		this.nextLevelDefinition = nextLevelDefinition;
	}
}
