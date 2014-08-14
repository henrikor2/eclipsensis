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


import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.SWT;

public abstract class AbstractNSISInformationControlCreator implements IInformationControlCreator, INSISConstants
{
    protected int mStyle = SWT.NONE;

    protected NSISInformationControl.IInformationPresenter mInformationPresenter = createInformationPresenter();

    public AbstractNSISInformationControlCreator(int style)
    {
        mStyle = style;
    }

    protected NSISInformationControl.IInformationPresenter createInformationPresenter()
    {
        return null;
    }
}