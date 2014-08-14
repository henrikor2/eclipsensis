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

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class NSISOpenAssociatedScriptOrHeadersAction extends NSISScriptAction
{
    private IFile mFile = null;
    private boolean mOpenAssociatedScriptAction = false;
    private boolean mOpenAssociatedHeadersAction = false;

    @Override
    protected void started(IPath script)
    {
    }

    @Override
    protected void stopped(IPath script, MakeNSISResults results)
    {
    }

    @Override
    public void run(IAction action)
    {
        if(mFile != null) {
            IWorkbenchPage page = null;
            if(mPart != null) {
                page = mPart.getSite().getPage();
            }
            NSISEditorUtilities.openAssociatedFiles(page,mFile);
        }
    }

    @Override
    protected void setInput(IPath input)
    {
        super.setInput(input);
        mFile = null;
        if(input != null && input.getDevice() == null) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(input);
            if(file != null) {
                String ext = file.getFileExtension();
                if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,ext,true)) {
                    mFile = file;
                }
                else if(Common.stringsAreEqual(INSISConstants.NSH_EXTENSION,ext,true)) {
                    mFile = file;
                }
            }
        }
    }

    @Override
    public void init(IAction action)
    {
        super.init(action);
        if(INSISConstants.OPEN_ASSOCIATED_HEADERS_ACTION_ID.equals(action.getId())||
           INSISConstants.OPEN_ASSOCIATED_HEADERS_POPUP_MENU_ID.equals(action.getId())) {
            mOpenAssociatedHeadersAction = true;
            mOpenAssociatedScriptAction = false;
        }
        else if(INSISConstants.OPEN_ASSOCIATED_SCRIPT_ACTION_ID.equals(action.getId())||
                INSISConstants.OPEN_ASSOCIATED_SCRIPT_POPUP_MENU_ID.equals(action.getId())) {
            mOpenAssociatedHeadersAction = false;
            mOpenAssociatedScriptAction = true;
        }
        else {
            mOpenAssociatedHeadersAction = false;
            mOpenAssociatedScriptAction = false;
        }
    }

    @Override
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            if(mFile != null) {
                String ext = mFile.getFileExtension();
                if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,ext,true) && mOpenAssociatedHeadersAction) {
                    return !Common.isEmptyCollection(NSISHeaderAssociationManager.getInstance().getAssociatedHeaders(mFile));
                }
                else if(Common.stringsAreEqual(INSISConstants.NSH_EXTENSION,ext,true) && mOpenAssociatedScriptAction) {
                    return (NSISHeaderAssociationManager.getInstance().getAssociatedScript(mFile) != null);
                }
            }
        }
        return false;
    }
}
