package com.evolveum.logviewer.actions;

import java.util.ArrayList;
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
			doIt(document, instructions);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	static class Region {
		int startLine;
		int followingLine;		// exclusive
		public Region(int startLine, int followingLine) {
			this.startLine = startLine;
			this.followingLine = followingLine;
		}
	}
	
	public static void doIt(IDocument document, List<KillInstruction> instructions) throws BadLocationException {
		
		String previousEntry = null, entry = null, header = null;
		
		final List<Region> regionsToKill = new ArrayList<>(); 
		
		for (int lineNumber = 0; lineNumber < document.getNumberOfLines(); lineNumber++) {
			
			String line = DocumentUtils.getLine(document, lineNumber);

			if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
				break;
			}
			
			if (ParsingUtils.isLogEntryStart(line)) {
				header = line;
				previousEntry = entry;
				entry = ParsingUtils.getLogEntry(document, lineNumber);
			}
			
			for (KillInstruction instr : instructions) {
				if (!instr.matches(line, entry, header)) {
					continue;
				}
				if (instr.getKind() == Kind.DUPLICATE_ENTRY && (previousEntry == null || entry == null || !entry.equals(previousEntry))) {
					continue;
				}
				if (instr.getKind() == Kind.LINE) {
					regionsToKill.add(new Region(lineNumber, lineNumber+1));
				} else {
					regionsToKill.add(new Region(lineNumber, ParsingUtils.findLastLogEntryLine(document, lineNumber) + 1));						
				}
				break;
			}
		}
		
		System.out.println("Going to remove " + regionsToKill.size() + " region(s)");
		int removedLines = 0;
		for (int i = 0; i < regionsToKill.size(); ) {
			Region region = regionsToKill.get(i);
			int startLine = region.startLine;
			int followingLine = region.followingLine;
			while (++i < regionsToKill.size() && regionsToKill.get(i).startLine == followingLine) {
				followingLine = regionsToKill.get(i).followingLine;
			}
			kill(document, startLine, followingLine, removedLines);
			removedLines += (followingLine-startLine);
			System.out.print("Removed " + i + " out of " + regionsToKill.size() + " regions; " + removedLines + " lines\r");
		}
		System.out.println();
	}

	private static void kill(IDocument document, int startLine, int followingLine, int alreadyRemovedLines) throws BadLocationException {
		startLine -= alreadyRemovedLines;
		followingLine -= alreadyRemovedLines;
		
		int start = document.getLineOffset(startLine);
		int end;
		if (followingLine >= document.getNumberOfLines()) {
			end = document.getLength();
		} else {
			end = document.getLineOffset(followingLine);
		}
		document.replace(start, end-start, "");
	}

}
