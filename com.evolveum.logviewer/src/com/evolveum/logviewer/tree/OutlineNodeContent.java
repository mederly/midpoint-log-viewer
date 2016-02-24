package com.evolveum.logviewer.tree;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public abstract class OutlineNodeContent {
	
	protected OutlineNode<? extends OutlineNodeContent> owner;
	protected String defaultLabel;

	public OutlineNode<? extends OutlineNodeContent> getOwner() {
		return owner;
	}

	void setOwner(OutlineNode<? extends OutlineNodeContent> owner) {
		this.owner = owner;
	}
	
	public String getDefaultLabel() {
		return defaultLabel;
	}

	public void setDefaultLabel(String defaultLabel) {
		this.defaultLabel = defaultLabel;
	}
	
	public void setDefaultLabel(String defaultLabel, String stripOff) {
		int i = defaultLabel.indexOf(stripOff);
		if (i >= 0) {
			defaultLabel = defaultLabel.substring(0, i);
		}
		setDefaultLabel(defaultLabel);
	}


	@SuppressWarnings("unchecked")
	public TreeNode createTreeNode(Parser parser) {
		TreeNode treeNode = new TreeNode(owner, getDefaultLabel(), owner.getRegion().getOffset(), owner.getRegion().getLength());
		addChildNodes(parser, treeNode);
		return treeNode;
	}
	
	protected void addChildNodes(Parser parser, TreeNode treeNode) {
		for (OutlineNode<? extends OutlineNodeContent> node : owner.getAllChildren()) {
			treeNode.addChild(node.createTreeNode(parser));
		}
	}
	
	public String toString() {
		return getClass().getSimpleName();
	}
	
}
