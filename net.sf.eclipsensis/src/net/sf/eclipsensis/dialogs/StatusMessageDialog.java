/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public abstract class StatusMessageDialog extends TrayDialog implements IDialogConstants
{
    protected String mMessage;
    protected Label mMessageLabel;
    protected Label mImageLabel;
    private DialogStatus mStatus = new DialogStatus(IStatus.OK, ""); //$NON-NLS-1$
    private Image mErrorImage = null;
    private Image mWarningImage = null;
    private Image mInfoImage = null;
    private Image mOKImage = null;
    private String mTitle = ""; //$NON-NLS-1$
    private Image mShellImage = EclipseNSISPlugin.getShellImage();

    /**
     * Creates a new dialog.
     *
     * @param parent
     *            the shell parent of the dialog
     */
    public StatusMessageDialog(Shell parent)
    {
        super(parent);
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
    }

    protected Control createMessageArea(Composite composite)
    {
        Image image = getImage();
        if (image != null)
        {
            mImageLabel = new Label(composite, SWT.NONE);
            image.setBackground(mImageLabel.getBackground());
            mImageLabel.setImage(image);
            mImageLabel
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));
        }
        // create message
        if (mMessage != null)
        {
            mMessageLabel = new Label(composite, getMessageLabelStyle());
            mMessageLabel.setText(mMessage);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            mMessageLabel.setLayoutData(data);
            if ((mMessageLabel.getStyle() & SWT.WRAP) == 0)
            {
                mMessageLabel.addControlListener(new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e)
                    {
                        updateMessageLabelToolTip();
                    }
                });
            }
        }
        return composite;
    }

    protected int getMessageLabelStyle()
    {
        return SWT.WRAP;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setImage(mShellImage);
        newShell.setText(mTitle);
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String title)
    {
        mTitle = title;
        if (getShell() != null)
        {
            getShell().setText(mTitle);
        }
    }

    public Image getShellImage()
    {
        return mShellImage;
    }

    public void setShellImage(Image shellImage)
    {
        mShellImage = shellImage;
        if (getShell() != null)
        {
            getShell().setImage(mShellImage);
        }
    }

    /**
     * @return Returns the status.
     */
    public DialogStatus getStatus()
    {
        return mStatus;
    }

    /**
     * Updates the status of the ok button to reflect the given status.
     * Subclasses may override this method to update additional buttons.
     *
     * @param status
     *            the status.
     */
    protected final void updateButtonsEnableState(IStatus status)
    {
        Button b = getButton(IDialogConstants.OK_ID);
        if (b != null && !b.isDisposed())
        {
            b.setEnabled(!status.matches(IStatus.ERROR));
        }
    }

    @Override
    protected Control createContents(Composite parent)
    {
        // create the top level composite for the dialog
        Composite composite = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        applyDialogFont(composite);
        // initialize the dialog units
        initializeDialogUnits(composite);
        // create the dialog area and button bar
        dialogArea = createDialogArea(composite);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
        buttonBar = createButtonBar(composite2);
        GridData gd = (GridData) buttonBar.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        return composite;
    }

    /*
     * @see Dialog#createDialogArea(Composite)
     */
    @Override
    protected final Control createDialogArea(Composite parent)
    {
        Composite parent2 = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent2.setLayout(layout);
        parent2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        mErrorImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("error.icon")); //$NON-NLS-1$
        mWarningImage = EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("warning.icon")); //$NON-NLS-1$
        mInfoImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("info.icon")); //$NON-NLS-1$

        int width = Math.max(mInfoImage.getBounds().width, Math.max(mErrorImage.getBounds().width, mWarningImage
                .getBounds().width));
        int height = Math.max(mInfoImage.getBounds().height, Math.max(mErrorImage.getBounds().height, mWarningImage
                .getBounds().height));
        Image tempImage = EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("transparent.icon")); //$NON-NLS-1$
        ImageData imageData = tempImage.getImageData();
        imageData = imageData.scaledTo(width, height);
        mOKImage = new Image(getShell().getDisplay(), imageData);

        createControlAndMessageArea(parent2);
        applyDialogFont(parent2);
        return parent2;
    }

    /**
     * @param parent
     */
    protected void createControlAndMessageArea(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Control control = createControl(composite);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(data);

        composite = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        mMessage = getMessage();
        createMessageArea(composite);
    }

    protected final void updateStatus(DialogStatus status)
    {
        mStatus = status;
        updateButtonsEnableState(status);
        if (mImageLabel != null && !mImageLabel.isDisposed())
        {
            mImageLabel.setImage(getImage());
        }
        if (mMessageLabel != null && !mMessageLabel.isDisposed())
        {
            mMessageLabel.setText(getMessage());
            updateMessageLabelToolTip();
        }
    }

    private void updateMessageLabelToolTip()
    {
        boolean ok = true;
        if (mMessageLabel != null && !mMessageLabel.isDisposed())
        {
            GC gc = new GC(mMessageLabel);
            Point extent = gc.stringExtent(mMessageLabel.getText());
            gc.dispose();
            ok = mMessageLabel.getSize().x >= extent.x;
        }
        mMessageLabel.setToolTipText(ok ? null : mMessageLabel.getText());
    }

    protected final Image getImage()
    {
        Image image = mStatus.getImage();
        if (image == null)
        {
            switch (mStatus.getSeverity())
            {
                case IStatus.ERROR:
                    return mErrorImage;
                case IStatus.WARNING:
                    return mWarningImage;
                case IStatus.INFO:
                    return mInfoImage;
                default:
                    return mOKImage;
            }
        }
        else
        {
            return image;
        }
    }

    protected final String getMessage()
    {
        return mStatus.getMessage();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#close()
     */
    @Override
    public boolean close()
    {
        if (mOKImage != null && !mOKImage.isDisposed())
        {
            mOKImage.dispose();
        }
        return super.close();
    }

    protected abstract Control createControl(Composite parent);

    protected class DialogStatus extends Status
    {
        private Image mImage;

        public DialogStatus(int severity, String message)
        {
            this(severity, message, null);
        }

        public DialogStatus(int severity, String message, Image image)
        {
            super(severity, INSISConstants.PLUGIN_ID, 0, message, null);
            mImage = image;
            refreshStatus();
        }

        @Override
        protected void setMessage(String message)
        {
            String message2 = message;
            if (message2 == null)
            {
                message2 = ""; //$NON-NLS-1$
            }
            super.setMessage(message2);
        }

        public Image getImage()
        {
            return mImage;
        }

        public void setImage(Image image)
        {
            mImage = image;
            refreshStatus();
        }

        public void setError(String message)
        {
            setSeverity(ERROR);
            setMessage(message);
            refreshStatus();
        }

        public void setWarning(String message)
        {
            setSeverity(WARNING);
            setMessage(message);
            refreshStatus();
        }

        public void setInformation(String message)
        {
            setSeverity(INFO);
            setMessage(message);
            refreshStatus();
        }

        public void setOK()
        {
            setSeverity(OK);
            setMessage(""); //$NON-NLS-1$
            refreshStatus();
        }

        protected final void refreshStatus()
        {
            if (mStatus == this)
            {
                updateStatus(this);
            }
        }
    }
}
