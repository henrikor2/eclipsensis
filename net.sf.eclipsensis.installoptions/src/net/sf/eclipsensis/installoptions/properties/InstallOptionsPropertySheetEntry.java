/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.*;
import org.eclipse.ui.views.properties.*;

public class InstallOptionsPropertySheetEntry extends PropertySheetEntry
{
    private static final String HELP_CONTEXT = IInstallOptionsConstants.PLUGIN_CONTEXT_PREFIX+"installoptions_designproperties_context"; //$NON-NLS-1$
    private InstallOptionsCommandHelper mHelper;

    private CommandStack mStack;

    private InstallOptionsPropertySheetEntry()
    {
    }

    public InstallOptionsPropertySheetEntry(CommandStack stack)
    {
        setCommandStack(stack);
    }

    /**
     * @see org.eclipse.ui.views.properties.PropertySheetEntry#createChildEntry()
     */
    @Override
    protected PropertySheetEntry createChildEntry()
    {
        return new InstallOptionsPropertySheetEntry();
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#dispose()
     */
    @Override
    public void dispose()
    {
        if(mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
        super.dispose();
    }

    private InstallOptionsCommandHelper getHelper()
    {
        if(mHelper == null) {
            mHelper = new InstallOptionsCommandHelper(getCommandStack()) {
                @Override
                protected void refresh()
                {
                    refreshFromRoot();
                }
            };
        }
        return mHelper;
    }

    @Override
    public Object getHelpContextIds()
    {
        Object helpContextIds = super.getHelpContextIds();
        if(helpContextIds == null) {
            helpContextIds = HELP_CONTEXT;
        }
        return helpContextIds;
    }

    private CommandStack getCommandStack()
    {
        //only the root has, and is listening too, the command stack
        if (getParent() != null) {
            return ((InstallOptionsPropertySheetEntry)getParent()).getCommandStack();
        }
        return mStack;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#resetPropertyValue()
     */
    @Override
    public void resetPropertyValue()
    {
        if (getParent() == null) {
            // root does not have a default value
            return;
        }

        //  Use our parent's values to reset our values.
        List<IPropertySource> sources = new ArrayList<IPropertySource>();
        Object[] objects = getParent().getValues();
        for (int i = 0; i < objects.length; i++) {
            IPropertySource source = getPropertySource(objects[i]);
            if (source != null) {
                sources.add(source);
            }
        }

        if(!Common.isEmptyCollection(sources)) {
            InstallOptionsCommandHelper helper = getHelper();
            helper.resetPropertyValue((String)getDescriptor().getId(), sources.toArray(new IPropertySource[sources.size()]));
        }
    }

    private void setCommandStack(CommandStack stack)
    {
        mStack = stack;
    }

    /**
     * @see PropertySheetEntry#valueChanged(PropertySheetEntry)
     */
    @Override
    protected void valueChanged(PropertySheetEntry child)
    {
        valueChanged((InstallOptionsPropertySheetEntry)child, new ForwardUndoCompoundCommand());
    }

    void valueChanged(InstallOptionsPropertySheetEntry child, CompoundCommand command)
    {
        List<IPropertySource> sources = new ArrayList<IPropertySource>();
        List<Object> newValues = new ArrayList<Object>();
        Object[] childValues = child.getValues();
        for (int i = 0; i < getValues().length; i++) {
            IPropertySource source = getPropertySource(getValues()[i]);
            if (source != null) {
                sources.add(source);
                newValues.add(childValues[i]);
            }
        }

        if(!Common.isEmptyCollection(sources)) {
            getHelper().valueChanged((String)child.getDescriptor().getId(),
                    child.getDisplayName(),
                    sources.toArray(new IPropertySource[sources.size()]),
                    newValues.toArray(new Object[newValues.size()]),
                    command);
        }

        // inform our parent
        if (getParent() != null) {
            ((InstallOptionsPropertySheetEntry)getParent()).valueChanged(this, command);
        }
        else {
            CommandStack stack = getCommandStack();
            //I am the root entry
            if(stack != null) {
                stack.execute(command);
            }
            else {
                command.execute();
            }
        }
    }
}