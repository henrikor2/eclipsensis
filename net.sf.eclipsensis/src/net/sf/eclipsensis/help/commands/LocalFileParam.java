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

import java.io.File;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.*;

public class LocalFileParam extends LocalFilesystemObjectParam
{
    public static final String TAG_FILTER = "filter"; //$NON-NLS-1$
    public static final String SETTING_FILE = "file"; //$NON-NLS-1$

    protected String[] mFilterNames;
    protected String[] mFilters;

    public LocalFileParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        loadFilters(node);
    }

    private void loadFilters(Node node)
    {
        List<String> filterNames = new ArrayList<String>();
        List<String> filters = new ArrayList<String>();
        Node[] children = XMLUtil.findChildren(node, TAG_FILTER);
        if(!Common.isEmptyArray(children)) {
            for (int i = 0; i < children.length; i++) {
                NamedNodeMap attr = children[i].getAttributes();
                String name = null;
                String value = XMLUtil.getStringValue(attr, ATTR_VALUE);
                if(!Common.isEmpty(value)) {
                    name = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(attr, ATTR_NAME));
                    filterNames.add(name==null?value:name);
                    filters.add(value);
                }
            }
        }
        mFilterNames = filterNames.toArray(new String[filterNames.size()]);
        mFilters = filters.toArray(new String[filters.size()]);
    }

    @Override
    protected LocalFilesystemObjectParamEditor createLocalFilesystemObjectParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new LocalFileParamEditor(command, parentEditor);
    }

    protected class LocalFileParamEditor extends LocalFilesystemObjectParamEditor
    {
        protected Text mFileText = null;

        public LocalFileParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        public void clear()
        {
            if(Common.isValid(mFileText)) {
                mFileText.setText(""); //$NON-NLS-1$
            }
            super.clear();
        }

        @Override
        protected String validateLocalFilesystemObjectParam()
        {
            if(Common.isValid(mFileText)) {
                String file = IOUtility.decodePath(mFileText.getText());
                if(file.length() == 0 ) {
                    if(isAllowBlank()) {
                        return null;
                    }
                    else {
                        return EclipseNSISPlugin.getResourceString("string.param.error"); //$NON-NLS-1$
                    }
                }
                if(IOUtility.isValidPathName(file)) {
                    if(IOUtility.isValidFile(new File(file))) {
                        return null;
                    }
                }
                return EclipseNSISPlugin.getResourceString("local.file.param.error"); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        protected String getPrefixableParamText()
        {
            if(Common.isValid(mFileText)) {
                String file = IOUtility.decodePath(mFileText.getText());
                return IOUtility.encodePath(file);
            }
            return null;
        }

        @Override
        protected void updateState(boolean state)
        {
            if(Common.isValid(mFileText)) {
                mFileText.setEnabled(state);
                Button b = (Button)mFileText.getData(DATA_BUTTON);
                if(Common.isValid(b)) {
                    b.setEnabled(state);
                }
            }
            super.updateState(state);
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if(Common.isValid(mFileText) && getSettings() != null) {
                getSettings().put(SETTING_FILE, mFileText.getText());
            }
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            if(Common.isValid(mFileText)) {
                mFileText.setText((String)getSettingValue(SETTING_FILE, String.class, "")); //$NON-NLS-1$
            }
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            Composite parent2 = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent2.setLayout(layout);
            mFileText = new Text(parent2,SWT.BORDER);
            setToolTip(mFileText);
            mFileText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
            final Button b = new Button(parent2,SWT.PUSH);
            b.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    FileDialog dialog = new FileDialog(b.getShell(),isSave()?SWT.SAVE:SWT.OPEN);
                    if(!Common.isEmptyArray(mFilters)) {
                        dialog.setFilterExtensions(mFilters);
                        dialog.setFilterNames(mFilterNames);
                    }
                    String file = IOUtility.decodePath(mFileText.getText());
                    if(!Common.isEmpty(file)) {
                        dialog.setFileName(file);
                    }
                    file = dialog.open();
                    if(file != null) {
                        mFileText.setText(IOUtility.encodePath(file));
                    }
                }
            });
            mFileText.setData(DATA_BUTTON,b);
            return parent2;
        }

        protected boolean isSave()
        {
            return false;
        }
    }
}
