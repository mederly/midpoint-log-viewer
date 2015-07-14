package com.evolveum.logviewer.outline;

import java.util.Date;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.editor.DocumentUtils;

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
	
	Date date;
	
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
	
	public DocumentItem(IRegion region, int startLine, IDocument document) {
		this(region, startLine);
		if (startLine > 1) {
			String previousLine = DocumentUtils.getLine(document, startLine-1);
			date = ParsingUtils.parseDate(previousLine);
		}

	}

	
}
