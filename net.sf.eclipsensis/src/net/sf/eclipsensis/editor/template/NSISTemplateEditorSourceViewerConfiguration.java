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

import net.sf.eclipsensis.editor.NSISSourceViewerConfigurationTools;
import net.sf.eclipsensis.editor.codeassist.NSISCompletionProcessor;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;

public class NSISTemplateEditorSourceViewerConfiguration extends NSISTemplateSourceViewerConfiguration
{
    private TemplateContextType mTemplateContextType = null;

    public NSISTemplateEditorSourceViewerConfiguration(IPreferenceStore preferenceStore, TemplateContextType templateContextType)
    {
        super(preferenceStore);
        mTemplateContextType = templateContextType;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createContentAssistant(sourceViewer,
                        new IContentAssistProcessor[] {new NSISTemplateVariableProcessor(mTemplateContextType),
                                                       new NSISCompletionProcessor()},
                        new String[][] {{IDocument.DEFAULT_CONTENT_TYPE},
                                         {NSISPartitionScanner.NSIS_STRING}},
                        true);
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IContentAssistant getInsertTemplateVariableAssistant(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createContentAssistant(sourceViewer,
                new IContentAssistProcessor[] {new NSISTemplateVariableProcessor(mTemplateContextType, true)},
                new String[][] { {IDocument.DEFAULT_CONTENT_TYPE} },
                false);
    }
}
