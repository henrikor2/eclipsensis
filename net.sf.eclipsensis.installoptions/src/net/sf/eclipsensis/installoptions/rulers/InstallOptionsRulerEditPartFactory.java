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

import org.eclipse.gef.*;
import org.eclipse.gef.internal.ui.rulers.RulerEditPartFactory;

@SuppressWarnings("restriction")
public class InstallOptionsRulerEditPartFactory extends RulerEditPartFactory
{
    /**
     * @param primaryViewer
     */
    public InstallOptionsRulerEditPartFactory(GraphicalViewer primaryViewer)
    {
        super(primaryViewer);
    }

    @Override
    protected EditPart createRulerEditPart(EditPart parentEditPart, Object model) {
        return new InstallOptionsRulerEditPart(model);
    }

    @Override
    protected EditPart createGuideEditPart(EditPart parentEditPart, Object model)
    {
        return new InstallOptionsGuideEditPart(model);
    }
}
