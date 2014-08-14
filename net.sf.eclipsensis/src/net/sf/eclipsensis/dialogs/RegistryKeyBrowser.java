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
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;
import net.sf.eclipsensis.util.winapi.WinAPI.HKEY;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class RegistryKeyBrowser extends Composite
{
    private RegistryRoot mRegistryRoot = new RegistryRoot();
    private String mSelection = null;
    private RegistryKey mRegistryKey = null;
    private Tree mTree = null;

    public RegistryKeyBrowser(Composite parent, int style)
    {
        super(parent, checkStyle(style));
        create();
    }

    static int checkStyle (int style) {
        int mask = SWT.BORDER | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        return style & mask;
    }

    private void create()
    {
        Listener listener = new Listener() {
            public void handleEvent(Event event)
            {
                switch (event.type) {
                    case SWT.Dispose:
                        new Thread(new Runnable() {
                            public void run()
                            {
                                ((RegistryKey)mRegistryRoot).close();
                            }

                        },EclipseNSISPlugin.getResourceString("registry.unloader.thread.name")).start(); //$NON-NLS-1$
                        break;
                    case SWT.Resize:
                        internalLayout (false);
                        break;
                }
            }
        };
        addListener(SWT.Dispose,listener);
        addListener(SWT.Resize,listener);

        int style = getStyle() & ~SWT.BORDER;
        mTree = new Tree(this,SWT.VIRTUAL|style);
        mTree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                final TreeItem item = (TreeItem)event.item;
                TreeItem parentItem = item.getParentItem();
                RegistryKey key;
                boolean expanded = false;
                if (parentItem == null) {
                    key = mRegistryRoot;
                    expanded = true;
                }
                else
                {
                    RegistryKey parent = (RegistryKey)parentItem.getData();
                    key = parent.getChildren()[parentItem.indexOf(item)];
                }
                item.setData(key);
                item.setText(key.getName());
                item.setImage(key.getImage());
                int childCount = key.getChildCount();
                item.setItemCount(childCount<0?1:childCount);
                if(expanded) {
                    item.setExpanded(expanded);
                }
                if(key instanceof RegistryRoot) {
                    updateSelection();
                }
            }
        });
        mTree.addTreeListener(new TreeListener() {
            public void treeExpanded(TreeEvent e)
            {
                final RegistryKey key = (RegistryKey)e.item.getData();
                BusyIndicator.showWhile(e.display,new Runnable() {
                    public void run()
                    {
                        key.open();
                    }
                });
                TreeItem item = (TreeItem)e.item;
                item.setItemCount(key.getChildCount());
                if(key.getChildCount() > 0) {
                    item.setImage(key.getExpandedImage());
                }
            }

            public void treeCollapsed(TreeEvent e)
            {
                RegistryKey key = (RegistryKey)e.item.getData();
                TreeItem item = (TreeItem)e.item;
                item.setImage(key.getImage());
            }
        });
        mTree.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event)
            {
                Event e = new Event ();
                e.time = event.time;
                e.stateMask = event.stateMask;
                notifyListeners (SWT.DefaultSelection, e);
            }

            public void widgetSelected(SelectionEvent event)
            {
                TreeItem item = (TreeItem)event.item;
                saveSelection(item, event);
            }
        });
        mTree.setItemCount(1); // The registry root "My Computer"
    }

    /**
     * @param item
     * @param regKey
     */
    private boolean select(final TreeItem item, final String regKey)
    {
        String keyName;
        String subKeyName;
        int n = regKey.indexOf('\\');
        if(n > 0) {
            subKeyName = regKey.substring(n+1);
            keyName = regKey.substring(0,n);
        }
        else {
            keyName = regKey;
            subKeyName = null;
        }
        RegistryKey key = (RegistryKey)item.getData();
        int index = key.find(keyName);
        if(index >= 0) {
            TreeItem childItem = item.getItem(index);
            if(subKeyName != null) {
                boolean isExpanded = item.getExpanded();
                if(!isExpanded) {
                    item.setExpanded(true);
                }
                key = key.getChildren()[index];
                key.open();
                if(childItem.getItemCount() != key.getChildCount()) {
                    childItem.setItemCount(key.getChildCount());
                }
                boolean result = select(childItem, subKeyName);
                if(!result) {
                    item.setExpanded(isExpanded);
                }
                return result;
            }
            else {
                Tree tree = childItem.getParent();
                tree.showItem(childItem);
                tree.setSelection(childItem);
                saveSelection(childItem, null);
                return true;
            }
        }
        return false;
    }

    private void saveSelection(TreeItem item, SelectionEvent event)
    {
        String oldSelection = mSelection;
        RegistryKey oldRegKey = mRegistryKey;

        mRegistryKey = (RegistryKey)item.getData();
        mSelection = null;
        if(mRegistryKey != null) {
            mSelection = mRegistryKey.toString();
        }
        Event e = new Event ();
        e.time = event == null?(int)System.currentTimeMillis():event.time;
        e.stateMask = event == null?0:event.stateMask;
        e.doit = event == null?true:event.doit;
        notifyListeners (SWT.Selection, e);
        if(event != null) {
            event.doit = e.doit;
        }
        if(!e.doit) {
            mSelection = oldSelection;
            mRegistryKey = oldRegKey;
        }
    }

    public void updateSelection()
    {
        if(mTree != null && mSelection != null) {
            int n = mSelection.indexOf("\\"); //$NON-NLS-1$
            HKEY rootKey;
            String subKey;
            if(n > 0) {
                rootKey = RegistryRoot.getRootKey(mSelection.substring(0,n));
                subKey = mSelection.substring(n+1);
            }
            else {
                rootKey = RegistryRoot.getRootKey(mSelection);
                subKey = null;
            }
            if(rootKey != null) {
                boolean exists = true;
                if(subKey != null) {
                    exists = WinAPI.INSTANCE.regKeyExists(rootKey.getHandle(), subKey);
                }
                if(exists) {
                    final TreeItem item = mTree.getItem(0);
                    final String regKey = subKey==null?RegistryRoot.getRootKeyName(rootKey):new StringBuffer(RegistryRoot.getRootKeyName(rootKey)).append("\\").append(subKey).toString(); //$NON-NLS-1$
                    item.getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            BusyIndicator.showWhile(item.getDisplay(),new Runnable() {
                                public void run()
                                {
                                    try {
                                        mTree.setRedraw(false);
                                        select(item, regKey);
                                    }
                                    finally {
                                        mTree.setRedraw(true);
                                        mTree.update();
                                    }
                                }
                            });
                        }
                    });
                }
                else {
                    mRegistryKey = null;
                    mSelection = null;
                }
            }
        }
    }

    @Override
    public void redraw ()
    {
        super.redraw();
        mTree.redraw();
    }

    @Override
    public void setToolTipText (String string)
    {
        checkWidget();
        super.setToolTipText(string);
        mTree.setToolTipText (string);
    }

    @Override
    public void redraw (int x, int y, int width, int height, boolean all)
    {
        super.redraw(x, y, width, height, true);
    }

    @Override
    public void setBackground (Color color)
    {
        super.setBackground(color);
        if (mTree != null) {
            mTree.setBackground(color);
        }
    }

    @Override
    public void setForeground (Color color)
    {
        super.setForeground(color);
        if (mTree != null) {
            mTree.setForeground(color);
        }
    }

    @Override
    public void setFont (Font font)
    {
        super.setFont(font);
        if (mTree != null) {
            mTree.setFont(font);
        }
        internalLayout (true);
    }

    void internalLayout (boolean changed)
    {
        Rectangle rect = getClientArea();
        int width = rect.width;
        int height = rect.height;
        mTree.setBounds (0, 0, width, height);
    }

    @Override
    public void setEnabled (boolean enabled)
    {
        super.setEnabled(enabled);
        if (mTree != null) {
            mTree.setVisible (false);
        }
    }

    @Override
    public boolean setFocus ()
    {
        checkWidget();
        return mTree.setFocus ();
    }

    @Override
    public void setLayout (Layout layout)
    {
        checkWidget ();
        return;
    }

    @Override
    public boolean isFocusControl ()
    {
        checkWidget();
        if (mTree.isFocusControl ()) {
            return true;
        }
        return super.isFocusControl ();
    }

    @Override
    public Control [] getChildren ()
    {
        checkWidget();
        return new Control [0];
    }

    public void addSelectionListener(SelectionListener listener)
    {
        checkWidget();
        if (listener == null) {
            SWT.error (SWT.ERROR_NULL_ARGUMENT);
        }
        TypedListener typedListener = new TypedListener (listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection,typedListener);
    }

    public RegistryKey getSelectedKey()
    {
        return mRegistryKey;
    }

    public String getSelection()
    {
        checkWidget();
        return mSelection;
    }

    public void removeSelectionListener(SelectionListener listener)
    {
        checkWidget();
        if (listener == null) {
            SWT.error (SWT.ERROR_NULL_ARGUMENT);
        }
        removeListener(SWT.Selection, listener);
        removeListener(SWT.DefaultSelection,listener);
    }

    public void select(String regKey)
    {
        checkWidget();
        mRegistryKey = null;
        mSelection = regKey;
        if(mTree != null && !mTree.isDisposed()) {
            updateSelection();
        }
    }

    public void deselect()
    {
        select(null);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed)
    {
        checkWidget ();
        Point size = mTree.computeSize(wHint, hHint, changed);
        int borderWidth = getBorderWidth ();
        int height = size.y;
        int width = size.x;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        return new Point (width + 2*borderWidth, height + 2*borderWidth);
    }

}
