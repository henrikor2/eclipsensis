/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.io.*;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class NSISExternalFileEditorInput implements IPathEditorInput, ILocationProvider, IWorkbenchAdapter, IStorageEditorInput
{
    private File mFile;
    private IStorage mStorage;

    public NSISExternalFileEditorInput(File file)
    {
        mFile = file;
    }

    protected File getFile()
    {
        return mFile;
    }

    public IPath getPath()
    {
        return Path.fromOSString(mFile.getAbsolutePath());
    }

    public boolean exists()
    {
        return mFile.exists();
    }

    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }

    public String getName()
    {
        return mFile.getName();
    }

    public IPersistableElement getPersistable()
    {
        return null;
    }

    public String getToolTipText()
    {
        return mFile.getAbsolutePath();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if(ILocationProvider.class.equals(adapter)) {
            return this;
        }
        else if(IWorkbenchAdapter.class.equals(adapter)) {
            return this;
        }
        return null;
    }

    public IPath getPath(Object element)
    {
        if(element instanceof NSISExternalFileEditorInput) {
            return ((NSISExternalFileEditorInput)element).getPath();
        }
        return null;
    }

    public Object[] getChildren(Object o)
    {
        return null;
    }

    public ImageDescriptor getImageDescriptor(Object object)
    {
        return null;
    }

    public String getLabel(Object o)
    {
        return ((NSISExternalFileEditorInput)o).getName();
    }

    public Object getParent(Object o)
    {
        return null;
    }

    @Override
    public int hashCode()
    {
        return mFile.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }

        if (o instanceof NSISExternalFileEditorInput) {
            NSISExternalFileEditorInput input= (NSISExternalFileEditorInput) o;
            return mFile.equals(input.mFile);
        }

        IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(o);
        if (input != null) {
            return getPath().equals(input.getPath());
        }

        return false;
    }

    public IStorage getStorage()
    {
        if(mStorage == null) {
            mStorage = new IStorage() {
                public InputStream getContents() throws CoreException
                {
                    try {
                        return new BufferedInputStream(new FileInputStream(mFile));
                    }
                    catch (FileNotFoundException e) {
                        throw new CoreException(new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID,
                                                           1,e.getMessage(),e));
                    }
                }

                public IPath getFullPath()
                {
                    return getPath();
                }

                public String getName()
                {
                    return NSISExternalFileEditorInput.this.getName();
                }

                public boolean isReadOnly()
                {
                    return (mFile != null && mFile.exists() && !mFile.canWrite());
                }

                @SuppressWarnings("unchecked")
                public Object getAdapter(Class adapter)
                {
                    return NSISExternalFileEditorInput.this.getAdapter(adapter);
                }
            };

        }
        return mStorage;
    }
}
