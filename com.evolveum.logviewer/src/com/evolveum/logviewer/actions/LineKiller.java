package com.evolveum.logviewer.actions;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import com.evolveum.logviewer.config.EditorConfiguration;
import com.evolveum.logviewer.config.KillInstruction;
import com.evolveum.logviewer.config.KillInstruction.Kind;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class LineKiller {

	public static void applyKillInstructions(IDocument document, EditorConfiguration configuration) {
		List<KillInstruction> instructions = configuration.getInstructions(KillInstruction.class);
		System.out.println("Applying " + instructions.size() + " killing instructions.");
		if (instructions.isEmpty()) {
			return;
		}
		try {
			int killed = doIt(document, instructions);
			System.out.println("Killed " + killed + " line(s)");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public static int doIt(IDocument document, List<KillInstruction> instructions) throws BadLocationException {
		
		int killed = 0;
		
		String entry = null, header = null; 
		
		for (int lineNumber = 0; lineNumber < document.getNumberOfLines(); lineNumber++) {
			
			String line = DocumentUtils.getLine(document, lineNumber);

			if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
				break;
			}
			
			if (ParsingUtils.isLogEntryStart(line)) {
				header = line;
				entry = ParsingUtils.getLogEntry(document, lineNumber);
			}
			
			for (KillInstruction instr : instructions) {
				if (instr.matches(line, entry, header)) {
					final int startLine, followingLine;
					if (instr.getKind() == Kind.LINE) {
						startLine = lineNumber;
						followingLine = lineNumber+1; 
					} else {
						startLine = lineNumber;
						followingLine = ParsingUtils.findLastLogEntryLine(document, startLine) + 1;
					}
					int start = document.getLineOffset(startLine);
					int end;
					if (followingLine >= document.getNumberOfLines()) {
						end = document.getLength();
					} else {
						end = document.getLineOffset(followingLine);
					}
					System.out.println("Killing lines #" + startLine + " to #" + (followingLine-1));
					document.replace(start, end-start, "");
					killed++;
					
					lineNumber = startLine-1;			// restart at deleted line
					break;
				}
			}
		}
		return killed;
	}


}
