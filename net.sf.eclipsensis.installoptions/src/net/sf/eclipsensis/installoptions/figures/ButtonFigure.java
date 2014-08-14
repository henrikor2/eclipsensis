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

public class ButtonFigure extends UneditableElementFigure
{
    public ButtonFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public ButtonFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    /**
     * @return
     */
    @Override
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Button button = new Button(parent, style);
        button.setText(getText());
        return button;
    }

    /**
     * @return
     */
    @Override
    public int getDefaultStyle()
    {
        return SWT.CENTER|SWT.PUSH;
    }
}
