package com.evolveum.logviewer.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import com.evolveum.logviewer.config.FoldingInstruction;
import com.evolveum.logviewer.config.FoldingInstruction.Kind;
import com.evolveum.logviewer.config.FoldingInstruction.Type;
import com.evolveum.logviewer.config.Scope;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class FoldingExecutor {

	public static void applyFoldingInstructions(List<FoldingInstruction> instructions, IDocument document,
			ProjectionAnnotationModel pam) {
		
		try {
			for (FoldingInstruction instr : instructions) {
				doIt(document, instr, pam);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public static void doIt(IDocument document, FoldingInstruction instruction, ProjectionAnnotationModel pam) throws BadLocationException {
		
		String entry = null, header = null; 
		
		for (int lineNumber = 0; lineNumber < document.getNumberOfLines(); lineNumber++) {
			
			IRegion region = document.getLineInformation(lineNumber);
			String line = document.get(region.getOffset(), region.getLength());

			if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
				break;
			}
			
			final boolean isLogEntryStart = ParsingUtils.isLogEntryStart(line);
			if (isLogEntryStart) {
				header = line;
				entry = ParsingUtils.getLogEntry(document, lineNumber);
			}
			
			if (instruction.getKind() == Kind.LINE) {
				if (instruction.getCondition().matches(line, entry, header, Scope.LINE)) {
					execute(instruction, lineNumber, region, pam);
				}
			} else {
				if (isLogEntryStart && instruction.getCondition().matches(line, entry, header, Scope.ENTRY)) {
					execute(instruction, lineNumber, region, pam);
				}
			}
		}
	}

	private static void execute(FoldingInstruction instruction, int lineNumber, IRegion region, ProjectionAnnotationModel pam) {
		Iterator iter = pam.getAnnotationIterator(region.getOffset(), region.getLength(), false, true);
		if (!iter.hasNext()) {
			return;
		}
		Object o = iter.next();
		if (o instanceof Annotation) {
			if (instruction.getType() == Type.COLLAPSE) {
				pam.collapse((Annotation) o);
			} else {
				pam.expand((Annotation) o);
			}
		} else {
			System.out.println("Warn: Unknown annotation for line " + lineNumber + ": " + o);
		}
	}

}
