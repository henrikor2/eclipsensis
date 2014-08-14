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

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

public abstract class NSISAction extends ActionDelegate implements IObjectActionDelegate, IEditorActionDelegate, INSISConstants
{
    protected EclipseNSISPlugin mPlugin = null;
    protected IWorkbenchPart mPart = null;
    protected NSISEditor mEditor = null;
    protected IAction mAction = null;

    /**
     * The constructor.
     */
    public NSISAction()
    {
        mPlugin = EclipseNSISPlugin.getDefault();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    @Override
    public void init(IAction action)
    {
        super.init(action);
        mAction = action;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    @Override
    public void dispose()
    {
        super.dispose();
        mAction = null;
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        if(targetPart instanceof IEditorPart) {
            setActiveEditor((IEditorPart)targetPart);
        }
        else {
            mPart = targetPart;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public final void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        setActiveEditor(targetEditor);
    }

    /**
     * @param targetEditor
     */
    public void setActiveEditor(IEditorPart targetEditor)
    {
        if(mEditor != null) {
            mEditor.removeAction(this);
        }
        if(targetEditor instanceof NSISEditor) {
            mEditor = (NSISEditor)targetEditor;
        }
        else {
            mEditor = null;
        }
        if(mEditor != null) {
            mEditor.addAction(this);
        }
        mPart = mEditor;
    }
}