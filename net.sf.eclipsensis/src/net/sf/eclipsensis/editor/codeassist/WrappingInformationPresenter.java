/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.util.StringTokenizer;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class WrappingInformationPresenter implements NSISInformationControl.IInformationPresenter
{
    static final String BREAK_CHARS = ",;|-.?!:"; //$NON-NLS-1$

    private String mIndent=""; //$NON-NLS-1$

    public WrappingInformationPresenter()
    {
        this("  "); //$NON-NLS-1$
    }

    public WrappingInformationPresenter(String indent)
    {
        mIndent = indent;
    }

    public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight)
    {
        String hoverInfo2 = hoverInfo.trim();
        int maxWidth2 = maxWidth;
        GC gc = new GC(display);
        try {
            maxWidth2 -= gc.getFontMetrics().getAverageCharWidth();
            Point p = gc.stringExtent(hoverInfo2);
            if (p.x > maxWidth2) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                StringTokenizer st = new StringTokenizer(hoverInfo2, "\r\n"); //$NON-NLS-1$
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    p = gc.stringExtent(token);
                    if (p.x <= maxWidth2) {
                        buf.append(token);
                    }
                    else {
                        //Wrap
                        char[] chars = token.toCharArray();
                        int start = 0;
                        int last = -1;
                        boolean previousWasWhitespace = false;
                        boolean init = false;
                        int index;

                        for (int i = 0; i < chars.length; i++) {
                            if (Character.isWhitespace(chars[i])) {
                                previousWasWhitespace = true;
                                continue;
                            }
                            else if (((index = BREAK_CHARS.indexOf(chars[i])) >= 0) || previousWasWhitespace) {
                                String string = new String(chars, start, i - start + 1);
                                if(init) {
                                    string = mIndent+string;
                                }
                                p = gc.stringExtent(string);
                                if (p.x <= maxWidth2) {
                                    last = (index >= 0?i:i - 1);
                                }
                                else {
                                    if (init) {
                                        buf.append("\n").append(mIndent); //$NON-NLS-1$
                                    }
                                    if (last >= start) {
                                        buf.append(new String(chars, start, last - start + 1));
                                        start = last + 1;
                                        i = start;
                                    }
                                    else {
                                        buf.append(string);
                                        start = i;
                                        i--;
                                    }
                                    init = true;
                                    last = -1;
                                }
                                previousWasWhitespace = false;
                            }
                        }
                        if (init) {
                            buf.append("\n").append(mIndent); //$NON-NLS-1$
                        }
                        String s = new String(chars, start, chars.length - start);
                        if (gc.stringExtent(s).x > maxWidth2 && last >= start) {
                            buf.append(new String(chars, start, last - start + 1)).append("\n").append(mIndent); //$NON-NLS-1$
                            start = last + 1;
                            s = new String(chars, start, chars.length - start);
                        }
                        buf.append(s);
                    }
                    if (st.hasMoreTokens()) {
                        buf.append("\n"); //$NON-NLS-1$
                    }
                }
                hoverInfo2 = buf.toString();
            }
        }
        finally {
            gc.dispose();
        }
        return hoverInfo2;
    }
}