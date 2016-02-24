package com.evolveum.logviewer.tree;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public abstract class OutlineNodeContent {
	
	public abstract TreeNode createTreeNode(Parser parser);
	
	protected OutlineNode<? extends OutlineNodeContent> owner;

	public OutlineNode<? extends OutlineNodeContent> getOwner() {
		return owner;
	}

	void setOwner(OutlineNode<? extends OutlineNodeContent> owner) {
		this.owner = owner;
	}
	
	public String toString() {
		return getClass().getSimpleName();
	}
	
}
