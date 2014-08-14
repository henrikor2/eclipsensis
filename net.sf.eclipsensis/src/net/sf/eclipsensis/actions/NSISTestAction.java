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

import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.util.NSISCompileTestUtility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.*;

public class NSISTestAction extends NSISScriptAction implements IElementStateListener
{
    @Override
    protected void started(IPath script)
    {
        if(mAction != null) {
            mAction.setEnabled(false);
        }
    }

    @Override
    protected void stopped(IPath script, MakeNSISResults results)
    {
        if(mAction != null) {
            mAction.setEnabled(isEnabled());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return (super.isEnabled() && (mEditor == null || !mEditor.isDirty()) && NSISCompileTestUtility.INSTANCE.canTest(getInput()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action)
    {
        NSISCompileTestUtility.INSTANCE.test(getInput());
    }

    public void elementContentAboutToBeReplaced(Object element)
    {
    }

    public void elementContentReplaced(Object element)
    {
    }

    public void elementDeleted(Object element)
    {
    }

    public void elementDirtyStateChanged(Object element, boolean isDirty)
    {
        if(mAction != null && mAction.isEnabled()) {
            mAction.setEnabled(!isDirty && isEnabled());
        }
    }

    public void elementMoved(Object originalElement, Object movedElement)
    {
    }

    @Override
    public void setActiveEditor(IEditorPart targetEditor)
    {
        if(mEditor != null) {
            IDocumentProvider provider = mEditor.getDocumentProvider();
            if(provider != null) {
                provider.removeElementStateListener(this);
            }
        }
        super.setActiveEditor(targetEditor);
        if(mEditor != null) {
            IDocumentProvider provider = mEditor.getDocumentProvider();
            if(provider != null) {
                provider.addElementStateListener(this);
            }
        }
    }
}
