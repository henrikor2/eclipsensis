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

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstructionDialog extends StatusMessageDialog
{
    private String mInstruction = ""; //$NON-NLS-1$
    private Combo mInstructionCombo = null;
    private Text mParametersText = null;

    /**
     * @param parentShell
     */
    public NSISInstructionDialog(Shell parentShell, String instruction)
    {
        super(parentShell);
        mInstruction = instruction;
        setHelpAvailable(false);
        setTitle(EclipseNSISPlugin.getResourceString((Common.isEmpty(mInstruction)?"add.instruction.dialog.title": //$NON-NLS-1$
        "edit.instruction.dialog.title"))); //$NON-NLS-1$
    }

    public NSISInstructionDialog(Shell parentShell)
    {
        this(parentShell,""); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);

        String instruction;
        String parameters;
        int n = mInstruction.indexOf(" "); //$NON-NLS-1$
        if(n > 0) {
            instruction = mInstruction.substring(0,n);
            parameters = mInstruction.substring(n+1);
        }
        else {
            instruction = mInstruction;
            parameters = ""; //$NON-NLS-1$
        }

        mInstructionCombo = createCombo(composite, EclipseNSISPlugin.getResourceString("instructions.instruction.text"), //$NON-NLS-1$
                        EclipseNSISPlugin.getResourceString("instructions.instruction.tooltip"), //$NON-NLS-1$
                        instruction);
        int textLimit;
        try {
            textLimit = Integer.parseInt(NSISPreferences.getInstance().getNSISHome().getNSISExe().getDefinedSymbol("NSIS_MAX_STRLEN")); //$NON-NLS-1$
        }
        catch(Exception ex){
            textLimit = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
        }
        mInstructionCombo.setTextLimit(textLimit);
        mInstructionCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                char[] chars = mInstructionCombo.getText().toCharArray();
                if(!Common.isEmptyArray(chars)) {
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    int pos = mInstructionCombo.getSelection().x;
                    for (int i = 0; i < chars.length; i++) {
                        if(Character.isLetterOrDigit(chars[i]) || i==0 && chars[i]=='!') {
                            buf.append(chars[i]);
                        }
                        else {
                            if(i <= pos && pos > 0) {
                                pos--;
                            }
                        }
                    }
                    if(buf.length() != chars.length) {
                        mInstructionCombo.setText(buf.toString());
                        mInstructionCombo.setSelection(new Point(pos,pos));
                        return;
                    }
                }
                validate();
            }
        });

        mParametersText = createText(composite, EclipseNSISPlugin.getResourceString("instructions.parameters.text"), //$NON-NLS-1$
                        EclipseNSISPlugin.getResourceString("instructions.parameters.tooltip"),parameters); //$NON-NLS-1$
        mParametersText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        return composite;
    }

    @Override
    public void create()
    {
        super.create();
        validate();
    }

    private void validate()
    {
        DialogStatus status = getStatus();
        if(Common.isEmpty(mInstructionCombo.getText())) {
            status.setError(EclipseNSISPlugin.getResourceString("instruction.blank.error")); //$NON-NLS-1$
        }
        else {
            status.setOK();
        }
    }

    protected Combo createCombo(Composite composite, String text, String tooltipText,
                    String value)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 1;
        label.setLayoutData(data);

        Combo combo = new Combo(composite, SWT.DROP_DOWN|SWT.BORDER);
        combo.setToolTipText(tooltipText);
        String[] items = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS);
        if(!Common.isEmptyArray(items)) {
            for(int i=0; i<items.length; i++) {
                combo.add(items[i]);
            }
        }
        items = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.INSTALLER_ATTRIBUTES);
        if(!Common.isEmptyArray(items)) {
            for(int i=0; i<items.length; i++) {
                combo.add(items[i]);
            }
        }

        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 1;
        combo.setLayoutData(data);
        combo.setText(value);
        return combo;
    }

    protected Text createText(Composite composite, String labelText, String tooltipText, String value)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(labelText);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 1;
        label.setLayoutData(data);

        Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setToolTipText(tooltipText);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = convertWidthInCharsToPixels(40);
        text.setLayoutData(data);
        text.setText(value);

        return text;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed()
    {
        mInstruction = new StringBuffer(mInstructionCombo.getText().trim()).append(" ").append(mParametersText.getText().trim()).toString().trim(); //$NON-NLS-1$
        super.okPressed();
    }

    /**
     * @return Returns the instruction.
     */
    public String getInstruction()
    {
        return mInstruction;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent) {
        Control control = super.createButtonBar(parent);
        Button okButton = getButton(OK_ID);
        okButton.setEnabled(!Common.isEmpty(mInstruction));
        return control;
    }
}
