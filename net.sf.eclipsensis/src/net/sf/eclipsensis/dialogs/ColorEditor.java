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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class ColorEditor {
    private Point mRect;
    private Image mImage;
    private RGB mRGB;
    private Color mColor;
    private Button mButton;

    public ColorEditor(Composite parent)
    {
        this(parent,SWT.NONE);
    }

    public ColorEditor(Composite parent, int style)
    {
        mButton= new Button(parent, style|SWT.PUSH);
        mRect= calculateSize(parent);
        mImage= new Image(parent.getDisplay(), mRect.x, mRect.y);

        GC gc= new GC(mImage);
        Color color = gc.getBackground();
        gc.setBackground(mButton.getBackground());
        gc.fillRectangle(0, 0, mRect.x, mRect.y);
        gc.setBackground(color);
        gc.dispose();

        mButton.setImage(mImage);
        mButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ColorDialog colorDialog= new ColorDialog(mButton.getShell());
                colorDialog.setRGB(mRGB);
                RGB newColor = colorDialog.open();
                if (newColor != null) {
                    mRGB= newColor;
                    updateColor();
                }
            }
        });

        mButton.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                if (mImage != null)  {
                    mImage.dispose();
                    mImage= null;
                }
                if (mColor != null) {
                    mColor.dispose();
                    mColor= null;
                }
            }
        });
    }

    public RGB getRGB() {
        return mRGB;
    }

    public void setRGB(RGB rgb) {
        mRGB= rgb;
        updateColor();
    }

    public Button getButton() {
        return mButton;
    }

    protected void updateColor() {

        Display display= mButton.getDisplay();

        GC gc= new GC(mImage);
        Color fgColor = gc.getForeground();
        Color bgColor = gc.getBackground();
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.drawRectangle(0, 2, mRect.x - 1, mRect.y - 4);

        if (mColor != null) {
            mColor.dispose();
        }

        mColor= new Color(display, mRGB);
        gc.setBackground(mColor);
        gc.fillRectangle(1, 3, mRect.x - 2, mRect.y - 5);

        gc.setForeground(fgColor);
        gc.setBackground(bgColor);
        gc.dispose();

        mButton.setImage(mImage);
    }

    protected Point calculateSize(Control window) {
        GC gc= new GC(window);
        Font old = gc.getFont();
        Font f= JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
        gc.setFont(f);
        int height= gc.getFontMetrics().getHeight();
        gc.setFont(old);
        gc.dispose();
        Point p= new Point(height * 3 - 6, height);
        return p;
    }
}