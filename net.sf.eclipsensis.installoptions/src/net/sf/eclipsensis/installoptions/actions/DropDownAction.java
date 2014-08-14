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
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.*;

public class DropDownAction extends PartEventAction implements IPropertyChangeListener
{
    private IPreferenceStore mPreferenceStore;
    private Map<String, DropDownActionWrapper> mChildren = new LinkedHashMap<String, DropDownActionWrapper>();
    private DropDownActionWrapper mCurrent;
    private Menu mMenu = null;
    private Set<Action> mEnabledActions = new HashSet<Action>();
    private IMenuCreator mMenuCreator = new IMenuCreator() {
        public void dispose()
        {
            if(mMenu != null) {
                mMenu.dispose();
                mMenu = null;
            }
        }

        public Menu getMenu(Control parent)
        {
            if(mMenu == null || mMenu.isDisposed()) {
                mMenu = new Menu(parent);
                for(Iterator<DropDownActionWrapper> iter=mChildren.values().iterator(); iter.hasNext(); ) {
                    ActionContributionItem item = new ActionContributionItem(iter.next());
                    item.fill(mMenu, -1);
                }
            }
            return mMenu;
        }

        public Menu getMenu(Menu parent)
        {
            return null;
        }
    };
    private boolean mDetectCurrent = true;
    private IPropertyChangeListener mActionListener = new IPropertyChangeListener() {
        private String mJobFamily = getClass().getName()+hashCode();
        private IJobStatusRunnable mEnablementRunnable = new IJobStatusRunnable() {
            public IStatus run(IProgressMonitor monitor)
            {
                if(mDetectCurrent && !mEnabledActions.contains(mCurrent)) {
                    for(Iterator<DropDownActionWrapper> iter=mChildren.values().iterator(); iter.hasNext(); ) {
                        DropDownActionWrapper element = iter.next();
                        if((element).isEnabled()) {
                            mPreferenceStore.setValue(getId(),element.getId());
                            break;
                        }
                    }
                }
                updateEnabled();
                return Status.OK_STATUS;
            }
        };
        private JobScheduler mScheduler = InstallOptionsPlugin.getDefault().getJobScheduler();

        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(IAction.ENABLED)) {
                IAction action = (IAction)event.getSource();
                DropDownActionWrapper delegate = mChildren.get(action.getId());
                if(delegate != null) {
                    Boolean bool = (Boolean) event.getNewValue();
                    if(bool.booleanValue()) {
                        mEnabledActions.add(delegate);
                    }
                    else {
                        mEnabledActions.remove(delegate);
                    }
                    if(!mScheduler.isScheduled(mJobFamily)) {
                        mScheduler.scheduleUIJob(mJobFamily,"",mEnablementRunnable); //$NON-NLS-1$
                    }
                }
            }
        }
    };

    public DropDownAction(String id, IPreferenceStore preferenceStore, RetargetAction[] delegates)
    {
        super("",IAction.AS_DROP_DOWN_MENU); //$NON-NLS-1$
        mPreferenceStore = preferenceStore;
        setId(id);
        init(delegates);
    }

    @Override
    public IMenuCreator getMenuCreator()
    {
        return mMenuCreator;
    }

    private void init(RetargetAction[] delegates)
    {
        mPreferenceStore.addPropertyChangeListener(this);
        if(!Common.isEmptyArray(delegates)) {
            for (int i = 0; i < delegates.length; i++) {
                mChildren.put(delegates[i].getId(),new DropDownActionWrapper(delegates[i]));
                if(delegates[i].isEnabled()) {
                    mEnabledActions.add(delegates[i]);
                }
                delegates[i].addPropertyChangeListener(mActionListener);
            }
            String delegateId = mPreferenceStore.getString(getId());
            DropDownActionWrapper action = mChildren.get(delegateId);
            if(action == null) {
                action = mChildren.get(delegates[0].getId());
                mPreferenceStore.setValue(getId(),action.getId());
            }
            else {
                setCurrent(action);
            }
        }
        updateEnabled();
    }

    @Override
    public void partActivated(IWorkbenchPart part)
    {
        super.partActivated(part);
        updateEnabled();
    }

    @Override
    public void partClosed(IWorkbenchPart part)
    {
        updateEnabled();
        super.partClosed(part);
    }

    @Override
    public void partDeactivated(IWorkbenchPart part)
    {
        super.partDeactivated(part);
        updateEnabled();
    }

    private void updateEnabled()
    {
        setEnabled(mEnabledActions.size() > 0);
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        if(getId().equals(event.getProperty())) {
            DropDownActionWrapper action = mChildren.get(event.getNewValue());
            if(action != null) {
                setCurrent(action);
            }
        }
    }

    private void setCurrent(DropDownActionWrapper action)
    {
        if(mCurrent != action) {
            if(mCurrent != null) {
                mCurrent.setChecked(false);
            }
            mCurrent = action;
            String temp = mCurrent.getText();
            if(!Common.isEmpty(temp)) {
                setText(temp);
            }
            temp = mCurrent.getToolTipText();
            if(!Common.isEmpty(temp)) {
                setToolTipText(temp);
            }
            ImageDescriptor desc = mCurrent.getImageDescriptor();
            if(desc != null) {
                setImageDescriptor(desc);
            }
            desc = mCurrent.getHoverImageDescriptor();
            if(desc != null) {
                setHoverImageDescriptor(desc);
            }
            desc = mCurrent.getDisabledImageDescriptor();
            if(desc != null) {
                setDisabledImageDescriptor(desc);
            }
            mCurrent.setChecked(true);
        }
    }

    @Override
    public void run()
    {
        mCurrent.run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose()
    {
        mPreferenceStore.removePropertyChangeListener(this);
        for (Iterator<DropDownActionWrapper> iter = mChildren.values().iterator(); iter.hasNext();) {
            DropDownActionWrapper action = iter.next();
            RetargetAction delegate = action.getDelegate();
            if(delegate != null) {
                delegate.removePropertyChangeListener(mActionListener);
            }
            action.dispose();
            iter.remove();
        }
    }

    protected boolean isDetectCurrent()
    {
        return mDetectCurrent;
    }

    protected void setDetectCurrent(boolean detectCurrent)
    {
        mDetectCurrent = detectCurrent;
    }

    private class DropDownActionWrapper extends Action implements IPropertyChangeListener
    {
        private RetargetAction mDelegate;

        public DropDownActionWrapper(RetargetAction delegate)
        {
            super(delegate.getText(), IAction.AS_CHECK_BOX);
            mDelegate = delegate;
            setId(delegate.getId());
            setAccelerator(delegate.getAccelerator());
            setActionDefinitionId(delegate.getActionDefinitionId());
            setChecked(delegate.isChecked());
            setDescription(delegate.getDescription());
            setDisabledImageDescriptor(delegate.getDisabledImageDescriptor());
            setEnabled(delegate.isEnabled());
            setHelpListener(delegate.getHelpListener());
            setHoverImageDescriptor(delegate.getHoverImageDescriptor());
            setImageDescriptor(delegate.getImageDescriptor());
            setMenuCreator(delegate.getMenuCreator());
            setToolTipText(delegate.getToolTipText());
            delegate.addPropertyChangeListener(this);
        }

        public RetargetAction getDelegate()
        {
            return mDelegate;
        }

        public void dispose()
        {
            if(mDelegate != null) {
                mDelegate.removePropertyChangeListener(this);
                mDelegate.dispose();
                mDelegate = null;
            }
        }

        @Override
        public void run()
        {
            if(mDelegate != null) {
                mDelegate.run();
                mPreferenceStore.setValue(DropDownAction.this.getId(),mDelegate.getId());
            }
        }

        @Override
        public void runWithEvent(Event event)
        {
            if(mDelegate != null) {
                mDelegate.runWithEvent(event);
                mPreferenceStore.setValue(DropDownAction.this.getId(),mDelegate.getId());
            }
        }

        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(IAction.ENABLED)) {
                Boolean bool = (Boolean) event.getNewValue();
                setEnabled(bool.booleanValue());
            }
            else if (event.getProperty().equals(IAction.CHECKED)) {
                Boolean bool = (Boolean) event.getNewValue();
                setChecked(bool.booleanValue());
            }
            else if (event.getProperty().equals(IAction.DESCRIPTION)) {
                String text = (String) event.getNewValue();
                setDescription(text);
            }
            else if (event.getProperty().equals(IAction.IMAGE)) {
                ImageDescriptor oldImage = (ImageDescriptor)event.getOldValue();
                ImageDescriptor newImage = (ImageDescriptor) event.getNewValue();
                if(oldImage == getImageDescriptor()) {
                    setImageDescriptor(newImage);
                }
                else if(oldImage == getDisabledImageDescriptor()) {
                    setDisabledImageDescriptor(newImage);
                }
                else if(oldImage == getHoverImageDescriptor()) {
                    setHoverImageDescriptor(newImage);
                }
            }
            else if (event.getProperty().equals(IAction.TEXT)) {
                String text = (String) event.getNewValue();
                setText(text);
            }
            else if (event.getProperty().equals(IAction.TOOL_TIP_TEXT)) {
                String text = (String) event.getNewValue();
                setToolTipText(text);
            }
        }
    }
}
