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

public class ComboFigure extends EditableElementFigure
{
    public ComboFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public ComboFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    /**
     * @return
     */
    @Override
    protected Control createSWTControl(Composite parent, int style)
    {
        Combo combo = new Combo(parent, style);
        String state = getState();
        combo.setText(state);
        return combo;
    }

    /**
     * @return
     */
    @Override
    public int getDefaultStyle()
    {
        return SWT.DROP_DOWN;
    }
}
