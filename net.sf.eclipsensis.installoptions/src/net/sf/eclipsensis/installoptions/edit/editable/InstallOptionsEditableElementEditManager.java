/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.editable;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsDirectEditManager;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;

public abstract class InstallOptionsEditableElementEditManager<T extends CellEditor> extends InstallOptionsDirectEditManager<T>
{
    public InstallOptionsEditableElementEditManager(GraphicalEditPart source, Class<T> editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    @Override
    protected String getInitialText(InstallOptionsWidget control)
    {
        return ((InstallOptionsEditableElement)control).getState();
    }

    protected abstract int getCellEditorStyle();

    @Override
    protected String getDirectEditProperty()
    {
        return InstallOptionsModel.PROPERTY_STATE;
    }
}
