/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.util.Iterator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.*;


public class NSISOutlineLabelProvider extends LabelProvider
{
    private static Image cRootImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("nsis.icon")); //$NON-NLS-1$
    private static ImageData cErrorImageData = EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("error.decoration.icon")).getImageData(); //$NON-NLS-1$
    private static ImageData cWarningImageData = EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("warning.decoration.icon")).getImageData(); //$NON-NLS-1$
    private NSISEditor mEditor;

    public NSISOutlineLabelProvider(NSISEditor editor)
    {
        mEditor = editor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element)
    {
        if(element instanceof NSISOutlineElement) {
            return decorateImage((NSISOutlineElement)element);
        }
        else {
            return super.getImage(element);
        }
    }

    private int getElementSeverity(NSISOutlineElement element)
    {
        IMarker[] markers = null;
        if(element.isRoot()) {
            markers = NSISEditorUtilities.getMarkers(mEditor, null);
        }
        else if(element.hasChildren()) {
            int severity = IMarker.SEVERITY_INFO;
            for(Iterator<NSISOutlineElement> iter = element.getChildren().iterator(); iter.hasNext(); ) {
                int s = getElementSeverity(iter.next());
                if(s > severity) {
                    severity = s;
                }
                if(severity == IMarker.SEVERITY_ERROR) {
                    break;
                }
            }
            return severity;
        }
        else {
            markers = NSISEditorUtilities.getMarkers(mEditor, new org.eclipse.jface.text.Region(element.getPosition().getOffset(),element.getPosition().getLength()));
        }
        if(!Common.isEmptyArray(markers)) {
            int severity = IMarker.SEVERITY_INFO;
            for (int i = 0; i < markers.length; i++) {
                int s = markers[i].getAttribute(IMarker.SEVERITY,IMarker.SEVERITY_INFO);
                if(s > severity) {
                    severity = s;
                }
                if(severity == IMarker.SEVERITY_ERROR) {
                    break;
                }
            }
            return severity;
        }
        return 0;
    }

    private Image decorateImage(NSISOutlineElement element)
    {
        final Image image = (element.isRoot()?cRootImage:element.getIcon());
        final ImageData data;
        String hashCode;
        int severity = getElementSeverity(element);
        switch(severity) {
            case IMarker.SEVERITY_ERROR:
                hashCode = image.hashCode() + "$error"; //$NON-NLS-1$
                data = cErrorImageData;
                break;
            case IMarker.SEVERITY_WARNING:
                hashCode = image.hashCode() + "$warning"; //$NON-NLS-1$
                data = cWarningImageData;
                break;
            default:
                return image;
        }
        Image image2 = EclipseNSISPlugin.getImageManager().getImage(hashCode);
        if(image2 == null) {
            EclipseNSISPlugin.getImageManager().putImageDescriptor(hashCode,
                    new CompositeImageDescriptor(){
                        @Override
                        protected void drawCompositeImage(int width, int height)
                        {
                            drawImage(image.getImageData(),0,0);
                            drawImage(data,0,getSize().y-data.height);
                        }

                        @Override
                        protected Point getSize()
                        {
                            return new Point(image.getBounds().width,image.getBounds().height);
                        }
                    });
            image2 = EclipseNSISPlugin.getImageManager().getImage(hashCode);
        }
        return image2;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property)
    {
        return false;
    }
}