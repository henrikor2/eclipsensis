/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import java.util.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

class Enabler
{
    private static Map<Button, Enabler> cEnablers = new HashMap<Button, Enabler>();

    private Button mButton;
    private Control[] mDependents;

    public Enabler(Button button, Control[] dependents)
    {
        mButton = button;
        mDependents = dependents;
        mButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                run();
            }
        });

        cEnablers.put(mButton, this);
        mButton.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                cEnablers.remove(mButton);
            }
        });
    }

    public void run()
    {
        run(true);
    }

    /**
     *
     */
    private void run(boolean flag)
    {
        boolean selected = mButton.getSelection();
        for (int i = 0; i < mDependents.length; i++) {
            mDependents[i].setEnabled(flag && selected);
            if(mDependents[i] instanceof Button) {
                Enabler enabler = get(mDependents[i]);
                if(enabler != null) {
                    enabler.run(selected);
                }
            }
        }
    }

    public static Enabler get(Control control)
    {
        return cEnablers.get(control);
    }
}