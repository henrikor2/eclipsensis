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

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class ToggleDialogSizeVisibilityAction extends Action
{
    public static final String ID = "net.sf.eclipsensis.installoptions.toggle_dialog_size_visibiliy"; //$NON-NLS-1$
    private InstallOptionsDesignEditor mEditor;

    public ToggleDialogSizeVisibilityAction(InstallOptionsDesignEditor editor)
    {
        super(InstallOptionsPlugin.getResourceString("show.dialog.size.action.name"), AS_CHECK_BOX); //$NON-NLS-1$
        mEditor = editor;
        setToolTipText(InstallOptionsPlugin.getResourceString("show.dialog.size.tooltip")); //$NON-NLS-1$
        setId(ID);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.dialog.size.icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setEnabled(isEnabled());
        setChecked(isChecked());
    }

    @Override
    public void run()
    {
        if(isEnabled()) {
            if(isEnabled()) {
                GraphicalViewer viewer = mEditor.getGraphicalViewer();
                Boolean b = (Boolean)viewer.getProperty(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE);
                viewer.setProperty(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE,b.booleanValue()?Boolean.FALSE:Boolean.TRUE);
            }
        }
    }

    @Override
    public boolean isEnabled()
    {
        GraphicalViewer viewer = mEditor.getGraphicalViewer();
        if(viewer != null) {
            return (viewer.getProperty(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE) != null);
        }
        return false;
    }

    @Override
    public boolean isChecked()
    {
        if(isEnabled()) {
            GraphicalViewer viewer = mEditor.getGraphicalViewer();
            Boolean b = (Boolean)viewer.getProperty(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE);
            return b.booleanValue();
        }
        return false;
    }
}
