/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.w3c.dom.Node;

public class LocalSaveFileParam extends LocalFileParam
{
    public LocalSaveFileParam(Node node)
    {
        super(node);
    }

    @Override
    protected LocalFilesystemObjectParamEditor createLocalFilesystemObjectParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new LocalSaveFileParamEditor(command, parentEditor);
    }

    protected class LocalSaveFileParamEditor extends LocalFileParamEditor
    {
        public LocalSaveFileParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        protected boolean isSave()
        {
            return true;
        }

        @Override
        protected String validateLocalFilesystemObjectParam()
        {
            if(Common.isValid(mFileText)) {
                String file = IOUtility.decodePath(mFileText.getText());
                if(file.length() == 0 ) {
                    if(isAllowBlank()) {
                        return null;
                    }
                    else {
                        return EclipseNSISPlugin.getResourceString("string.param.error"); //$NON-NLS-1$
                    }
                }
                if(IOUtility.isValidPathName(file)) {
                    return null;
                }
                return EclipseNSISPlugin.getResourceString("local.save.file.param.error"); //$NON-NLS-1$
            }
            return null;
        }
    }
}
