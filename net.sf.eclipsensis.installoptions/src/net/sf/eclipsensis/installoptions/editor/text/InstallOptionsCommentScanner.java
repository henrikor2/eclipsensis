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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.jface.text.IDocument;

public class InstallOptionsCommentScanner extends InstallOptionsSyntaxScanner
{
    @Override
    protected void reset()
    {
        setDefaultReturnToken(createToken(IInstallOptionsConstants.COMMENT_STYLE));
    }

    @Override
    public void setRange(IDocument document, int offset, int length)
    {
        super.setRange(document, offset, length);
        Arrays.sort(fDelimiters,new Comparator<char[]>() {
            public int compare(char[] a, char[] b)
            {
                return (b).length-(a).length;
            }
        });
    }
}
