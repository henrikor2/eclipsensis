/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.editor.text.*;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;

public class NSISTemplateSourceViewerConfiguration extends NSISSourceViewerConfiguration
{
    /**
     * @param preferenceStore
     */
    public NSISTemplateSourceViewerConfiguration(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createPresentationReconciler(sourceViewer,
                new ITokenScanner[] {new NSISTemplateCodeScanner(mPreferenceStore),
                                    new NSISCommentScanner(mPreferenceStore),
                                    new NSISStringScanner(mPreferenceStore)},
                new String[][] {{IDocument.DEFAULT_CONTENT_TYPE},
                                {NSISPartitionScanner.NSIS_SINGLELINE_COMMENT,
                                 NSISPartitionScanner.NSIS_MULTILINE_COMMENT},
                                {NSISPartitionScanner.NSIS_STRING}});
    }
}