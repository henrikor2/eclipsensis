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

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElementFactory;
import net.sf.eclipsensis.installoptions.template.*;

import org.eclipse.gef.*;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.*;
import org.eclipse.jface.viewers.StructuredSelection;

public class InstallOptionsTemplateTransferDropTargetListener extends TemplateTransferDropTargetListener
{
    /**
     * @param viewer
     */
    public InstallOptionsTemplateTransferDropTargetListener(EditPartViewer viewer)
    {
        super(viewer);
        setTransfer(InstallOptionsTemplateTransfer.INSTANCE);
    }

    @Override
    protected Request createTargetRequest()
    {
        Request request = super.createTargetRequest();
        if(request instanceof CreateRequest) {
            CreateRequest req = (CreateRequest)request;
            req.setFactory(getFactory(InstallOptionsTemplateTransfer.INSTANCE.getTemplate()));
            if(InstallOptionsTemplateCreationFactory.TYPE.equals(req.getNewObjectType())) {
                req.setType(IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE);
            }
        }
        return request;
    }

    @Override
    protected CreationFactory getFactory(Object type)
    {
        if (type instanceof String) {
            return InstallOptionsElementFactory.getFactory((String)type);
        }
        else if(type instanceof IInstallOptionsTemplate) {
            return InstallOptionsTemplateManager.INSTANCE.getTemplateFactory((IInstallOptionsTemplate)type);
        }
        return null;
    }

    @Override
    protected void handleDrop() {
        super.handleDrop();
        selectAddedObjects();
    }

    protected void selectAddedObjects()
    {
        Object model = getCreateRequest().getNewObject();
        if (model == null || !model.getClass().isArray()) {
            return;
        }
        EditPartViewer viewer = getViewer();
        viewer.getControl().forceFocus();
        Object[] models = (Object[])model;
        List<EditPart> selection = new ArrayList<EditPart>();
        for (int i = 0; i < models.length; i++) {
            Object editpart = viewer.getEditPartRegistry().get(models[i]);
            if (editpart instanceof EditPart) {
                selection.add((EditPart) editpart);
            }
        }
        if (selection.size() > 0) {
            //Force a layout first.
            getViewer().flush();
            viewer.setSelection(new StructuredSelection(selection));
        }
    }
}
