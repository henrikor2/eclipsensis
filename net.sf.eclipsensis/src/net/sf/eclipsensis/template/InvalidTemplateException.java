/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import net.sf.eclipsensis.EclipseNSISPlugin;

public class InvalidTemplateException extends RuntimeException
{
    private static final long serialVersionUID = -7503021375588123015L;

    public InvalidTemplateException()
    {
        super(EclipseNSISPlugin.getResourceString("invalid.template.error")); //$NON-NLS-1$
    }

    public InvalidTemplateException(String message)
    {
        super(message);
    }
}
