package com.evolveum.logviewer.outline;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class MappingItem extends DocumentItem {

	MappingItem previousMappingItem;
	
	public MappingItem(IRegion region, int startLine, IDocument document, MappingItem previousMappingItem, String label, List<TreeNode> scriptsAndExpressions) {
		super(region, startLine, document);
		this.previousMappingItem = previousMappingItem;
		
		if (this.date != null && previousMappingItem != null && previousMappingItem.date != null) {
			label += " [delta: " + (date.getTime() - previousMappingItem.date.getTime()) + " ms]";
		}
		
		treeNode = new TreeNode(label, region);		
		treeNode.addChildren(scriptsAndExpressions);
	}

}
