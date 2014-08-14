/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.help.NSISHelpURLProvider;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

public class NSISHelpInformationControlCreator extends NSISInformationControlCreator
{
    public NSISHelpInformationControlCreator(String[] ids)
    {
        super(ids);
    }

    public NSISHelpInformationControlCreator(String[] ids, int style)
    {
        super(ids, style);
    }

    @Override
    protected boolean shouldBuildStatusText()
    {
        boolean b = super.shouldBuildStatusText();
        if (b)
        {
            b = NSISHelpURLProvider.getInstance().isNSISHelpAvailable();
        }
        return b;
    }

    @Override
    protected NSISInformationControl.IInformationPresenter createInformationPresenter()
    {
        return new WrappingInformationPresenter("\t\t") { //$NON-NLS-1$
            @Override
            public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation,
                    int maxWidth, int maxHeight)
            {
                String hoverInfo2 = super.updatePresentation(display, hoverInfo, presentation, maxWidth, maxHeight);
                int n = hoverInfo2.indexOf(' ');
                if (n <= 0)
                {
                    n = hoverInfo2.length();
                }
                presentation.addStyleRange(new StyleRange(0, n, display.getSystemColor(SWT.COLOR_INFO_FOREGROUND),
                        display.getSystemColor(SWT.COLOR_INFO_BACKGROUND), SWT.BOLD));
                return hoverInfo2;
            }
        };
    }
}