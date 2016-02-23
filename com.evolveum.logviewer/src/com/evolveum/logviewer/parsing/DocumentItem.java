package com.evolveum.logviewer.parsing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.OutlineInstruction;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.TreeNode;

/**
 *
 * Any relevant log file section. See subclasses.
 * 
 * @author mederly
 */

public class DocumentItem {
	
	List<DocumentItem> children = new ArrayList<>();

	TreeNode treeNode;				// may be null if currently not existing
	IRegion region;
	int startLine = -1, endLine = -1;
	
	Date date;
	
	public DocumentItem() {
	}
	
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

	final private OutlineInstruction outlineInstruction;

	public GenericDocumentItem(OutlineInstruction outlineInstruction) {
		this.outlineInstruction = outlineInstruction;
	}

	public OutlineInstruction getOutlineInstruction() {
		return outlineInstruction;
	}

	
	public void parseLine(int lineNumber, String line, IRegion region2) {
		if (outlineInstruction.matches(line)) {
			
		}
		
	}
}
