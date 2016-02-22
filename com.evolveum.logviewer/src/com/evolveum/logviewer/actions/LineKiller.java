package com.evolveum.logviewer.actions;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import com.evolveum.logviewer.config.ConfigurationParser;
import com.evolveum.logviewer.config.KillInstruction;
import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.ParsingUtils;

public class LineKiller {

	public static void applyKillInstructions(IDocument document) {
		List<KillInstruction> instructions = ConfigurationParser.getAllKillInstructions(document);
		System.out.println("Applying " + instructions.size() + " killing instructions.");
		if (instructions.isEmpty()) {
			return;
		}
		int killed = 0;
		try {
			int lineNumber = 0;
			for (;;) {
				
				int lines = document.getNumberOfLines();
				if (lineNumber >= lines) {
					break;
				}
				
				String line;
				for (;;) {
					line = DocumentUtils.getLine(document, lineNumber);
					if (ParsingUtils.isLogEntryStart(line)) {
						break;
					}
					lineNumber++;
					if (lineNumber >= lines) {
						return;						// next log entry was not found
					}
				}
				
				int logStartLineNumber = lineNumber;
				String logStartLine = line;
				
				StringBuilder logEntry = new StringBuilder();
				for (;;) {
					logEntry.append(line).append("\n");
					lineNumber++;
					if (lineNumber >= lines) {
						break;
					}
					line = DocumentUtils.getLine(document, lineNumber);
					if (ParsingUtils.isLogEntryStart(line)) {
						break;
					}
				}
				
				// lineNumber is at next log entry
				// logEntry contains whole entry
				
				for (KillInstruction instr : instructions) {
					if (instr.appliesTo(logStartLine, logEntry.toString())) {
						int start = document.getLineOffset(logStartLineNumber);
						int end;
						if (lineNumber >= lines) {
							end = document.getLength();
						} else {
							end = document.getLineOffset(lineNumber);
						}
						System.out.println("Killing lines #" + (logStartLineNumber+1) + " to #" + (lineNumber+1));
						document.replace(start, end-start, "");
						killed++;
						
						lineNumber = logStartLineNumber;			// restart at deleted line
						break;										// skipping other instructions
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		System.out.println("Killed " + killed + " line(s)");
		
	}

}
