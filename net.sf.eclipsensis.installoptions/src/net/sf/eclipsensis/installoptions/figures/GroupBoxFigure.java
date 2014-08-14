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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class GroupBoxFigure extends UneditableElementFigure
{
    public GroupBoxFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
        setOpaque(false);
    }

    public GroupBoxFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
        setOpaque(false);
    }

    @Override
    public boolean isClickThrough()
    {
        return true;
    }

    /**
     * @return
     */
    @Override
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Group group = new Group(parent, style);
        group.setText(mText);
        return group;
    }

    /**
     * @return
     */
    @Override
    public int getDefaultStyle()
    {
        return SWT.SHADOW_ETCHED_IN;
    }
}
