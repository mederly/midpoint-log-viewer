package com.evolveum.logviewer.tree;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class GeneralNodeContent extends OutlineNodeContent {
	
	private String label;
	
	public GeneralNodeContent(String label) {
		this.label = label;
	}

	@SuppressWarnings("unchecked")
	public TreeNode createTreeNode(Parser parser) {
		TreeNode treeNode = new TreeNode(label, owner.getRegion().getOffset(), owner.getRegion().getLength());
		
		for (OutlineNode<? extends OutlineNodeContent> node : owner.getAllChildren()) {
			treeNode.addChild(node.createTreeNode(parser));
		}
		
		return treeNode;
	}
}
