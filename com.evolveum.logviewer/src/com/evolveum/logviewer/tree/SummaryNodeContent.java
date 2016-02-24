package com.evolveum.logviewer.tree;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class SummaryNodeContent extends OutlineNodeContent {

	@Override
	public TreeNode createTreeNode(Parser parser) {
		if (owner == null) {
			return null;
		}
		if (owner.getRegion() == null) {
			System.err.println("null region in " + owner);
			return null;
		}
		TreeNode treeNode = new TreeNode(owner, "### Execution summary ###", owner.getRegion().getOffset(), owner.getRegion().getLength());
		
		for (OutlineNode<? extends OutlineNodeContent> node : owner.getAllChildren()) {
			treeNode.addChild(node.createTreeNode(parser));
		}
		
		return treeNode;

	}
	

}
