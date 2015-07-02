package com.evolveum.logviewer.outline;

import org.eclipse.jface.viewers.LabelProvider;

public class TreeLabelProvider extends LabelProvider {

	public String getText(Object element) {
		if (!(element instanceof TreeNode)) {
			return "Unknown type: " + element.getClass(); 
		}
		return ((TreeNode) element).getLabel();
	}
}
