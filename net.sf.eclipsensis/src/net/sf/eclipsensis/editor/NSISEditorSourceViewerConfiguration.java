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

import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.editor.template.NSISTemplateCompletionProcessor;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.information.*;
import org.eclipse.jface.text.source.*;

public class NSISEditorSourceViewerConfiguration extends NSISSourceViewerConfiguration
{
    private static final String[] ANNOTATION_TYPES = new String[]{PROBLEM_MARKER_ID,TASK_MARKER_ID,ERROR_ANNOTATION_NAME,WARNING_ANNOTATION_NAME};

    protected InformationPresenter mInformationPresenter = null;
    protected NSISTextHover mTextHover = null;
    protected NSISAnnotationHover mAnnotationHover = null;
    protected IInformationControlCreator mInformationControlCreator = null;

    public NSISEditorSourceViewerConfiguration(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
        mTextHover = new NSISTextHover(ANNOTATION_TYPES);
        mInformationControlCreator = new NSISInformationControlCreator(null);
        mAnnotationHover = new NSISAnnotationHover(ANNOTATION_TYPES);
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return mAnnotationHover;
    }

    /* (non-Javadoc)
     * Method declared on SourceViewerConfiguration
     */
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
    {
        return mTextHover;
    }


    @Override
    public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer)
    {
        return mAnnotationHover;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationControlCreator(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer)
    {
        return mInformationControlCreator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationPresenter(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer)
    {
        if(mInformationPresenter == null) {
            mInformationPresenter = NSISEditorUtilities.createStickyHelpInformationPresenter();
        }
        return mInformationPresenter;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createContentAssistant(sourceViewer,
                        new IContentAssistProcessor[] {new NSISTemplateCompletionProcessor(),
                                                       new NSISCompletionProcessor()},
                        new String[][] { {IDocument.DEFAULT_CONTENT_TYPE},
                                         {NSISPartitionScanner.NSIS_STRING}},
                        true);
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IContentAssistant getInsertTemplateAssistant(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createContentAssistant(sourceViewer,
                new IContentAssistProcessor[] {new NSISTemplateCompletionProcessor(true)},
                new String[][] { {IDocument.DEFAULT_CONTENT_TYPE} },
                false);
    }
}