package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;

public class ProjectionContextLevelDefinition extends OutlineLevelDefinition<ProjectionContextNodeContent> {

	private ProjectionContextLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MatchResult<ProjectionContextNodeContent> matches(OutlineNode<ProjectionContextNodeContent> existingNode, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("    PROJECTION ShadowType Discr")) {
			return null;
		}
		ProjectionContextNodeContent content = new ProjectionContextNodeContent();
		content.setDefaultLabel(line.substring(4));
		return createMatchResult(existingNode, content, region, lineNumber, line, document);
	}

	public boolean isHeaderLast() {
		return true;
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new ProjectionContextLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}
}
