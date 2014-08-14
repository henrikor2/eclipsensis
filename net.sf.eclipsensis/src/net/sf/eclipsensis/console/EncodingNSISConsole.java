/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.io.UnsupportedEncodingException;

import net.sf.eclipsensis.EclipseNSISPlugin;

public class EncodingNSISConsole implements INSISConsole
{
    private String mEncoding;
    private INSISConsole mDelegate;

    public EncodingNSISConsole(INSISConsole delegate, String encoding)
    {
        mDelegate = delegate;
        mEncoding = encoding;
    }

    public void appendLine(NSISConsoleLine line)
    {
        NSISConsoleLine line2 = line;
        try {
            String text = new String(line.toString().getBytes(),mEncoding);
            line2 = new NSISConsoleLine(text, line.getType(),line.getSource(),line.getLineNum());
        }
        catch (UnsupportedEncodingException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        mDelegate.appendLine(line2);
    }

    public void clearConsole()
    {
        mDelegate.clearConsole();
    }
}
