/*******************************************************************************
 * Copyright (c) 2007-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import net.sf.eclipsensis.makensis.MakeNSISResults;

public interface INSISMarkerAssistant
{
    public void updateMarkers(NSISEditor editor, MakeNSISResults results);
    public void updateMarkers(MakeNSISResults results);
    public void clearMarkers();
    public boolean hasMarkers();
}
