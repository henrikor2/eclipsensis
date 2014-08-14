/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsRootEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

public class InstallOptionsXYStatusContribution extends StatusLineContributionItem
{
    private InstallOptionsDesignEditor mEditor = null;
    private Viewport mViewport;
    private XYStatusMouseListener mMouseListener = new XYStatusMouseListener();
    private FigureCanvas mFigureCanvas;
    private int mKey;

    public InstallOptionsXYStatusContribution()
    {
        super("InstallOptionsXYStatus",true,44); //$NON-NLS-1$
    }

    public void editorChanged(IEditorPart part) {
        if (mEditor != null) {
            if(mFigureCanvas != null && !mFigureCanvas.isDisposed()) {
                mFigureCanvas.removeMouseListener(mMouseListener);
                mFigureCanvas.removeMouseMoveListener(mMouseListener);
            }
            mViewport = null;
            mFigureCanvas = null;
        }
        mEditor = null;

        if (part instanceof InstallOptionsDesignEditor) {
            try {
                InstallOptionsRootEditPart editPart = (InstallOptionsRootEditPart)((InstallOptionsDesignEditor)part).getGraphicalViewer().getRootEditPart();
                mFigureCanvas = (FigureCanvas)editPart.getViewer().getControl();
                mViewport = mFigureCanvas.getViewport();
                mFigureCanvas.addMouseListener(mMouseListener);
                mFigureCanvas.addMouseMoveListener(mMouseListener);
                mEditor = (InstallOptionsDesignEditor)part;
                mFigureCanvas.addKeyListener(new KeyListener(){
                    public void keyPressed(KeyEvent e)
                    {
                        if((e.stateMask & SWT.ALT) == 0 && (e.stateMask & SWT.CTRL) == 0) {
                            mKey = e.character;
                        }
                        else {
                            mKey = -1;
                        }
                    }

                    public void keyReleased(KeyEvent e)
                    {
                        mKey = -1;
                    }
                });
            }
            catch(Exception ex) {
                mEditor = null;
            }
        }
    }

    private final class XYStatusMouseListener extends MouseAdapter implements MouseMoveListener
    {
        private boolean mMouseDown = false;
        private Point mOrigin = null;

        @Override
        public void mouseDown(MouseEvent e)
        {
            mMouseDown = true;
            Rectangle r = mViewport.getClientArea();
            int x = r.x+e.x;
            int y = r.y+e.y;
            IFigure figure = mFigureCanvas.getContents().findFigureAt(x,y);
            if(figure == null) {
                //This is a possible marquee- check for panning
                if(mKey != 0x20) {
                    mOrigin = FigureUtility.pixelsToDialogUnits(new Point(x,y), FontUtility.getInstallOptionsFont());
                }
            }
        }

        @Override
        public void mouseUp(MouseEvent e)
        {
            mMouseDown = false;
            mOrigin = null;
        }

        public void mouseMove(MouseEvent e)
        {
            Rectangle r = mViewport.getClientArea();
            StringBuffer buf = new StringBuffer();
            if(mMouseDown && mOrigin != null) {
                buf.append("(x:").append(mOrigin.x).append(",y:").append(mOrigin.y).append(")->"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            Font f = FontUtility.getInstallOptionsFont();
            int x = FigureUtility.pixelsToDialogUnitsX(r.x+e.x, f);
            int y = FigureUtility.pixelsToDialogUnitsY(r.y+e.y, f);
            buf.append("(x:").append(x).append(",y:").append(y).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(mMouseDown && mOrigin != null) {
                buf.append("=(").append(Math.abs(x-mOrigin.x)+1).append("x").append(Math.abs(y-mOrigin.y)+1).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            setText(buf.toString());
        }
    }
}
