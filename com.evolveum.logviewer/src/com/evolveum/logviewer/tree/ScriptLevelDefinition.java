package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;

public class ScriptLevelDefinition extends OutlineLevelDefinition<ScriptNodeContent> {

	private ScriptLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MatchResult<ScriptNodeContent> matches(OutlineNode<ScriptNodeContent> existingNode, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ SCRIPT")) {
			return null;
		}
		ScriptNodeContent content = new ScriptNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return createMatchResult(existingNode, content, region, lineNumber, line, document);
	}

	public boolean isHeaderLast() {
		return true;
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ScriptLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}
}
