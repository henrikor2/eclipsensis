/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.link;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.UneditableElementDirectEditPolicy;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.tools.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class InstallOptionsLinkEditPart extends InstallOptionsLabelEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsLink model = (InstallOptionsLink)getModel();
            ColorDialog dialog = new ColorDialog(getViewer().getControl().getShell());
            RGB value = model.getTxtColor();
            if (value != null) {
                dialog.setRGB(value);
            }
            else {
                dialog.setRGB(InstallOptionsLink.DEFAULT_TXTCOLOR);
            }
            if (dialog.open() != null) {
                mNewValue = dialog.getRGB();
                return true;
            }
            else {
                return false;
            }
        }

        public Object getNewValue()
        {
            return mNewValue;
        }

    };

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class key)
    {
        if(IExtendedEditSupport.class.equals(key)) {
            return mExtendedEditSupport;
        }
        return super.getAdapter(key);
    }

    @Override
    protected UneditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new UneditableElementDirectEditPolicy();
    }

    @Override
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsLinkExtendedEditPolicy(this));
    }

    @Override
    protected String getExtendedEditLabelProperty()
    {
        return "link.extended.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getDirectEditLabelProperty()
    {
        return "link.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        if(cIsNT) {
            //This is a hack because Windows NT Labels don't seem to respond to the
            //WM_PRINT message (see SWTControl.getImage(Control)
            return new NTLinkFigure(getInstallOptionsWidget());
        }
        else {
            return new LinkFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
    }

    @Override
    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            ILinkFigure figure2 = (ILinkFigure)getFigure();
            figure2.setTxtColor((RGB)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_MULTILINE)) {
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return new InstallOptionsLinkEditManager(part, locator);
    }

    /**
     * @return
     */
    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("link.type.name"); //$NON-NLS-1$
    }

    public static interface ILinkFigure extends ILabelFigure
    {
        public void setTxtColor(RGB rgb);
    }

    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    protected class NTLinkFigure extends NTLabelFigure implements ILinkFigure
    {
        private RGB mTxtColor;
        private IPropertySource mSource;

        public NTLinkFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }

        @Override
        protected void init(IPropertySource propertySource)
        {
            super.init(propertySource);
            mSource = propertySource;
            setTxtColor((RGB)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TXTCOLOR));
        }

        @Override
        public boolean isMultiLine()
        {
            return Boolean.TRUE.equals(mSource.getPropertyValue(InstallOptionsModel.PROPERTY_MULTILINE));
        }

        @Override
        public RGB getTxtColor()
        {
            return mTxtColor==null?InstallOptionsLink.DEFAULT_TXTCOLOR:mTxtColor;
        }

        public void setTxtColor(RGB txtColor)
        {
            if(!getTxtColor().equals(txtColor)) {
                mTxtColor = txtColor;
                refresh();
            }
        }
    }
}
