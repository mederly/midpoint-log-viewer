package com.evolveum.logviewer.tree;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class MappingNodeContent extends OutlineNodeContent {

	MappingNodeContent previousMappingItem;
	
//	public MappingItemContent(IRegion region, int startLine, IDocument document, MappingItemContent previousMappingItem, String label, List<TreeNode> scriptsAndExpressions) {
//		super(region, startLine, document);
//		this.previousMappingItem = previousMappingItem;
//		
//		if (this.date != null && previousMappingItem != null && previousMappingItem.date != null) {
//			label += " [delta: " + (date.getTime() - previousMappingItem.date.getTime()) + " ms]";
//		}
//		
//		treeNode = new TreeNode(label, region);		
//		treeNode.addChildren(scriptsAndExpressions);
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
