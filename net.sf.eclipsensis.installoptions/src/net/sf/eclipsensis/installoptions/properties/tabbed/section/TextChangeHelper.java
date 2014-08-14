/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public abstract class TextChangeHelper
{
    private boolean mNonUserChange = false;
    private Listener mListener =  new Listener() {
        public void handleEvent(Event e)
        {
            if(e.widget instanceof Text) {
                boolean traverseNext = false;
                final Text text = (Text)e.widget;
                switch(e.type) {
                    case SWT.FocusOut:
                        break;
                    case SWT.KeyDown:
                        if(!text.getEditable()) {
                            text.getDisplay().beep();
                            return;
                        }
                        if(e.character == SWT.CR || e.character == SWT.LF) {
                            boolean isMulti = (text.getStyle() & SWT.MULTI) > 0;
                            if((isMulti && e.stateMask == SWT.CTRL) ||
                               (!isMulti && e.stateMask == 0)) {
                                traverseNext = true;
                                break;
                            }
                        }
                        else if(e.character == SWT.ESC && e.stateMask == 0) {
                            try {
                                setNonUserChange(true);
                                text.setText(getResetValue(text));
                            }
                            finally {
                                setNonUserChange(false);
                            }
                            text.traverse(SWT.TRAVERSE_ESCAPE);
                            return;
                        }
                        return;
                    default:
                        return;
                }
                if(mValidator != null) {
                    String error = mValidator.isValid(text.getText());
                    if(!Common.isEmpty(error)) {
                        try {
                            disconnect(text);
                            Common.openError(text.getShell(), error, InstallOptionsPlugin.getShellImage());
                            text.setText(getResetValue(text));
                            text.forceFocus();
                        }
                        finally {
                            connect(text);
                        }
                        return;
                    }
                }
                handleTextChange(text);
                if(traverseNext) {
                    text.traverse(SWT.TRAVERSE_TAB_NEXT);
                }
            }
        }
    };
    private ICellEditorValidator mValidator;

    public TextChangeHelper(ICellEditorValidator validator)
    {
        mValidator = validator;
    }

    public TextChangeHelper()
    {
        this(null);
    }

    public boolean isNonUserChange()
    {
        return mNonUserChange;
    }

    public void setNonUserChange(boolean nonUserChange)
    {
        mNonUserChange = nonUserChange;
    }

    public void connect(final Text text)
    {
        text.addListener(SWT.FocusOut,mListener);
        text.addListener(SWT.KeyDown,mListener);
        text.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                disconnect(text);
            }
        });
    }

    public void disconnect(Text text)
    {
        text.removeListener(SWT.FocusOut,mListener);
        text.removeListener(SWT.KeyDown,mListener);
    }

    protected abstract String getResetValue(Text text);

    protected abstract void handleTextChange(Text text);
}
