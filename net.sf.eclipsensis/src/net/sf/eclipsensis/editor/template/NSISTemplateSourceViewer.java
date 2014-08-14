/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 */
package net.sf.eclipsensis.editor.template;

import net.sf.eclipsensis.editor.NSISSourceViewer;

import org.eclipse.jface.resource.*;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.widgets.Composite;

public class NSISTemplateSourceViewer extends NSISSourceViewer
{
    public static final int INSERT_TEMPLATE_VARIABLE = REMOVE_BLOCK_COMMENT+1;
    private IContentAssistant mInsertTemplateVariableAssistant = null;
    private boolean mInsertTemplateVariableAssistantInstalled = false;

    private ColorRegistry mColorRegistry = null;
    /**
     * @param parent
     * @param ruler
     * @param overviewRuler
     * @param showsAnnotationOverview
     * @param styles
     */
    public NSISTemplateSourceViewer(Composite parent, IVerticalRuler ruler,
            IOverviewRuler overviewRuler, boolean showsAnnotationOverview,
            int styles)
    {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ISourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
     */
    @Override
    public void configure(SourceViewerConfiguration configuration)
    {
        mColorRegistry = JFaceResources.getColorRegistry();
        mColorRegistry.addListener(this);
        if(configuration instanceof NSISTemplateEditorSourceViewerConfiguration) {
            mInsertTemplateVariableAssistant = ((NSISTemplateEditorSourceViewerConfiguration)configuration).getInsertTemplateVariableAssistant(this);
            if(mInsertTemplateVariableAssistant != null) {
                mInsertTemplateVariableAssistant.install(this);
                mInsertTemplateVariableAssistantInstalled = true;
            }
        }
        super.configure(configuration);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     */
    @Override
    public void unconfigure()
    {
        mColorRegistry.removeListener(this);
        if (mInsertTemplateVariableAssistant != null) {
            mInsertTemplateVariableAssistant.uninstall();
            mInsertTemplateVariableAssistantInstalled= false;
            mInsertTemplateVariableAssistant= null;
        }
        super.unconfigure();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTargetExtension#enableOperation(int, boolean)
     */
    @Override
    public void enableOperation(int operation, boolean enable)
    {
        switch(operation) {
            case INSERT_TEMPLATE_VARIABLE:
                if (mInsertTemplateVariableAssistant == null) {
                    return;
                }
                if (enable) {
                    if (!mInsertTemplateVariableAssistantInstalled) {
                        mInsertTemplateVariableAssistant.install(this);
                        mInsertTemplateVariableAssistantInstalled= true;
                    }
                }
                else if (mInsertTemplateVariableAssistantInstalled) {
                    mInsertTemplateVariableAssistant.uninstall();
                    mInsertTemplateVariableAssistantInstalled= false;
                }
                break;
            default:
                super.enableOperation(operation, enable);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTarget#canDoOperation(int)
     */
    @Override
    public boolean canDoOperation(int operation)
    {
        switch(operation) {
            case INSERT_TEMPLATE_VARIABLE:
                return mInsertTemplateVariableAssistant != null && mInsertTemplateVariableAssistantInstalled && isEditable();
            default:
                return super.canDoOperation(operation);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
     */
    @Override
    public void doOperation(int operation)
    {
        switch(operation) {
            case INSERT_TEMPLATE_VARIABLE:
            {
                mInsertTemplateVariableAssistant.showPossibleCompletions();
                return;
            }
            default:
            {
                super.doOperation(operation);
                return;
            }
        }
    }
}
