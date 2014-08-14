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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.ToggleEnablementCommand;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleEnablementAction extends SelectionAction
{
    public static final String ID = "net.sf.eclipsensis.installoptions.toggle_enablement"; //$NON-NLS-1$

    /**
     * @param part
     */
    public ToggleEnablementAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(true);
    }

    /**
     * Initializes this action's text and images.
     */
    @Override
    protected void init()
    {
        super.init();
        setId(ID);
        setEnabled(false);
    }

    public Command createToggleEnablementCommand(List<?> objects)
    {
        if (objects.isEmpty()) {
            return null;
        }

        ToggleEnablementCommand cmd = null;
        List<InstallOptionsWidget> list = new ArrayList<InstallOptionsWidget>();
        List<InstallOptionsWidgetEditPart> editPartList = Common.makeGenericList(InstallOptionsWidgetEditPart.class, objects);
        if (!editPartList.isEmpty())
        {
            Iterator<InstallOptionsWidgetEditPart> iter = editPartList.iterator();
            InstallOptionsWidget part = (InstallOptionsWidget)iter.next().getModel();
            if (part != null)
            {
                if (!getFlags(part).contains(InstallOptionsModel.FLAGS_DISABLED))
                {
                    return null;
                }
                boolean shouldEnable = shouldEnable(part);
                list.add(part);
                while (iter.hasNext())
                {
                    part = (InstallOptionsWidget)iter.next().getModel();
                    if (part != null)
                    {
                        if (getFlags(part).contains(InstallOptionsModel.FLAGS_DISABLED)
                                && shouldEnable == shouldEnable(part))
                        {
                            list.add(part);
                            continue;
                        }
                    }
                    return null;
                }
                cmd = new ToggleEnablementCommand(list.toArray(new InstallOptionsWidget[list.size()]), shouldEnable);
                String label = (shouldEnable ? "enable.action.name" : "disable.action.name"); //$NON-NLS-1$ //$NON-NLS-2$
                setText(InstallOptionsPlugin.getResourceString(label));
                setToolTipText(label);
            }
        }
        return cmd;
    }

    private Collection<String> getFlags(InstallOptionsWidget part)
    {
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(part.getType());
        return (typeDef == null?Collections.<String>emptySet():typeDef.getFlags());
    }

    private boolean shouldEnable(InstallOptionsWidget part)
    {
        return part.getFlags().contains(InstallOptionsModel.FLAGS_DISABLED);
    }

    @Override
    protected boolean calculateEnabled() {
        Command cmd = createToggleEnablementCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    @Override
    public void run() {
        execute(createToggleEnablementCommand(getSelectedObjects()));
    }
}
