package com.evolveum.logviewer.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class MyConfiguration extends SourceViewerConfiguration {
	private ColorManager colorManager;
	private SpecificOidScanner specificOidScanner;

	public MyConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			MyPartitionScanner.PARTITION_OID };
	}

	protected SpecificOidScanner getSpecificOidScanner(IDocument document) {
		if (specificOidScanner == null) {
			specificOidScanner = new SpecificOidScanner(colorManager, document);
		}
		return specificOidScanner;
	}
	
	public void update(ISourceViewer sourceViewer) {
		specificOidScanner = null;
		IPresentationReconciler reconciler = getPresentationReconciler(sourceViewer);
		//reconciler.uninstall(); causes NPE
		reconciler.install(sourceViewer);
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSpecificOidScanner(sourceViewer.getDocument()));
		reconciler.setDamager(dr, MyPartitionScanner.PARTITION_OID);
		reconciler.setRepairer(dr, MyPartitionScanner.PARTITION_OID);

		return reconciler;
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (MyPartitionScanner.PARTITION_OID.equals(contentType)) {
			return new OidTextHover();
		} else {
			return null;
		}
	}

}