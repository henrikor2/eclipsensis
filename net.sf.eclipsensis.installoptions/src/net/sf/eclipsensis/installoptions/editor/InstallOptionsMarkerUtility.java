/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class InstallOptionsMarkerUtility
{
    private InstallOptionsMarkerUtility()
    {
    }

    public static Collection<IMarker> getMarkers(IFile file)
    {
        List<IMarker> list = new ArrayList<IMarker>();
        if(file != null) {
            try {
                IMarker[] markers = file.findMarkers(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID,
                                                     false,IResource.DEPTH_ZERO);
                for (int i = 0; i < markers.length; i++) {
                    list.add(markers[i]);
                }
            }
            catch (CoreException e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }
        return list;
    }

    public static int getMarkerIntAttribute(IMarker marker, String attribute)
    {
        try {
            Integer i = (Integer)marker.getAttribute(attribute);
            return i.intValue();
        }
        catch(Exception ex) {
            return -1;
        }
    }
}
