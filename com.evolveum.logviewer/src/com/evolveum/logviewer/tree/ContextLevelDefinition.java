package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.parsing.MatchResult;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class ContextLevelDefinition extends OutlineLevelDefinition<ContextNodeContent> {

	private ContextLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MatchResult<ContextNodeContent> matches(OutlineNode<ContextNodeContent> outlineNode, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {

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
		
		ContextNodeContent content = new ContextNodeContent();
		String line2 = DocumentUtils.getLine(document, lineNumber+1);
		content.parseWaveInfo(line2);
		content.parseLabelCore(line);
		
		//content.setLabelSuffix(ParsingUtils.suffix(document, lineNumber, newLine));
		content.setLabelSuffix("");
		
		if (outlineNode.getContent() == null) {
			outlineNode.setCoordinates(region, lineNumber, line, document);
			outlineNode.setContent(content);
			OutlineNode<ContextNodeContent> newNode = new OutlineNode<ContextNodeContent>(editorConfiguration, this.getLevel());
			return new MatchResult<>(newNode);			
		} else {
			OutlineNode<ContextNodeContent> newNode0 = new OutlineNode<ContextNodeContent>(editorConfiguration, this.getLevel());
			newNode0.setContent(content);
			newNode0.setCoordinates(region, lineNumber, line, document);
			OutlineNode<SummaryNodeContent> newNode = new OutlineNode<SummaryNodeContent>(editorConfiguration, this.getLevel());
			return new MatchResult<>(newNode0, newNode);
		}
	}

	public boolean isHeaderLast() {
		return true;
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ContextLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}

}
