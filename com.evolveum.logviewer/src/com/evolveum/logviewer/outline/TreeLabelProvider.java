package com.evolveum.logviewer.outline;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider implements ITableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof TreeNode)) {
			return "Unknown type: " + element.getClass(); 
		}
		TreeNode tn = (TreeNode) element;
		switch (columnIndex) {
		case 0: return tn.getLabel();
		case 1: return String.valueOf(tn.getDate());
		case 2: return String.valueOf(tn.getStartLine());
		case 3: return tn.getDelta();
		case 4: return tn.getSum();
		}
		return null;
	}

	public String getText(Object element) {
		
		return ((TreeNode) element).getLabel();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
