/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.EmptyContentProvider;

import org.eclipse.jface.viewers.Viewer;

public class NSISInstallElementTreeContentProvider extends EmptyContentProvider
{
    private NSISWizardSettings mSettings = null;

    public NSISInstallElementTreeContentProvider(NSISWizardSettings settings)
    {
        mSettings = settings;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        if(newInput == null || newInput instanceof NSISWizardSettings) {
            mSettings = (NSISWizardSettings)newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        if(mSettings == inputElement) {
            return new Object[]{mSettings.getInstaller()};
        }
        else {
            return new Object[0];
        }
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if(parentElement instanceof INSISInstallElement) {
            return ((INSISInstallElement)parentElement).getChildren();
        }
        else {
            return null;
        }
    }

    @Override
    public Object getParent(Object element)
    {
        if(element instanceof INSISInstallElement) {
            return ((INSISInstallElement)element).getParent();
        }
        else {
            return null;
        }
    }

    @Override
    public boolean hasChildren(Object element)
    {
        if(element instanceof INSISInstallElement) {
            return !Common.isEmptyArray(((INSISInstallElement)element).getChildren());
        }
        else {
            return false;
        }
    }
}