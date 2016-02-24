package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.parsing.MatchResult;

public class SummaryLevelDefinition extends OutlineLevelDefinition<SummaryNodeContent> {

	private SummaryLevelDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public boolean isHeaderLast() {
		return true;
	}

	@Override
	public MatchResult<SummaryNodeContent> matches(OutlineNode<SummaryNodeContent> outlineItem,
			int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (line.startsWith("###[ CLOCKWORK SUMMARY")) {
			SummaryNodeContent summaryNodeContent = new SummaryNodeContent();
			if (outlineItem.getContent() == null) {
				outlineItem.setContent(summaryNodeContent);
				outlineItem.setCoordinates(region, lineNumber, line, document);
				OutlineNode<SummaryNodeContent> node = new OutlineNode<SummaryNodeContent>(editorConfiguration, this.getLevel());
				return new MatchResult<>(node);
			} else {
				OutlineNode<SummaryNodeContent> node0 = new OutlineNode<SummaryNodeContent>(editorConfiguration, this.getLevel());
				node0.setContent(summaryNodeContent);
				node0.setCoordinates(region, lineNumber, line, document);
				OutlineNode<SummaryNodeContent> node = new OutlineNode<SummaryNodeContent>(editorConfiguration, this.getLevel());
				return new MatchResult<>(node0, node);
			}
		} else {
			return null;
		}
	}

	public static OutlineLevelDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new SummaryLevelDefinition(editorConfiguration).parseLevelFromLine(line);
	}

}
