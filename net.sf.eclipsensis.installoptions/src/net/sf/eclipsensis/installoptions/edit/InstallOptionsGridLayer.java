/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.swt.graphics.Font;

public class InstallOptionsGridLayer extends GridLayer implements IInstallOptionsConstants
{
    private String mStyle = GRID_STYLE_LINES;
    public static final String PROPERTY_GRID_STYLE="net.sf.eclipsensis.installoptions.grid_style"; //$NON-NLS-1$

    public InstallOptionsGridLayer()
    {
        super();
    }

    @Override
    protected void paintGrid(Graphics g)
    {
        try {
            Font f = FontUtility.getInstallOptionsFont();
            double dpuX = ((double)FigureUtility.dialogUnitsToPixelsX(1000,f))/1000;
            double dpuY = ((double)FigureUtility.dialogUnitsToPixelsY(1000,f))/1000;
            g.pushState();
            Rectangle clip = g.getClip(Rectangle.SINGLETON);
            double clipX = clip.x / dpuX;
            double clipWidth = clip.width / dpuX;
            double clipY = clip.y / dpuY;
            double clipHeight = clip.height / dpuY;
            double originX = origin.x;
            double originY = origin.y;
            int distanceX = gridX;
            int distanceY = this.gridY;

            if (distanceX > 0) {
                if (originX >= clipX) {
                    while (originX - distanceX >= clipX) {
                        originX -= distanceX;
                    }
                }
                else {
                    while (originX < clipX) {
                        originX += distanceX;
                    }
                }
            }
            if (distanceY > 0) {
                if (originY >= clipY) {
                    while (originY - distanceY >= clipY) {
                        originY -= distanceY;
                    }
                }
                else {
                    while (originY < clipY) {
                        originY += distanceY;
                    }
                }
            }

            if (GRID_STYLE_DOTS.equals(mStyle)) {
                g.setForegroundColor(ColorConstants.black);
                if (distanceY > 0 && distanceY > 0) {
                    for (double i = originY; i < clipY + clipHeight; i += distanceY) {
                        for (double j = originX; j < clipX + clipWidth; j += distanceX) {
                            int x = FigureUtility.dialogUnitsToPixelsX((int)j,f);//(int)(i * mDpuY);
                            int y = FigureUtility.dialogUnitsToPixelsY((int)i,f);//(int)(i * mDpuY);
                            g.drawPoint(x,y);
                        }
                    }
                }
            }
            else {
                g.setForegroundColor(ColorConstants.lightGray);
                if (distanceX > 0) {
                    for (double i = originY; i < clipY + clipHeight; i += distanceY) {
                        int y = FigureUtility.dialogUnitsToPixelsY((int)i,f);//(int)(i * mDpuY);
                        g.drawLine(clip.x, y, clip.x + clip.width, y);
                    }
                }
                if (distanceY > 0) {
                    for (double i = originX; i < clipX + clipWidth; i += distanceX) {
                        int x = FigureUtility.dialogUnitsToPixelsX((int)i,f);//(int)(i * mDpuY);
                        g.drawLine(x, clip.y, x, clip.y + clip.height);
                    }
                }
            }
        }
        finally {
            g.popState();
            g.restoreState();
        }
    }

    public String getStyle()
    {
        return mStyle;
    }

    public void setStyle(String style)
    {
        String style2 = style;
        if (style2 == null || !IInstallOptionsConstants.GRID_STYLE_DOTS.equals(style2)) {
            style2 = GRID_STYLE_LINES;
        }
        if (!style2.equals(mStyle)) {
            mStyle = style2;
            repaint();
        }
    }
}
