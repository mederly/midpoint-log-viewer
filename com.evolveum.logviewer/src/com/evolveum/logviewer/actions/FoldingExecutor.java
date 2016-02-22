package com.evolveum.logviewer.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import com.evolveum.logviewer.config.FoldingInstruction;
import com.evolveum.logviewer.config.FoldingInstruction.Type;
import com.evolveum.logviewer.config.FoldingInstruction.When;
import com.evolveum.logviewer.outline.MyContentOutlinePage;

public class FoldingExecutor {

	public static void applyFoldingInstructions(List<FoldingInstruction> instructions, IDocument document,
			ProjectionAnnotationModel pam) {
		
		try {
			for (FoldingInstruction instr : instructions) {
				int lines = document.getNumberOfLines();
				for (int lineNumber = 0; lineNumber < lines; lineNumber++) {
					IRegion region = document.getLineInformation(lineNumber);
					String line = document.get(region.getOffset(), region.getLength());
					if (line.contains(MyContentOutlinePage.CONFIG_MARKER)) {
						break;
					}
					if (instr.when == When.CONTAINING && line.contains(instr.string)
							|| (instr.when == When.NOT_CONTAINING && !line.contains(instr.string))) {
						Iterator iter = pam.getAnnotationIterator(region.getOffset(), region.getLength(), false, true);
						if (!iter.hasNext()) {
//							System.out.println("Warn: no annotation for line " + (lineNumber+1)
//									+ " was found, skipping folding instruction");
							continue;
						}
						Object o = iter.next();
						if (o instanceof Annotation) {
							if (instr.type == Type.COLLAPSE) {
								pam.collapse((Annotation) o);
							} else {
								pam.expand((Annotation) o);
							}
						} else {
							System.out.println("Warn: Unknown annotation for line " + lineNumber + ": " + o);
						}
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
