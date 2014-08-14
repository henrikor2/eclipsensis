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

public class LineFigure extends SWTControlFigure
{
    public LineFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    @Override
    protected void setControlBounds(Control control, int x, int y, int width, int height)
    {
        boolean isHorizontal = (getStyle() & SWT.HORIZONTAL) > 0;
        super.setControlBounds(control, x, y, isHorizontal?width:2, isHorizontal?2:height);
    }

    @Override
    protected void handleClickThrough(Control control)
    {
    }

    @Override
    protected boolean isTransparentAt(int x, int y)
    {
        if( (getStyle() & SWT.HORIZONTAL) > 0) {
            return y > TRANSPARENCY_TOLERANCE + 2;
        }
        else {
            return x > TRANSPARENCY_TOLERANCE + 2;
        }
    }

    @Override
    public boolean isClickThrough()
    {
        return true;
    }

    @Override
    protected Control createSWTControl(Composite parent, int style)
    {
        return new Label(parent, SWT.SEPARATOR|style);
    }

    @Override
    public int getDefaultStyle()
    {
        return SWT.HORIZONTAL;
    }

    @Override
    protected boolean supportsScrollBars()
    {
        return false;
    }

}
