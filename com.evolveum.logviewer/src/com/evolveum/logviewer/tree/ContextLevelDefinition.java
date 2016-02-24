package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.parsing.MatchResult;

public class ContextLevelDefinition extends OutlineLevelDefinition<ContextNodeContent> {

	private ContextLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MatchResult<ContextNodeContent> matches(OutlineNode<ContextNodeContent> existingNode, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {

		if (line.contains("---[ SYNCHRONIZATION")) {
			line = line.substring(line.indexOf("---["));
		} else if (line.startsWith("---[ PROJECTOR") || 
				line.startsWith("---[ CLOCKWORK") ||
				line.startsWith("---[ preview")) {
			// continue
		} else {
			return null;
		}
		
		ContextNodeContent content = new ContextNodeContent();
		String line2 = DocumentUtils.getLine(document, lineNumber+1);
		content.parseWaveInfo(line2);
		content.parseLabelCore(line);
		
		//content.setLabelSuffix(ParsingUtils.suffix(document, lineNumber, newLine));
		content.setLabelSuffix("");
		
		return createMatchResult(existingNode, content, region, lineNumber, line, document);
	}

	public boolean isHeaderLast() {
		return true;
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ContextLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}

}
