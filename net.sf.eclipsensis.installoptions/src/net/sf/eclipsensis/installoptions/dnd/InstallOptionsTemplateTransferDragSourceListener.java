/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;

public class InstallOptionsTemplateTransferDragSourceListener extends TemplateTransferDragSourceListener
{
    public InstallOptionsTemplateTransferDragSourceListener(EditPartViewer viewer)
    {
        super(viewer);
        setTransfer(InstallOptionsTemplateTransfer.INSTANCE);
    }

    @Override
    public void dragFinished(DragSourceEvent event)
    {
        InstallOptionsTemplateTransfer.INSTANCE.setTemplate(null);
    }

    @Override
    public void dragStart(DragSourceEvent event)
    {
        Object template = getTemplate();
        if (template == null) {
            event.doit = false;
        }
        InstallOptionsTemplateTransfer.INSTANCE.setTemplate(template);
    }
}
