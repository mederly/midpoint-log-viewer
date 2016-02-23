package com.evolveum.logviewer.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;

import com.evolveum.logviewer.config.ConfigurationParser;
import com.evolveum.logviewer.config.OidInfo;

public class OidTextHover implements ITextHover {

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		try {
			IDocument doc = textViewer.getDocument();
			String oid = doc.get(hoverRegion.getOffset(), hoverRegion.getLength());
			OidInfo oidInfo = ConfigurationParser.findOidInfo(doc, oid);
			if (oidInfo != null) {
				return oidInfo.toString();
			} else {
				return "?";
			}
		} catch (BadLocationException e) {
			return "?";
		}
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findOid(textViewer.getDocument(), offset);
	}
	
	private IRegion findOid(IDocument document, int offset) {
		try {
			return document.getPartition(offset);
		} catch (BadLocationException e) {
			return null;
		}
	}

}
