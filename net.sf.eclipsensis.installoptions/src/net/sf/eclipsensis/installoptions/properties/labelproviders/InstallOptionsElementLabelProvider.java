/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.labelproviders;

import java.util.Iterator;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.tabbed.InstallOptionsElementTypeMapper;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

public class InstallOptionsElementLabelProvider extends LabelProvider
{
    private ITypeMapper typeMapper;

    /**
     * constructor.
     */
    public InstallOptionsElementLabelProvider()
    {
        typeMapper = new InstallOptionsElementTypeMapper();
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object objects)
    {
        if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
            return null;
        }
        final boolean multiple[] = {false};
        Object object = getObject(objects, multiple);
        if (object == null) {
            return InstallOptionsDialog.INSTALLOPTIONS_ICON;
        }
        else {
            InstallOptionsElement element = getInstallOptionsElement(object);
            if(element != null) {
                return element.getIcon();
            }
            else {
                return super.getImage(object);
            }
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object objects)
    {
        if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
            return InstallOptionsPlugin.getResourceString("no.items.selected.message"); //$NON-NLS-1$
        }
        final boolean multiple[] = {false};
        final Object object = getObject(objects, multiple);
        if (object == null || ((IStructuredSelection) objects).size() > 1) {
            return InstallOptionsPlugin.getFormattedString("multiple.items.selected.message", new Object[] {new Integer(((IStructuredSelection) objects).size())}); //$NON-NLS-1$
        }
        else {
            InstallOptionsElement element = getInstallOptionsElement(object);
            if(element != null) {
                return element.getType();
            }
            else {
                return super.getText(object);
            }
        }
    }

    private InstallOptionsElement getInstallOptionsElement(Object object)
    {
        if(object instanceof InstallOptionsElement) {
            return (InstallOptionsElement)object;
        }
        else if(object instanceof EditPart) {
            return getInstallOptionsElement(((EditPart)object).getModel());
        }
        return null;
    }

    private Object getObject(Object objects, boolean multiple[])
    {
        Object object = null;
        if (objects instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) objects;
            object = selection.getFirstElement();
            if (selection.size() == 1) {
                // one element selected
                multiple[0] = false;
                return object;
            }
            // multiple elements selected
            multiple[0] = true;
            Class<?> firstClass = typeMapper.mapType(object);
            // determine if all the objects in the selection are the same type
            if (selection.size() > 1) {
                for (Iterator<?> i = selection.iterator(); i.hasNext();) {
                    Object next = i.next();
                    Class<?> nextClass = typeMapper.mapType(next);
                    if (!nextClass.equals(firstClass)) {
                        // two elements not equal == multiple selected unequal
                        multiple[0] = false;
                        object = null;
                        break;
                    }
                }
            }
        }
        else {
            multiple[0] = false;
            object = objects;
        }
        return object;
    }
}
