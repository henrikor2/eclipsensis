/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class EditableElementFigure extends SWTControlFigure implements IEditableElementFigure
{
    protected String mState;

    public EditableElementFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public EditableElementFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    @Override
    protected void init(IPropertySource propertySource)
    {
        setState((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_STATE));
        super.init(propertySource);
   }

    public String getState()
    {
        return mState==null?"":mState; //$NON-NLS-1$
    }

    public void setState(String state)
    {
        mState = state;
    }

    @Override
    protected boolean supportsScrollBars()
    {
        return false;
    }
}
