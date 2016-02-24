package com.evolveum.logviewer.tree;

import com.evolveum.logviewer.outline.TreeNode;
import com.evolveum.logviewer.parsing.Parser;

public class GeneralNodeContent extends OutlineNodeContent {
	
	public GeneralNodeContent(String label) {
		setDefaultLabel(label);
	}

	public String toString() {
		return super.toString() + ": " + getDefaultLabel();
	}
}
