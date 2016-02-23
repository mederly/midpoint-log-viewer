package com.evolveum.logviewer.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.parsing.ParsingUtils;

public class ConfigurationParser {
	
	public static OidInfo findOidInfo(IDocument document, String oid) {
		try {
			int lineNumber = document.getNumberOfLines()-1;
			while (lineNumber >= 0) {
				IRegion lineReg = document.getLineInformation(lineNumber);
				String line = document.get(lineReg.getOffset(), lineReg.getLength());
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || ParsingUtils.isLogEntryStart(line)) {
					return null;
				}
				if (line.startsWith("%oid "+oid)) {
					return OidInfo.parseFromLine(line);
				}
				lineNumber--;
			}
			return null;
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static List<OidInfo> getAllOidInfos(IDocument document) {
		List<OidInfo> rv = new ArrayList<>();
		if (document == null) {
			return rv;
		}
		try {
			int lineNumber = document.getNumberOfLines()-1;
			while (lineNumber >= 0) {
				IRegion lineReg = document.getLineInformation(lineNumber);
				String line = document.get(lineReg.getOffset(), lineReg.getLength());
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || ParsingUtils.isLogEntryStart(line)) {
					return rv;
				}
				if (line.startsWith("%oid ")) {
					OidInfo oidInfo = OidInfo.parseFromLine(line);
					if (oidInfo != null) {
						rv.add(oidInfo);
					}
				}
				lineNumber--;
			}
			return rv;
		} catch (BadLocationException e) {
			e.printStackTrace();
			return rv;
		}

	}

	// TODO move
	public static List<FoldingInstruction> getAllFoldingInstructions(IDocument document) {
		List<FoldingInstruction> rv = new ArrayList<>();
		if (document == null) {
			return rv;
		}
		try {
			int lineNumber = document.getNumberOfLines()-1;
			while (lineNumber >= 0) {
				IRegion lineReg = document.getLineInformation(lineNumber);
				String line = document.get(lineReg.getOffset(), lineReg.getLength());
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || ParsingUtils.isLogEntryStart(line)) {
					return rv;
				}
				if (line.startsWith("%collapse ") || line.startsWith("%expand ")) {
					FoldingInstruction instr = FoldingInstruction.parseFromLine(line);
					if (instr != null) {
						rv.add(instr);
					}
				}
				lineNumber--;
			}
			Collections.reverse(rv);
			return rv;
		} catch (BadLocationException e) {
			e.printStackTrace();
			Collections.reverse(rv);
			return rv;
		}

	}
	
	//TODO move
	public static List<KillInstruction> getAllKillInstructions(IDocument document) {
		List<KillInstruction> rv = new ArrayList<>();
		if (document == null) {
			return rv;
		}
		try {
			int lineNumber = document.getNumberOfLines()-1;
			while (lineNumber >= 0) {
				IRegion lineReg = document.getLineInformation(lineNumber);
				String line = document.get(lineReg.getOffset(), lineReg.getLength());
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || ParsingUtils.isLogEntryStart(line)) {
					return rv;
				}
				if (line.startsWith("%kill ")) {
					KillInstruction instr = KillInstruction.parseFromLine(line);
					if (instr != null) {
						rv.add(instr);
					}
				}
				lineNumber--;
			}
			Collections.reverse(rv);
			return rv;
		} catch (BadLocationException e) {
			e.printStackTrace();
			Collections.reverse(rv);
			return rv;
		}
	}
	
	public static EditorConfiguration getConfiguration(IDocument document) {
		EditorConfiguration config = new EditorConfiguration(); 
		if (document == null) {
			System.out.println("No document, no config.");
			return config;
		}
		try {
			int lines = document.getNumberOfLines();
			int lineNumber = lines;
			while (--lineNumber >= 0) {
				IRegion lineReg = document.getLineInformation(lineNumber);
				String line = document.get(lineReg.getOffset(), lineReg.getLength());
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER)) {
					parseConfiguration(document, config, lineNumber);
					System.out.println("Configuration successfully read: " + config.getSummary());
					return config;
				}
			}
			System.out.println("No configuration found, using default one.");
			return config;
		} catch (BadLocationException e) {
			e.printStackTrace();
			return config;
		}
	}

	private static int parseConfiguration(IDocument document, EditorConfiguration config, int lineNumber) {
		int lines = document.getNumberOfLines();
		String line;
		while (++lineNumber < lines) {
			line = DocumentUtils.getLine(document, lineNumber);
			if (line.startsWith("%skip-thread-processing")) {
				config.skipThreadProcessing = true;
			} else if (line.startsWith("%no-component-names")) {
				config.componentNames = false;
			} else if (line.startsWith("%component-names")) {
				config.componentNames = true;
			} else if (line.startsWith("%error-marking")) {
				ErrorMarkingInstruction ei = ErrorMarkingInstruction.parseFromLine(line);
				if (ei != null) {
					config.addErrorInstruction(ei);
				}
			} else if (line.startsWith("%error-if-delay")) {
				config.setErrorIfDelay(parseProblemIfDelay(line));
			} else if (line.startsWith("%warning-if-delay")) {
				config.setWarningIfDelay(parseProblemIfDelay(line));
			} else if (line.startsWith("%info-if-delay")) {
				config.setInfoIfDelay(parseProblemIfDelay(line));
			} else if (line.startsWith("%outline")) {
				config.addOutlineInstruction(OutlineInstruction.parseFromLine(line));
			}
		}
		return lineNumber;
	}

	private static Integer parseProblemIfDelay(String line) {
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.err.println("Couldn't parse if-delay instruction: " + line);
			return null;
		}
		
		return Integer.valueOf(line.substring(space1+1));
	}

}
