/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class RadioParam extends GroupParam
{
    public static final String ATTR_DEFAULT = "default"; //$NON-NLS-1$
    public static final String SETTING_RADIO_SELECTED = "radioSelected"; //$NON-NLS-1$

    private int mDefaultIndex;

    public RadioParam(Node node)
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
    protected NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new RadioParamEditor(command, parentEditor);
    }

    protected class RadioParamEditor extends GroupParamEditor
    {
        private Map<INSISParamEditor,Button> mRadioButtonMap = new HashMap<INSISParamEditor,Button>();

        public RadioParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        protected void appendParamText(StringBuffer buf, boolean preview)
        {
            if (!Common.isEmptyCollection(mParamEditors))
            {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();)
                {
                    INSISParamEditor editor = iter.next();
                    Button radioButton = mRadioButtonMap.get(editor);
                    if (Common.isValid(radioButton))
                    {
                        if (radioButton.getSelection())
                        {
                            editor.appendText(buf, preview);
                            return;
                        }
                    }
                    else
                    {
                        return;
                    }
                }
            }
        }

        @Override
        public void clear()
        {
            selectDefault();
            super.clear();
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            if (!Common.isEmptyCollection(mParamEditors))
            {
                int i = ((Integer) getSettingValue(SETTING_RADIO_SELECTED, Integer.class, new Integer(-1))).intValue();
                if (i >= 0 && i < mParamEditors.size())
                {
                    Button radioButton = mRadioButtonMap.get(mParamEditors.get(i));
                    if (Common.isValid(radioButton))
                    {
                        radioButton.setSelection(true);
                        return;
                    }
                }
                selectDefault();
            }
        }

        /**
         *
         */
        private void selectDefault()
        {
            int defaultIndex = mDefaultIndex < mParamEditors.size() ? mDefaultIndex : 0;
            Button radioButton = mRadioButtonMap.get(mParamEditors.get(defaultIndex));
            if (Common.isValid(radioButton))
            {
                radioButton.setSelection(true);
            }
        }

        @Override
        protected int getLayoutNumColumns()
        {
            return 1 + super.getLayoutNumColumns();
        }

        @Override
        protected void createChildParamControl(Composite parent, final INSISParamEditor editor)
        {
            final Button radioButton = new Button(parent, SWT.RADIO);
            radioButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            mRadioButtonMap.put(editor, radioButton);
            super.createChildParamControl(parent, editor);
            if (editor instanceof NSISParamEditor)
            {
                Label nameLabel = ((NSISParamEditor) editor).mNameLabel;
                if (Common.isValid(nameLabel))
                {
                    // Steal the name
                    radioButton.setText(nameLabel.getText());
                    nameLabel.setText(""); //$NON-NLS-1$

                    Composite parent2 = nameLabel.getParent();
                    GridLayout layout = (GridLayout) parent2.getLayout();
                    GridData data = (GridData) nameLabel.getLayoutData();
                    if (layout.numColumns > data.horizontalSpan)
                    {
                        GridData data2;
                        if (Common.isValid(((NSISParamEditor) editor).mControl))
                        {
                            data2 = (GridData) ((NSISParamEditor) editor).mControl.getLayoutData();
                        }
                        else
                        {
                            data2 = (GridData) radioButton.getLayoutData();
                        }
                        data2.horizontalSpan += data.horizontalSpan;
                        data2.grabExcessHorizontalSpace |= data.grabExcessHorizontalSpace;
                        data2.grabExcessVerticalSpace |= data.grabExcessVerticalSpace;
                        nameLabel.dispose();
                        parent2.layout(true);
                    }
                }
            }
            radioButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    editor.setEnabled(radioButton.getSelection());
                }
            });
            editor.setEnabled(radioButton.getSelection());
        }

        @Override
        protected void updateState(boolean state)
        {
            super.updateState(state);
            if (!Common.isEmptyCollection(mParamEditors))
            {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();)
                {
                    INSISParamEditor editor = iter.next();
                    Button radioButton = mRadioButtonMap.get(editor);
                    if (Common.isValid(radioButton))
                    {
                        radioButton.setEnabled(state);
                        if (state)
                        {
                            editor.setEnabled(radioButton.getSelection());
                        }
                        else
                        {
                            editor.setEnabled(state);
                        }
                    }
                }
            }
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if (!Common.isEmptyCollection(mParamEditors))
            {
                int i = 0;
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();)
                {
                    INSISParamEditor editor = iter.next();
                    Button radioButton = mRadioButtonMap.get(editor);
                    if (Common.isValid(radioButton))
                    {
                        if (radioButton.getSelection())
                        {
                            getSettings().put(SETTING_RADIO_SELECTED, new Integer(i));
                            return;
                        }
                    }
                    i++;
                }
            }
        }

        @Override
        protected String validateParam()
        {
            if (!Common.isEmptyCollection(mParamEditors))
            {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();)
                {
                    INSISParamEditor editor = iter.next();
                    Button radioButton = mRadioButtonMap.get(editor);
                    if (Common.isValid(radioButton))
                    {
                        if (radioButton.getSelection())
                        {
                            return editor.validate();
                        }
                    }
                }
                return EclipseNSISPlugin.getResourceString("radio.param.error"); //$NON-NLS-1$
            }
            return null;
        }
    }
}
