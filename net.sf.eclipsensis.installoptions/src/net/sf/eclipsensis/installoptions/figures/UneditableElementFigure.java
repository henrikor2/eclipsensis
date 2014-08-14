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

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class UneditableElementFigure extends SWTControlFigure implements IUneditableElementFigure
{
    protected String mText;

    public UneditableElementFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public UneditableElementFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    @Override
    protected void init(IPropertySource propertySource)
    {
        setText((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TEXT));
        super.init(propertySource);
   }

    public String getText()
    {
        return mText==null?"":mText; //$NON-NLS-1$
    }

    public void setText(String text)
    {
        mText = text;
    }

    @Override
    protected boolean supportsScrollBars()
    {
        return true;
    }

    @Override
    protected final Control createSWTControl(Composite parent, int style)
    {
        Control control = createUneditableSWTControl(parent, style);
        createScrollBars(control);
        return control;
    }

    protected abstract Control createUneditableSWTControl(Composite parent, int style);
}
