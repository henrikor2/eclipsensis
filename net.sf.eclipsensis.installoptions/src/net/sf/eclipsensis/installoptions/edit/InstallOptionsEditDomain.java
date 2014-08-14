/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.io.File;
import java.util.Stack;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.part.EditorActionBarContributor;

public class InstallOptionsEditDomain extends DefaultEditDomain implements IAdaptable
{
    private IFile[] mFiles = new IFile[1];
    private File mFile;
    private Tool mDefaultTool = new SelectionTool();
    private Shell mShell;

    /**
     * @param editorPart
     */
    public InstallOptionsEditDomain(IEditorPart editorPart)
    {
        super(editorPart);
        setDefaultTool(mDefaultTool);
        setCommandStack(new InstallOptionsCommandStack());
    }

    @Override
    public void setCommandStack(CommandStack stack)
    {
        if(stack instanceof InstallOptionsCommandStack) {
            super.setCommandStack(stack);
        }
    }

    private Shell getShell()
    {
        if(mShell == null) {
            IEditorPart editorPart = getEditorPart();
            if(editorPart != null) {
                IEditorSite site = editorPart.getEditorSite();
                if (site != null) {
                    IWorkbenchPage page = site.getPage();
                    if (page != null) {
                        IWorkbenchWindow window = page.getWorkbenchWindow();
                        if (window != null) {
                            mShell = window.getShell();
                        }
                    }
                }
            }
        }
        return mShell;
    }

    public boolean validateEdit()
    {
        if(mFiles[0] != null) {
            return ResourcesPlugin.getWorkspace().validateEdit(mFiles, getShell()).isOK();
        }
        else if (IOUtility.isValidFile(mFile)){
            if(!mFile.canWrite()) {
                if(Common.openQuestion(getShell(), InstallOptionsPlugin.getResourceString("read.only.question.title"), //$NON-NLS-1$
                                InstallOptionsPlugin.getFormattedString("read.only.question", new String[] {mFile.getAbsolutePath()}),  //$NON-NLS-1$
                                InstallOptionsPlugin.getShellImage())) {
                    int attributes = WinAPI.INSTANCE.getFileAttributes(mFile.getAbsolutePath());
                    if( (attributes & WinAPI.FILE_ATTRIBUTE_READONLY) > 0) {
                        WinAPI.INSTANCE.setFileAttributes(mFile.getAbsolutePath(), attributes & ~WinAPI.FILE_ATTRIBUTE_READONLY);
                    }
                }
                return mFile.canWrite();
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    public void setFile(File file)
    {
        mFile = file;
        mFiles[0] = null;
    }

    public void setFile(IFile file)
    {
        mFiles[0] = file;
        mFile = null;
    }

    private class InstallOptionsCommandStack extends CommandStack implements IModelCommandListener
    {
        private Stack<Command> mCurrentCommands = new Stack<Command>();

        @Override
        public synchronized void execute(Command command)
        {
            Command command2 = command;
            if(validateEdit()) {
                CompoundCommand cmd = new CompoundCommand(command2.getLabel());
                cmd.add(command2);
                command2 = cmd;
                mCurrentCommands.push(command2);
                super.execute(command2);
                mCurrentCommands.pop();
            }
            else {
                IEditorActionBarContributor contributor= getEditorPart().getEditorSite().getActionBarContributor();
                if (contributor instanceof EditorActionBarContributor) {
                    IActionBars actionBars= ((EditorActionBarContributor) contributor).getActionBars();
                    if (actionBars != null) {
                        IStatusLineManager manager = actionBars.getStatusLineManager();
                        if(manager != null) {
                            if(mFile != null) {
                                manager.setMessage(InstallOptionsPlugin.getFormattedString("read.only.error",new Object[]{mFile.getName()})); //$NON-NLS-1$
                            }
                            else if(mFiles[0] != null) {
                                manager.setMessage(InstallOptionsPlugin.getFormattedString("read.only.error",new Object[]{mFiles[0].getName()})); //$NON-NLS-1$
                            }
                        }
                    }
                }
                getEditorPart().getEditorSite().getShell().getDisplay().beep();
            }
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.installoptions.model.commands.IModelCommandListener#executeModelCommand(net.sf.eclipsensis.installoptions.model.commands.ModelCommandEvent)
         */
        public void executeModelCommand(ModelCommandEvent event)
        {
            Command command = event.getCommand();
            if(command != null) {
                if(mCurrentCommands.size() > 0) {
                    CompoundCommand current = (CompoundCommand)mCurrentCommands.peek();
                    current.add(command);
                }
                else {
                    execute(command);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if(adapter == IModelCommandListener.class) {
            return getCommandStack();
        }
        return null;
    }
}

