/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class RepeatableParam extends NSISParam
{
    public static final String ATTR_LABEL="label"; //$NON-NLS-1$
    public static final String SETTING_CHILD_SETTINGS = "childSettings"; //$NON-NLS-1$
    private static MessageFormat cAddFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("add.repeatable.param.format")); //$NON-NLS-1$
    private static MessageFormat cRemoveFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("remove.repeatable.param.format")); //$NON-NLS-1$

    private String mLabel;
    private NSISParam mChildParam;

    public RepeatableParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        mLabel = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(node.getAttributes(), ATTR_LABEL), ""); //$NON-NLS-1$
        mChildParam = loadChildParam(node);
    }

    protected NSISParam loadChildParam(Node node)
    {
        Node child = XMLUtil.findFirstChild(node, TAG_PARAM);
        if(child != null) {
            return NSISCommandManager.createParam(child);
        }
        return null;
    }

    @Override
    protected NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new RepeatableParamEditor(command, parentEditor);
    }

    protected class RepeatableParamEditor extends NSISParamEditor
    {
        public static final String DATA_PARENT = "PARENT"; //$NON-NLS-1$
        public static final String DATA_BUTTONS = "BUTTONS"; //$NON-NLS-1$
        private List<INSISParamEditor> mChildParamEditors = new ArrayList<INSISParamEditor>();

        public RepeatableParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        public void clear()
        {
            int n = mChildParamEditors.size();
            if (n > 1) {
                for (int i = 1; i < n; i++) {
                    disposeEditor(mChildParamEditors.remove(1));
                }
                updateControl((Composite)getControl());
            }
            (mChildParamEditors.get(0)).clear();
            super.clear();
        }

        /**
         * @param editor
         */
        private void disposeEditor(INSISParamEditor editor)
        {
            if(editor != null) {
                Control ctrl = editor.getControl();
                if(Common.isValid(ctrl)) {
                    Composite composite = (Composite)ctrl.getData(DATA_PARENT);
                    if(Common.isValid(composite)) {
                        composite.dispose();
                    }
                }
                editor.dispose();
            }
        }

        @Override
        protected String validateParam()
        {
            String error = null;
            for (Iterator<INSISParamEditor> iter = mChildParamEditors.iterator(); iter.hasNext();) {
                error = (iter.next()).validate();
                if(error != null) {
                    break;
                }
            }
            return error;
        }

        @Override
        protected void appendParamText(StringBuffer buf, boolean preview)
        {
            for (Iterator<INSISParamEditor> iter = mChildParamEditors.iterator(); iter.hasNext();) {
                (iter.next()).appendText(buf, preview);
            }
        }

        @Override
        public void setSettings(Map<String, Object> settings)
        {
            super.setSettings(settings);
            if(settings != null) {
                if(mChildParamEditors.size() > 0) {
                    List<Map<String,Object>> childSettingsList = getChildSettingsList();
                    ListIterator<INSISParamEditor> iter1 = mChildParamEditors.listIterator();
                    ListIterator<Map<String,Object>> iter2 = childSettingsList.listIterator();
                    for(;iter1.hasNext() || iter2.hasNext();) {
                        INSISParamEditor editor = null;
                        Map<String, Object> childSettings = null;
                        if(iter1.hasNext()) {
                            editor = iter1.next();
                        }
                        if(iter2.hasNext()) {
                            childSettings = iter2.next();
                        }
                        if(editor != null) {
                            if(childSettings == null) {
                                childSettings = new HashMap<String, Object>();
                            }
                            editor.setSettings(childSettings);
                        }
                    }
                }
            }
            else {
                for (Iterator<INSISParamEditor> iter = mChildParamEditors.iterator(); iter.hasNext();) {
                    (iter.next()).setSettings(null);
                }
            }
        }

        @Override
        public void saveSettings()
        {
            List<Map<String,Object>> childSettingsList = getChildSettingsList();
            if(childSettingsList != null) {
                childSettingsList.clear();
                if(mChildParamEditors.size() > 0) {
                    for (Iterator<INSISParamEditor> iter = mChildParamEditors.iterator(); iter.hasNext();) {
                        childSettingsList.add((iter.next()).getSettings());
                    }
                }
                getSettings().put(SETTING_CHILD_SETTINGS, childSettingsList);
            }
            super.saveSettings();
        }

        @SuppressWarnings("unchecked")
        private List<Map<String,Object>> getChildSettingsList()
        {
            List<Map<String,Object>> childSettingsList;
            if(getSettings() != null) {
                childSettingsList = (List<Map<String,Object>>)getSettingValue(SETTING_CHILD_SETTINGS, List.class, null);
                if(childSettingsList == null) {
                    childSettingsList = new ArrayList<Map<String,Object>>();
                }
            }
            else {
                childSettingsList = null;
            }
            return childSettingsList;
        }

        @Override
        public void reset()
        {
            super.reset();
            if (mChildParamEditors.size() > 0) {
                for (Iterator<INSISParamEditor> iter = mChildParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = iter.next();
                    editor.reset();
                    disposeEditor(editor);
                    iter.remove();
                }
                updateControl((Composite)getControl());
            }
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();

            if(getSettings() != null) {
                List<Map<String,Object>> childSettingsList = getChildSettingsList();
                if(childSettingsList.size() == 0) {
                    createChildParamEditor(0, new HashMap<String,Object>());
                }
                else {
                    int i=0;
                    for (Iterator<Map<String,Object>> iter = childSettingsList.iterator(); iter.hasNext();) {
                        createChildParamEditor(i++, iter.next());
                    }
                }
            }
            else {
                createChildParamEditor(0,null);
            }
            Composite container = (Composite)getControl();
            if(Common.isValid(container)) {
                for (Iterator<INSISParamEditor> iter = mChildParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = iter.next();
                    addEditor(container, editor);
                }
                updateControl(container);
            }
        }

        /**
         * @param childSettings
         */
        private INSISParamEditor createChildParamEditor(int index, Map<String, Object> childSettings)
        {
            INSISParamEditor editor = mChildParam.createEditor(getCommand(), this);
            editor.setSettings(childSettings);
            mChildParamEditors.add(index, editor);
            return editor;
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            Composite container = new Composite(parent,SWT.NONE);
            GridLayout gridLayout = new GridLayout(1,false);
            gridLayout.marginHeight = gridLayout.marginWidth = 0;
            container.setLayout(gridLayout);
            return container;
        }

        @Override
        protected boolean createMissing()
        {
            return false;
        }

        /**
         * @param parent
         * @param composite
         */
        private Control addEditor(final Composite container, final INSISParamEditor editor)
        {
            final Composite control = new Composite(container,SWT.NONE);
            GridLayout layout = new GridLayout(3,false);
            layout.marginHeight = layout.marginWidth = 0;
            layout.horizontalSpacing = 2;
            control.setLayout(layout);
            control.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

            Composite composite = new Composite(control,SWT.NONE);
            layout = new GridLayout();
            layout.marginHeight = layout.marginWidth = 0;
            layout.numColumns = (editor.getParam().isOptional()?1:0)+(Common.isEmpty(editor.getParam().getName())?0:1)+1;
            layout.makeColumnsEqualWidth = false;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
            Control c = editor.createControl(composite);
            if(c != null) {
                c.setData(DATA_PARENT,control);

                Object[] formatArgs = new Object[] {mLabel==null?Common.ZERO:new Integer(mLabel.length()), mLabel};
                c.setLayoutData(new GridData(SWT.FILL,(c instanceof Composite?SWT.FILL:SWT.CENTER),true,true));
                final Button delButton = new Button(control,SWT.PUSH);
                delButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
                delButton.setToolTipText(cRemoveFormat.format(formatArgs));
                final Button addButton = new Button(control,SWT.PUSH);
                addButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
                addButton.setToolTipText(cAddFormat.format(formatArgs));

                delButton.setImage(CommonImages.DELETE_SMALL_ICON);
                addButton.setImage(CommonImages.ADD_SMALL_ICON);

                delButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                            public void run()
                            {
                                mChildParamEditors.remove(editor);
                                editor.dispose();
                                control.dispose();
                                updateControl(container);
                            }
                        });
                    }
                });
                addButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        BusyIndicator.showWhile(Display.getCurrent(),new Runnable() {
                            public void run()
                            {
                                Control c = addEditor(container, createChildParamEditor(mChildParamEditors.indexOf(editor) + 1, getSettings() != null?new HashMap<String,Object>():null));
                                c.moveBelow(control);
                                updateControl(container);
                            }
                        });
                    }
                });
                c.setData(DATA_BUTTONS,new Button[] {delButton,addButton});
            }
            editor.initEditor();
            return control;
        }

        @Override
        protected void updateState(boolean state)
        {
            updateEditors(state);
            super.updateState(state);
        }

        /**
         * @param state
         */
        private void updateEditors(boolean state)
        {
            for (int i=0; i<mChildParamEditors.size(); i++) {
                INSISParamEditor editor = mChildParamEditors.get(i);
                if(editor != null) {
                    Control ctrl = editor.getControl();
                    if(Common.isValid(ctrl)) {
                        Button[] buttons = (Button[])ctrl.getData(DATA_BUTTONS);
                        if(buttons != null && buttons.length == 2) {
                            if(Common.isValid(buttons[0]) && Common.isValid(buttons[1])) {
                                if(state) {
                                    buttons[0].setEnabled(i != 0 || (mChildParamEditors.size() > 1));
                                    buttons[1].setEnabled(true);
                                }
                                else {
                                    buttons[0].setEnabled(false);
                                    buttons[1].setEnabled(false);
                                }
                            }
                        }
                    }
                    editor.setEnabled(state);
                }
            }
        }

        /**
         * @param container
         */
        private void updateControl(Composite container)
        {
            if(Common.isValid(container)) {
                container.layout(true,true);
                Shell shell = container.getShell();
                shell.layout(new Control[] {container});
                Point size = shell.getSize();
                shell.setSize(size.x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            }
            updateEditors(isSelected());
        }
    }
}
