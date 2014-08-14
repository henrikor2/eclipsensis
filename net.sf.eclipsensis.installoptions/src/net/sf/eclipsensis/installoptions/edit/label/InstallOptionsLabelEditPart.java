/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.label;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.*;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.*;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class InstallOptionsLabelEditPart extends InstallOptionsUneditableElementEditPart
{
    protected static boolean cIsNT = "Windows NT".equals(System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$

    @Override
    protected String getDirectEditLabelProperty()
    {
        return "label.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        if(cIsNT) {
            //This is a hack because Windows NT Labels don't seem to respond to the
            //WM_PRINT message (see SWTControl.getImage(Control)
            return new NTLabelFigure(getInstallOptionsWidget());
        }
        else {
            return new LabelFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
    }

    @Override
    protected UneditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new UneditableElementDirectEditPolicy() {
            @Override
            protected String getDirectEditValue(DirectEditRequest edit)
            {
                return TypeConverter.ESCAPED_STRING_CONVERTER.asType(super.getDirectEditValue(edit));
            }
        };
    }

    /**
     * @return
     */
    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("label.type.name"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#creatDirectEditManager(net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart, java.lang.Class, org.eclipse.gef.tools.CellEditorLocator)
     */
    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return new InstallOptionsLabelEditManager(part, locator);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#createCellEditorLocator(net.sf.eclipsensis.installoptions.figures.UneditableElementFigure)
     */
    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new LabelCellEditorLocator((ILabelFigure)getFigure());
    }

    public static interface ILabelFigure extends IUneditableElementFigure
    {
        public boolean isMultiLine();
    }


    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    protected class NTLabelFigure extends NTFigure implements ILabelFigure
    {
        protected FlowPage mFlowPage;
        protected FlowPage mShadowFlowPage;
        protected TextFlow mShadowTextFlow;
        protected TextFlow mTextFlow;
        private String mText;

        public NTLabelFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }

        public boolean isMultiLine()
        {
            return true;
        }

        @Override
        protected void createChildFigures()
        {
            mShadowTextFlow = new TextFlow(""); //$NON-NLS-1$
            mShadowTextFlow.setFont(FontUtility.getInstallOptionsFont());
            mShadowFlowPage = new FlowPage();
            mShadowFlowPage.setVisible(false);
            mShadowFlowPage.add(mShadowTextFlow);
            add(mShadowFlowPage);

            mTextFlow = new TextFlow(""); //$NON-NLS-1$
            mTextFlow.setFont(FontUtility.getInstallOptionsFont());
            mFlowPage = new FlowPage();
            mFlowPage.add(mTextFlow);
            add(mFlowPage);
        }

        @Override
        protected void init(IPropertySource propertySource)
        {
            super.init(propertySource);
            mTextFlow.setForegroundColor(ColorManager.getColor(getTxtColor()));
            mShadowTextFlow.setForegroundColor(ColorManager.getSystemColor(WinAPI.COLOR_3DHILIGHT));
            setText((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TEXT));
        }

        public String getText()
        {
            return mText==null?"":mText; //$NON-NLS-1$
        }

        public void setText(String text)
        {
            if(!Common.stringsAreEqual(mText, text)) {
                mText = text;
                refresh();
            }
        }

        public RGB getTxtColor()
        {
            return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB();
        }

        @Override
        protected void setChildConstraints(Rectangle rect)
        {
            setConstraint(mFlowPage, new Rectangle(0,0,rect.width,rect.height));
            setConstraint(mShadowFlowPage, new Rectangle(1,1,rect.width,rect.height));
        }

        @Override
        public void refresh()
        {
            super.refresh();
            String text = isMultiLine()?TypeConverter.ESCAPED_STRING_CONVERTER.asString(getText()):getText();
            mTextFlow.setText(text);
            mShadowTextFlow.setText(text);
            mTextFlow.setForegroundColor((isDisabled()?ColorManager.getSystemColor(WinAPI.COLOR_GRAYTEXT):ColorManager.getColor(getTxtColor())));
            mShadowFlowPage.setVisible(isDisabled());
        }
    }
}
