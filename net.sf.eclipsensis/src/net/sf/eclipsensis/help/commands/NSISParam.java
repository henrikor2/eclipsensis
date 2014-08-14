/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.*;

public abstract class NSISParam
{
    public static final String ATTR_INCLUDE_PREVIOUS = "includePrevious"; //$NON-NLS-1$
    public static final String ATTR_OPTIONAL = "optional"; //$NON-NLS-1$
    public static final String SETTING_OPTIONAL = ATTR_OPTIONAL;
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String TAG_PARAM = "param"; //$NON-NLS-1$
    public static final String ATTR_VALUE = "value"; //$NON-NLS-1$
    public static final String ATTR_TOOLTIP = "tooltip"; //$NON-NLS-1$

    private String mName;
    private boolean mOptional;
    private MessageFormat mErrorFormat;
    private String mToolTip;
    private boolean mIncludePrevious;

    public NSISParam(Node node)
    {
        init(node);
    }

    /**
     * @param node
     */
    protected void init(Node node)
    {
        mErrorFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("param.error.format")); //$NON-NLS-1$
        NamedNodeMap attributes = node.getAttributes();
        String name = XMLUtil.getStringValue(attributes, ATTR_NAME);
        if (!Common.isEmpty(name))
        {
            setName(name);
        }
        setOptional(XMLUtil.getBooleanValue(attributes, ATTR_OPTIONAL));
        mToolTip = XMLUtil.getStringValue(attributes, ATTR_TOOLTIP);
        setIncludePrevious(XMLUtil.getBooleanValue(node.getAttributes(), ATTR_INCLUDE_PREVIOUS));
    }

    protected boolean isIncludePrevious()
    {
        return mIncludePrevious;
    }

    protected void setIncludePrevious(boolean includePrevious)
    {
        mIncludePrevious = includePrevious;
    }

    public String getName()
    {
        return mName;
    }

    public boolean isOptional()
    {
        return mOptional;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public void setOptional(boolean optional)
    {
        mOptional = optional;
    }

    protected String maybeQuote(String text)
    {
        String text2 = text;
        if (shouldQuote(text2))
        {
            text2 = Common.quote(text2);
        }
        return text2;
    }

    protected boolean shouldQuote(String text)
    {
        boolean shouldQuote = false;
        if (!Common.isQuoted(text) && !Common.isQuoted(text, '\'') && !Common.isQuoted(text, '`'))
        {
            shouldQuote = Common.shouldQuote(text);
        }
        return shouldQuote;
    }

    public INSISParamEditor createEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return createParamEditor(command, parentEditor);
    }

    protected String getDefaultValue()
    {
        return null;
    }

    protected abstract NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor);

    protected abstract class NSISParamEditor implements INSISParamEditor
    {
        private NSISCommand mCommand = null;
        protected Control mControl = null;
        protected Button mOptionalButton = null;
        protected Label mNameLabel = null;
        protected List<INSISParamEditor> mDependents = null;
        private boolean mEnabled = true;
        private Map<String,Object> mSettings = null;
        private INSISParamEditor mParentEditor;
        private boolean mRequiredFields = false;

        public NSISParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            mCommand = command;
            mParentEditor = parentEditor;
        }

        public NSISCommand getCommand()
        {
            return mCommand;
        }

        public boolean hasRequiredFields()
        {
            if (!mRequiredFields)
            {
                List<INSISParamEditor> childEditors = getChildEditors();
                if (!Common.isEmptyCollection(childEditors))
                {
                    for (Iterator<INSISParamEditor> iter = childEditors.iterator(); iter.hasNext();)
                    {
                        INSISParamEditor child = iter.next();
                        if (child.hasRequiredFields())
                        {
                            return true;
                        }
                    }
                }
                return false;
            }
            return true;
        }

        public void clear()
        {
            if (Common.isValid(mOptionalButton))
            {
                mOptionalButton.setSelection(false);
            }
            updateState(isSelected());
        }

        public void reset()
        {
            // if(mNameLabel != null) {
            // NSISWizardDialogUtil.undecorate(mNameLabel);
            // }
        }

        public void dispose()
        {
            if (Common.isValid(mControl))
            {
                mControl.dispose();
            }
            if (Common.isValid(mOptionalButton))
            {
                mOptionalButton.dispose();
            }
            mParentEditor = null;
            List<INSISParamEditor> children = getChildEditors();
            if (!Common.isEmptyCollection(children))
            {
                for (Iterator<INSISParamEditor> iter = children.iterator(); iter.hasNext();)
                {
                    iter.next().dispose();
                }
            }
            mRequiredFields = false;
        }

        public INSISParamEditor getParentEditor()
        {
            return mParentEditor;
        }

        public List<INSISParamEditor> getChildEditors()
        {
            return Collections.emptyList();
        }

        public boolean isEnabled()
        {
            return mEnabled;
        }

        public Control getControl()
        {
            return mControl;
        }

        public void initEditor()
        {
            initParamEditor();
            updateEnabled();
        }

        protected void initParamEditor()
        {
            if (Common.isValid(mOptionalButton))
            {
                mOptionalButton
                        .setSelection(((Boolean) getSettingValue(SETTING_OPTIONAL, Boolean.class, Boolean.FALSE))
                                .booleanValue());
            }
        }

        protected boolean createMissing()
        {
            return true;
        }

        protected Object getSettingValue(String name, Class<?>clasz, Object defaultValue)
        {
            Object value;
            if (getSettings() != null)
            {
                value = getSettings().get(name);
                if (value != null)
                {
                    if (!clasz.isAssignableFrom(value.getClass()))
                    {
                        value = defaultValue;
                    }
                }
                else
                {
                    value = defaultValue;
                }
            }
            else
            {
                value = defaultValue;
            }
            return value;
        }

        public Control createControl(Composite parent)
        {
            Composite parent2 = parent;
            if (Common.isValid(mControl))
            {
                throw new RuntimeException(EclipseNSISPlugin.getResourceString("create.editor.error")); //$NON-NLS-1$
            }
            int availableGridCells = ((GridLayout) parent2.getLayout()).numColumns;
            int n = 0;
            Control[] children = parent2.getChildren();
            if (!Common.isEmptyArray(children))
            {
                for (int i = 0; i < children.length; i++)
                {
                    GridData layoutData = (GridData) children[i].getLayoutData();
                    n += layoutData.horizontalSpan;
                }
            }
            availableGridCells -= n % availableGridCells;
            boolean emptyName = Common.isEmpty(getName());
            int neededGridCells = (isOptional() ? 1 : 0) + (!emptyName ? 1 : 0) + 1;
            if (neededGridCells > availableGridCells)
            {
                parent2 = new Composite(parent2, SWT.None);
                GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
                data.horizontalSpan = availableGridCells;
                GridLayout layout = new GridLayout(neededGridCells, false);
                layout.marginHeight = layout.marginWidth = 0;
                parent2.setLayout(layout);
            }

            int horizontalSpan = 1;

            if (isOptional())
            {
                mOptionalButton = new Button(parent2, SWT.CHECK);
                GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
                mOptionalButton.setLayoutData(gridData);
                if (!emptyName)
                {
                    mOptionalButton.setText(getName());
                    gridData.horizontalSpan = 2;
                }
            }
            else
            {
                if (availableGridCells == 3 || availableGridCells == 2 && emptyName)
                {
                    if (createMissing())
                    {
                        Label l = new Label(parent2, SWT.None);
                        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
                        data.widthHint = 12;
                        l.setLayoutData(data);
                    }
                    else
                    {
                        horizontalSpan++;
                    }
                }
            }
            if (!emptyName)
            {
                if (!isOptional())
                {
                    mRequiredFields = shouldDecorate();
                    mNameLabel = NSISWizardDialogUtil.createLabel(parent2, null, true, null, mRequiredFields);
                    mNameLabel.setText(getName());
                    mNameLabel.addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e)
                        {
                            if (e.widget == mNameLabel)
                            {
                                mRequiredFields = false;
                            }
                        }
                    });
                    GridData data = (GridData) mNameLabel.getLayoutData();
                    data.horizontalAlignment = SWT.FILL;
                }
            }
            else
            {
                if (availableGridCells == 3)
                {
                    if (createMissing())
                    {
                        Label l = new Label(parent2, SWT.None);
                        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
                    }
                    else
                    {
                        horizontalSpan++;
                    }
                }
            }
            mControl = createParamControl(parent2);
            if (mControl != null)
            {
                GridData gridData = new GridData(SWT.FILL, (mControl instanceof Composite ? SWT.FILL : SWT.CENTER),
                        true, true);
                gridData.horizontalSpan = horizontalSpan;
                mControl.setLayoutData(gridData);
                if (mOptionalButton != null)
                {
                    mOptionalButton.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e)
                        {
                            updateState(mOptionalButton.getSelection());
                        }
                    });
                }
                mControl.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        mControl = null;
                    }
                });
            }
            else
            {
                if (mNameLabel != null)
                {
                    ((GridData) mNameLabel.getLayoutData()).horizontalSpan++;
                }
                else if (mOptionalButton != null)
                {
                    ((GridData) mOptionalButton.getLayoutData()).horizontalSpan++;
                }
            }
            return mControl;
        }

        protected boolean shouldDecorate()
        {
            return false;
        }

        /**
         *
         */
        protected void setToolTip(Control ctrl)
        {
            if (Common.isValid(ctrl))
            {
                if (!Common.isEmpty(mToolTip))
                {
                    ctrl.setToolTipText(mToolTip);
                }
                else if (!Common.isEmpty(mName))
                {
                    ctrl.setToolTipText(mName);
                }
            }
        }

        public final boolean isSelected()
        {
            if (isOptional())
            {
                if (Common.isValid(mOptionalButton))
                {
                    return mOptionalButton.getSelection();
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }

        public final List<INSISParamEditor> getDependents()
        {
            return mDependents;
        }

        public final void setDependents(List<INSISParamEditor> dependents)
        {
            mDependents = dependents;
            updateDependents(mEnabled && isSelected());
        }

        protected void updateState(boolean state)
        {
            if (Common.isValid(mNameLabel))
            {
                NSISWizardDialogUtil.setEnabled(mNameLabel, state);
            }
            if (Common.isValid(mControl))
            {
                mControl.setEnabled(state);
            }
            updateDependents(state);
        }

        /**
         * @param state
         */
        protected void updateDependents(boolean state)
        {
            if (!Common.isEmptyCollection(mDependents))
            {
                for (Iterator<INSISParamEditor> iter = mDependents.iterator(); iter.hasNext();)
                {
                    iter.next().setEnabled(state);
                }
            }
        }

        public final void setEnabled(boolean enabled)
        {
            mEnabled = enabled;
            updateEnabled();
        }

        /**
         * @param enabled
         */
        private void updateEnabled()
        {
            boolean enabled = mEnabled;
            if (Common.isValid(mOptionalButton))
            {
                mOptionalButton.setEnabled(enabled);
                if (enabled)
                {
                    enabled = mOptionalButton.getSelection();
                }
            }
            updateState(enabled);
        }

        public final void appendText(StringBuffer buf, boolean preview)
        {
            if (getSettings() != null && !preview)
            {
                saveSettings();
            }
            if (isSelected())
            {
                internalAppendText(buf, preview);
            }
        }

        protected void internalAppendText(StringBuffer buf, boolean preview)
        {
            if (isOptional() && isIncludePrevious())
            {
                INSISParamEditor parentEditor = getParentEditor();
                if (parentEditor != null)
                {
                    List<INSISParamEditor> children = parentEditor.getChildEditors();
                    if (!Common.isEmptyCollection(children))
                    {
                        int n = children.indexOf(this);
                        if (n > 0)
                        {
                            INSISParamEditor child = children.get(n - 1);
                            if (child instanceof NSISParamEditor && !child.isSelected())
                            {
                                ((NSISParamEditor) child).internalAppendText(buf, preview);
                            }
                        }
                    }
                }
            }
            if (!isSelected())
            {
                String defaultValue = getDefaultValue();
                if (defaultValue != null)
                {
                    buf.append(" ").append(maybeQuote(defaultValue)); //$NON-NLS-1$
                }
            }
            else
            {
                appendParamText(buf, preview);
            }
        }

        public final String validate()
        {
            if (isSelected())
            {
                return internalValidate();
            }
            return null;
        }

        protected String internalValidate()
        {
            String error = validateParam();
            if (error != null)
            {
                if (!Common.isEmpty(getName()))
                {
                    error = mErrorFormat.format(new String[] { getName(), error });
                }
                return error;
            }
            else
            {
                if (isOptional() && isIncludePrevious())
                {
                    INSISParamEditor parentEditor = getParentEditor();
                    if (parentEditor != null)
                    {
                        List<INSISParamEditor> children = parentEditor.getChildEditors();
                        if (!Common.isEmptyCollection(children))
                        {
                            int n = children.indexOf(this);
                            if (n > 1)
                            {
                                INSISParamEditor child = children.get(n - 1);
                                if (child instanceof NSISParamEditor && !child.isSelected())
                                {
                                    return ((NSISParamEditor) child).internalValidate();
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        public NSISParam getParam()
        {
            return NSISParam.this;
        }

        public Map<String,Object> getSettings()
        {
            return mSettings;
        }

        public void setSettings(Map<String,Object> settings)
        {
            mSettings = settings;
        }

        public void saveSettings()
        {
            if (Common.isValid(mOptionalButton) && getSettings() != null)
            {
                getSettings().put(SETTING_OPTIONAL, Boolean.valueOf(mOptionalButton.getSelection()));
            }
        }

        protected abstract void appendParamText(StringBuffer buf, boolean preview);

        protected abstract Control createParamControl(Composite parent);

        protected abstract String validateParam();
    }
}
