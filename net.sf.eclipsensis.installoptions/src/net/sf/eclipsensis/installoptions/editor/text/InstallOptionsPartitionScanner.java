/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.text;

import java.util.*;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.*;

public class InstallOptionsPartitionScanner extends RuleBasedPartitionScanner implements IInstallOptionsScanner
{
    public static final String INSTALLOPTIONS_COMMENT = "__installoptions_comment";  //$NON-NLS-1$

    public static final String[] INSTALLOPTIONS_PARTITION_TYPES = new String[] {
            INSTALLOPTIONS_COMMENT
    };

    public final static String INSTALLOPTIONS_PARTITIONING= "__installoptions_partitioning";   //$NON-NLS-1$

    public InstallOptionsPartitionScanner()
    {
        super();
        IToken singlelineComment = new Token(INSTALLOPTIONS_COMMENT);

        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
        rules.add(new CompleteLineRule(";", singlelineComment));  //$NON-NLS-1$

        IPredicateRule[] result= new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPartitionTokenScanner#setPartialRange(org.eclipse.jface.text.IDocument, int, int, java.lang.String, int)
     */
    @Override
    public void setPartialRange(IDocument document, int offset, int length,
            String contentType, int partitionOffset)
    {
        super.setPartialRange(document, offset, length, contentType, partitionOffset);
        Arrays.sort(fDelimiters,new Comparator<char[]>() {
            public int compare(char[] a, char[] b)
            {
                return (b).length-(a).length;
            }
        });
    }

    public int getOffset()
    {
        return fOffset;
    }

    public IDocument getDocument()
    {
        return fDocument;
    }
}
