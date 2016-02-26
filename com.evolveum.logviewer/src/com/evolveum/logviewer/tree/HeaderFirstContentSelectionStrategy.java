package com.evolveum.logviewer.tree;

import java.util.Map;
import java.util.TreeMap;

public class HeaderFirstContentSelectionStrategy implements ContentSelectionStrategy {

	@Override
	public Result computeContent(OutlineNode<?> outlineNode, TreeMap<Integer, OutlineNode<?>> availableNodes) {

		final int thisLevel = outlineNode.getLevel();
		int currentLine = outlineNode.getStartLine();
		for (;;) {
			Map.Entry<Integer, OutlineNode<?>> nextEntry = availableNodes.higherEntry(currentLine);
			if (nextEntry == null || nextEntry.getValue().getLevel() == thisLevel) {
				return new Result(availableNodes.subMap(outlineNode.getStartLine(), false, currentLine, true), currentLine);
			}
			currentLine = nextEntry.getKey();
		}
	}

}
