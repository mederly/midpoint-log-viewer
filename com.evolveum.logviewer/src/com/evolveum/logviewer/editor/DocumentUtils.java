package com.evolveum.logviewer.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class DocumentUtils {
	
	public static String getLine(IDocument document, int number) {
		try {
			IRegion region = document.getLineInformation(number);
			String line = document.get(region.getOffset(), region.getLength());
			return line;
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}


}
