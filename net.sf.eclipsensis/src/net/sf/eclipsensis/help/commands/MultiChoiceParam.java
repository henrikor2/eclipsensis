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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class MultiChoiceParam extends ChoiceParam
{
    public MultiChoiceParam(Node node)
    {
        super(node);
    }

    @Override
    protected PrefixableParamEditor createPrefixableParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new MultiChoiceParamEditor(command, parentEditor);
    }

    protected class MultiChoiceParamEditor extends PrefixableParamEditor
    {
        public static final String DATA_CHOICE = "CHOICE"; //$NON-NLS-1$
        protected Button[] mChoiceButtons = null;

        public MultiChoiceParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        public void clear()
        {
            if (!Common.isEmptyArray(mChoiceButtons))
            {
                for (int i = 0; i < mChoiceButtons.length; i++)
                {
                    if (Common.isValid(mChoiceButtons[i]))
                    {
                        mChoiceButtons[i].setSelection(false);
                    }
                }
            }
            super.clear();
        }

        @Override
        @SuppressWarnings("null")
        protected String getPrefixableParamText()
        {
            if (!Common.isEmptyArray(mChoiceButtons))
            {
                boolean first = true;
                StringBuffer buf = null;
                for (int i = 0; i < mChoiceButtons.length; i++)
                {
                    if (Common.isValid(mChoiceButtons[i]) && mChoiceButtons[i].getSelection())
                    {
                        if (!first)
                        {
                            buf.append("|"); //$NON-NLS-1$
                        }
                        else
                        {
                            buf = new StringBuffer(""); //$NON-NLS-1$
                            first = false;
                        }
                        buf.append(mChoiceButtons[i].getData(DATA_CHOICE));
                    }
                }
                return buf != null ? buf.toString() : null;
            }
            return null;
        }

        @Override
        protected void updateState(boolean state)
        {
            super.updateState(state);
            if (!Common.isEmptyArray(mChoiceButtons))
            {
                for (int i = 0; i < mChoiceButtons.length; i++)
                {
                    if (Common.isValid(mChoiceButtons[i]))
                    {
                        mChoiceButtons[i].setEnabled(state);
                    }
                }
            }
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if (!Common.isEmptyArray(mChoiceButtons) && getSettings() != null)
            {
                for (int i = 0; i < mChoiceButtons.length; i++)
                {
                    if (Common.isValid(mChoiceButtons[i]))
                    {
                        getSettings().put((String)mChoiceButtons[i].getData(DATA_CHOICE),
                                Boolean.valueOf(mChoiceButtons[i].getSelection()));
                    }
                }
            }
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            Composite parent2 = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = layout.marginWidth = 0;
            parent2.setLayout(layout);
            ComboEntry[] choices = getComboEntries();
            if (!Common.isEmptyArray(choices))
            {
                layout.numColumns = Math.min(4, choices.length);
                mChoiceButtons = new Button[choices.length];
                for (int i = 0; i < choices.length; i++)
                {
                    Button b = new Button(parent2, SWT.CHECK);
                    b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
                    b.setText(choices[i].getDisplay());
                    b.setData(DATA_CHOICE, choices[i].getValue());
                    mChoiceButtons[i++] = b;
                }
            }
            return parent2;
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            if (!Common.isEmptyArray(mChoiceButtons))
            {
                for (int i = 0; i < mChoiceButtons.length; i++)
                {
                    if (Common.isValid(mChoiceButtons[i]))
                    {
                        mChoiceButtons[i].setSelection(((Boolean) getSettingValue((String) mChoiceButtons[i]
                                .getData(DATA_CHOICE), Boolean.class, Boolean.FALSE)).booleanValue());
                    }
                }
            }
        }

        @Override
        public String validateParam()
        {
            if (!Common.isEmptyArray(mChoiceButtons))
            {
                for (int i = 0; i < mChoiceButtons.length; i++)
                {
                    if (!Common.isValid(mChoiceButtons[i]))
                    {
                        break;
                    }
                    if (mChoiceButtons[i].getSelection())
                    {
                        return null;
                    }
                }
            }
            return EclipseNSISPlugin.getResourceString("multi.choice.param.error"); //$NON-NLS-1$
        }
    }
}
