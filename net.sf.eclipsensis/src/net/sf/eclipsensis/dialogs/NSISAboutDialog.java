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
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISAboutDialog extends Dialog implements INSISConstants
{
    private static final Image cAboutImage;
    private static final String cAboutTitle;
    private static final String cAboutHeader;
    private static final String cAboutText;

    static {
        cAboutImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("about.icon")); //$NON-NLS-1$

        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        String name = plugin.getName();
        cAboutTitle = EclipseNSISPlugin.getFormattedString("about.title.format", //$NON-NLS-1$
                        new Object[]{name});

        cAboutHeader = EclipseNSISPlugin.getFormattedString("about.header.format", //$NON-NLS-1$
                        new Object[]{name, plugin.getVersion()});

        cAboutText = EclipseNSISPlugin.getResourceString("about.text"); //$NON-NLS-1$
    }

    /**
     * @param parentShell
     */
    public NSISAboutDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setImage(EclipseNSISPlugin.getShellImage());
        newShell.setText(cAboutTitle);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        // create OK button
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                        true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Display display = getShell().getDisplay();
        Color background = JFaceColors.getBannerBackground(display);
        Color foreground = JFaceColors.getBannerForeground(display);

        Composite composite = (Composite)super.createDialogArea(parent);
        composite.setBackground(background);
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.LEFT);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(JFaceResources.getBannerFont());
        if(NSISPreferences.getInstance().getNSISHome() != null) {
            StringBuffer buf = new StringBuffer(cAboutHeader).append(INSISConstants.LINE_SEPARATOR);
            buf.append(EclipseNSISPlugin.getFormattedString("about.header.format", //$NON-NLS-1$
                            new Object[]{EclipseNSISPlugin.getResourceString("makensis.display.name"),  //$NON-NLS-1$
                            NSISPreferences.getInstance().getNSISHome().getNSISExe().getVersion().toString()}));
            label.setText(buf.toString());
        }
        else {
            label.setText(cAboutHeader);
        }
        GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        data.horizontalSpan = 1;
        label.setLayoutData(data);

        label = new Label(composite, SWT.CENTER);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setImage(cAboutImage);
        data = new GridData(SWT.END, SWT.BEGINNING, false, false);
        data.horizontalSpan = 1;
        label.setLayoutData(data);

        Link link = new Link(composite, SWT.WRAP);
        data = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        data.horizontalSpan = 2;
        data.widthHint = convertWidthInCharsToPixels(80);
        link.setLayoutData(data);
        link.setBackground(background);
        link.setText(cAboutText);
        link.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                openLink(e.text);
            }
        });

        label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent)
    {
        Control ctl = super.createButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setFocus();
        return ctl;
    }

    public synchronized void openLink(String link)
    {
        Common.openExternalBrowser(link);
    }
}
