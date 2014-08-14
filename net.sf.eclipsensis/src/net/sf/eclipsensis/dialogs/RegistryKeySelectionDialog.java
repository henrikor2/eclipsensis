/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class RegistryKeySelectionDialog extends StatusMessageDialog
{
    private String mRegKey = null;
    private String mText = null;

    private RegistryKeyBrowser mBrowser;

    public RegistryKeySelectionDialog(Shell parent)
    {
        super(parent);
        setHelpAvailable(false);
        setTitle(EclipseNSISPlugin.getResourceString("regkey.dialog.title")); //$NON-NLS-1$
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String text)
    {
        mText = text;
    }

    @Override
    protected int getMessageLabelStyle()
    {
        return SWT.NONE;
    }

    public String getRegKey()
    {
        return mRegKey;
    }

    public void setRegKey(String regKey)
    {
        mRegKey = regKey;
        if(mBrowser != null && !mBrowser.isDisposed()) {
            mBrowser.select(mRegKey);
        }
    }

    @Override
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        if(mText != null) {
            Label l =  new Label(composite,SWT.WRAP);
            l.setText(mText);
            l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        }
        mBrowser = new RegistryKeyBrowser(composite,SWT.BORDER);
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        data.widthHint = data.heightHint = 400;
        mBrowser.setLayoutData(data);
        if(mRegKey != null) {
            mBrowser.select(mRegKey);
        }
        mBrowser.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                okPressed();
            }

            public void widgetSelected(SelectionEvent e)
            {
                mRegKey = mBrowser.getSelection();
            }
        });
        return composite;
    }
}
