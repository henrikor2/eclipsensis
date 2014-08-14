/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.link;

import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditManager;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;

public class InstallOptionsLinkEditManager extends InstallOptionsLabelEditManager
{
    public InstallOptionsLinkEditManager(GraphicalEditPart source, CellEditorLocator locator)
    {
        super(source, locator);
    }
}
