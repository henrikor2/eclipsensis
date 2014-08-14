/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.descriptors.PropertyDescriptorHelper;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;

public abstract class InstallOptionsDirectEditManager<T extends CellEditor> extends DirectEditManager
{
    private Class<T> mEditorType;

    /**
     * @param source
     * @param editorType
     * @param locator
     */
    public InstallOptionsDirectEditManager(GraphicalEditPart source,
                    Class<T> editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
        mEditorType = editorType;
    }

    @Override
    protected final T createCellEditorOn(Composite composite)
    {
        InstallOptionsModelTypeDef typeDef = ((InstallOptionsWidget)getEditPart().getModel()).getTypeDef();
        if(typeDef == null || !typeDef.getSettings().contains(getDirectEditProperty())) {
            return null;
        }
        else {
            return mEditorType.cast(createCellEditor(composite));
        }
    }

    protected T createCellEditor(Composite composite)
    {
        return mEditorType.cast(super.createCellEditorOn(composite));
    }

    @Override
    protected final void initCellEditor()
    {
        InstallOptionsWidget control = (InstallOptionsWidget)getEditPart().getModel();
        IPropertyDescriptor descriptor = control.getPropertyDescriptor(getDirectEditProperty());
        if(descriptor instanceof PropertyDescriptor) {
            try {
                ICellEditorValidator validator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) descriptor);
                if (validator != null) {
                    getCellEditor().setValidator(validator);
                }
            }
            catch (Throwable t) {
                InstallOptionsPlugin.getDefault().log(t);
            }
        }
        String initialText = getInitialText(control);
        getCellEditor().setValue(initialText);
        selectCellEditorText();
    }

    protected abstract String getInitialText(InstallOptionsWidget control);
    protected abstract void selectCellEditorText();
    protected abstract String getDirectEditProperty();
}
