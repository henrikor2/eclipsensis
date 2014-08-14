/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listbox;

import java.util.List;

import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditManager;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.editors.ListCellEditor;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsListboxEditManager extends InstallOptionsEditableElementEditManager<ListCellEditor>
{
    public InstallOptionsListboxEditManager(GraphicalEditPart source, Class<ListCellEditor> editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    @Override
    protected ListCellEditor createCellEditor(Composite composite)
    {
        InstallOptionsListbox listbox = (InstallOptionsListbox)getEditPart().getModel();
        List<String> items = listbox.getListItems();
        ListCellEditor cellEditor = new ListCellEditor(composite,items,getCellEditorStyle());
        cellEditor.setCaseInsensitive(true);
        return cellEditor;
    }

    @Override
    protected void selectCellEditorText()
    {
    }

    @Override
    protected int getCellEditorStyle()
    {
        List<String> flags = ((InstallOptionsListbox)getEditPart().getModel()).getFlags();
        return (flags.contains(InstallOptionsModel.FLAGS_MULTISELECT)||
                flags.contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT))?SWT.MULTI:SWT.SINGLE;
    }
}
