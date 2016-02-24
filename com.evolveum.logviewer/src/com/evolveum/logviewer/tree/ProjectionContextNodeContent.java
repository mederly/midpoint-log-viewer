package com.evolveum.logviewer.tree;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class ProjectionContextNodeContent extends OutlineNodeContent {

	@SuppressWarnings("unchecked")
	public TreeNode createTreeNode(Parser parser) {
		TreeNode treeNode = new TreeNode(owner, "TODO", owner.getRegion().getOffset(), owner.getRegion().getLength());
		
		for (OutlineNode<? extends OutlineNodeContent> node : owner.getAllChildren()) {
			treeNode.addChild(node.createTreeNode(parser));
		}
		
		return treeNode;
	}
}
