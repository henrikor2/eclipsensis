/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISScrollTipHelper
{
    private ITextViewer mTextViewer;
    private SelectionAdapter mSelAdapter = null;

    public NSISScrollTipHelper(ITextViewer textViewer)
    {
        super();
        mTextViewer = textViewer;
    }

    public void connect()
    {
        if(mSelAdapter == null) {
            final StyledText st = mTextViewer.getTextWidget();
            if(st != null) {
                final ScrollBar sb = st.getVerticalBar();
                if(sb != null) {
                    mSelAdapter = new SelectionAdapter() {
                        private Label mLabel = null;

                        @Override
                        public void widgetSelected(SelectionEvent e)
                        {
                            switch(e.detail) {
                                case SWT.NONE:
                                    if(mLabel != null) {
                                        mLabel.getShell().dispose();
                                        mLabel = null;
                                    }
                                    break;
                                case SWT.DRAG:
                                    Point stLoc = st.toDisplay(0,0);
                                    Point stSize = st.getSize();
                                    Point sbSize = sb.getSize();
                                    int arrowHeight = WinAPI.INSTANCE.getSystemMetrics(WinAPI.SM_CYVSCROLL);
                                    int scrollTop = stLoc.y+arrowHeight;
                                    int scrollHeight = sbSize.y-2*arrowHeight;

                                    Shell shell;
                                    if(mLabel == null || mLabel.isDisposed()) {
                                        shell = makeShell();
                                    }
                                    else {
                                        shell = mLabel.getShell();
                                    }

                                    String text = new StringBuffer().append(mTextViewer.getTopIndex()+1).append(" - ").append(mTextViewer.getBottomIndex()+1).toString(); //$NON-NLS-1$
                                    mLabel.setText(text);
                                    Point extent = shell.computeSize(SWT.DEFAULT,SWT.DEFAULT);
                                    int x = stLoc.x+stSize.x-sbSize.x-extent.x-4;
                                    shell.setBounds(x,scrollTop + (sb.getSelection()-sb.getMinimum())*scrollHeight/(sb.getMaximum()-sb.getMinimum()),
                                                    extent.x,extent.y);
                                    if(!shell.isVisible()) {
                                        shell.setVisible(true);
                                    }
                                    break;
                            }
                        }

                        private Shell makeShell()
                        {
                            Shell shell = new Shell(st.getShell(), SWT.TOOL | SWT.NO_TRIM | SWT.NO_FOCUS | SWT.ON_TOP);
                            Display display = shell.getDisplay();
                            shell.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                            GridLayout layout = new GridLayout(1,true);
                            layout.marginHeight=1;
                            layout.marginWidth=1;
                            shell.setLayout(layout);
                            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
                            shell.setLayoutData(data);
                            Composite composite = new Composite(shell,SWT.NONE);
                            data = new GridData(SWT.FILL, SWT.FILL, true, true);
                            composite.setLayoutData(data);
                            composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                            layout = new GridLayout(1,true);
                            layout.marginHeight=2;
                            layout.marginWidth=2;
                            composite.setLayout(layout);
                            mLabel= new Label(composite, SWT.RIGHT);
                            data= new GridData(SWT.FILL, SWT.FILL, true, true);
                            mLabel.setLayoutData(data);
                            mLabel.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                            mLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

                            return shell;
                        }
                    };
                    sb.addSelectionListener(mSelAdapter);
                }
            }
        }
    }

    public void disconnect()
    {
        if(mSelAdapter != null) {
            StyledText st = mTextViewer.getTextWidget();
            if(st != null) {
                ScrollBar sb = st.getVerticalBar();
                if(sb != null) {
                    sb.removeSelectionListener(mSelAdapter);
                }
            }
            mSelAdapter = null;
        }
    }
}
