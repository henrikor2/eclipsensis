/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class StringParam extends SubstitutableParam
{
    public static final String SETTING_TEXT = "text"; //$NON-NLS-1$

    public StringParam(Node node)
    {
        super(node);
    }

    @Override
    protected PrefixableParamEditor createPrefixableParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new StringParamEditor(command, parentEditor);
    }

    protected String validateText(String text)
    {
        if (isAllowBlank() || text != null && text.length() > 0)
        {
            return null;
        }
        return EclipseNSISPlugin.getResourceString("string.param.error"); //$NON-NLS-1$
    }

    public boolean verifyText(String text)
    {
        return true;
    }

    protected class StringParamEditor extends PrefixableParamEditor
    {
        protected Text mText = null;

        public StringParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        public void clear()
        {
            if (Common.isValid(mText))
            {
                mText.setText(""); //$NON-NLS-1$
            }
            super.clear();
        }

        @Override
        protected String getPrefixableParamText()
        {
            if (Common.isValid(mText))
            {
                return mText.getText();
            }
            return null;
        }

        @Override
        protected String validateParam()
        {
            if (Common.isValid(mText))
            {
                return StringParam.this.validateText(mText.getText());
            }
            return null;
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if (Common.isValid(mText) && getSettings() != null)
            {
                getSettings().put(SETTING_TEXT, mText.getText());
            }
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            mText = new Text(parent, SWT.BORDER);
            setToolTip(mText);
            mText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            mText.addVerifyListener(new VerifyListener() {
                public void verifyText(VerifyEvent e)
                {
                    Text text = (Text) e.widget;
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    buf.append(text.getText().substring(0, e.start)).append(e.text).append(
                            text.getText().substring(e.end));
                    e.doit = StringParam.this.verifyText(buf.toString());
                    if (!e.doit)
                    {
                        e.display.beep();
                    }
                }
            });
            return mText;
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            if (Common.isValid(mText))
            {
                mText.setText((String) getSettingValue(SETTING_TEXT, String.class, "")); //$NON-NLS-1$
            }
        }
    }
}
