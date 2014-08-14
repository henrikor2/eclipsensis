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
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.winapi.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class CheckBoxFigure extends ButtonFigure
{
    protected boolean mState;
    protected boolean mLeftText;

    public CheckBoxFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public CheckBoxFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void init(IPropertySource propertySource)
    {
        List<String> flags = (List<String>)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setLeftText(flags != null && flags.contains(InstallOptionsModel.FLAGS_RIGHT));
        setState(InstallOptionsModel.STATE_CHECKED.equals(propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_STATE)));
        super.init(propertySource);
    }

    public void setState(boolean state)
    {
        mState = state;
    }

    public void setLeftText(boolean leftText)
    {
        mLeftText = leftText;
    }

    public boolean isLeftText()
    {
        return mLeftText;
    }

    @Override
    public int getDefaultStyle()
    {
        return SWT.LEFT|SWT.CHECK;
    }

    /**
     * @return
     */
    @Override
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Button button = (Button)super.createUneditableSWTControl(parent, style);
        button.setSelection(mState);
        if(mLeftText) {
            IHandle handle = Common.getControlHandle(button);
            WinAPI.INSTANCE.setWindowLong(handle,WinAPI.GWL_STYLE,
                            WinAPI.INSTANCE.getWindowLong(handle,WinAPI.GWL_STYLE)|WinAPI.BS_LEFTTEXT);
        }
        return button;
    }
}
