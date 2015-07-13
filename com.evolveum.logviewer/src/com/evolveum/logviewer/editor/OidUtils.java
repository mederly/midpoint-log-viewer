package com.evolveum.logviewer.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.logviewer.outline.MyContentOutlinePage;
import com.evolveum.logviewer.outline.OidInfo;

public class OidUtils {
	
	public static OidInfo findOidInfo(IDocument document, String oid) {
		try {
			int lineNumber = document.getNumberOfLines()-1;
			while (lineNumber >= 0) {
				IRegion lineReg = document.getLineInformation(lineNumber);
				String line = document.get(lineReg.getOffset(), lineReg.getLength());
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || MyContentOutlinePage.isLogEntryStart(line)) {
					return null;
				}
				if (line.startsWith("%"+oid)) {
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
				if (line.equals(MyContentOutlinePage.CONFIG_MARKER) || MyContentOutlinePage.isLogEntryStart(line)) {
					return rv;
				}
				if (line.startsWith("%")) {
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


}
