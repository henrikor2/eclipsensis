/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.text;

import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditManager;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.*;

public abstract class InstallOptionsGenericTextEditManager extends InstallOptionsEditableElementEditManager<TextCellEditor>
{
    public InstallOptionsGenericTextEditManager(GraphicalEditPart source, Class<TextCellEditor> editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    @Override
    protected void selectCellEditorText()
    {
        Text text = (Text)getCellEditor().getControl();
        text.selectAll();
    }

    @Override
    protected TextCellEditor createCellEditor(Composite composite)
    {
        return new TextCellEditor(composite, getCellEditorStyle());
    }
}
