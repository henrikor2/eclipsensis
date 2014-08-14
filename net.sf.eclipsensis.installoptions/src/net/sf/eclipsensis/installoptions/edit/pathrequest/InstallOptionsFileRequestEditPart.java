/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.pathrequest;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.ModifyFilterCommand;
import net.sf.eclipsensis.installoptions.properties.dialogs.FileFilterDialog;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;
import net.sf.eclipsensis.installoptions.util.FileFilter;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;

public class InstallOptionsFileRequestEditPart extends InstallOptionsPathRequestEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsFileRequest model = (InstallOptionsFileRequest)getModel();
            FileFilterDialog dialog = new FileFilterDialog(getViewer().getControl().getShell(), model.getFilter());
            dialog.setValidator(new NSISStringLengthValidator(InstallOptionsPlugin.getResourceString("filter.property.name"))); //$NON-NLS-1$
            if (dialog.open() == Window.OK) {
                mNewValue = dialog.getFilter();
                return true;
            }
            else {
                return false;
            }
        }

        public Object getNewValue()
        {
            return mNewValue;
        }

    };

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class key)
    {
        if(IExtendedEditSupport.class.equals(key)) {
            return mExtendedEditSupport;
        }
        return super.getAdapter(key);
    }

    @Override
    protected String getDirectEditLabelProperty()
    {
        return "filerequest.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getExtendedEditLabelProperty()
    {
        return "filerequest.extended.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("filerequest.type.name"); //$NON-NLS-1$
    }

    @Override
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsExtendedEditPolicy(this) {
            @SuppressWarnings("unchecked")
            @Override
            protected Command getExtendedEditCommand(ExtendedEditRequest request)
            {
                ModifyFilterCommand command = new ModifyFilterCommand((InstallOptionsFileRequest)request.getEditPart().getModel(), (List<FileFilter>)request.getNewValue());
                return command;
            }

            @Override
            protected String getExtendedEditProperty()
            {
                return InstallOptionsModel.PROPERTY_FILTER;
            }
        });
    }
}