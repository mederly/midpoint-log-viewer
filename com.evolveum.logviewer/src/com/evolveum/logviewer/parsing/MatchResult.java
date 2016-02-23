package com.evolveum.logviewer.parsing;

import com.evolveum.logviewer.tree.OutlineNode;
import com.evolveum.logviewer.tree.OutlineNodeContent;

public class MatchResult<C extends OutlineNodeContent> {

	final private OutlineNode<C> newDocumentItem;

	public MatchResult() {
		this.newDocumentItem = null;
	}
	
	public MatchResult(OutlineNode<C> newDocumentItem) {
		this.newDocumentItem = newDocumentItem;
	}

	public OutlineNode<C> getNewDocumentItem() {
		return newDocumentItem;
	}
	
	
	
	
}
