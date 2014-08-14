/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.List;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.winapi.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class SWTControlFigure extends ScrollBarsFigure
{
    private static final int PRINT_BITS = WinAPI.PRF_NONCLIENT | WinAPI.PRF_CLIENT | WinAPI.PRF_ERASEBKGND | WinAPI.PRF_CHILDREN;
    protected static final int TRANSPARENCY_TOLERANCE = 2;

    private Composite mParent;
    private Image mImage;
    private ImageData mImageData;
    private boolean mNeedsReScrape = true;

    private boolean mDisabled = false;
    private boolean mHScroll;
    private boolean mVScroll;
    private int mStyle = -1;
    private PaintListener mSWTPaintListener = new PaintListener() {
        public void paintControl(PaintEvent e)
        {
            final Control source = (Control) e.getSource();
            IHandle handle = Common.getControlHandle(source);
            if(source != null && !source.isDisposed() && !WinAPI.ZERO_HANDLE.equals(handle)) {
                try {
                    source.removePaintListener(mSWTPaintListener);
                    scrapeImage(source);
                }
                catch(Exception ex) {
                    InstallOptionsPlugin.getDefault().log(ex);
                }
                finally {
                    source.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            try {
                                if(!source.isDisposed()) {
                                    source.dispose();
                                }
                            }
                            catch(Exception ex) {
                                InstallOptionsPlugin.getDefault().log(ex);
                            }
                        }
                    });
                }
            }
        }
    };

    public SWTControlFigure(Composite parent, IPropertySource propertySource)
    {
        this(parent, propertySource, -1);
    }

    public SWTControlFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super();
        mStyle = style;
        setLayoutManager(new XYLayout());
        mParent = parent;
        if(mParent != null) {
            mParent.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent e)
                {
                    if(mImage != null && !mImage.isDisposed()) {
                        mImage.dispose();
                    }
                }
            });
        }
        init(propertySource);
    }

    protected void init(IPropertySource propertySource)
    {
        List<?> flags = (List<?>)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
        setHScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_HSCROLL));
        setVScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_VSCROLL));
        setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
    }

    public void setDisabled(boolean disabled)
    {
        mDisabled = disabled;
    }

    public boolean isDisabled()
    {
        return mDisabled;
    }

    public void refresh()
    {
        if(!isNeedsReScrape()) {
            setNeedsReScrape(true);
        }
        layout();
    }

    protected boolean isNeedsReScrape()
    {
        return mNeedsReScrape;
    }

    protected void setNeedsReScrape(boolean needsReScrape)
    {
        mNeedsReScrape = needsReScrape;
    }

    @Override
    public void setBounds(Rectangle rect)
    {
        if(bounds.width != rect.width || bounds.height != rect.height) {
            setNeedsReScrape(true);
        }
        super.setBounds(rect);
    }

    @Override
    protected void paintFigure(Graphics g)
    {
        super.paintFigure(g);
        if (mImage != null && !mImage.isDisposed()) {
            Border b = getBorder();
            Insets insets;
            if(b != null) {
                insets = b.getInsets(this);
            }
            else {
                insets = new Insets(0,0,0,0);
            }
            g.drawImage(mImage,bounds.x+insets.left,bounds.y+insets.top);
        }
    }

    /*
     * @see org.eclipse.draw2d.Figure#layout()
     */
    @Override
    protected synchronized void layout()
    {
        if(isNeedsReScrape()) {
            if (mImage != null && !mImage.isDisposed()) {
                mImage.dispose();
            }
            mImage = null;
            mImageData = null;
            if(isVisible()) {
                int style = mStyle <0?getDefaultStyle():mStyle;
                if(isHScroll()) {
                    style |= SWT.H_SCROLL;
                }
                if(isVScroll()) {
                    style |= SWT.V_SCROLL;
                }
                Control control = createSWTControl(mParent, style);
                control.setFont(FontUtility.getInstallOptionsFont());
                control.setVisible(true);
                control.setEnabled(!mDisabled);
                ControlSubclasser.subclassControl(control, this);
                Point p1 = IInstallOptionsConstants.EMPTY_POINT.getCopy();
                translateToAbsolute(p1);
                Border b = getBorder();
                Insets insets;
                if(b != null) {
                    insets = b.getInsets(this);
                }
                else {
                    insets = new Insets(0,0,0,0);
                }
                setControlBounds(control, bounds.x + p1.x + insets.left, bounds.y + p1.y + insets.right,
                                bounds.width - (insets.left+insets.right),
                                bounds.height - (insets.top+insets.bottom));
                control.addPaintListener(mSWTPaintListener);
                setNeedsReScrape(false);

                scrapeImage(control);

                //Force a repaint
                control.redraw();
            }
        }
        super.layout();
    }

    protected void setControlBounds(Control control, int x, int y, int width, int height)
    {
        control.setBounds(x, y, width, height);
    }

    @Override
    protected boolean isTransparentAt(int x, int y)
    {
        int pixel = mImageData != null?mImageData.transparentPixel:-1;
        if(pixel != -1) {
            Rectangle rect = new Rectangle(x-TRANSPARENCY_TOLERANCE,y-TRANSPARENCY_TOLERANCE,
                            2*TRANSPARENCY_TOLERANCE+1,2*TRANSPARENCY_TOLERANCE+1);
            Rectangle cropped = getBounds().crop(getInsets());
            rect = rect.intersect(cropped).getTranslated(cropped.getLocation().getNegated());
            if(!rect.isEmpty()) {
                int right = rect.x+rect.width;
                for(int i=rect.x; i<right; i++) {
                    int bottom = rect.y+rect.height;
                    for(int j=rect.y; j<bottom; j++) {
                        int p = mImageData.getPixel(i,j);
                        if(p != pixel) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    int getStyle()
    {
        return mStyle;
    }

    public boolean isHScroll()
    {
        return mHScroll;
    }

    public void setHScroll(boolean scroll)
    {
        mHScroll = scroll;
    }

    public boolean isVScroll()
    {
        return mVScroll;
    }

    public void setVScroll(boolean scroll)
    {
        mVScroll = scroll;
    }

    protected void createScrollBars(Control control)
    {
        int style;
        if(isHScroll() || isVScroll()) {
            IHandle handle = Common.getControlHandle(control);
            style = WinAPI.INSTANCE.getWindowLong(handle, WinAPI.GWL_STYLE);
            if (isHScroll()) {
                style |= WinAPI.WS_HSCROLL;
            }
            if (isVScroll()) {
                style |= WinAPI.WS_VSCROLL;
            }
            WinAPI.INSTANCE.setWindowLong(handle,WinAPI.GWL_STYLE,style);
        }
    }

    private void scrapeImage(Control control)
    {
        org.eclipse.swt.graphics.Rectangle rect = control.getBounds();
        mImageData = null;
        if (rect.width <= 0 || rect.height <= 0) {
            if(mImage != null && !mImage.isDisposed()) {
                mImage.dispose();
                mImage = null;
            }
            mImage = new Image(control.getDisplay(), 1, 1);
        }
        else {
            if(mImage != null) {
                if(!mImage.isDisposed()) {
                    if(!mImage.getBounds().equals(rect)) {
                        mImage.dispose();
                        mImage = null;
                    }
                }
                else {
                    mImage = null;
                }
            }
            if(mImage == null) {
                mImage = new Image (control.getDisplay(), rect.width, rect.height);
            }
            GC gc = new GC (mImage);
            IHandle handle = Common.getControlHandle(control);
            IHandle handle2 = Common.getGraphicsHandle(gc);
            WinAPI.INSTANCE.sendMessage (handle, WinAPI.WM_PRINT,
                            WinAPI.INSTANCE.createLongPtr(handle2.getValue()),
                            WinAPI.INSTANCE.createLongPtr(PRINT_BITS));
            gc.dispose ();
        }
        mImageData = mImage.getImageData();
        handleClickThrough(control);
        repaint();
    }

    /**
     * @param control
     */
    protected void handleClickThrough(Control control)
    {
        if(isClickThrough()) {
            mImage.dispose();
            mImageData.transparentPixel=mImageData.getPixel(0,0);
            mImage = new Image(control.getDisplay(), mImageData);
        }
    }

    protected abstract Control createSWTControl(Composite parent, int style);
    public abstract int getDefaultStyle();
}
