package com.evolveum.logviewer.outline;

import org.eclipse.jface.text.IRegion;

public class MappingItem extends DocumentItem {

	public MappingItem(IRegion region, int startLine, TreeNode node) {
		super(region, startLine, node);
	}

}
