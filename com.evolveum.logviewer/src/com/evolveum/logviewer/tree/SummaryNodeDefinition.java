package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;

public class SummaryNodeDefinition extends OutlineNodeDefinition<SummaryNodeContent> {

	private SummaryNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	@Override
	public SummaryNodeContent recognize(int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (line.startsWith("###[ CLOCKWORK SUMMARY")) {
			SummaryNodeContent summaryNodeContent = new SummaryNodeContent();
			summaryNodeContent.setDefaultLabel("### Execution summary ###");
			return summaryNodeContent;
		} else {
			return null;
		}
	}

	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new SummaryNodeDefinition(editorConfiguration).parseFromLine(line);
	}

}
