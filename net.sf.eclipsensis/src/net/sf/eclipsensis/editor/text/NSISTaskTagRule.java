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

import net.sf.eclipsensis.editor.NSISTaskTag;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.jface.text.rules.*;

public class NSISTaskTagRule implements IRule
{
    private IToken mToken;
    private Map<String, NSISTaskTag> mTaskTags;
    private String[] mMatchStrings;
    /**
     *
     */
    public NSISTaskTagRule(IToken token)
    {
        this();
        mToken = token;
    }

    public NSISTaskTagRule()
    {
        loadTaskTags();
    }

    private void loadTaskTags()
    {
        Collection<NSISTaskTag> taskTags = NSISPreferences.getInstance().getTaskTags();
        boolean caseSensitive = NSISPreferences.getInstance().isCaseSensitiveTaskTags();
        if(caseSensitive) {
            mTaskTags = new HashMap<String, NSISTaskTag>();
        }
        else {
            mTaskTags = new CaseInsensitiveMap<NSISTaskTag>();
        }
        String[] tagNames = new String[taskTags.size()];
        int i=0;
        for (Iterator<NSISTaskTag> iter = taskTags.iterator(); iter.hasNext();) {
            NSISTaskTag tag = iter.next();
            tagNames[i] = tag.getTag();
            mTaskTags.put(tagNames[i],tag);
            i++;
        }
        Arrays.sort(tagNames, new Comparator<String>() {
            public int compare(String tag1, String tag2)
            {
                int n= tag2.length()-tag1.length();
                if(n == 0) {
                    n = tag1.compareTo(tag2);
                }
                return n;
            }
        });
        mMatchStrings = new String[tagNames[0].length()];
        for (int j = 0; j < mMatchStrings.length; j++) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (int k = 0; k < tagNames.length; k++) {
                if(tagNames[k].length() > j) {
                    String s = tagNames[k].substring(j,j+1);
                    if(buf.indexOf(s) == -1) {
                        buf.append(s);
                        if(!caseSensitive) {
                            if(Character.isUpperCase(s.charAt(0))) {
                                buf.append(s.toLowerCase());
                            }
                            else {
                                buf.append(s.toUpperCase());
                            }
                        }
                    }
                }
            }
            mMatchStrings[j] = buf.toString();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        if(scanner instanceof INSISBackwardScanner) {
            return evaluate((INSISBackwardScanner)scanner);
        }
        return Token.UNDEFINED;
    }

    private IToken evaluate(INSISBackwardScanner scanner)
    {
        int c = scanner.getPreviousCharacter(0);
        boolean ok = false;
        switch(c) {
            case ICharacterScanner.EOF:
            case '#':
            case ';':
                ok = true;
                break;
            case '*':
                c = scanner.getPreviousCharacter(1);
                ok = (c == '/');
                break;
            default:
                ok = Character.isWhitespace((char)c);
        }
        if(ok) {
            c = scanner.read();
            if (c != ICharacterScanner.EOF) {
                int counter = 0;
                if(mMatchStrings[counter].indexOf(c) != -1) {
                    StringBuffer fBuffer = new StringBuffer(""); //$NON-NLS-1$
                    do {
                        fBuffer.append((char) c);
                        counter++;
                        c= scanner.read();
                    } while (c != ICharacterScanner.EOF && counter < mMatchStrings.length && (mMatchStrings[counter].indexOf(c) != -1));
                    boolean isCandidate = false;
                    if(Character.isWhitespace((char)c) || c == ICharacterScanner.EOF) {
                        isCandidate = true;
                    }
                    scanner.unread();

                    if (isCandidate) {
                        String tagName = fBuffer.toString();
                        if(mTaskTags.containsKey(tagName)) {
                            return (mToken !=null?mToken:new Token(mTaskTags.get(tagName)));
                        }
                    }
                    unreadBuffer(fBuffer, scanner);

                    return Token.UNDEFINED;
                }
            }

            scanner.unread();
        }
        return Token.UNDEFINED;
    }

    /**
     * Returns the characters in the buffer to the scanner.
     *
     * @param scanner the scanner to be used
     */
    protected void unreadBuffer(StringBuffer buffer, ICharacterScanner scanner)
    {
        for (int i= buffer.length() - 1; i >= 0; i--) {
            scanner.unread();
        }
    }
}
