package com.evolveum.logviewer.outline;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider implements ITableLabelProvider {
	
	public static final int LABEL = 0;
	public static final int DATE = 1;
	public static final int DELTA = 2;
	public static final int SUM = 3;
	public static final int LINE = 4;
	public static final int THREAD = 5;

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof TreeNode)) {
			return "Unknown type: " + element.getClass(); 
		}
		TreeNode tn = (TreeNode) element;
		switch (columnIndex) {
		case LABEL: return tn.getLabel();
		case DATE: 
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
			if (tn.getDate() != null) { 
				return format.format(tn.getDate());
			} else {
				return "";
			}
		case DELTA: return tn.getDelta();
		case SUM: return tn.getSum();
		case LINE: return String.valueOf(tn.getStartLine());
		case THREAD: return tn.getThread();
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
