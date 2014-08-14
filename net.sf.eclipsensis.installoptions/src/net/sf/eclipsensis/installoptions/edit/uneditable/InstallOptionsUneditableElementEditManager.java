/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.uneditable;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsDirectEditManager;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.*;

public abstract class InstallOptionsUneditableElementEditManager extends InstallOptionsDirectEditManager<TextCellEditor>
{
    public InstallOptionsUneditableElementEditManager(GraphicalEditPart source, CellEditorLocator locator)
    {
        super(source, TextCellEditor.class, locator);
    }

    @Override
    protected String getInitialText(InstallOptionsWidget control)
    {
        return ((InstallOptionsUneditableElement)control).getText();
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

    @Override
    protected String getDirectEditProperty()
    {
        return InstallOptionsModel.PROPERTY_TEXT;
    }

    protected abstract int getCellEditorStyle();
}
