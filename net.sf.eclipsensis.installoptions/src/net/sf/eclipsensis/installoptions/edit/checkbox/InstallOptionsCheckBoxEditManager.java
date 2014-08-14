/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.checkbox;

import net.sf.eclipsensis.installoptions.edit.button.InstallOptionsButtonEditManager;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.swt.SWT;

public class InstallOptionsCheckBoxEditManager extends InstallOptionsButtonEditManager
{
    public InstallOptionsCheckBoxEditManager(GraphicalEditPart source, CellEditorLocator locator)
    {
        super(source, locator);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditManager#getCellEditorStyle()
     */
    @Override
    protected int getCellEditorStyle()
    {
        return SWT.SINGLE|SWT.LEFT;
    }
}
