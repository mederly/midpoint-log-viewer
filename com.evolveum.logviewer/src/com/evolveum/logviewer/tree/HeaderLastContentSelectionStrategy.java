package com.evolveum.logviewer.tree;

import java.util.Map;
import java.util.TreeMap;

public class HeaderLastContentSelectionStrategy implements ContentSelectionStrategy {

	@Override
	public Result computeContent(final OutlineNode<?> outlineNode, TreeMap<Integer, OutlineNode<?>> availableNodes) {

		final int thisLevel = outlineNode.getLevel();
		int currentLine = outlineNode.getStartLine();
		for (;;) {
			Map.Entry<Integer, OutlineNode<?>> previousEntry = availableNodes.lowerEntry(currentLine);
			if (previousEntry == null || previousEntry.getValue().getLevel() == thisLevel) {
				return new Result(availableNodes.subMap(currentLine, true, outlineNode.getStartLine(), false), outlineNode.getStartLine());
			}
			currentLine = previousEntry.getKey();
		}
	}

}
