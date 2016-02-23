package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.parsing.MatchResult;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class ContextLevelDefinition extends OutlineLevelDefinition<ContextNodeContent> {

	@Override
	public MatchResult<ContextNodeContent> matches(OutlineNode<ContextNodeContent> outlineItem, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {

		boolean newLine;
		if (line.contains("---[ SYNCHRONIZATION")) {
			line = line.substring(line.indexOf("---["));
			newLine = false;
		} else if (line.startsWith("---[ PROJECTOR") || 
				line.startsWith("---[ CLOCKWORK") ||
				line.startsWith("---[ preview")) {
			newLine = true;
		} else {
			return null;
		}
		
		if (outlineItem.getContent() != null) {
			System.err.println("Unexpected content in " + outlineItem);
		}
		
		outlineItem.setCoordinates(region, lineNumber, line, document);

		ContextNodeContent content = new ContextNodeContent();
		String line2 = DocumentUtils.getLine(document, lineNumber+1);
		content.parseWaveInfo(line2);
		content.setLabelCore(line.substring(5));
		content.setLabelSuffix(ParsingUtils.suffix(document, lineNumber, newLine));
		
		outlineItem.setContent(content);
		
		return new MatchResult<>();
	}

	public boolean isHeaderLast() {
		return true;
	}

}
