/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.texteditor.IUpdate;

public class RefreshDiagramAction extends Action implements IUpdate
{
    public static final String ID = "net.sf.eclipsensis.installoptions.refresh_diagram"; //$NON-NLS-1$
    private InstallOptionsDesignEditor mEditor = null;

    public RefreshDiagramAction(InstallOptionsDesignEditor editor)
    {
        super(InstallOptionsPlugin.getResourceString("refresh.diagram.action.label")); //$NON-NLS-1$
        mEditor = editor;
        setToolTipText(InstallOptionsPlugin.getResourceString("refresh.diagram.action.tooltip")); //$NON-NLS-1$
        setId(ID);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("refresh.icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("refresh.disabled.icon"))); //$NON-NLS-1$
        setEnabled(isEnabled());
    }

    @Override
    public void run()
    {
        if(isEnabled()) {
            ((InstallOptionsDialogEditPart)mEditor.getGraphicalViewer().getContents()).refreshDiagram();
        }
    }

    @Override
    public boolean isEnabled()
    {
        if (mEditor != null && !mEditor.isDisposed() && mEditor.getGraphicalViewer() != null
            && mEditor.getGraphicalViewer().getContents() != null) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    public void update()
    {
        super.setEnabled(isEnabled());
    }
}
