/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import org.eclipse.core.runtime.IProgressMonitor;

public interface INSISHomeListener
{
    public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome);
}
