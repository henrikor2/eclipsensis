/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.handlers;

import java.util.regex.Pattern;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.*;

public abstract class NSISHandler extends AbstractHandler
{
    private Pattern mExtensionPattern = null;

    public Object execute(ExecutionEvent event)
    {
        Widget w = ((Event)event.getTrigger()).widget;
        if(w instanceof Tree) {
            TreeItem[] items = ((Tree)w).getSelection();
            for (int i = 0; i < items.length; i++) {
                Object object = items[i].getData();
                if(object instanceof IFile && ((IFile)object).getFileExtension() != null &&
                        getExtensionPattern().matcher(((IFile)object).getFileExtension()).matches()) {
                    handleScript((IFile)object);
                }
            }
        }
        return null;
    }

    public Pattern getExtensionPattern()
    {
        if(mExtensionPattern == null) {
            mExtensionPattern = createExtensionPattern();
        }
        return mExtensionPattern;
    }

    protected Pattern createExtensionPattern()
    {
        return Pattern.compile(INSISConstants.NSI_WILDCARD_EXTENSION,Pattern.CASE_INSENSITIVE);
    }

    protected abstract void handleScript(IFile file);
}
