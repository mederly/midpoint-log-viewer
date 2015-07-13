package com.evolveum.logviewer.outline;

import org.eclipse.jface.text.IRegion;

/**
 *
 * Any relevant log file section. See subclasses.
 * 
 * @author mederly
 */

public class DocumentItem {

	TreeNode treeNode;				// may be null if currently not existing
	IRegion region;
	int startLine = -1, endLine = -1;
	
	public DocumentItem(IRegion region, int startLine) {
		super();
		this.region = region;
		this.startLine = startLine;
	}
	
	public DocumentItem(IRegion region, int startLine, TreeNode node) {
		super();
		this.region = region;
		this.startLine = startLine;
		this.treeNode = node;
	}

	
}
