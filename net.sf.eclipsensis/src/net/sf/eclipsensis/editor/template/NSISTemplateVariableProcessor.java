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

import net.sf.eclipsensis.editor.codeassist.NSISCompletionProcessor;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.templates.*;
import org.eclipse.swt.graphics.Point;

public class NSISTemplateVariableProcessor extends NSISCompletionProcessor implements INSISTemplateConstants
{
    private static final IRegion[] EMPTY_IREGION_ARRAY = new IRegion[0];
    private char[] mAutoActivationChars = null;
    private boolean mInsertTemplateVariablesMode = false;

    private static Comparator<NSISTemplateVariableProposal> mTemplateVariableProposalComparator= new Comparator<NSISTemplateVariableProposal>() {
        public int compare(NSISTemplateVariableProposal proposal0, NSISTemplateVariableProposal proposal1) {
            return proposal0.getDisplayString().compareTo(proposal1.getDisplayString());
        }
    };

    private TemplateContextType mContextType;

    /**
     *
     */
    public NSISTemplateVariableProcessor(TemplateContextType contextType)
    {
        this(contextType,false);
    }
    /**
     * @param insertTemplateVariablesMode
     */
    public NSISTemplateVariableProcessor(TemplateContextType contextType, boolean insertTemplateVariablesMode)
    {
        super();
        setContextType(contextType);
        mInsertTemplateVariablesMode = insertTemplateVariablesMode;
        mAutoActivationChars = new char[] {IDENTIFIER_BOUNDARY};
        if(!mInsertTemplateVariablesMode) {
            mAutoActivationChars = (char[])Common.appendArray(mAutoActivationChars, super.getCompletionProposalAutoActivationCharacters());
        }
    }
    /**
     * Sets the context type.
     */
    public void setContextType(TemplateContextType contextType)
    {
        mContextType= contextType;
    }

    /**
     * Gets the context type.
     */
    public TemplateContextType getContextType()
    {
        return mContextType;
    }

    /*
     * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
     */
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        List<NSISTemplateVariableProposal> proposals= new ArrayList<NSISTemplateVariableProposal>();
        if (mContextType != null) {
            String text= viewer.getDocument().get();
            int start= getStart(text, documentOffset);
            int end= documentOffset;
            Point selectedRange = viewer.getSelectedRange();
            int selectionEnd = selectedRange.x+selectedRange.y;

            String string= text.substring(start, end);
            String prefix= (string.length() >= 1?string.substring(1):null);

            if(mInsertTemplateVariablesMode || (string.length() > 0 && string.charAt(0) == IDENTIFIER_BOUNDARY)) {
                int offset= start;
                int length= selectionEnd - start;

                for (Iterator<?> iterator= mContextType.resolvers(); iterator.hasNext(); ) {
                    TemplateVariableResolver variable= (TemplateVariableResolver) iterator.next();

                    if (Common.isEmpty(prefix) || variable.getType().startsWith(prefix)) {
                        proposals.add(new NSISTemplateVariableProposal(variable, offset, length, viewer));
                    }
                }

                Collections.sort(proposals, mTemplateVariableProposalComparator);
            }
        }
        ICompletionProposal[] completionProposals = proposals.toArray(new ICompletionProposal[proposals.size()]);
        if(!mInsertTemplateVariablesMode) {
            completionProposals = (ICompletionProposal[])Common.appendArray(completionProposals, super.computeCompletionProposals(viewer, documentOffset));
        }
        return completionProposals;
    }

    /* Guesses the start position of the completion */
    private int getStart(String string, int end)
    {
        IRegion[] variables = parsePattern(string);
        int regionStart = 0;
        for (int i = 0; i < variables.length; i++) {
            int offset = variables[i].getOffset();
            if(end < offset) {
                break;
            }
            else {
                int endOffset = offset+variables[i].getLength();
                if(end <= (endOffset-1)) {
                    regionStart = offset;
                    break;
                }
                else {
                    regionStart = endOffset;
                }
            }
        }

        int start= end;

        if (start >= (regionStart+1) && string.charAt(start - 1) == IDENTIFIER_BOUNDARY) {
            return start - 1;
        }

        while ((start > regionStart) && Character.isUnicodeIdentifierPart(string.charAt(start - 1))) {
            start--;
        }

        if (start >= (regionStart+1) && string.charAt(start - 1) == IDENTIFIER_BOUNDARY) {
            return start - 1;
        }

        return end;
    }

    private IRegion[] parsePattern(String string)
    {
        int state= TEXT;
        ArrayList<Region> list = new ArrayList<Region>();

        int offset = -1;
        for (int i= 0; i != string.length(); i++) {
            char ch= string.charAt(i);

            switch (state) {
            case TEXT:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        state= ESCAPE;
                        offset = i;
                        break;
                    default:
                        break;
                }
                break;
            case ESCAPE:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        list.add(new Region(offset,i-offset+1));
                        state= TEXT;
                        offset = -1;
                        break;
                    default:
                        if(!Character.isLetter(ch)) {
                            offset = -1;
                            state= TEXT;
                        }
                        else {
                            state= IDENTIFIER;
                        }
                }
                break;
            case IDENTIFIER:
                switch (ch) {
                case IDENTIFIER_BOUNDARY:
                    list.add(new Region(offset,i-offset+1));
                    state= TEXT;
                    offset = -1;
                    break;
                default:
                    if (!Character.isLetterOrDigit(ch) && ch != '_') {
                        offset = -1;
                        state= TEXT;
                    }
                    break;
                }
                break;
            }
        }

        return list.toArray(EMPTY_IREGION_ARRAY);
    }
    /*
     * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
     */
    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return null;
    }

    /*
     * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    @Override
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return mAutoActivationChars;
    }

    /*
     * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    @Override
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /*
     * @see IContentAssistProcessor#getErrorMessage()
     */
    @Override
    public String getErrorMessage()
    {
        return null;
    }

    /*
     * @see IContentAssistProcessor#getContextInformationValidator()
     */
    @Override
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }
}
