/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.gef.commands.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsCommandHelper
{
    private CommandStackListener mCommandStackListener;
    private CommandStack mStack;

    public InstallOptionsCommandHelper(CommandStack stack)
    {
        super();
        mStack = stack;
        if(stack != null) {
            mCommandStackListener = new CommandStackListener() {
                public void commandStackChanged(EventObject e) {
                    refresh();
                }
            };
            stack.addCommandStackListener(mCommandStackListener);
        }
    }

    public CommandStack getCommandStack()
    {
        return mStack;
    }

    public void dispose()
    {
        if (mStack != null) {
            mStack.removeCommandStackListener(mCommandStackListener);
        }
    }

    public Command createChangeTypeCommand(IPropertySource target, String displayName, Object propertyId, Object value)
    {
        if(target instanceof InstallOptionsWidget) {
            InstallOptionsWidget oldChild = (InstallOptionsWidget)target;
            if(oldChild.getParent() != null) {
                INIFile iniFile = oldChild.getParent().updateINIFile();
                String oldType = oldChild.getType();
                String newType = (String)value;
                if(!Common.stringsAreEqual(oldType, newType)) {
                    InstallOptionsElementFactory oldFactory = InstallOptionsElementFactory.getFactory(oldType);
                    InstallOptionsElementFactory newFactory = InstallOptionsElementFactory.getFactory(newType);
                    if(!oldFactory.getObjectType().equals(newFactory.getObjectType())) {
                        INISection oldSection = oldChild.updateSection();
                        final INISection section = (INISection)oldSection.copy();
                        INIKeyValue[] keyValues = section.findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                        if(!Common.isEmptyArray(keyValues)) {
                            keyValues[0].setValue(newType);
                        }
                        else {
                            INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_TYPE);
                            keyValue.setValue(newType);
                            section.addChild(keyValue);
                        }
                        section.validate(iniFile.getValidateFixMode());
                        if(section.hasErrors()) {
                            Display.getDefault().syncExec(new Runnable() {
                                private int getErrorsAndMaxLength(List<INIProblem> list, INILine line)
                                {
                                    int maxLength = 0;
                                    for(Iterator<INIProblem> iter=line.getErrors().iterator(); iter.hasNext(); ) {
                                        INIProblem error = iter.next();
                                        maxLength = Math.max(maxLength, error.getMessage().length());
                                        list.add(error);
                                        if(line instanceof INISection) {
                                            for(Iterator<INILine> iter2=section.getChildren().iterator(); iter2.hasNext(); ) {
                                                maxLength = Math.max(maxLength, getErrorsAndMaxLength(list,iter2.next()));
                                            }
                                        }
                                    }
                                    return maxLength;
                                }

                                public void run()
                                {
                                    ListDialog dialog = new ListDialog(Display.getCurrent().getActiveShell()) {
                                        {
                                            setShellStyle(getShellStyle()|SWT.RESIZE);
                                        }
                                        @Override
                                        protected int getTableStyle()
                                        {
                                            return super.getTableStyle() | SWT.READ_ONLY;
                                        }
                                    };
                                    dialog.setHelpAvailable(false);
                                    dialog.setAddCancelButton(false);
                                    ArrayList<INIProblem> list = new ArrayList<INIProblem>();
                                    int maxLength = getErrorsAndMaxLength(list, section);
                                    dialog.setWidthInChars(maxLength);
                                    dialog.setHeightInChars(list.size());
                                    dialog.setContentProvider(new CollectionContentProvider());
                                    dialog.setLabelProvider(new LabelProvider());
                                    dialog.setTitle(EclipseNSISPlugin.getResourceString("error.title")); //$NON-NLS-1$
                                    dialog.setMessage(InstallOptionsPlugin.getResourceString("change.type.command.error")); //$NON-NLS-1$
                                    dialog.setInput(list);
                                    dialog.open();
                                }
                            });
                            return null;
                        }
                        InstallOptionsWidget newChild = (InstallOptionsWidget)newFactory.getNewObject(section);
                        ChangeTypeCommand cmd = null;
                        cmd = new ChangeTypeCommand(oldChild.getParent(), oldChild, newChild);
                        return cmd;
                    }
                }
            }
            else {
                return null;
            }
        }
        return createSetValueCommand(target, displayName, propertyId, value);
    }

    public void resetPropertyValue(String id, IPropertySource[] sources)
    {
        CompoundCommand cc = new CompoundCommand();
        ResetValueCommand restoreCmd;

        for (int i = 0; i < sources.length; i++) {
            IPropertySource source = sources[i];
            if (source.isPropertySet(id)) {
                //source.resetPropertyValue(getDescriptor()getId());
                restoreCmd = new ResetValueCommand();
                restoreCmd.setTarget(source);
                restoreCmd.setPropertyId(id);
                cc.add(restoreCmd);
            }
        }
        if (cc.size() > 0) {
            mStack.execute(cc);
            refresh();
        }
    }

    public Command createSetValueCommand(IPropertySource target, String displayName, Object propertyId, Object value)
    {
        SetValueCommand setCommand = new SetValueCommand(displayName);
        setCommand.setTarget(target);
        setCommand.setPropertyId(propertyId);
        setCommand.setPropertyValue(value);
        return setCommand;
    }

    public void propertyChanged(String propertyId, String name, IPropertySource target, Object newValue)
    {
        CompoundCommand command = createPropertyChangedCommand(propertyId, name, target, newValue);
        if(command.size() > 0) {
            execute(command);
        }
    }

    /**
     * @param command
     */
    public void execute(Command command)
    {
        CommandStack stack = getCommandStack();
        if(stack != null) {
            stack.execute(command);
        }
        else {
            command.execute();
        }
    }

    /**
     * @param propertyId
     * @param name
     * @param target
     * @param newValue
     * @return
     */
    public CompoundCommand createPropertyChangedCommand(String propertyId, String name, IPropertySource target, Object newValue)
    {
        CompoundCommand command = new ForwardUndoCompoundCommand();
        valueChanged(propertyId, name, new IPropertySource[] {target}, new Object[] {newValue}, command);
        return command;
    }

    public void valueChanged(String propertyId, String name, IPropertySource[] targets, Object[] newValues, CompoundCommand command)
    {
        CompoundCommand cc = new CompoundCommand();
        command.add(cc);

        for (int i = 0; i < targets.length; i++) {
            IPropertySource target = targets[i];
            Object oldValue = target.getPropertyValue(propertyId);
            Object newValue = (newValues.length > i?newValues[i]:null);
            if(!Common.objectsAreEqual(oldValue, newValue)) {
                if(InstallOptionsModel.PROPERTY_TYPE.equals(propertyId)) {
                    cc.add(createChangeTypeCommand(target, name, propertyId, newValue));
                }
                else {
                    cc.add(createSetValueCommand(target, name, propertyId, newValue));
                }
            }
        }
    }

    protected abstract void refresh();
}
