/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

public abstract class NSISScriptAction extends NSISAction implements IMakeNSISRunListener
{
    protected static final int TYPE_UNKNOWN = 0;
    protected static final int TYPE_SCRIPT = 1;
    protected static final int TYPE_HEADER = 2;

    private IPath mInput = null;
    private int mType = TYPE_UNKNOWN;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    @Override
    public void init(IAction action)
    {
        super.init(action);
        MakeNSISRunner.addListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    @Override
    public void dispose()
    {
        super.dispose();
        MakeNSISRunner.removeListener(this);
    }

    public int getType()
    {
        return mType;
    }

    @Override
    public void setActiveEditor(IEditorPart targetEditor)
    {
        super.setActiveEditor(targetEditor);
        updateInput();
    }

    protected void setInput(IPath input)
    {
        mInput = input;
    }

    protected IPath getInput()
    {
        return mInput;
    }

    /**
     *
     */
    public void updateInput()
    {
        IPath input = null;
        if(mEditor != null) {
            IPathEditorInput editorInput = NSISEditorUtilities.getPathEditorInput(mEditor);
            if(editorInput !=null) {
                if(editorInput instanceof IFileEditorInput) {
                    input = ((IFileEditorInput)editorInput).getFile().getFullPath();
                }
                else {
                    input = editorInput.getPath();
                }
            }
        }
        setInput(input);
        validateExtension();
        updateActionState();
    }

    private void validateExtension()
    {
        mType = TYPE_UNKNOWN;
        if(mInput != null) {
            String ext = mInput.getFileExtension();
            if(ext != null) {
                if(Common.stringsAreEqual(ext, NSI_EXTENSION, true)) {
                    mType = TYPE_SCRIPT;
                }
                else if(Common.stringsAreEqual(ext, NSH_EXTENSION, true)) {
                    mType = TYPE_HEADER;
                }
            }
        }
    }

    public void updateActionState()
    {
        if(mAction != null) {
            try {
                mAction.setEnabled(isEnabled());
            }
            catch(Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
            }
        }
    }

    protected final IPath getAssociatedScript()
    {
        return NSISCompileTestUtility.INSTANCE.getCompileScript(mInput);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {
        if(selection instanceof IStructuredSelection) {
            //This is for the popup context menu handling
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            if(!selection.isEmpty()) {
                setInput(((IFile)structuredSelection.getFirstElement()).getFullPath());
            }
            else {
                setInput(null);
            }
        }
        validateExtension();
        updateActionState();
    }

    public boolean isEnabled()
    {
        if (mPlugin != null && mPlugin.isConfigured()) {
            switch(mType) {
                case TYPE_SCRIPT:
                    return enableForScript();
                case TYPE_HEADER:
                    return enableForHeader();
            }
        }
        return false;
    }

    protected boolean enableForHeader()
    {
        return getAssociatedScript() != null;
    }

    protected boolean enableForScript()
    {
        return true;
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        switch(event.getType()) {
            case MakeNSISRunEvent.STARTED:
                started(event.getScript());
                break;
            case MakeNSISRunEvent.STOPPED:
                stopped(event.getScript(), (MakeNSISResults)event.getData());
                break;
        }
    }

    protected abstract void started(IPath script);
    protected abstract void stopped(IPath script, MakeNSISResults results);
}