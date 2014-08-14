/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.*;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.*;


public class NSISPartitionScanner extends RuleBasedPartitionScanner implements NSISScanner
{
    public static final String NSIS_SINGLELINE_COMMENT = "__nsis_singleline_comment";  //$NON-NLS-1$
    public static final String NSIS_MULTILINE_COMMENT = "__nsis_multiline_comment";  //$NON-NLS-1$
    public static final String NSIS_STRING = "__nsis_string"; //$NON-NLS-1$

    public static final String[] NSIS_PARTITION_TYPES = new String[] {
            NSIS_SINGLELINE_COMMENT,
            NSIS_MULTILINE_COMMENT,
            NSIS_STRING
    };

    public final static String NSIS_PARTITIONING= "__nsis_partitioning";   //$NON-NLS-1$

    public NSISPartitionScanner()
    {
        super();
        IToken singlelineComment = new Token(NSIS_SINGLELINE_COMMENT);
        IToken multilineComment = new Token(NSIS_MULTILINE_COMMENT);
        IToken string = new Token(NSIS_STRING);

        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
        rules.add(new NSISEndOfLineRule("#", singlelineComment));  //$NON-NLS-1$
        rules.add(new NSISEndOfLineRule(";", singlelineComment));  //$NON-NLS-1$
        rules.add(new NSISStringRule('"', string));
        rules.add(new NSISStringRule('\'', string));
        rules.add(new NSISStringRule('`', string));

        EmptyCommentRule emptyCommentRule= new EmptyCommentRule(multilineComment);
        rules.add(emptyCommentRule);
        rules.add(new NSISMultiLineRule("/*","*/", multilineComment)); //$NON-NLS-2$ //$NON-NLS-1$

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
                return b.length-a.length;
            }
        });
    }

    public int getOffset()
    {
        return fOffset;
    }

    /**
     * Word rule for empty comments.
     */
    private static class EmptyCommentRule extends NSISWordRule implements IPredicateRule {

        private IToken mSuccessToken;
        /**
         * Constructor for EmptyCommentRule.
         * @param defaultToken
         */
        public EmptyCommentRule(IToken successToken) {
            super(new EmptyCommentDetector());
            mSuccessToken= successToken;
            addWord("/**/", mSuccessToken); //$NON-NLS-1$
        }

        /*
         * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
         */
        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return evaluate(scanner);
        }

        /*
         * @see IPredicateRule#getSuccessToken()
         */
        public IToken getSuccessToken() {
            return mSuccessToken;
        }
    }

    /**
     * Detector for empty comments.
     */
    private static class EmptyCommentDetector implements IWordDetector {

        /*
         * @see IWordDetector#isWordStart
         */
        public boolean isWordStart(char c) {
            return (c == '/');
        }

        /*
         * @see IWordDetector#isWordPart
         */
        public boolean isWordPart(char c) {
            return (c == '*' || c == '/');
        }
    }
}
