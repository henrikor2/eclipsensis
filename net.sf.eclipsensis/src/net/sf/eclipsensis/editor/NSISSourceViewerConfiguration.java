/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.codeassist.NSISDoubleClickSelector;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.settings.INSISEditorPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.*;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class NSISSourceViewerConfiguration extends SourceViewerConfiguration implements INSISConstants, INSISEditorPreferenceConstants
{
    protected IPreferenceStore mPreferenceStore = null;
    protected ITextDoubleClickStrategy mDoubleClickStrategy = null;

    public NSISSourceViewerConfiguration(IPreferenceStore preferenceStore)
    {
        super();
        mPreferenceStore = preferenceStore;
        mDoubleClickStrategy = new NSISDoubleClickSelector();
    }

    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer)
    {
        return NSISPartitionScanner.NSIS_PARTITIONING;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.CONFIGURED_CONTENT_TYPES;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public String getDefaultPrefix(ISourceViewer sourceViewer,
            String contentType)
    {
        return (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" : null); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(
            ISourceViewer sourceViewer, String contentType)
    {
        return mDoubleClickStrategy;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType)
    {
        if(sourceViewer instanceof NSISSourceViewer) {
            return ((NSISSourceViewer)sourceViewer).calculatePrefixes();
        }
        else {
            return new String[]{"\t","    "}; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createPresentationReconciler(sourceViewer,
                                            new ITokenScanner[] {new NSISCodeScanner(mPreferenceStore),
                                                                new NSISCommentScanner(mPreferenceStore),
                                                                new NSISStringScanner(mPreferenceStore)},
                                            new String[][] {{IDocument.DEFAULT_CONTENT_TYPE},
                                                            {NSISPartitionScanner.NSIS_SINGLELINE_COMMENT,
                                                             NSISPartitionScanner.NSIS_MULTILINE_COMMENT},
                                                            {NSISPartitionScanner.NSIS_STRING}});
    }

    /* (non-Javadoc)
     * Method declared on SourceViewerConfiguration
     */
    @Override
    public int getTabWidth(ISourceViewer sourceViewer)
    {
        return mPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    }

    public IPreferenceStore getPreferenceStore()
    {
        return mPreferenceStore;
    }
}