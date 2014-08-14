/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import java.text.*;
import java.util.Date;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.*;

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

class NSISLaunchProcess implements IProcess, IMakeNSISRunListener, IWorkbenchAdapter
{
    private static final DateFormat cDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private MakeNSISResults mResult = null;
    private boolean mTerminated = false;
    private ILaunch mLaunch;
    private IPath mScript;
    private String mCmdLine = null;
    private boolean mStarted = false;
    private String mLabel;

    public NSISLaunchProcess(IPath script, ILaunch launch)
    {
        mScript = script;
        mLaunch = launch;
        MakeNSISRunner.addListener(this);
        fireCreationEvent();
        NSISHome home = NSISPreferences.getInstance().getNSISHome();
        mLabel = MessageFormat.format("{0} ({1})",new Object[]{home == null?null:home.getNSISExe().getFile().getAbsolutePath(), //$NON-NLS-1$
                        cDateFormat.format(new Date(System.currentTimeMillis()))}).trim();
    }

    protected void fireCreationEvent()
    {
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }

    protected void fireEvent(DebugEvent event)
    {
        DebugPlugin manager= DebugPlugin.getDefault();
        if (manager != null) {
            manager.fireDebugEventSet(new DebugEvent[]{event});
        }
    }

    protected void fireTerminateEvent()
    {
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }

    protected void fireChangeEvent()
    {
        fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
    }

    public String getLabel()
    {
        return mLabel;
    }

    public ILaunch getLaunch()
    {
        return mLaunch;
    }

    public IStreamsProxy getStreamsProxy()
    {
        return null;
    }

    public void setAttribute(String key, String value)
    {
    }

    public String getAttribute(String key)
    {
        if(key.equals(ATTR_CMDLINE))  {
            return mCmdLine;
        }
        else if(key.equals(ATTR_PROCESS_LABEL)) {
            return getLabel();
        }
        else if(key.equals(ATTR_PROCESS_TYPE)) {
            return "nsis"; //$NON-NLS-1$
        }
        return null;
    }

    public int getExitValue() throws DebugException
    {
        if(mResult != null) {
            return mResult.getReturnCode();
        }
        throw new DebugException(new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID,IStatus.ERROR,EclipseNSISPlugin.getResourceString("launch.process.exitvalue.error"),null)); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if(IWorkbenchAdapter.class.equals(adapter)) {
            return this;
        }
        return null;
    }

    public boolean canTerminate()
    {
        synchronized(this) {
            return !mTerminated;
        }
    }

    public boolean isTerminated()
    {
        synchronized (this) {
            return mTerminated;
        }
    }

    public void terminate()
    {
        synchronized(this) {
            if (!mTerminated) {
                if(mStarted) {
                    MakeNSISRunner.cancel();
                }
                else {
                    doTerminate();
                }
            }
        }
    }

    private void doTerminate()
    {
        synchronized(this) {
            mStarted = false;
            MakeNSISRunner.removeListener(this);
            mTerminated = true;
            fireTerminateEvent();
        }
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        if(event.getScript() == mScript) {
            switch(event.getType()) {
                case MakeNSISRunEvent.STARTED:
                    synchronized(this) {
                        mStarted = true;
                    }
                    break;
                case MakeNSISRunEvent.STOPPED:
                    mResult = (MakeNSISResults)event.getData();
                    synchronized (this) {
                        doTerminate();
                    }
                    break;
                case MakeNSISRunEvent.CREATED_PROCESS:
                    mCmdLine = (String)event.getData();
                    fireChangeEvent();
                    break;
            }
        }
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
        return getLabel();
    }

    public Object getParent(Object o)
    {
        return mLaunch;
    }
}