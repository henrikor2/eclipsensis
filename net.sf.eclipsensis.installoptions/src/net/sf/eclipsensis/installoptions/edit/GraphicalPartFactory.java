/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;


import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.*;

public class GraphicalPartFactory implements EditPartFactory
{
    public static final GraphicalPartFactory INSTANCE = new GraphicalPartFactory();

    private GraphicalPartFactory()
    {
    }

    public EditPart createEditPart(EditPart context, Object model)
    {
        EditPart child = null;

        if(model != null) {
            if(model instanceof InstallOptionsWidget) {
                InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(((InstallOptionsWidget)model).getType());
                if(typeDef != null) {
                    child = typeDef.createEditPart();
                }
            }
            else if (model instanceof InstallOptionsDialog) {
                child = new InstallOptionsDialogEditPart();
            }
            if (child != null) {
                child.setModel(model);
            }
        }
        return child;
    }

}
