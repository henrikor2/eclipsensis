/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import java.util.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class MasterSlaveController extends SelectionAdapter
{
    private Button mMaster;
    private Map<Control, MasterSlaveEnabler> mSlaves = new HashMap<Control, MasterSlaveEnabler>();
    private boolean mIsReverse = false;

    public MasterSlaveController(Button button)
    {
        this(button,false);
    }

    public MasterSlaveController(Button button, boolean isReverse)
    {
        mMaster = button;
        mIsReverse = isReverse;
        mMaster.addSelectionListener(this);
        mMaster.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        mMaster.removeSelectionListener(MasterSlaveController.this);
                        mSlaves.clear();
                        mMaster = null;
                    }
                });
    }

    public void addSlave(Control control)
    {
        mSlaves.put(control, null);
    }

    public void addSlave(Control control, MasterSlaveEnabler enabler)
    {
        mSlaves.put(control, enabler);
        Label l = (Label)control.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            if(mSlaves.get(l) == null) {
                mSlaves.put(l, enabler);
            }
        }
    }

    public void setEnabler(Control control, MasterSlaveEnabler enabler)
    {
        if(mSlaves.containsKey(control)) {
            addSlave(control, enabler);
        }
    }

    public void removeSlave(Control control)
    {
        mSlaves.remove(control);
    }

    public void updateSlaves()
    {
        updateSlavesInternal(mMaster.getSelection());
    }

    public void updateSlaves(boolean hint)
    {
        updateSlavesInternal(mMaster.getSelection() && hint);
    }

    /**
     * @param selection
     */
    private void updateSlavesInternal(boolean selection)
    {
        for(Iterator<Control> iter=mSlaves.keySet().iterator(); iter.hasNext(); ) {
            Control slave = iter.next();
            MasterSlaveEnabler enabler = mSlaves.get(slave);
            recursiveSetEnabled(slave, (mIsReverse?!selection:selection), enabler);
        }
    }

    private void recursiveSetEnabled(Control control, boolean enabled, MasterSlaveEnabler enabler)
    {
        MasterSlaveEnabler enabler2 = enabler;
        if(control instanceof Composite) {
            Control[] children = ((Composite)control).getChildren();
            for (int i = 0; i < children.length; i++) {
                recursiveSetEnabled(children[i],enabled, enabler2);
            }
        }
        MasterSlaveEnabler enabler3 = mSlaves.get(control);
        if(enabler3 != null) {
            enabler2 = enabler3;
        }
        boolean enabled2 = enabled?(enabler2 != null?enabler2.canEnable(control):enabled):enabled;
        NSISWizardDialogUtil.setEnabled(control,enabled2);
        if(enabler2 != null) {
            enabler2.enabled(control,enabled2);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e)
    {
        updateSlaves();
    }
}