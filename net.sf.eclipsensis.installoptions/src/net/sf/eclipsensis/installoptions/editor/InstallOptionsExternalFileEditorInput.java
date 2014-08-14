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

import java.io.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.*;

public class InstallOptionsExternalFileEditorInput implements IInstallOptionsEditorInput, ILocationProvider
{
    private TextFileDocumentProvider mDocumentProvider;
    private IPathEditorInput mInput;
    private boolean mSwitching = false;
    private IStorage mStorage = null;

    public InstallOptionsExternalFileEditorInput(IPathEditorInput input)
    {
        mInput = input;
        mDocumentProvider = new TextFileDocumentProvider();
    }

    public void prepareForSwitch()
    {
        if(!mSwitching) {
            mSwitching = true;
            try {
                mDocumentProvider.connect(this);
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    public void completedSwitch()
    {
        if(mSwitching) {
            mSwitching = false;
            if(mDocumentProvider != null) {
                mDocumentProvider.disconnect(this);
            }
        }
    }

    public TextFileDocumentProvider getDocumentProvider()
    {
        return mDocumentProvider;
    }

    public Object getSource()
    {
        return getPath();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (o instanceof IPathEditorInput) {
            IPathEditorInput input= (IPathEditorInput)o;
            return getPath().equals(input.getPath());
        }
        if(o instanceof IAdaptable) {
            IPathEditorInput input= (IPathEditorInput)((IAdaptable)o).getAdapter(IPathEditorInput.class);
            if (input != null) {
                return getPath().equals(input.getPath());
            }
        }
        return false;
    }

    public IPath getPath()
    {
        return mInput.getPath();
    }

    public boolean exists()
    {
        return mInput.exists();
    }

    public ImageDescriptor getImageDescriptor()
    {
        return mInput.getImageDescriptor();
    }

    public String getName()
    {
        return mInput.getName();
    }

    public IPersistableElement getPersistable()
    {
        return mInput.getPersistable();
    }

    public String getToolTipText()
    {
        return mInput.getToolTipText();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if(adapter == TextFileDocumentProvider.class) {
            return mDocumentProvider;
        }
        if(adapter == ILocationProvider.class) {
            return this;
        }
        return mInput.getAdapter(adapter);
    }

    public IStorage getStorage() throws CoreException
    {
        if(mInput instanceof IStorageEditorInput) {
            return ((IStorageEditorInput)mInput).getStorage();
        }
        else {
            if(mStorage == null) {
                mStorage = new IStorage() {
                    private File mFile = new File(mInput.getPath().toOSString());

                    public InputStream getContents() throws CoreException
                    {
                        try {
                            return new BufferedInputStream(new FileInputStream(mFile));
                        }
                        catch (FileNotFoundException e) {
                            throw new CoreException(new Status(IStatus.ERROR,IInstallOptionsConstants.PLUGIN_ID,
                                                               1,e.getMessage(),e));
                        }
                    }

                    public IPath getFullPath()
                    {
                        return getPath();
                    }

                    public String getName()
                    {
                        return InstallOptionsExternalFileEditorInput.this.getName();
                    }

                    public boolean isReadOnly()
                    {
                        return (mFile != null && mFile.exists() && !mFile.canWrite());
                    }

                    @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter)
                    {
                        return InstallOptionsExternalFileEditorInput.this.getAdapter(adapter);
                    }
                };
            }
            return mStorage;
        }
    }

    public IPath getPath(Object element)
    {
        if(element instanceof InstallOptionsExternalFileEditorInput) {
            return ((InstallOptionsExternalFileEditorInput)element).getPath();
        }
        ILocationProvider provider = (ILocationProvider)mInput.getAdapter(ILocationProvider.class);
        if(provider != null) {
            return provider.getPath(element);
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        return mInput.hashCode();
    }
}
