/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.util.*;

import net.sf.eclipsensis.util.Common;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class NSISInformationControlCreator extends AbstractNSISInformationControlCreator
{
    private ParameterizedCommand[] mCommands = null;
    private NSISBrowserInformationControlCreator mCreator = new NSISBrowserInformationControlCreator(SWT.V_SCROLL
            | SWT.H_SCROLL);

    public NSISInformationControlCreator(String[] commandIds)
    {
        this(commandIds, SWT.NONE);
    }

    public NSISInformationControlCreator(String[] commandIds, int style)
    {
        super(style);
        List<ParameterizedCommand> list = new ArrayList<ParameterizedCommand>();
        if (!Common.isEmptyArray(commandIds))
        {
            for (int i = 0; i < commandIds.length; i++)
            {
                ParameterizedCommand command = NSISInformationUtility.getCommand(commandIds[i]);
                if (command != null)
                {
                    if (!list.contains(command))
                    {
                        list.add(command);
                    }
                }
            }
        }
        mCommands = list.toArray(new ParameterizedCommand[list.size()]);
    }

    public IInformationControl createInformationControl(Shell parent)
    {
        String statusText = null;
        if (shouldBuildStatusText())
        {
            statusText = NSISInformationUtility.buildStatusText(mCommands);
        }
        NSISInformationControl informationControl = new NSISInformationControl(parent, mStyle, mInformationPresenter,
                statusText);
        informationControl.setInformationPresenterControlCreator(mCreator);
        return informationControl;
    }

    protected boolean shouldBuildStatusText()
    {
        return mCommands != null;
    }
}