/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public abstract class ComboParam extends PrefixableParam
{
    public static final String SETTING_SELECTED = "selected"; //$NON-NLS-1$
    public static final String ATTR_DEFAULT = "default"; //$NON-NLS-1$
    protected static final ComboEntry[] EMPTY_COMBO_ENTRIES = new ComboEntry[0];

    private int mDefaultIndex;

    public ComboParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        mDefaultIndex = XMLUtil.getIntValue(node.getAttributes(), ATTR_DEFAULT);
    }

    @Override
    protected PrefixableParamEditor createPrefixableParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new ComboParamEditor(command, parentEditor);
    }

    protected boolean isUserEditable()
    {
        return false;
    }

    protected String validateUserValue(String value)
    {
        return null;
    }

    @Override
    protected String getDefaultValue2()
    {
        if (!isAllowBlank())
        {
            ComboEntry[] entries = getComboEntries();
            if (!Common.isEmptyArray(entries))
            {
                int defaultIndex = mDefaultIndex < entries.length ? mDefaultIndex : 0;
                return entries[defaultIndex].getValue();
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected abstract ComboEntry[] getComboEntries();

    protected class ComboEntry
    {
        private String mValue = ""; //$NON-NLS-1$
        private String mDisplay = ""; //$NON-NLS-1$

        public ComboEntry(String value, String display)
        {
            mValue = value;
            mDisplay = display;
        }

        protected String getDisplay()
        {
            return mDisplay;
        }

        protected String getValue()
        {
            return mValue;
        }

        @Override
        public int hashCode()
        {
            int result = 31 + (mValue == null ? 0 : mValue.hashCode());
            result = 31 * result + (mDisplay == null ? 0 : mDisplay.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof ComboEntry)
            {
                if (obj == this)
                {
                    return true;
                }
                else
                {
                    return Common.stringsAreEqual(((ComboEntry) obj).getDisplay(), getDisplay())
                            && Common.stringsAreEqual(((ComboEntry) obj).getValue(), getValue());
                }
            }
            return false;
        }
    }

    protected class ComboParamEditor extends PrefixableParamEditor
    {
        protected ComboViewer mChoicesViewer = null;

        public ComboParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        protected String getPrefixableParamText()
        {
            if (mChoicesViewer != null && Common.isValid(mChoicesViewer.getCombo()))
            {
                IStructuredSelection sel = (IStructuredSelection) mChoicesViewer.getSelection();
                if (!sel.isEmpty())
                {
                    return ((ComboEntry) sel.getFirstElement()).getValue();
                }
                else if (isAllowBlank())
                {
                    return ""; //$NON-NLS-1$
                }
            }
            return null;
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if (mChoicesViewer != null && Common.isValid(mChoicesViewer.getCombo()) && getSettings() != null)
            {
                IStructuredSelection sel = (IStructuredSelection) mChoicesViewer.getSelection();
                if (!sel.isEmpty())
                {
                    getSettings().put(SETTING_SELECTED, ((ComboEntry) sel.getFirstElement()).getValue());
                }
                else if (isUserEditable())
                {
                    getSettings().put(SETTING_SELECTED, mChoicesViewer.getCombo().getText());
                }
                else
                {
                    getSettings().put(SETTING_SELECTED, null);
                }
            }
        }

        @Override
        public void clear()
        {
            selectDefault((ComboEntry[]) mChoicesViewer.getInput());
            super.clear();
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            Combo combo;
            if (mChoicesViewer != null && Common.isValid(combo = mChoicesViewer.getCombo()))
            {
                String selected = (String) getSettingValue(SETTING_SELECTED, String.class, null);
                ComboEntry entry = null;
                ComboEntry[] entries = (ComboEntry[]) mChoicesViewer.getInput();
                for (int i = 0; i < entries.length; i++)
                {
                    if (Common.stringsAreEqual(entries[i].getValue(), selected))
                    {
                        entry = entries[i];
                        break;
                    }
                }
                if (entry != null)
                {
                    mChoicesViewer.setSelection(new StructuredSelection(entry));
                }
                else if (selected != null && isUserEditable())
                {
                    combo.setText(selected);
                }
                else
                {
                    selectDefault(entries);
                }
            }
        }

        /**
         * @param entries
         */
        @SuppressWarnings("null")
        private void selectDefault(ComboEntry[] entries)
        {
            int defaultIndex = entries != null && mDefaultIndex < entries.length ? mDefaultIndex : -1;
            if (defaultIndex >= 0)
            {
                mChoicesViewer.setSelection(new StructuredSelection(entries[defaultIndex]));
            }
            else
            {
                mChoicesViewer.setSelection(StructuredSelection.EMPTY);
            }
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            Composite container = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = layout.marginWidth = 0;
            container.setLayout(layout);

            int style = SWT.DROP_DOWN;
            if (!isUserEditable())
            {
                style |= SWT.READ_ONLY;
            }
            Combo combo = new Combo(container, style);
            setToolTip(combo);
            combo.setLayoutData(new GridData(isUserEditable() ? SWT.FILL : SWT.LEFT, SWT.CENTER, isUserEditable(),
                    false));
            mChoicesViewer = new ComboViewer(combo);
            mChoicesViewer.setContentProvider(new ArrayContentProvider());
            mChoicesViewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element)
                {
                    if (element instanceof ComboEntry)
                    {
                        return ((ComboEntry) element).getDisplay();
                    }
                    return super.getText(element);
                }
            });
            ComboEntry[] entries = getComboEntries();
            mChoicesViewer.setInput(entries);
            return container;
        }

        @Override
        protected void updateState(boolean state)
        {
            super.updateState(state);
            if (mChoicesViewer != null && Common.isValid(mChoicesViewer.getCombo()))
            {
                mChoicesViewer.getCombo().setEnabled(state);
            }
        }

        @Override
        public String validateParam()
        {
            if (mChoicesViewer != null && Common.isValid(mChoicesViewer.getCombo()))
            {
                IStructuredSelection sel = (IStructuredSelection) mChoicesViewer.getSelection();
                if (!sel.isEmpty())
                {
                    return null;
                }
                if (isUserEditable())
                {
                    String value = mChoicesViewer.getCombo().getText();
                    if (value.length() > 0)
                    {
                        return validateUserValue(value);
                    }
                }
                if (isAllowBlank())
                {
                    return null;
                }
            }
            return EclipseNSISPlugin.getResourceString("combo.param.error"); //$NON-NLS-1$
        }
    }
}
