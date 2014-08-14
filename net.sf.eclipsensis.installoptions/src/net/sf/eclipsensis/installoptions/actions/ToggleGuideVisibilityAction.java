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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class ToggleGuideVisibilityAction extends Action
{
    public static final String PROPERTY_GUIDE_VISIBILITY = "net.sf.eclipsensis.installoptions.guides_visibility"; //$NON-NLS-1$
    public static final String ID = "net.sf.eclipsensis.installoptions.toggle_guides_visibility"; //$NON-NLS-1$

    private GraphicalViewer diagramViewer;

    /**
     * Constructor
     * @param   diagramViewer   the GraphicalViewer whose grid enablement and
     *                          visibility properties are to be toggled
     */
    public ToggleGuideVisibilityAction(GraphicalViewer diagramViewer) {
        super(InstallOptionsPlugin.getResourceString("show.guides.action.name"), AS_CHECK_BOX); //$NON-NLS-1$
        this.diagramViewer = diagramViewer;
        setToolTipText(InstallOptionsPlugin.getResourceString("show.guides.tooltip")); //$NON-NLS-1$
        setId(ID);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.guides.icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setChecked(isChecked());
    }

    /**
     * @see org.eclipse.jface.action.IAction#isChecked()
     */
    @Override
    public boolean isChecked() {
        Boolean val = (Boolean)diagramViewer.getProperty(PROPERTY_GUIDE_VISIBILITY);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public void run()
    {
        diagramViewer.setProperty(PROPERTY_GUIDE_VISIBILITY, (isChecked()?Boolean.FALSE:Boolean.TRUE));
    }
}
