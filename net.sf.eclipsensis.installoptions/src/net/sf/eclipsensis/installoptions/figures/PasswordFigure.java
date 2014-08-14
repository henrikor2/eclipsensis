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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySource;

public class PasswordFigure extends TextFigure
{
    public PasswordFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public PasswordFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    @Override
    protected void init(IPropertySource propertySource)
    {
        super.init(propertySource);
        setOnlyNumbers(false);
        setMultiLine(false);
        setNoWordWrap(true);
        setReadOnly(false);
   }

    @Override
    public int getDefaultStyle()
    {
        int style = super.getDefaultStyle();
        return style|SWT.PASSWORD;
    }
}
