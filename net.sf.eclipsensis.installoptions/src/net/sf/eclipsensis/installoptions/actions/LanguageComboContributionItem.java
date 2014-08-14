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

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.IInstallOptionsEditor;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class LanguageComboContributionItem extends ContributionItem implements PropertyChangeListener, IPropertyChangeListener
{
    private static final NSISLanguage DEFAULT;
    private static final Comparator<NSISLanguage> cLanguageComparator = new Comparator<NSISLanguage>() {
        public int compare(NSISLanguage o1, NSISLanguage o2)
        {
            return o1.toString().compareTo(o2.toString());
        }
    };

    private ComboViewer mComboViewer;
    private ToolItem mToolitem;
    private IPartService mPartService;
    private IPartListener mPartListener = new IPartListener() {
        public void partActivated(IWorkbenchPart part)
        {
            if(mComboViewer == null) {
                return;
            }
            Combo combo = mComboViewer.getCombo();
            boolean b = part instanceof IInstallOptionsEditor;
            if(combo != null && !combo.isDisposed()) {
                combo.setEnabled(b);
            }
        }

        public void partBroughtToTop(IWorkbenchPart part)
        {
        }

        public void partClosed(IWorkbenchPart part)
        {
        }

        public void partDeactivated(IWorkbenchPart part)
        {
        }

        public void partOpened(IWorkbenchPart part)
        {
        }
    };
    private IPreferenceStore mPreferenceStore = InstallOptionsPlugin.getDefault().getPreferenceStore();

    static
    {
        String defaultLang = InstallOptionsPlugin.getResourceString("option.default","(Default)"); //$NON-NLS-1$ //$NON-NLS-2$
        DEFAULT = new NSISLanguage(defaultLang, defaultLang, 0, 0);
    }

    public LanguageComboContributionItem(IPartService partService)
    {
        super();
        mPartService = partService;
        mPartService.addPartListener(mPartListener);
        NSISLanguageManager.getInstance().addPropertyChangedListener(this);
        mPreferenceStore.addPropertyChangeListener(this);
    }

    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event)
    {
        if(event.getProperty().equals(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG)) {
            if(mComboViewer == null) {
                return;
            }
            Combo combo = mComboViewer.getCombo();
            if(combo != null && !combo.isDisposed()) {
                String newValue = (String)event.getNewValue();
                Object newSel;
                if(newValue.equals("")) { //$NON-NLS-1$
                    newSel = DEFAULT;
                }
                else {
                    newSel = NSISLanguageManager.getInstance().getLanguage(newValue);
                }
                IStructuredSelection sel = (IStructuredSelection)mComboViewer.getSelection();
                if(!sel.isEmpty()) {
                    Object oldSel = sel.getFirstElement();
                    if(oldSel.equals(newSel)) {
                        return;
                    }
                }
                mComboViewer.setSelection(new StructuredSelection(newSel));
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(NSISLanguageManager.PROPERTY_LANGUAGES)) {
            loadLanguages();
        }
    }

    private void loadLanguages()
    {
        Runnable r = new Runnable() {
            public void run()
            {
                if (mComboViewer == null) {
                    return;
                }
                Combo combo = mComboViewer.getCombo();
                if (combo == null || combo.isDisposed()) {
                    return;
                }
                String pref = mPreferenceStore.getString(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG);
                Object sel;
                List<NSISLanguage> languages = new ArrayList<NSISLanguage>(NSISLanguageManager.getInstance().getLanguages());
                Collections.sort(languages, cLanguageComparator);
                languages.add(0, DEFAULT);
                mComboViewer.setInput(languages);
                if (pref.equals("")) { //$NON-NLS-1$
                    sel = DEFAULT;
                }
                else {
                    sel = NSISLanguageManager.getInstance().getLanguage(pref);
                }
                if (!languages.contains(sel)) {
                    mPreferenceStore.setValue(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG, ""); //$NON-NLS-1$
                }
                else {
                    mComboViewer.setSelection(new StructuredSelection(sel));
                }
            }
        };
        if(Display.getCurrent() != null) {
            r.run();
        }
        else {
            Display.getDefault().syncExec(r);
        }
    }

    protected Control createControl(Composite parent)
    {
        String tooltip = InstallOptionsPlugin.getResourceString("preview.lang.tooltip"); //$NON-NLS-1$
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setToolTipText(tooltip);
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        Label label = new Label(composite,SWT.NONE);
        label.setToolTipText(tooltip);
        label.setText(InstallOptionsPlugin.getResourceString("preview.lang.label")); //$NON-NLS-1$
        Combo combo = new Combo(composite, SWT.DROP_DOWN|SWT.READ_ONLY|SWT.BORDER);
        combo.setToolTipText(tooltip);
        mComboViewer = new ComboViewer(combo);
        mComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                Object firstElement = ((IStructuredSelection)event.getSelection()).getFirstElement();
                String pref;
                if(firstElement == null || firstElement.equals(DEFAULT)) {
                    pref = ""; //$NON-NLS-1$
                }
                else {
                    pref = ((NSISLanguage)firstElement).getName();
                }
                mPreferenceStore.setValue(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG,pref);
            }
        });
        mComboViewer.setContentProvider(new CollectionContentProvider());
        mComboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element)
            {
                if(element instanceof NSISLanguage) {
                    return ((NSISLanguage)element).getDisplayName();
                }
                return super.getText(element);
            }
        });

        // Initialize width of combo
        loadLanguages();
        boolean b = mPartService.getActivePart() instanceof IInstallOptionsEditor;
        combo.setEnabled(b);

        mToolitem.setWidth(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        return composite;
    }

    @Override
    public void dispose()
    {
        NSISLanguageManager.getInstance().removePropertyChangedListener(this);
        mPreferenceStore.removePropertyChangeListener(this);
        if (mPartListener == null) {
            return;
        }
        mPartService.removePartListener(mPartListener);
        mPartListener = null;
        if(mComboViewer == null) {
            return;
        }
        Combo combo = mComboViewer.getCombo();
        if(combo != null && !combo.isDisposed()) {
            combo.dispose();
        }
        mComboViewer = null;
    }

    @Override
    public final void fill(Composite parent)
    {
        createControl(parent);
    }

    @Override
    public final void fill(Menu parent, int index)
    {
    }

    @Override
    public void fill(ToolBar parent, int index)
    {
        mToolitem = new ToolItem(parent, SWT.SEPARATOR, index);
        Control control = createControl(parent);
        mToolitem.setControl(control);
    }
}
