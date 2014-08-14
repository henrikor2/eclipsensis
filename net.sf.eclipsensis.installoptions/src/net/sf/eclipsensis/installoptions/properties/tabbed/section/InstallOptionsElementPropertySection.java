/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElement;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.*;

public abstract class InstallOptionsElementPropertySection extends AbstractPropertySection
{
    private InstallOptionsElement mElement;
    private InstallOptionsCommandHelper mCommandHelper;

    private TabbedPropertySheetPage mPage;
    private Composite mParent;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage page)
    {
        super.createControls(parent, page);
        mPage = page;
        getWidgetFactory().setBorderStyle(SWT.BORDER);
        mParent = getWidgetFactory().createComposite(parent);
        mParent.setLayout(new GridLayout(1,false));
    }

    @Override
    public void dispose()
    {
        super.dispose();
        mPage = null;
        if(mParent != null && !mParent.isDisposed()) {
            mParent.dispose();
        }
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public final void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object input = ((IStructuredSelection)selection).getFirstElement();
            InstallOptionsElement element = getElement(input);
            if(element != null && !Common.objectsAreEqual(mElement, element)) {
                mElement = element;
                CommandStack stack = null;
                if(input instanceof EditPart) {
                    stack = ((EditPart)input).getViewer().getEditDomain().getCommandStack();
                }
                if(mCommandHelper != null) {
                    if(!Common.objectsAreEqual(stack, mCommandHelper.getCommandStack())) {
                        mCommandHelper.dispose();
                        createCommandHelper(stack);
                    }
                }
                else {
                    createCommandHelper(stack);
                }
                inputChanged(mElement);
            }
        }
    }

    private InstallOptionsElement getElement(Object input)
    {
        if(input instanceof InstallOptionsElement) {
            return (InstallOptionsElement)input;
        }
        else if(input instanceof EditPart) {
            return getElement(((EditPart)input).getModel());
        }
        return null;
    }

    /**
     * @param stack
     */
    private void createCommandHelper(CommandStack stack)
    {
        mCommandHelper = new InstallOptionsCommandHelper(stack) {
            @Override
            protected void refresh()
            {
                InstallOptionsElementPropertySection.this.refresh();
            }
        };
    }

    private void inputChanged(InstallOptionsElement newElement)
    {
        if(newElement != null && mParent != null && !mParent.isDisposed()) {
            Control[] controls = mParent.getChildren();
            if(!Common.isEmptyArray(controls)) {
                for (int i = 0; i < controls.length; i++) {
                    if(controls[i] != null && !controls[i].isDisposed()) {
                        controls[i].dispose();
                    }
                }
            }
            mParent.layout(true, true);
            final Control c = createSection(newElement, mParent, mPage, mCommandHelper);
            if(c != null) {
                c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
                mParent.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if(!mParent.isDisposed() && !c.isDisposed()) {
                            mParent.getShell().layout(new Control[] {c});
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean shouldUseExtraSpace()
    {
        return true;
    }

    protected Composite createSectionComposite(Composite parent)
    {
        final Composite section = getWidgetFactory().createComposite(parent);
        section.setLayoutDeferred(true);
        final JobScheduler scheduler = EclipseNSISPlugin.getDefault()
                .getJobScheduler();
        final Object jobFamily = new Object();
        section.addControlListener(new ControlAdapter() {
            private IJobStatusRunnable mRunnable = new IJobStatusRunnable() {
                public IStatus run(IProgressMonitor monitor)
                {
                    if(!section.isDisposed()) {
                        section.setLayoutDeferred(false);
                        section.setLayoutDeferred(true);
                    }
                    return Status.OK_STATUS;
                }
            };

            @Override
            public void controlResized(ControlEvent e)
            {
                if (!scheduler.isScheduled(jobFamily)) {
                    scheduler.scheduleUIJob(jobFamily, InstallOptionsPlugin.getResourceString("tabbed.property.update.layout.job.name"), //$NON-NLS-1$
                            mRunnable);
                }
            }
        });
        return section;
    }

    protected abstract Control createSection(InstallOptionsElement element, Composite parent, TabbedPropertySheetPage page, InstallOptionsCommandHelper commandHelper);
}
