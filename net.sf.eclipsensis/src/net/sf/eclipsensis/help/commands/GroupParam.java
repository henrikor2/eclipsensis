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

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class GroupParam extends NSISParam
{
    public static final String ATTR_DEPENDS = "depends"; //$NON-NLS-1$
    public static final String SETTING_CHILD_SETTINGS = "childSettings"; //$NON-NLS-1$
    protected NSISParam[] mChildParams;
    protected Map<NSISParam, List<NSISParam>> mDependencies;

    public GroupParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        loadParams(node);
    }

    public NSISParam[] getChildParams()
    {
        return mChildParams;
    }

    @Override
    protected NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new GroupParamEditor(command, parentEditor);
    }

    private <T> void addDependent(Map<T,List<NSISParam>> map, T parent, NSISParam dependent)
    {
        List<NSISParam> dependents = map.get(parent);
        if(dependents == null) {
            dependents = new ArrayList<NSISParam>();
            map.put(parent, dependents);
        }
        dependents.add(dependent);
    }

    private void loadParams(Node node)
    {
        mDependencies = new HashMap<NSISParam, List<NSISParam>>();
        Map<Integer, List<NSISParam>> tempDependencies = new HashMap<Integer, List<NSISParam>>();
        List<NSISParam> params = new ArrayList<NSISParam>();
        Node[] children = XMLUtil.findChildren(node, TAG_PARAM);
        if(!Common.isEmptyArray(children)) {
            for (int i = 0; i < children.length; i++) {
                NSISParam param = NSISCommandManager.createParam(children[i]);
                if(param != null) {
                    params.add(param);
                    int depends = XMLUtil.getIntValue(children[i].getAttributes(), ATTR_DEPENDS,-1);
                    int index = params.size()-1;
                    if(depends >= 0 && depends != index) {
                        if(depends < params.size()) {
                            NSISParam dependsParam = params.get(depends);
                            if(dependsParam.isOptional()) {
                                addDependent(mDependencies, dependsParam, param);
                            }
                        }
                        else {
                            addDependent(tempDependencies, new Integer(depends), param);
                        }
                    }
                    List<NSISParam> dependents = tempDependencies.remove(new Integer(index));
                    if(dependents != null) {
                        if(param.isOptional()) {
                            mDependencies.put(param, dependents);
                        }
                    }
                }
            }
        }
        mChildParams = params.toArray(new NSISParam[params.size()]);
    }

    protected class GroupParamEditor extends NSISParamEditor
    {
        protected List<INSISParamEditor> mParamEditors;

        public GroupParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
            Map<NSISParam, INSISParamEditor> map = new HashMap<NSISParam, INSISParamEditor>();
            mParamEditors = new ArrayList<INSISParamEditor>(mChildParams.length);
            for (int i = 0; i < mChildParams.length; i++) {
                mParamEditors.add(mChildParams[i].createEditor(command, this));
                map.put(mChildParams[i], mParamEditors.get(i));
            }
            for (Iterator<NSISParam> iter= mDependencies.keySet().iterator(); iter.hasNext(); ) {
                NSISParam param = iter.next();
                List<NSISParam> dependents = mDependencies.get(param);
                if (!Common.isEmptyCollection(dependents)) {
                    INSISParamEditor parentEditor2 = map.get(param);
                    List<INSISParamEditor> list = new ArrayList<INSISParamEditor>();
                    for (Iterator<NSISParam> iterator = dependents.iterator(); iterator.hasNext();) {
                        list.add(map.get(iterator.next()));
                    }
                    parentEditor2.setDependents(list);
                }
            }
        }

        @Override
        public List<INSISParamEditor> getChildEditors()
        {
            return mParamEditors;
        }

        @Override
        protected void updateState(boolean state)
        {
            super.updateState(state);
            if(!Common.isEmptyCollection(mParamEditors)) {
                List<INSISParamEditor> dependents = new ArrayList<INSISParamEditor>();
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = iter.next();
                    if(!dependents.contains(editor)) {
                        editor.setEnabled(state);
                    }
                    List<INSISParamEditor> list = editor.getDependents();
                    if(!Common.isEmptyCollection(list)) {
                        dependents.addAll(list);
                    }
                }
            }
        }

        @Override
        public void clear()
        {
            if(!Common.isEmptyCollection(mParamEditors)) {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = iter.next();
                    editor.clear();
                }
            }
            super.clear();
        }

        @Override
        public void reset()
        {
            super.reset();
            if(mParamEditors.size() > 0) {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    iter.next().reset();
                }
            }
        }

        @Override
        protected String validateParam()
        {
            String validText = null;
            if(!Common.isEmptyCollection(mParamEditors)) {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    validText = iter.next().validate();
                    if(validText != null) {
                        break;
                    }
                }
            }
            return validText;
        }

        @Override
        protected void appendParamText(StringBuffer buf, boolean preview)
        {
            if(!Common.isEmptyCollection(mParamEditors)) {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    iter.next().appendText(buf, preview);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setSettings(Map<String, Object> settings)
        {
            super.setSettings(settings);
            if(!Common.isEmptyCollection((mParamEditors))) {
                int size = mParamEditors.size();
                if(settings != null) {
                    Map<String,Object>[] childSettings = (Map<String,Object>[]) settings.get(SETTING_CHILD_SETTINGS);
                    if(childSettings == null || childSettings.length != size) {
                        childSettings = new Map[size];
                        settings.put(SETTING_CHILD_SETTINGS, childSettings);
                    }
                    for (int i = 0; i < size; i++) {
                        if(childSettings[i] == null) {
                            childSettings[i] = new HashMap<String,Object>();
                        }
                        mParamEditors.get(i).setSettings(childSettings[i]);
                    }
                }
                else {
                    for (int i = 0; i < size; i++) {
                        mParamEditors.get(i).setSettings(null);
                    }
                }
            }
        }

        @Override
        public void saveSettings()
        {
            super.saveSettings();
            if(!Common.isEmptyCollection(mParamEditors) && getSettings() != null) {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    iter.next().saveSettings();
                }
            }
        }

        @Override
        protected boolean createMissing()
        {
            return false;
        }

        @Override
        protected Control createParamControl(Composite parent)
        {
            Composite composite = null;
            if(!Common.isEmptyArray(mChildParams)) {
                composite = new Group(parent,SWT.NONE);
                GridLayout layout = new GridLayout(1,false);
                layout.marginHeight = layout.marginWidth = 2;
                composite.setLayout(layout);
                layout.numColumns = getLayoutNumColumns();

                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = iter.next();
                    createChildParamControl(composite, editor);
                }
            }
            return composite;
        }

        @Override
        protected void initParamEditor()
        {
            super.initParamEditor();
            if(!Common.isEmptyCollection(mParamEditors)) {
                for (Iterator<INSISParamEditor> iter = mParamEditors.iterator(); iter.hasNext();) {
                    iter.next().initEditor();
                }
            }
        }

        protected int getLayoutNumColumns()
        {
            boolean isOptional = false;
            boolean hasName = false;
            for (int i = 0; i < mChildParams.length; i++) {
                if(!Common.isEmpty(mChildParams[i].getName())) {
                    hasName = true;
                }
                if(mChildParams[i].isOptional()) {
                    isOptional = true;
                }
            }

            return 1+(isOptional?1:0)+(hasName?1:0);
        }

        protected void createChildParamControl(Composite parent, INSISParamEditor editor)
        {
            editor.createControl(parent);
        }
    }
}
