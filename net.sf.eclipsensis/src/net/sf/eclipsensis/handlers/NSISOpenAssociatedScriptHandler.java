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
import net.sf.eclipsensis.editor.NSISEditorUtilities;

import org.eclipse.core.resources.IFile;

public class NSISOpenAssociatedScriptHandler extends NSISHandler
{
    @Override
    protected void handleScript(IFile file)
    {
        NSISEditorUtilities.openAssociatedFiles(null,file);
    }

    @Override
    protected Pattern createExtensionPattern()
    {
        return Pattern.compile(INSISConstants.NSH_EXTENSION,Pattern.CASE_INSENSITIVE);
    }

}
