/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.ui.rulers.*;

@SuppressWarnings("restriction")
public class InstallOptionsRulerDragTracker extends RulerDragTracker
{
    public InstallOptionsRulerDragTracker(RulerEditPart source)
    {
        super(source);
    }

    @Override
    protected boolean isCreationValid()
    {
        if(getCurrentPosition() < 0) {
            return false;
        }
        GraphicalViewer viewer = (GraphicalViewer)source.getViewer().getProperty(GraphicalViewer.class.toString());
        if(viewer == null) {
            return false;
        }
        return super.isCreationValid();
    }
}
