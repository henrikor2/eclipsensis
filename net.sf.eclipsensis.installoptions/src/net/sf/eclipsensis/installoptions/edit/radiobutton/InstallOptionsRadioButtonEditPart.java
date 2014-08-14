/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.radiobutton;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.checkbox.InstallOptionsCheckBoxEditPart;
import net.sf.eclipsensis.installoptions.figures.*;

import org.eclipse.swt.widgets.Composite;

public class InstallOptionsRadioButtonEditPart extends InstallOptionsCheckBoxEditPart
{
    @Override
    protected String getDirectEditLabelProperty()
    {
        return "radiobutton.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getExtendedEditLabelProperty()
    {
        return "radiobutton.extended.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new RadioButtonFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("radiobutton.type.name"); //$NON-NLS-1$
    }
}
