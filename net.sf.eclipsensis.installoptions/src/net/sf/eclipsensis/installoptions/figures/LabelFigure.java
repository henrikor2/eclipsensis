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

import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart.ILabelFigure;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class LabelFigure extends UneditableElementFigure implements ILabelFigure
{
    public LabelFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public LabelFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    public boolean isMultiLine()
    {
        return true;
    }

    /**
     * @return
     */
    @Override
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Label label = new Label(parent, style);
        label.setText(isMultiLine()?TypeConverter.ESCAPED_STRING_CONVERTER.asString(mText):mText);
        return label;
    }

    /**
     * @return
     */
    @Override
    public int getDefaultStyle()
    {
        return SWT.LEFT|SWT.WRAP;
    }
}
