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

import net.sf.eclipsensis.installoptions.edit.combobox.InstallOptionsComboboxEditManager;
import net.sf.eclipsensis.installoptions.properties.editors.EditableComboBoxCellEditor;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.swt.SWT;

public class InstallOptionsDropListEditManager extends InstallOptionsComboboxEditManager
{
    public InstallOptionsDropListEditManager(GraphicalEditPart source, Class<EditableComboBoxCellEditor> editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    @Override
    protected void selectCellEditorText()
    {
    }

    @Override
    protected int getCellEditorStyle()
    {
        return super.getCellEditorStyle()|SWT.READ_ONLY;
    }
}
