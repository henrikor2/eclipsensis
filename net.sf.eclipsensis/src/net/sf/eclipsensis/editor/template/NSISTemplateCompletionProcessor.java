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

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.codeassist.NSISInformationUtility;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.*;
import org.eclipse.swt.graphics.Image;

public class NSISTemplateCompletionProcessor extends TemplateCompletionProcessor
{
    private static final Image TEMPLATE_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("template.icon")); //$NON-NLS-1$
    private boolean mInsertTemplatesMode = false;
    protected static final Comparator<ICompletionProposal> PROPOSAL_COMPARATOR = new Comparator<ICompletionProposal>() {

        public int compare(ICompletionProposal o1, ICompletionProposal o2)
        {
            TemplateProposal tp1 = (TemplateProposal)o1;
            TemplateProposal tp2 = (TemplateProposal)o2;
            int n = tp2.getRelevance()-tp1.getRelevance();
            if(n == 0) {
                n = tp1.getDisplayString().toLowerCase().compareTo(tp2.getDisplayString().toLowerCase());
            }
            return n;
        }

    };

    /**
     *
     */
    public NSISTemplateCompletionProcessor()
    {
        this(false);
    }

    /**
     * @param insertTemplatesMode
     */
    public NSISTemplateCompletionProcessor(boolean insertTemplatesMode)
    {
        super();
        mInsertTemplatesMode = insertTemplatesMode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
     */
    @Override
    protected Template[] getTemplates(String contextTypeId)
    {
        return EclipseNSISPlugin.getDefault().getTemplateStore().getTemplates();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#createContext(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    @Override
    protected TemplateContext createContext(ITextViewer viewer, IRegion region)
    {
        TemplateContextType contextType= getContextType(viewer, region);
        if (contextType != null) {
            IDocument document= viewer.getDocument();
            return new NSISDocumentTemplateContext(contextType, document, region.getOffset(), region.getLength(), mInsertTemplatesMode);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    @Override
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region)
    {
        if(mInsertTemplatesMode || region.getLength() > 0) {
            return EclipseNSISPlugin.getDefault().getContextTypeRegistry().getContextType(NSISTemplateContextType.NSIS_TEMPLATE_CONTEXT_TYPE);
        }
        else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
     */
    @Override
    protected Image getImage(Template template)
    {
        return TEMPLATE_IMAGE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
            int offset)
    {
        ICompletionProposal[] proposals = super.computeCompletionProposals(viewer, offset);
        List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
        for (int i = 0; i < proposals.length; i++) {
            if(((TemplateProposal)proposals[i]).getRelevance() > 0) {
                list.add(proposals[i]);
            }
        }
        Collections.sort(list, PROPOSAL_COMPARATOR);
        proposals = list.toArray(NSISInformationUtility.EMPTY_COMPLETION_PROPOSAL_ARRAY);
        if(!mInsertTemplatesMode) {
            proposals = (ICompletionProposal[])Common.appendArray(proposals,
                                                                  NSISInformationUtility.getCompletionsAtOffset(viewer, offset));
        }
        return proposals;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getRelevance(org.eclipse.jface.text.templates.Template, java.lang.String)
     */
    @Override
    protected int getRelevance(Template template, String prefix)
    {
        if( (mInsertTemplatesMode && Common.isEmpty(prefix)) ||
            (template.getName().toLowerCase().startsWith(prefix.toLowerCase()))) {
            return 90;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    @Override
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return NSISInformationUtility.getCompletionProposalAutoActivationCharacters();
    }
}
