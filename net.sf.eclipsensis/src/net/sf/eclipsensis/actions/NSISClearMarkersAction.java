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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class NSISClearMarkersAction extends NSISScriptAction
{
    private IResourceChangeListener mResourceChangeListener = new IResourceChangeListener() {
        public void resourceChanged(IResourceChangeEvent event)
        {
            IPath input = getInput();
            if(input != null) {
                IMarkerDelta[] deltas = event.findMarkerDeltas(INSISConstants.PROBLEM_MARKER_ID, true);
                if(deltas != null) {
                    for (int i = 0; i < deltas.length; i++) {
                        if(input.equals(deltas[i].getResource().getFullPath())) {
                            updateActionState();
                            return;
                        }
                    }
                }
            }
        }
    };

    public NSISClearMarkersAction()
    {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(mResourceChangeListener);
    }

    @Override
    protected boolean enableForHeader()
    {
        return true;
    }

    @Override
    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(mResourceChangeListener);
        super.dispose();
    }

    @Override
    protected void started(IPath script)
    {
        IPath assocScript = getAssociatedScript();
        if(mAction != null && mAction.isEnabled() && assocScript != null &&
           script.toString().equalsIgnoreCase(assocScript.toString())) {
            mAction.setEnabled(false);
        }
    }

    @Override
    protected void stopped(IPath script, MakeNSISResults results)
    {
        if(mAction != null) {
            IPath assocScript = getAssociatedScript();
            if(assocScript != null && script.toString().equalsIgnoreCase(assocScript.toString())) {
                if(!results.isCanceled()) {
                    return;
                }
            }
            mAction.setEnabled(isEnabled());
        }
    }

    @Override
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            IPath input = getInput();
            if(input != null) {
                return NSISEditorUtilities.hasMarkers(input);
            }
        }
        return false;
    }

    @Override
    public void run(IAction action)
    {
        NSISEditorUtilities.clearMarkers(getInput());
    }
}
