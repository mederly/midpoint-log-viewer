package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;

public class ExpressionLevelDefinition extends OutlineLevelDefinition<ExpressionNodeContent> {

	private ExpressionLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MatchResult<ExpressionNodeContent> matches(OutlineNode<ExpressionNodeContent> existingNode, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ EXPRESSION")) {
			return null;
		}
		ExpressionNodeContent content = new ExpressionNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return createMatchResult(existingNode, content, region, lineNumber, line, document);
	}

	public boolean isHeaderLast() {
		return true;
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ExpressionLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}
}
