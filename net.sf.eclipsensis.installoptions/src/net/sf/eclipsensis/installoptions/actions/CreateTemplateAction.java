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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.template.InstallOptionsTemplateDialog;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;

public class CreateTemplateAction extends SelectionAction
{
    public static final String ID = "net.sf.eclipsensis.installoptions.create_template"; //$NON-NLS-1$
    /**
     * @param part
     */
    public CreateTemplateAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(false);
    }

    /**
     * Initializes this action's text and images.
     */
    @Override
    protected void init()
    {
        super.init();
        setText(InstallOptionsPlugin.getResourceString("create.template.action.label")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("create.template.action.tooltip")); //$NON-NLS-1$
        setId(ID);
        ImageDescriptor descriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.template.icon")); //$NON-NLS-1$
        setHoverImageDescriptor(descriptor);
        setImageDescriptor(descriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.template.disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
    }

    @Override
    protected boolean calculateEnabled()
    {
        List<?> objects = getSelectedObjects();
        if (objects.isEmpty()) {
            return false;
        }

        for (Iterator<?> iter = objects.iterator(); iter.hasNext();) {
            Object object = iter.next();
            if(!(object instanceof InstallOptionsWidgetEditPart)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void run()
    {
        List<InstallOptionsWidget> objects = new ArrayList<InstallOptionsWidget>();

        for (Iterator<?> iter = getSelectedObjects().iterator(); iter.hasNext();) {
            InstallOptionsWidgetEditPart object = (InstallOptionsWidgetEditPart)iter.next();
            objects.add((InstallOptionsWidget)object.getModel());
        }

        InstallOptionsTemplateDialog dialog = new InstallOptionsTemplateDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), objects.toArray(new InstallOptionsWidget[objects.size()]));
        dialog.open();
    }
}
