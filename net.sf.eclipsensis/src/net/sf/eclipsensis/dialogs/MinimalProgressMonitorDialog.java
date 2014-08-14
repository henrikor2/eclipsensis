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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class MinimalProgressMonitorDialog extends ProgressMonitorDialog
{
    public static final int MINIMUM_WIDTH = 500;
    public static final int MAXIMUM_WIDTH = 800;
    public static final int VERTICAL_OFFSET = 85;

    private static final int BAR_DLUS = 9;

    private int mMinimumWidth;
    private int mMaximumWidth;

    private Image mBGImage = null;

    private RGB mForegroundRGB = null;
    private Color mFGColor = null;

    private String mCaption = EclipseNSISPlugin.getDefault().getName();
    private PaintListener mPaintListener =  new PaintListener() {
        public void paintControl(PaintEvent e)
        {
            if(mBGImage != null) {
                Control control = (Control)e.widget;
                Point location = control.toDisplay(0,0);
                Point shellLocation = getShell().getLocation();
                location.x -= shellLocation.x;
                location.y -= shellLocation.y;
                Point size = control.getSize();
                e.gc.drawImage(mBGImage,location.x,location.y,size.x,size.y,0,0,size.x,size.y);
            }
        }
    };
    private PaintListener mLabelPaintListener =  new PaintListener() {
        public void paintControl(PaintEvent e)
        {
            if(mBGImage != null) {
                Label label = (Label)e.widget;
                Point location = label.toDisplay(0,0);
                Point shellLocation = getShell().getLocation();
                location.x -= shellLocation.x;
                location.y -= shellLocation.y;
                Point size = label.getSize();
                final Image newImage = new Image(label.getDisplay(),new Rectangle(0,0,size.x,size.y));
                GC gc = new GC(newImage);
                gc.drawImage(mBGImage,location.x,location.y,size.x,size.y,0,0,size.x,size.y);
                gc.dispose();
                label.removePaintListener(this);
                label.setBackgroundImage(newImage);
                label.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        newImage.dispose();
                    }
                });
            }
        }
    };


    /**
     * Construct an instance of this dialog.
     *
     * @param parent
     */
    public MinimalProgressMonitorDialog(Shell parent)
    {
        this(parent, MINIMUM_WIDTH, MAXIMUM_WIDTH);
    }

    public MinimalProgressMonitorDialog(Shell parent, int minimumWidth, int maximumWidth)
    {
        super(parent);
        mMaximumWidth = maximumWidth;
        mMinimumWidth = minimumWidth;
        setShellStyle(SWT.NONE);
    }

    public Image getBGImage()
    {
        return mBGImage;
    }

    public void setBGImage(Image image)
    {
        mBGImage = image;
        if(getShell() != null && !getShell().isDisposed()) {
            getShell().redraw();
            getShell().update();
        }
    }

    @Override
    protected void configureShell(Shell shell)
    {
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                if(mFGColor != null && !mFGColor.isDisposed()) {
                    mFGColor.dispose();
                }
            }
        });
        super.configureShell(shell);
    }

    public RGB getForegroundRGB()
    {
        return mForegroundRGB;
    }

    public void setForegroundRGB(RGB foregroundRGB)
    {
        mForegroundRGB = foregroundRGB;
        updateForeground();
    }

    private void updateForeground()
    {
        Shell shell = getShell();
        if(shell != null && !shell.isDisposed()) {
            if(mForegroundRGB == null) {
                if(mFGColor != null) {
                    Color fgColor = shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
                    setFGColor(shell,fgColor);
                    mFGColor.dispose();
                    mFGColor = null;
                }
            }
            else {
                Color fgColor = shell.getForeground();
                if(!Common.objectsAreEqual(fgColor.getRGB(),mForegroundRGB)) {
                    fgColor = new Color(shell.getDisplay(),mForegroundRGB);
                    setFGColor(shell, fgColor);
                    if(mFGColor != null) {
                        mFGColor.dispose();
                        mFGColor = null;
                    }
                    mFGColor = fgColor;
                }
            }
        }
    }

    private void setFGColor(Control control, Color fgColor)
    {
        if(!(control instanceof ProgressBar)) {
            control.setForeground(fgColor);
            if(control instanceof Composite) {
                Composite composite = (Composite)control;
                Control[] controls = composite.getChildren();
                if(!Common.isEmptyArray(controls)) {
                    for (int i = 0; i < controls.length; i++) {
                        setFGColor(controls[i], fgColor);
                    }
                }
            }
        }
    }

    public String getCaption()
    {
        return mCaption;
    }

    public void setCaption(String caption)
    {
        mCaption = caption;
    }

    @Override
    protected Control createContents(Composite parent)
    {
        final Composite container = new Composite(parent, (mBGImage==null?SWT.NONE:SWT.NO_BACKGROUND));
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        container.setLayout(gridLayout);

        final Composite progressArea = new Composite(container, SWT.NONE);
        initializeDialogUnits(progressArea);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 5;//convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = gridLayout.marginWidth;
        layout.verticalSpacing = 2;//convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = 2;//convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING) * 2;
        progressArea.setLayout(layout);
        progressArea.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
        createDialogAndButtonArea(progressArea);

        updateForeground();
        if(mBGImage != null) {
            container.addPaintListener(mPaintListener);
            progressArea.addPaintListener(mPaintListener);
            getShell().addPaintListener(mPaintListener);
        }
        return container;
    }

    @Override
    protected Image getImage()
    {
        return null;
    }

    @Override
    protected Control createMessageArea(Composite composite)
    {
        // create message
        if (message != null) {
            messageLabel = new Label(composite, SWT.NONE);
            messageLabel.setText(message);
            GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
            data.horizontalSpan = 2;
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            messageLabel.setLayoutData(data);
            taskLabel = messageLabel;
            if(mBGImage != null) {
                messageLabel.addPaintListener(mLabelPaintListener);
            }
        }
        return composite;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        // task label
        message = ""; //$NON-NLS-1$
        createMessageArea(parent);

        // progress indicator
        progressIndicator = new ProgressIndicator(parent);
        GridData gd = new GridData(SWT.FILL,SWT.CENTER,true,false);
        gd.heightHint = convertVerticalDLUsToPixels(BAR_DLUS);
        gd.horizontalSpan = 2;
        progressIndicator.setLayoutData(gd);

        // label showing sub task
        subTaskLabel = new Label(parent, SWT.LEFT);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.minimumWidth = mMinimumWidth / 2;
        subTaskLabel.setLayoutData(gd);
        subTaskLabel.setFont(parent.getFont());

        Label label = new Label(parent, SWT.RIGHT);
        label.moveBelow(subTaskLabel);
        gd = new GridData(SWT.RIGHT);
        label.setLayoutData(gd);
        label.setFont(parent.getFont());
        label.setText(mCaption);

        if(mBGImage != null) {
            subTaskLabel.addPaintListener(mLabelPaintListener);
            label.addPaintListener(mLabelPaintListener);
        }
        return parent;
    }

    /*
     * see org.eclipse.jface.Window.getInitialLocation()
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        Composite parent = getShell().getParent();

        if (parent == null) {
            return super.getInitialLocation(initialSize);
        }

        Rectangle bounds = null;
        IWorkbench workbench = PlatformUI.getWorkbench();
        if(workbench != null) {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if(window != null && window.getShell().isVisible()) {
                bounds = window.getShell().getBounds();
            }
        }
        if(bounds == null) {
            Monitor monitor = parent.getMonitor();
            bounds = monitor.getBounds();
        }
        Point center = Geometry.centerPoint(bounds);

        return new Point(center.x - (initialSize.x / 2),
                Math.max(bounds.y, Math.min(center.y +VERTICAL_OFFSET, bounds.y+ bounds.height - initialSize.y)));
    }

    @Override
    protected Point getInitialSize()
    {
        Point calculatedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT,true);
        if (calculatedSize.x < mMinimumWidth) {
            calculatedSize.x = mMinimumWidth;
        }
        if (calculatedSize.x > mMaximumWidth) {
            calculatedSize.x = mMaximumWidth;
        }
        return calculatedSize;
    }

    @Override
    protected Control createButtonBar(Composite parent)
    {
        return null;
    }
}
