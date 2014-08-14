/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.Collection;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISTaskTag;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISTaskTagDialog extends StatusMessageDialog
{
    private NSISTaskTag mTaskTag = null;
    private Collection<String> mExistingTags = null;
    /**
     * @param parentShell
     */
    public NSISTaskTagDialog(Shell parentShell, NSISTaskTag taskTag)
    {
        super(parentShell);
        mTaskTag = taskTag;
        setHelpAvailable(false);
        setTitle(EclipseNSISPlugin.getResourceString((Common.isEmpty(mTaskTag.getTag())?"new.task.tag.dialog.title": //$NON-NLS-1$
                                                                                        "edit.task.tag.dialog.title"))); //$NON-NLS-1$
    }

    /**
     * @param existingTags The existingTags to set.
     */
    public void setExistingTags(Collection<String> existingTags)
    {
        mExistingTags = existingTags;
    }

    /**
     * @return Returns the taskTag.
     */
    public NSISTaskTag getTaskTag()
    {
        return mTaskTag;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#create()
     */
    @Override
    public void create()
    {
        super.create();
        validate();
    }

    @Override
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2,false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.LEFT);
        label.setText(EclipseNSISPlugin.getResourceString("task.tag.label")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = convertWidthInCharsToPixels(50);
        text.setLayoutData(data);
        text.setText(mTaskTag.getTag());

        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mTaskTag.setTag(text.getText());
                validate();
            }
        });

        label = new Label(composite, SWT.LEFT);
        label.setText(EclipseNSISPlugin.getResourceString("task.tag.priority.label")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        final Combo combo = new Combo(composite,SWT.DROP_DOWN|SWT.READ_ONLY);
        if(!Common.isEmptyArray(NSISTaskTag.PRIORITY_LABELS)) {
            combo.setItems(NSISTaskTag.PRIORITY_LABELS);
        }
        int priority = mTaskTag.getPriority();
        if(priority >= 0 && combo.getItemCount() > priority) {
            combo.select(priority);
        }
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mTaskTag.setPriority(combo.getSelectionIndex());
            }
        });

        Dialog.applyDialogFont(composite);
        return composite;
    }

    private void validate()
    {
        DialogStatus status = getStatus();
        String tag = mTaskTag.getTag();
        if(Common.isEmpty(tag)) {
            status.setError(EclipseNSISPlugin.getResourceString("task.tag.dialog.missing.tag")); //$NON-NLS-1$
        }
        else if(!Common.isEmptyCollection(mExistingTags) && mExistingTags.contains(tag)) {
            status.setError(EclipseNSISPlugin.getResourceString("task.tag.dialog.duplicate.tag")); //$NON-NLS-1$
        }
        else if(Character.isWhitespace(tag.charAt(0)) || Character.isWhitespace(tag.charAt(tag.length()-1))) {
            status.setError(EclipseNSISPlugin.getResourceString("task.tag.dialog.whitespace.error")); //$NON-NLS-1$
        }
        else if(tag.indexOf("/*") != -1 || tag.indexOf("*/") != -1 || tag.indexOf('#') != -1 || tag.indexOf(';') != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            status.setError(EclipseNSISPlugin.getResourceString("task.tag.dialog.invalid.chars")); //$NON-NLS-1$
        }
        else {
            status.setOK();
        }
    }
}
