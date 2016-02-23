package com.evolveum.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.parsing.MatchResult;

public class SummaryLevelDefinition extends OutlineLevelDefinition<SummaryNodeContent> {

	@Override
	public boolean isHeaderLast() {
		return true;
	}

	@Override
	public MatchResult<SummaryNodeContent> matches(OutlineNode<SummaryNodeContent> outlineItem,
			int lineNumber, String line, IRegion region, IDocument document) throws BadLocationException {
		if (line.startsWith("###[ CLOCKWORK SUMMARY")) {
			SummaryNodeContent summaryNodeContent = new SummaryNodeContent();
			outlineItem.setContent(summaryNodeContent);
			return new MatchResult<>();
		} else {
			return null;
		}
	}

}
