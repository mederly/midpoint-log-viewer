package com.evolveum.logviewer.parsing;

import com.evolveum.logviewer.tree.OutlineNode;
import com.evolveum.logviewer.tree.OutlineNodeContent;

public class MatchResult<C extends OutlineNodeContent> {

	final private OutlineNode[] newNodes;

	public MatchResult(OutlineNode... newNodes) {
		if (newNodes.length == 0) {
			throw new IllegalStateException("No nodes in MatchResult");
		}
		this.newNodes = newNodes;
	}

	public OutlineNode<?> addNodesIntoChain(OutlineNode currentNode) {
		OutlineNode parent = currentNode.getParent();
		for (OutlineNode newNode : newNodes) {
			newNode.setParent(parent);
			newNode.setPreviousSibling(currentNode);
			currentNode.setNextSibling(newNode);
			currentNode = newNode;
		}
		return currentNode;
	}
}
