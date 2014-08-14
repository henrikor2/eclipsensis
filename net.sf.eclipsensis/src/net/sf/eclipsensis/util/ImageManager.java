/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.net.URL;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageManager
{
    private AbstractUIPlugin mPlugin;
    private ImageRegistry mImageRegistry;

    public ImageManager(AbstractUIPlugin plugin)
    {
        mPlugin = plugin;
    }

    private ImageRegistry getImageRegistry()
    {
        if(mImageRegistry == null) {
            if(mPlugin != null) {
                if(Display.getCurrent() != null) {
                    mImageRegistry = mPlugin.getImageRegistry();
                }
                else {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run()
                        {
                            mImageRegistry = mPlugin.getImageRegistry();
                        }
                    });
                }
            }
            else {
                Display.getDefault().syncExec(new Runnable() {
                    public void run()
                    {
                        mImageRegistry = new ImageRegistry(Display.getDefault());
                    }
                });
            }
        }
        return mImageRegistry;
    }

    public synchronized ImageDescriptor getImageDescriptor(String location)
    {
        return getImageDescriptor(makeLocationURL(location));
    }

    /**
     * @param location
     * @return
     */
    private URL makeLocationURL(String location)
    {
        if(mPlugin == null || Common.isEmpty(location)) {
            return null;
        }
        else {
            return mPlugin.getBundle().getEntry(location);
        }
    }

    public synchronized ImageDescriptor getImageDescriptor(URL url)
    {
        String urlString = (url != null?url.toString():""); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = null;
        if(getImageRegistry() != null) {
            imageDescriptor = getImageRegistry().getDescriptor(urlString);
            if(imageDescriptor == null) {
                imageDescriptor = createImageDescriptor(url);
                getImageRegistry().put(urlString.toLowerCase(), imageDescriptor);
            }
        }

        return imageDescriptor;
    }

    /**
     * @param location
     * @return
     */
    private ImageDescriptor createImageDescriptor(URL url)
    {
        ImageDescriptor imageDescriptor;
        if(url != null) {
            imageDescriptor = ImageDescriptor.createFromURL(url);
        }
        else {
            imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        return imageDescriptor;
    }

    public synchronized Image getImage(String location)
    {
        Image image = getImageRegistry().get(location==null?null:location.toLowerCase());
        if(image == null) {
            return getImage(makeLocationURL(location));
        }
        return image;
    }

    public synchronized boolean containsImage(URL url)
    {
        return containsImage(url==null?null:url.toString());
    }

    public synchronized boolean containsImage(String name)
    {
        return (getImageRegistry().get(name==null?null:name.toLowerCase()) != null);
    }

    public synchronized void putImage(URL url, Image image)
    {
        putImage(url == null?null:url.toString(),image);
    }

    public synchronized void putImage(String s, Image image)
    {
        getImageRegistry().put(s==null?null:s.toLowerCase(),image);
    }

    public synchronized void putImageDescriptor(String s, ImageDescriptor image)
    {
        getImageRegistry().put(s==null?null:s.toLowerCase(),image);
    }

    public synchronized Image getImage(URL url)
    {
        Image image = null;
        if(url != null) {
            String urlString = url.toString().toLowerCase();
            image = getImage(urlString);
            if(image == null) {
                putImageDescriptor(urlString,createImageDescriptor(url));
                image = getImage(urlString);
                if(image == null) {
                    getImageRegistry().remove(urlString);
                    putImageDescriptor(urlString, ImageDescriptor.getMissingImageDescriptor());
                    image = getImage(urlString);
                }
            }
        }
        return image;
    }
}
