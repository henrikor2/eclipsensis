/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.droplist;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.combobox.InstallOptionsComboboxEditPart;
import net.sf.eclipsensis.installoptions.properties.editors.EditableComboBoxCellEditor;

import org.eclipse.gef.tools.*;

public class InstallOptionsDropListEditPart extends InstallOptionsComboboxEditPart
{
    @Override
    protected String getDirectEditLabelProperty()
    {
        return "droplist.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getExtendedEditLabelProperty()
    {
        return "droplist.extended.edit.label"; //$NON-NLS-1$
    }

    /**
     * @return
     */
    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("droplist.type.name"); //$NON-NLS-1$
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class<EditableComboBoxCellEditor> clasz, CellEditorLocator locator)
    {
        return new InstallOptionsDropListEditManager(part,clasz,locator);
    }
}
