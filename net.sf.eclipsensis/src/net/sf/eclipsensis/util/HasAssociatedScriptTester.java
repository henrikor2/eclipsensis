/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

public class HasAssociatedScriptTester extends PropertyTester
{
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
        IFile file = getFile(receiver);
        if(file != null && "hasAssociatedScript".equals(property)) { //$NON-NLS-1$
            return NSISHeaderAssociationManager.getInstance().getAssociatedScript(file) != null;
        }
        return false;
    }

    private IFile getFile(Object receiver)
    {
        if(receiver instanceof IFile) {
            return (IFile)receiver;
        }
        else if(receiver instanceof IAdaptable) {
            return (IFile)((IAdaptable)receiver).getAdapter(IFile.class);
        }
        return null;
    }
}
