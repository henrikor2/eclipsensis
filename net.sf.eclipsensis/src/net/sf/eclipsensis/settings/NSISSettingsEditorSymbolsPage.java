/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.NSISSymbolDialog;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class NSISSettingsEditorSymbolsPage extends NSISSettingsEditorPage
{
    protected TableViewer mSymbols = null;

    public NSISSettingsEditorSymbolsPage(NSISSettings settings)
    {
        super("symbols",settings); //$NON-NLS-1$
    }

    @Override
    public void enableControls(boolean state)
    {
        if(state) {
            //Hack to properly enable the buttons
            mSymbols.setSelection(mSymbols.getSelection());
        }
    }

    @Override
    protected Control createControl(final Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditSymbol(parent.getShell(),"",""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e)
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)((IStructuredSelection)mSymbols.getSelection()).getFirstElement();
                addOrEditSymbol(parent.getShell(),entry.getKey(),entry.getValue());
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e)
            {
                Map<String, String> map = (Map<String, String>)mSymbols.getInput();
                IStructuredSelection selection = (IStructuredSelection)mSymbols.getSelection();
                for(Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
                    map.remove(((Map.Entry<String, String>)iter.next()).getKey());
                    fireChanged();
                }
                mSymbols.refresh();
            }
        };

        TableViewerUpDownMover<Map<String, String>, Map.Entry<String, String>> mover =
            new TableViewerUpDownMover<Map<String, String>, Map.Entry<String, String>>() {

                @Override
                @SuppressWarnings("unchecked")
                protected List<Map.Entry<String, String>> getAllElements()
                {
                    return new ArrayList<Map.Entry<String, String>>(((Map<String, String>)((TableViewer)getViewer()).getInput()).entrySet());
                }

                @Override
                protected void updateStructuredViewerInput(Map<String, String> input, List<Map.Entry<String, String>> elements,
                        List<Map.Entry<String, String>> move, boolean isDown)
                {
                    input.clear();
                    for(Iterator<Map.Entry<String, String>> iter=elements.iterator(); iter.hasNext(); ) {
                        Map.Entry<String, String> entry = iter.next();
                        input.put(entry.getKey(),entry.getValue());
                        fireChanged();
                    }
                }

            };

        IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
            @SuppressWarnings("unchecked")
            public void doubleClick(DoubleClickEvent event)
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)((IStructuredSelection)event.getSelection()).getFirstElement();
                addOrEditSymbol(parent.getShell(),entry.getKey(),entry.getValue());
            }
        };

        mSymbols = createTableViewer(composite, mSettings.getSymbols(), new MapContentProvider(), new MapLabelProvider(),
                                     EclipseNSISPlugin.getResourceString("symbols.description"), //$NON-NLS-1$
                                     new String[] {
                                         EclipseNSISPlugin.getResourceString("symbols.name.text"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.value.text")}, //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.add.tooltip"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.edit.tooltip"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.remove.tooltip"), //$NON-NLS-1$
                                     addAdapter,editAdapter,removeAdapter, doubleClickListener,
                                     mover);
        return composite;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean performApply(NSISSettings settings)
    {
        if (getControl() != null) {
            mSettings.setSymbols((Map<String, String>)mSymbols.getInput());
        }
        return true;
    }

    @Override
    public void reset()
    {
        mSymbols.setInput(mSettings.getSymbols());
    }

    @Override
    public void setDefaults()
    {
        mSymbols.setInput(mSettings.getDefaultSymbols());
    }

    @SuppressWarnings("unchecked")
    private void addOrEditSymbol(Shell shell, String oldName, String oldValue)
    {
        Map<String, String> map = (Map<String, String>)mSymbols.getInput();
        NSISSymbolDialog dialog = new NSISSymbolDialog(shell,oldName, oldValue);
        Collection<String> coll = new HashSet<String>(map.keySet());
        coll.remove(oldName);
        dialog.setExistingSymbols(coll);
        if(dialog.open() == Window.OK) {
            String newName = dialog.getName();
            boolean dirty = false;
            if(!Common.isEmpty(oldName)) {
                if(!oldName.equals(newName)) {
                    map.remove(oldName);
                    dirty = true;
                }
            }
            else {
                dirty = true;
            }
            String newValue = dialog.getValue();
            if(!oldValue.equals(newValue)) {
                dirty = true;
            }
            map.put(newName,newValue);
            mSymbols.refresh(true);
            if(dirty) {
                fireChanged();
            }
        }
    }

    @Override
    public boolean canEnableControls()
    {
        return true;
    }
}
