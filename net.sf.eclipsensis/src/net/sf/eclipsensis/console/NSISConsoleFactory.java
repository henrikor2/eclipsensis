/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.ui.console.*;

public class NSISConsoleFactory implements IConsoleFactory
{
    public void openConsole()
    {
        showConsole();
    }

    public static void showConsole()
    {
        NSISConsole console = EclipseNSISPlugin.getDefault().getConsole();
        if (console != null) {
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            IConsole[] existing = manager.getConsoles();
            boolean exists = false;
            for (int i = 0; i < existing.length; i++) {
                if(console == existing[i]) {
                    exists = true;
                }
            }
            if(! exists) {
                manager.addConsoles(new IConsole[] {console});
            }
            manager.showConsoleView(console);
        }
    }

    public static void closeConsole()
    {
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        NSISConsole console = EclipseNSISPlugin.getDefault().getConsole();
        if (console != null) {
            manager.removeConsoles(new IConsole[] {console});
            manager.addConsoleListener(console.getLifecycleListener());
        }
    }
}
