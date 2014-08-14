/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.label;

import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditManager;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.swt.SWT;

public class InstallOptionsLabelEditManager extends InstallOptionsUneditableElementEditManager
{
    public InstallOptionsLabelEditManager(GraphicalEditPart source, CellEditorLocator locator)
    {
        super(source, locator);
    }

    /**
     * @param control
     * @return
     */
    @Override
    protected String getInitialText(InstallOptionsWidget control)
    {
        if(((InstallOptionsLabel)control).isMultiLine()) {
            return TypeConverter.ESCAPED_STRING_CONVERTER.asString(((InstallOptionsUneditableElement)control).getText());
        }
        else {
            return super.getInitialText(control);
        }
    }

    @Override
    protected int getCellEditorStyle()
    {
        if(((InstallOptionsLabel)((InstallOptionsLabelEditPart)getEditPart()).getModel()).isMultiLine()) {
            return SWT.MULTI|SWT.LEFT|SWT.WRAP|SWT.V_SCROLL;
        }
        else {
            return SWT.LEFT;
        }
    }
}
