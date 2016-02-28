package com.evolveum.logviewer.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.editor.DocumentUtils;
import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.parsing.ParsingUtils;
import com.evolveum.logviewer.tree.ContextNodeDefinition;
import com.evolveum.logviewer.tree.ExecutionNodeDefinition;
import com.evolveum.logviewer.tree.ExpressionNodeDefinition;
import com.evolveum.logviewer.tree.GenericNodeDefinition;
import com.evolveum.logviewer.tree.MappingNodeDefinition;
import com.evolveum.logviewer.tree.ProjectionContextNodeDefinition;
import com.evolveum.logviewer.tree.ScriptNodeDefinition;
import com.evolveum.logviewer.tree.SummaryNodeDefinition;

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
		} catch (RuntimeException e) {
			System.err.println("Couldn't parse configuration.");
			e.printStackTrace();
			throw e;
		}
	}

	private static int parseConfiguration(IDocument document, EditorConfiguration config, int lineNumber) {
		System.out.println("Parsing configuration from line " + lineNumber);
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
			} else if (line.startsWith("%outline startup")) {
				config.addOutlineInstruction(GenericNodeDefinition.parseFromLineAsStartupDefinition(config, line));
			} else if (line.startsWith("%outline test")) {
				config.addOutlineInstruction(GenericNodeDefinition.parseFromLineAsTestDefinition(config, line));
			} else if (line.startsWith("%outline operation-summary")) {
				config.addOutlineInstruction(SummaryNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline operation-context")) {
				config.addOutlineInstruction(ContextNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline projection-context")) {
				config.addOutlineInstruction(ProjectionContextNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline mapping")) {
				config.addOutlineInstruction(MappingNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline expression")) {
				config.addOutlineInstruction(ExpressionNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline script")) {
				config.addOutlineInstruction(ScriptNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline execution")) {
				config.addOutlineInstruction(ExecutionNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%outline custom")) {
				config.addOutlineInstruction(GenericNodeDefinition.parseFromLine(config, line));
			} else if (line.startsWith("%oid") || line.startsWith("%thread")) {
				//nothing here
			} else if (line.startsWith("%")) {
				Instruction i = parseLine(line);
				if (i == null) {
					System.err.println("Unparseable config line: " + line);
				} else {
					config.addInstruction(i);
				}
			}
		}
		config.sortOutlineLevelDefinitions();
		return lineNumber;
	}

	private static final List<Class<? extends Instruction>> INSTRUCTION_DEFINITIONS = Arrays.asList(MarkDelayInstruction.class, MarkProblemInstruction.class);
	
	private static Instruction parseLine(String line) {
		for (Class<? extends Instruction> definition : INSTRUCTION_DEFINITIONS) {
			try {
				Method parseMethod = definition.getMethod("parseFromLine", String.class);
				Instruction i = (Instruction) parseMethod.invoke(null, line);
				if (i != null) {
					return i;
				}
			} catch (NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
				System.err.println("Couldn't invoke parseFromLine on " + definition);
				e.printStackTrace();
			}
		}
		return null;
	}

	private static Integer parseProblemIfDelay(String line) {
		int space1 = line.indexOf(' ');
		if (space1 < 0) {
			System.err.println("Couldn't parse if-delay instruction: " + line);
			return null;
		}
		
		return Integer.valueOf(line.substring(space1+1));
	}

	// 'xxx'->xxx, etc
	public static String unwrapText(String text) {
		if (text == null || text.isEmpty()) {
			return null;
		} else if (text.length() == 1) {
			System.err.println("Improperly wrapped text: " + text);
			return text;
		}
		char start = text.charAt(0);
		char end = text.charAt(text.length()-1);
		if (start != end) {
			System.err.println("Improperly wrapped text: " + text);
			return text;
		}
		return text.substring(1, text.length()-1);
	}

}
