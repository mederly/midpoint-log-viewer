package com.evolveum.logviewer.outline;

import org.eclipse.jface.text.IRegion;

public class ExecutionItem extends DocumentItem {

	public ExecutionItem(IRegion region, int startLine, TreeNode node) {
		super(region, startLine, node);
	}

}
