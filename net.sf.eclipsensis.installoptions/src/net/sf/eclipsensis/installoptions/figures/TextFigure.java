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

import java.util.List;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class TextFigure extends EditableElementFigure
{
    private boolean mMultiLine;
    private boolean mReadOnly;
    private boolean mNoWordWrap;
    private boolean mOnlyNumbers;

    public TextFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public TextFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    @Override
    protected void init(IPropertySource propertySource)
    {
        List<?> flags = (List<?>)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setOnlyNumbers(flags != null && flags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS));
        setMultiLine(flags != null && flags.contains(InstallOptionsModel.FLAGS_MULTILINE));
        setNoWordWrap(flags != null && flags.contains(InstallOptionsModel.FLAGS_NOWORDWRAP));
        setReadOnly(flags != null && flags.contains(InstallOptionsModel.FLAGS_READONLY));
        super.init(propertySource);
   }

    public boolean isOnlyNumbers()
    {
        return mOnlyNumbers;
    }

    public void setOnlyNumbers(boolean onlyNumbers)
    {
        mOnlyNumbers = onlyNumbers;
    }

    public boolean isMultiLine()
    {
        return mMultiLine;
    }

    public void setMultiLine(boolean multiLine)
    {
        mMultiLine = multiLine;
    }

    public boolean isNoWordWrap()
    {
        return mNoWordWrap;
    }

    public void setNoWordWrap(boolean noWordWrap)
    {
        mNoWordWrap = noWordWrap;
    }

    public boolean isReadOnly()
    {
        return mReadOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        mReadOnly = readOnly;
    }

    /**
     * @return
     */
    @Override
    protected Control createSWTControl(Composite parent, int style)
    {
        int style2 = style;
        if(!isMultiLine()) {
            if(isHScroll()) {
                style2 &= ~SWT.H_SCROLL;
            }
            if(isVScroll()) {
                style2 &= ~SWT.V_SCROLL;
            }
        }
        Text text = new Text(parent, style2);
        String state = getState();
        if(!isMultiLine()) {
            createScrollBars(text);
        }
        else {
            state = TypeConverter.ESCAPED_STRING_CONVERTER.asString(state);
        }
        text.setText(state);
        return text;
    }

    /**
     * @return
     */
    @Override
    public int getDefaultStyle()
    {
        int style = SWT.BORDER;
        if(mMultiLine) {
            style |= SWT.MULTI;
            if(!mNoWordWrap && !isHScroll()) {
                style |= SWT.WRAP;
            }
        }
        if(mReadOnly) {
            style |= SWT.READ_ONLY;
        }
        return style;
    }
}
