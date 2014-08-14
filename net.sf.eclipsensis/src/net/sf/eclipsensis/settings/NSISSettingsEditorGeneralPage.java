/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.NSISInstructionDialog;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public abstract class NSISSettingsEditorGeneralPage extends NSISSettingsEditorPage
{
    private static final String LABELS = "LABELS"; //$NON-NLS-1$

    protected Button mHdrInfo = null;
    protected Button mLicense = null;
    protected Button mNoConfig = null;
    protected Button mNoCD = null;
    protected Combo mVerbosity = null;
    protected Combo mProcessPriority = null;
    protected Combo mCompressor = null;
    protected Button mSolidCompression = null;
    protected TableViewer mInstructions = null;
    private int mProcessPriorityIndex = 0;

    private Group mGroup = null;

    public NSISSettingsEditorGeneralPage(NSISSettings settings)
    {
        super("general",settings); //$NON-NLS-1$
    }

    @Override
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        Composite child = createMasterControl(composite);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        child.setLayoutData(data);

        mGroup = createNSISOptionsGroup(composite);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        mGroup.setLayoutData(data);

        createInstructionsViewer(composite);
        return composite;
    }

    @Override
    public void enableControls(boolean state)
    {
        if(mGroup != null) {
            enableComposite(mGroup,state);
            if(state) {
                //Hack to properly enable the buttons
                setSolidCompressionState();
            }
        }
        if(mInstructions != null) {
            enableComposite(mInstructions.getControl().getParent(),state);
            if(state) {
                //Hack to properly enable the buttons
                mInstructions.setSelection(mInstructions.getSelection());
            }
        }
    }

    private Group createNSISOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("nsis.options.group.text")); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2,false);
        group.setLayout(layout);

        mHdrInfo = createCheckBox(group, EclipseNSISPlugin.getResourceString("hdrinfo.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("hdrinfo.tooltip"), //$NON-NLS-1$
                                  mSettings.getHdrInfo());

        mLicense = createCheckBox(group, EclipseNSISPlugin.getResourceString("license.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("license.tooltip"), //$NON-NLS-1$
                                  mSettings.getLicense());

        mNoConfig = createCheckBox(group, EclipseNSISPlugin.getResourceString("noconfig.text"), //$NON-NLS-1$
                                   EclipseNSISPlugin.getResourceString("noconfig.tooltip"), //$NON-NLS-1$
                                   mSettings.getNoConfig());

        mNoCD = createCheckBox(group, EclipseNSISPlugin.getResourceString("nocd.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("nocd.tooltip"), //$NON-NLS-1$
                               mSettings.getNoCD());

        Composite composite  = new Composite(group,SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        mVerbosity = createCombo(composite,EclipseNSISPlugin.getResourceString("verbosity.text"),EclipseNSISPlugin.getResourceString("verbosity.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 INSISSettingsConstants.VERBOSITY_ARRAY, mSettings.getVerbosity()+1);
        Label l = new Label(composite,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        mCompressor = createCombo(composite,EclipseNSISPlugin.getResourceString("compressor.text"),EclipseNSISPlugin.getResourceString("compressor.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY,mSettings.getCompressor());
        mSolidCompression = createCheckBox(composite, EclipseNSISPlugin.getResourceString("solid.compression.text"), //$NON-NLS-1$
                                          EclipseNSISPlugin.getResourceString("solid.compression.tooltip"), //$NON-NLS-1$
                                          mSettings.getSolidCompression());
        mSolidCompression.setVisible(NSISPreferences.getInstance().isSolidCompressionSupported());
        mCompressor.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                setSolidCompressionState();
            }
        });

        createProcessPriorityCombo(composite);
        internalSetProcessPriorityVisible(NSISPreferences.getInstance().isProcessPrioritySupported());
        return group;
    }

    /**
     * @param parent
     */
    protected void createProcessPriorityCombo(Composite parent)
    {
        Label l;
        mProcessPriority = createCombo(parent,EclipseNSISPlugin.getResourceString("process.priority.text"),EclipseNSISPlugin.getResourceString("process.priority.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                INSISSettingsConstants.PROCESS_PRIORITY_ARRAY, mSettings.getProcessPriority()+1);
        mProcessPriorityIndex = mProcessPriority.getSelectionIndex();
        l = new Label(parent,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        mProcessPriority.setData(LABELS,new Object[] {mProcessPriority.getData(LABEL),l});
        mProcessPriority.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (isProcessPrioritySupported()) {
                    if (isWarnProcessPriority()) {
                        if (mProcessPriority.getSelectionIndex() > INSISSettingsConstants.PROCESS_PRIORITY_HIGH && mProcessPriorityIndex <= INSISSettingsConstants.PROCESS_PRIORITY_HIGH) {
                            MessageDialogWithToggle dialog = new MessageDialogWithToggle(mProcessPriority.getShell(),
                                    EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                    EclipseNSISPlugin.getShellImage(),
                                    EclipseNSISPlugin.getResourceString("process.priority.question"), //$NON-NLS-1$
                                    MessageDialog.WARNING,
                                    new String[] { IDialogConstants.YES_LABEL,
                                                   IDialogConstants.NO_LABEL }, 1,
                                    EclipseNSISPlugin.getResourceString("process.priority.toggle"), //$NON-NLS-1$
                                    false);
                            dialog.open();
                            setWarnProcessPriority(!dialog.getToggleState());
                            if(dialog.getReturnCode()==IDialogConstants.NO_ID) {
                                mProcessPriority.select(mProcessPriorityIndex);
                                return;
                            }
                        }
                    }
                }
                mProcessPriorityIndex = mProcessPriority.getSelectionIndex();
            }
        });
    }

    protected void setProcessPriorityVisible(boolean visible)
    {
        if(mProcessPriority != null && !mProcessPriority.isDisposed()) {
            boolean oldVisible = mProcessPriority.isVisible();
            if(oldVisible != visible) {
                internalSetProcessPriorityVisible(visible);
                mProcessPriority.getShell().layout(new Control[] {mProcessPriority.getParent()});
            }
        }
    }

    /**
     * @param visible
     */
    protected void internalSetProcessPriorityVisible(boolean visible)
    {
        mProcessPriority.setVisible(visible);
        ((GridData)mProcessPriority.getLayoutData()).exclude = !visible;
        Object[] data = (Object[])mProcessPriority.getData(LABELS);
        if(!Common.isEmptyArray(data)) {
            for (int i = 0; i < data.length; i++) {
                if(data[i] instanceof Control && !((Control)data[i]).isDisposed()) {
                    ((Control)data[i]).setVisible(visible);
                    ((GridData)((Control)data[i]).getLayoutData()).exclude = !visible;
                }
            }
        }
    }

    private void setSolidCompressionState()
    {
        int n = mCompressor.getSelectionIndex();
        mSolidCompression.setEnabled(n != MakeNSISRunner.COMPRESSOR_DEFAULT && n != MakeNSISRunner.COMPRESSOR_BEST);
    }

    private Control createInstructionsViewer(final Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditInstruction(parent.getShell(),""); //$NON-NLS-1$
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditInstruction(parent.getShell(),((String)((IStructuredSelection)mInstructions.getSelection()).getFirstElement()).trim());
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e)
            {
                Collection<String> collection = (Collection<String>)mInstructions.getInput();
                IStructuredSelection selection = (IStructuredSelection)mInstructions.getSelection();
                for(Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
                    collection.remove(iter.next());
                    fireChanged();
                }
                mInstructions.refresh();
            }
        };

        TableViewerUpDownMover<List<String>,String> mover = new TableViewerUpDownMover<List<String>,String>() {

            @Override
            @SuppressWarnings("unchecked")
            protected List<String> getAllElements()
            {
                return (List<String>)((TableViewer)getViewer()).getInput();
            }

            @Override
            protected void updateStructuredViewerInput(List<String> input, List<String> elements, List<String> move, boolean isDown)
            {
                input.clear();
                input.addAll(elements);
                fireChanged();
            }
        };

        IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {

                addOrEditInstruction(parent.getShell(),((String)((IStructuredSelection)event.getSelection()).getFirstElement()).trim());
            }
        };

        mInstructions = createTableViewer(composite, mSettings.getInstructions(),
                                      new CollectionContentProvider(), new CollectionLabelProvider(),
                                      EclipseNSISPlugin.getResourceString("instructions.description"), //$NON-NLS-1$
                                      new String[]{EclipseNSISPlugin.getResourceString("instructions.instruction.text")}, //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.add.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.edit.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.remove.tooltip"), //$NON-NLS-1$
                                      addAdapter,editAdapter,removeAdapter, doubleClickListener, mover);
        ((GridLayout)composite.getLayout()).marginWidth = 0;
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);

        return composite;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean performApply(NSISSettings settings)
    {
        if(getControl() != null) {
            settings.setHdrInfo(mHdrInfo.getSelection());
            settings.setLicense(mLicense.getSelection());
            settings.setNoConfig(mNoConfig.getSelection());
            settings.setNoCD(mNoCD.getSelection());
            settings.setVerbosity(mVerbosity.getSelectionIndex()-1);
            settings.setCompressor(mCompressor.getSelectionIndex());
            settings.setSolidCompression(mSolidCompression.getSelection());
            if (NSISPreferences.getInstance().isProcessPrioritySupported()) {
                settings.setProcessPriority(mProcessPriority.getSelectionIndex()-1);
            }
            settings.setInstructions((List<String>)mInstructions.getInput());
        }
        return true;
    }

    @Override
    public void reset()
    {
        mHdrInfo.setSelection(mSettings.getHdrInfo());
        mLicense.setSelection(mSettings.getLicense());
        mNoConfig.setSelection(mSettings.getNoConfig());
        mNoCD.setSelection(mSettings.getNoCD());
        mVerbosity.select(mSettings.getVerbosity()+1);
        mCompressor.select(mSettings.getCompressor());
        mSolidCompression.setSelection(mSettings.getSolidCompression());
        if (NSISPreferences.getInstance().isProcessPrioritySupported()) {
            mProcessPriority.select(mSettings.getProcessPriority()+1);
            mProcessPriorityIndex = mProcessPriority.getSelectionIndex();
        }
        mInstructions.setInput(mSettings.getInstructions());
    }

    @Override
    public void setDefaults()
    {
        mHdrInfo.setSelection(mSettings.getDefaultHdrInfo());
        mLicense.setSelection(mSettings.getDefaultLicense());
        mNoConfig.setSelection(mSettings.getDefaultNoConfig());
        mNoCD.setSelection(mSettings.getDefaultNoCD());
        mVerbosity.select(mSettings.getDefaultVerbosity()+1);
        mCompressor.select(mSettings.getDefaultCompressor());
        mSolidCompression.setSelection(mSettings.getDefaultSolidCompression());
        if (NSISPreferences.getInstance().isProcessPrioritySupported()) {
            mProcessPriority.select(mSettings.getDefaultProcessPriority()+1);
            mProcessPriorityIndex = mProcessPriority.getSelectionIndex();
        }
        mInstructions.setInput(mSettings.getDefaultInstructions());
    }

    @SuppressWarnings("unchecked")
    private void addOrEditInstruction(Shell shell, String oldInstruction)
    {
        NSISInstructionDialog dialog = new NSISInstructionDialog(shell,oldInstruction);
        if(dialog.open() == Window.OK) {
            String newInstruction = dialog.getInstruction();
            Collection<String> collection = (Collection<String>)mInstructions.getInput();
            if(!Common.isEmpty(oldInstruction)) {
                if(!oldInstruction.equals(newInstruction)) {
                    collection.remove(oldInstruction);
                    fireChanged();
                }
            }
            else {
                fireChanged();
            }
            collection.add(newInstruction);
            mInstructions.refresh(true);
        }
    }

    protected boolean isProcessPrioritySupported()
    {
        return NSISPreferences.getInstance().isProcessPrioritySupported();
    }

    protected boolean isWarnProcessPriority()
    {
        return NSISPreferences.getInstance().getPreferenceStore().getBoolean(INSISPreferenceConstants.WARN_PROCESS_PRIORITY);
    }

    protected void setWarnProcessPriority(boolean warnProcessPriority)
    {
        NSISPreferences.getInstance().getPreferenceStore().setValue(INSISPreferenceConstants.WARN_PROCESS_PRIORITY,warnProcessPriority);
    }

    protected abstract Composite createMasterControl(Composite parent);
}
