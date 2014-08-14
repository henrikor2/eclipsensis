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
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.*;

public class SubCommandParam extends NSISParam
{
    public static final String ATTR_COMMAND = "command"; //$NON-NLS-1$
    public static final String TAG_SUBCOMMAND = "subcommand"; //$NON-NLS-1$
    public static final String SETTING_SUBCOMMAND = "subcommand"; //$NON-NLS-1$

    protected Map<String,String> mSubCommands;

    public SubCommandParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        loadSubCommands(node);
    }

    private void loadSubCommands(Node node)
    {
        mSubCommands = new LinkedHashMap<String,String>();
        Node[] children = XMLUtil.findChildren(node, TAG_SUBCOMMAND);
        if (!Common.isEmptyArray(children))
        {
            for (int i = 0; i < children.length; i++)
            {
                NamedNodeMap attr = children[i].getAttributes();
                String command = XMLUtil.getStringValue(attr, ATTR_COMMAND);
                if (!Common.isEmpty(command))
                {
                    String name = XMLUtil.getStringValue(attr, ATTR_NAME);
                    mSubCommands.put(command, (name == null ? command : name));
                }
            }
        }
    }

    @Override
    protected NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new SubCommandParamEditor(command, parentEditor);
    }

    protected class SubCommandParamEditor extends NSISParamEditor
    {
        private INSISParamEditor mCommandEditor = null;
        private ComboViewer mComboViewer;

        public SubCommandParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        public void clear()
        {
            if (mComboViewer != null && Common.isValid(mComboViewer.getControl()))
            {
                mComboViewer.setSelection(StructuredSelection.EMPTY);
            }
            super.clear();
        }

        @Override
        public void reset()
        {
            super.reset();
            if (mCommandEditor != null)
            {
                mCommandEditor.reset();
                mCommandEditor.dispose();
                mCommandEditor = null;
            }
        }

        @Override
        protected String validateParam()
        {
            if (isSelected())
            {
                if (mComboViewer != null && Common.isValid(mComboViewer.getControl()))
                {
                    if (mComboViewer.getSelection().isEmpty())
                    {
                        return EclipseNSISPlugin.getResourceString("sub.command.param.error"); //$NON-NLS-1$
                    }
                }
                if (mCommandEditor != null)
                {
                    return mCommandEditor.validate();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        protected Map.Entry<String,String> getCurrentCommand()
        {
            Map.Entry<String,String> command = null;
            if (mComboViewer != null && Common.isValid(mComboViewer.getControl()))
            {
                IStructuredSelection ssel = (IStructuredSelection) mComboViewer.getSelection();
                if (!ssel.isEmpty())
                {
                    command = (Map.Entry<String,String>) ssel.getFirstElement();
                }
            }
            else if (mSubCommands.size() == 1)
            {
                command = mSubCommands.entrySet().iterator().next();
            }
            return command;
        }

        @Override
        protected void appendParamText(StringBuffer buf, boolean preview)
        {
            if (mCommandEditor != null)
            {
                Map.Entry<String,String> command = getCurrentCommand();
                if (command != null)
                {
                    if (buf.length() > 0)
                    {
                        buf.append(" "); //$NON-NLS-1$
                    }
                    buf.append(command.getValue());
                    mCommandEditor.appendText(buf, preview);
                }
            }
        }

        @Override
        protected void updateState(boolean state)
        {
            super.updateState(state);
            if (mComboViewer != null)
            {
                if (Common.isValid(mComboViewer.getControl()))
                {
                    mComboViewer.getControl().setEnabled(state);
                }
            }
            if (mCommandEditor != null)
            {
                mCommandEditor.setEnabled(state);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setSettings(Map<String,Object> settings)
        {
            super.setSettings(settings);
            if (mCommandEditor != null)
            {
                if (settings != null)
                {
                    Map.Entry<String,String> command = getCurrentCommand();
                    if (command != null)
                    {
                        Map<String, Object> childSettings = (Map<String,Object>) settings.get(command.getKey());
                        if (childSettings == null)
                        {
                            childSettings = new HashMap<String,Object>();
                            settings.put(command.getKey(), childSettings);
                        }
                        mCommandEditor.setSettings(childSettings);
                    }
                }
                else
                {
                    mCommandEditor.setSettings(null);
                }
            }
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if (getSettings() != null)
            {
                Map.Entry<String,String> command = getCurrentCommand();
                if (command != null)
                {
                    getSettings().put(SETTING_SUBCOMMAND, command.getKey());
                }
                if (mCommandEditor != null)
                {
                    mCommandEditor.saveSettings();
                }
            }
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            if (mSubCommands.size() > 1)
            {
                final Composite container = new Composite(parent, SWT.NONE);
                GridLayout layout = new GridLayout(1, false);
                layout.marginHeight = layout.marginWidth = 0;
                container.setLayout(layout);

                Combo combo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
                combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

                mComboViewer = new ComboViewer(combo);
                mComboViewer.setContentProvider(new MapContentProvider());
                mComboViewer.setLabelProvider(new MapLabelProvider());

                mComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(final SelectionChangedEvent event)
                    {
                        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                            @SuppressWarnings("unchecked")
                            public void run()
                            {
                                boolean changed = false;
                                if (mCommandEditor != null && Common.isValid(mCommandEditor.getControl()))
                                {
                                    mCommandEditor.dispose();
                                    mCommandEditor = null;
                                    changed = true;
                                }
                                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                                if (!sel.isEmpty())
                                {
                                    String commandName = ((Map.Entry<String,String>) sel.getFirstElement()).getKey();
                                    NSISCommand cmd = NSISCommandManager.getCommand(commandName);
                                    if (cmd != null)
                                    {
                                        createCommandEditor(container, cmd);
                                        changed = true;
                                    }
                                }
                                if (changed)
                                {
                                    container.layout(true);
                                    Shell shell = container.getShell();
                                    Point size = shell.getSize();
                                    shell.setSize(size.x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                                }
                            }
                        });
                    }
                });

                mComboViewer.setInput(mSubCommands);
                return container;
            }
            else if (mSubCommands.size() == 1)
            {
                createCommandEditor(parent, NSISCommandManager.getCommand(getCurrentCommand().getKey()));
                return mCommandEditor.getControl();
            }
            else
            {
                return null;
            }
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            if (mComboViewer != null && Common.isValid(mComboViewer.getControl()))
            {
                String commandName = (String) getSettingValue(SETTING_SUBCOMMAND, String.class, null);
                for (Iterator<Map.Entry<String,String>> iter = mSubCommands.entrySet().iterator(); iter.hasNext();)
                {
                    Map.Entry<String,String> entry = iter.next();
                    if (entry.getKey().equals(commandName))
                    {
                        mComboViewer.setSelection(new StructuredSelection(entry));
                        break;
                    }
                }
            }
            if (mCommandEditor != null)
            {
                mCommandEditor.initEditor();
            }
        }

        /**
         * @param container
         * @param commandName
         * @param cmd
         */
        @SuppressWarnings("unchecked")
        private void createCommandEditor(Composite container, NSISCommand cmd)
        {
            mCommandEditor = cmd.createEditor();
            if (getSettings() != null)
            {
                Map<String, Object> commandSettings = (Map<String, Object>) getSettings().get(cmd.getName());
                if (commandSettings == null)
                {
                    commandSettings = new HashMap<String,Object>();
                    getSettings().put(cmd.getName(), commandSettings);
                }
                mCommandEditor.setSettings(commandSettings);
            }
            Control c = mCommandEditor.createControl(container);
            if (Common.isValid(c))
            {
                c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
            }
            mCommandEditor.initEditor();
        }
    }
}
