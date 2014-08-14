/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.internal.ui.rulers.RulerFigure;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class InstallOptionsRulerFigure extends RulerFigure
{
    private double mDPU = -1.0;
    private int mUnit;

    public InstallOptionsRulerFigure(EditPart editPart, boolean isHorizontal, int measurementUnit)
    {
        super(isHorizontal, measurementUnit);
    }

    @Override
    public int getUnit()
    {
        return mUnit;
    }

    @Override
    public void setUnit(int newUnit)
    {
        if (mUnit != newUnit) {
            mUnit = newUnit;
            mDPU = -1.0;
            repaint();
        }
    }

    @Override
    protected void handleZoomChanged()
    {
        mDPU = -1.0;
        repaint();
        layout();
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Figure#invalidate()
     */
    @Override
    public void invalidate()
    {
        super.invalidate();
        mDPU = -1.0;
    }

    @Override
    protected double getDPU()
    {
        if (mDPU <= 0) {
            if (getUnit() == RulerProvider.UNIT_PIXELS) {
                mDPU = 1.0;
            }
            else if(getUnit() == InstallOptionsRulerProvider.UNIT_DLU) {
                Font font = FontUtility.getInstallOptionsFont();//mEditPart.getViewer().getControl().getFont();
                if(isHorizontal()) {
                    mDPU = (FigureUtility.dialogUnitsToPixelsX(1000,font)/1000.0);
                }
                else {
                    mDPU = (FigureUtility.dialogUnitsToPixelsY(1000,font)/1000.0);
                }
            }
            else
            {
                mDPU = transposer.t(new Dimension(Display.getCurrent().getDPI())).height;
                if (getUnit() == RulerProvider.UNIT_CENTIMETERS) {
                    mDPU = mDPU / 2.54;
                }
            }
            if (zoomManager != null) {
                mDPU = mDPU * zoomManager.getZoom();
            }
        }
        return mDPU;
    }
}