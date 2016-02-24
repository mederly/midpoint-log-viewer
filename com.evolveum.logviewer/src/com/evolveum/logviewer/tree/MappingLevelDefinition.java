package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;

public class MappingLevelDefinition extends OutlineLevelDefinition<MappingNodeContent> {

	private MappingLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public MatchResult<MappingNodeContent> matches(OutlineNode<MappingNodeContent> existingNode, int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ MAPPING")) {
			return null;
		}
		MappingNodeContent content = new MappingNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return createMatchResult(existingNode, content, region, lineNumber, line, document);
	}

	public boolean isHeaderLast() {
		return true;
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new MappingLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}
}
