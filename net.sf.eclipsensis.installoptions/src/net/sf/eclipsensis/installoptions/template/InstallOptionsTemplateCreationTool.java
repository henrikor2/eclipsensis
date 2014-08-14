/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.*;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.viewers.StructuredSelection;

public class InstallOptionsTemplateCreationTool extends CreationTool
{
    public InstallOptionsTemplateCreationTool(InstallOptionsTemplateCreationFactory factory)
    {
        super(factory);
    }

    public InstallOptionsTemplateCreationTool()
    {
        super();
    }

    @Override
    public void setFactory(CreationFactory factory)
    {
        if(factory instanceof InstallOptionsTemplateCreationFactory) {
            super.setFactory(factory);
        }
    }

    @Override
    protected Request createTargetRequest()
    {
        Request request = super.createTargetRequest();
        request.setType(IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE);
        return request;
    }

    @Override
    protected void performCreation(int button) {
        executeCurrentCommand();
        selectAddedObjects();
    }

    /*
     * Add the newly created object to the viewer's selected objects.
     */
    private void selectAddedObjects() {
        final Object[] models = (Object[])getCreateRequest().getNewObject();
        if(!Common.isEmptyArray(models)) {
            EditPartViewer viewer = getCurrentViewer();
            List<EditPart> selection = new ArrayList<EditPart>();
            for (int i = 0; i < models.length; i++) {
                Object editpart = viewer.getEditPartRegistry().get(models[i]);
                if (editpart instanceof EditPart) {
                    selection.add((EditPart) editpart);
                }
            }
            //Force the new object to get positioned in the viewer.
            viewer.flush();
            viewer.setSelection(new StructuredSelection(selection));
        }
    }

}
