package com.evolveum.logviewer.parsing;

import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.outline.TreeNode;

public class ExecutionItem extends DocumentItem {

	public ExecutionItem(IRegion region, int startLine, TreeNode node) {
		super(region, startLine, node);
	}

}
