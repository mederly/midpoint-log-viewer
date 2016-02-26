package com.evolveum.logviewer.tree;

import java.util.NavigableMap;
import java.util.TreeMap;

public interface ContentSelectionStrategy {

	Result computeContent(OutlineNode<?> outlineNode, TreeMap<Integer, OutlineNode<?>> availableNodes);
	
	final ContentSelectionStrategy HEADER_FIRST = new HeaderFirstContentSelectionStrategy();
	final ContentSelectionStrategy HEADER_LAST = new HeaderLastContentSelectionStrategy();
	
	public class Result {
		private final NavigableMap<Integer, OutlineNode<?>> content;
		private final int continueParsingAfter;
		
		public Result(NavigableMap<Integer, OutlineNode<?>> content, int continueParsingAfter) {
			super();
			this.content = content;
			this.continueParsingAfter = continueParsingAfter;
		}
		
		public NavigableMap<Integer, OutlineNode<?>> getContent() {
			return content;
		}
		public int getContinueParsingAfter() {
			return continueParsingAfter;
		}
		
	}
}
