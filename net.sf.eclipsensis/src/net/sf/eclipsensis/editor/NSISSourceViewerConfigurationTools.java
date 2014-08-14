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

import net.sf.eclipsensis.editor.text.NSISPartitionScanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.presentation.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class NSISSourceViewerConfigurationTools
{
    protected static final String[] CONFIGURED_CONTENT_TYPES;

    static {
        CONFIGURED_CONTENT_TYPES = new String[NSISPartitionScanner.NSIS_PARTITION_TYPES.length+1];
        CONFIGURED_CONTENT_TYPES[0]=IDocument.DEFAULT_CONTENT_TYPE;
        System.arraycopy(NSISPartitionScanner.NSIS_PARTITION_TYPES,0,CONFIGURED_CONTENT_TYPES,1,NSISPartitionScanner.NSIS_PARTITION_TYPES.length);
    }

    private NSISSourceViewerConfigurationTools()
    {
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public static IContentAssistant createContentAssistant(ISourceViewer sourceViewer,
                                        IContentAssistProcessor[] contentAssistProcessors,
                                        String[][] contentTypes, boolean autoActivation)
    {
        ContentAssistant assistant = new ContentAssistant();
        assistant.setDocumentPartitioning(NSISPartitionScanner.NSIS_PARTITIONING);
        for (int i = 0; i < contentAssistProcessors.length; i++) {
            for (int j = 0; j < contentTypes[i].length; j++) {
                assistant.setContentAssistProcessor(contentAssistProcessors[i],
                        contentTypes[i][j]);
            }
        }

        assistant.enableAutoInsert(true);
        assistant.enableAutoActivation(autoActivation);
        assistant.setAutoActivationDelay(100);
        assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
        assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        assistant.setContextInformationPopupBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        return assistant;
    }

    public static IPresentationReconciler createPresentationReconciler(ISourceViewer sourceViewer,
                                                                       ITokenScanner[] scanners, String[][] contentTypes)
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(NSISPartitionScanner.NSIS_PARTITIONING);
        if(scanners.length == contentTypes.length) {
            for (int i = 0; i < scanners.length; i++) {
                DefaultDamagerRepairer dr = new NSISDamagerRepairer(scanners[i]);
                for (int j = 0; j < contentTypes[i].length; j++) {
                    reconciler.setDamager(dr, contentTypes[i][j]);
                    reconciler.setRepairer(dr, contentTypes[i][j]);
                }
            }
        }

        return reconciler;
    }
}
