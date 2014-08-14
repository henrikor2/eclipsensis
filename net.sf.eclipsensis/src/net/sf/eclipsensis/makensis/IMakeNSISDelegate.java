/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.util.List;

public interface IMakeNSISDelegate
{
    public void startup();

    public void shutdown();

    public boolean isUnicode();

    public long getHwnd();

    void reset();

    String getOutputFileName();

    String getScriptFileName();

    List<String> getErrors();

    List<String> getWarnings();
}
