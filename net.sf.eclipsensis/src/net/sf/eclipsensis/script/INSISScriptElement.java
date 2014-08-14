/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import net.sf.eclipsensis.EclipseNSISPlugin;

public interface INSISScriptElement
{
    public static final int SCRIPT_MAX_LINE_LENGTH = Integer.parseInt(EclipseNSISPlugin.getResourceString("script.max.line.length","80")); //$NON-NLS-1$ //$NON-NLS-2$
    public void write(NSISScriptWriter writer);
}
