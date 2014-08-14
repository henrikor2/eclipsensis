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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

public class InstallOptionsEditorInput extends FileEditorInput implements IInstallOptionsEditorInput
{
    private TextFileDocumentProvider mDocumentProvider;
    private IFileEditorInput mInput;
    private boolean mSwitching = false;

    /**
     * @throws CoreException
     *
     */
    public InstallOptionsEditorInput(IFileEditorInput input)
    {
        super(input.getFile());
        mInput = input;
        mDocumentProvider = new TextFileDocumentProvider();
    }

    @Override
    public int hashCode()
    {
        return mInput.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof InstallOptionsEditorInput) {
            return mInput.equals(((InstallOptionsEditorInput)obj).mInput);
        }
        else if (obj instanceof IFileEditorInput) {
            return mInput.equals(obj);
        }
        return super.equals(obj);
    }

    public void prepareForSwitch()
    {
        if(!mSwitching) {
            mSwitching = true;
            try {
                mDocumentProvider.connect(this);
            }
            catch (CoreException e) {
                InstallOptionsPlugin.getDefault().log(e);
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.IFileEditorInput#getFile()
     */
    @Override
    public IFile getFile()
    {
        return mInput.getFile();
    }

    public Object getSource()
    {
        return getFile();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStorageEditorInput#getStorage()
     */
    @Override
    public IStorage getStorage()
    {
        try {
            return mInput.getStorage();
        }
        catch (CoreException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return getFile();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    @Override
    public boolean exists()
    {
        return mInput.exists();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return mInput.getImageDescriptor();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    @Override
    public String getName()
    {
        return mInput.getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    @Override
    public IPersistableElement getPersistable()
    {
        return mInput.getPersistable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    @Override
    public String getToolTipText()
    {
        return mInput.getToolTipText();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter)
    {
        if(adapter == TextFileDocumentProvider.class) {
            return mDocumentProvider;
        }
        return mInput.getAdapter(adapter);
    }
}
