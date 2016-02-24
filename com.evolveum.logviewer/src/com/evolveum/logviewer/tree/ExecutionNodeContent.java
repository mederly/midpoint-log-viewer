package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class ExecutionNodeContent extends OutlineNodeContent {

//	public ExecutionItemContent(IRegion region, int startLine, TreeNode node) {
//		super(region, startLine, node);
//	}
	
	@SuppressWarnings("unchecked")
	public TreeNode createTreeNode(Parser parser) {
		TreeNode treeNode = new TreeNode(owner, "TODO", owner.getRegion().getOffset(), owner.getRegion().getLength());
		
		for (OutlineNode<? extends OutlineNodeContent> node : owner.getAllChildren()) {
			treeNode.addChild(node.createTreeNode(parser));
		}
		
		return treeNode;
	}

}
