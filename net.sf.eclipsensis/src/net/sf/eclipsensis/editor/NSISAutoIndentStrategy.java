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

import net.sf.eclipsensis.editor.text.NSISTextUtility;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;

public class NSISAutoIndentStrategy extends NSISAutoEditStrategy
{
    /**
     * @param preferenceStore
     */
    public NSISAutoIndentStrategy(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    protected int findEndOfWhiteSpace(IDocument doc, int offset, int end) throws BadLocationException {
        int offset2 = offset;
        while (offset2 < end) {
            char c= doc.getChar(offset2);
            if (c != ' ' && c != '\t') {
                return offset2;
            }
            offset2++;
        }
        return end;
    }

    protected void autoIndentAfterNewLine(IDocument doc, DocumentCommand cmd)
    {

        if (cmd.offset == -1 || doc.getLength() == 0) {
            return;
        }

        try {
            // find start of line
            int p= (cmd.offset == doc.getLength() ? cmd.offset  - 1 : cmd.offset);
            IRegion info= doc.getLineInformationOfOffset(p);
            int start= info.getOffset();

            // find white spaces
            int end= findEndOfWhiteSpace(doc, start, cmd.offset);

            StringBuffer buf= new StringBuffer(cmd.text);
            if (end > start) {
                // append to input
                String indent = doc.get(start, end - start);
                if(mUseSpacesForTabs) {
                    int position = 0;
                    char[] chars = indent.toCharArray();
                    for(int i=0; i<chars.length; i++) {
                        if(chars[i]=='\t') {
                            position += NSISTextUtility.insertTabString(buf,position,mTabWidth);
                        }
                        else {
                            buf.append(chars[i]);
                            position++;
                        }
                    }
                }
                else {
                    buf.append(indent);
                }
            }

            cmd.text= buf.toString();

        } catch (BadLocationException excp) {
            // stop work
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
     */
    public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd)
    {
        if (cmd.length == 0 && cmd.text != null && TextUtilities.endsWith(doc.getLegalLineDelimiters(), cmd.text) != -1) {
            autoIndentAfterNewLine(doc, cmd);
        }
    }
}
