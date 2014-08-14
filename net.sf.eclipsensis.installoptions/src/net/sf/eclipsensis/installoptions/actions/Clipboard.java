/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.beans.*;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.ObjectTransfer;

import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class Clipboard
{
    /**
     * The event name used for {@link Clipboard#fireContentsAvailable()}
     */
    public static final String CONTENTS_AVAILABLE_EVENT = "ContentsAvailable"; //$NON-NLS-1$

    protected static Clipboard cInstance = new Clipboard();

    private static Transfer mTransfer;
    private PropertyChangeSupport mListeners = new PropertyChangeSupport(this);
    private boolean mContentsAvailable = false;

    /**
     * Do not allow direct instantiation of a Clipboard
     */
    private Clipboard()
    {
        super();
        mTransfer = new ObjectTransfer() {
            private final String TYPE_NAME = "net.sf.eclipsensis.clipboard.transfer"; //$NON-NLS-1$
            private final int TYPE_ID = registerType(TYPE_NAME);
            @Override
            protected int[] getTypeIds() {
                return new int[] {TYPE_ID};
            }
            @Override
            protected String[] getTypeNames() {
                return new String[] {TYPE_NAME};
            }
        };
        fireContentsAvailable();
        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            Runnable runnable = new Runnable() {
                public void run()
                {
                    fireContentsAvailable();
                }
            };

            @Override
            public void run()
            {
                Thread.currentThread().setName(InstallOptionsPlugin.getResourceString("clipboard.monitor.thread.name")); //$NON-NLS-1$
                try {
                    Display.getDefault().asyncExec(runnable);
                } catch (Exception e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
        },0,10);
        Display.getDefault().disposeExec(new Runnable() {
           public void run()
           {
               timer.cancel();
           }
        });
    }

    /**
     * Get the default Clipboard
     * @return - The default Clipboard
     */
    public static Clipboard getDefault()
    {
        return cInstance;
    }

    /**
     * Add a {@link PropertyChangeListener} to this Clipboard
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        mListeners.addPropertyChangeListener(l);
    }

    /**
     * Remove a {@link PropertyChangeListener} to this Clipboard
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        mListeners.removePropertyChangeListener(l);
    }

    public boolean isContentsAvailable()
    {
        return (getContents() != null);
    }

    /**
     * Fires a {@link PropertyChangeEvent} anytime the contents of the
<code>Clipboard</code> are set.
     *
     */
    public void fireContentsAvailable()
    {
        if(mContentsAvailable != isContentsAvailable()) {
            mContentsAvailable = !mContentsAvailable;
            PropertyChangeEvent event = new PropertyChangeEvent(this, CONTENTS_AVAILABLE_EVENT,
                            Boolean.valueOf(!mContentsAvailable), Boolean.valueOf(mContentsAvailable) );
            mListeners.firePropertyChange( event );
        }
    }

    public Object getContents()
    {
        org.eclipse.swt.dnd.Clipboard cb = new org.eclipse.swt.dnd.Clipboard(null);
        Object contents = cb.getContents(mTransfer);
        cb.dispose();
        return contents;
    }

    public void setContents(Object contents)
    {
        org.eclipse.swt.dnd.Clipboard cb = new org.eclipse.swt.dnd.Clipboard(null);
        if(contents != null) {
            cb.setContents(new Object[] {contents}, new Transfer[] {mTransfer});
        }
        else {
            cb.clearContents();
        }
        cb.dispose();
    }
}


