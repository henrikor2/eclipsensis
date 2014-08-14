/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISDocumentSetupParticipant;
import net.sf.eclipsensis.editor.template.*;
import net.sf.eclipsensis.editor.text.NSISTextUtility;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class NSISTemplatePreferencePage extends TemplatePreferencePage
{
    private EclipseNSISPlugin mPlugin = null;
    private NSISTemplateSourceViewer mViewer = null;

    /**
     *
     */
    public NSISTemplatePreferencePage()
    {
        super();
        mPlugin = EclipseNSISPlugin.getDefault();
        setPreferenceStore(mPlugin.getPreferenceStore());
        setTemplateStore(mPlugin.getTemplateStore());
        setContextTypeRegistry(mPlugin.getContextTypeRegistry());
    }

    /*
     * @see PreferencePage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        TableViewer viewer = getTableViewer();
        TableColumn[] columns = viewer.getTable().getColumns();
        if (columns != null && columns.length > 3)
        {
            for (int i = 3; i < columns.length; i++)
            {
                columns[i].dispose();
            }
        }
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_templateprefs_context"); //$NON-NLS-1$
    }

    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#
     * isShowFormatterSetting()
     */
    @Override
    protected boolean isShowFormatterSetting()
    {
        return false;
    }

    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#
     * getFormatterPreferenceKey()
     */
    @Override
    protected String getFormatterPreferenceKey()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer
     * (org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected SourceViewer createViewer(Composite parent)
    {
        mViewer = new NSISTemplateSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        NSISTextUtility.hookSourceViewer(mViewer);
        SourceViewerConfiguration configuration = new NSISTemplateSourceViewerConfiguration(new ChainedPreferenceStore(
                new IPreferenceStore[] { getPreferenceStore(), EditorsUI.getPreferenceStore() }));
        mViewer.configure(configuration);

        IDocument document = new Document();
        new NSISDocumentSetupParticipant().setup(document);
        mViewer.setDocument(document);
        mViewer.setEditable(false);

        return mViewer;
    }

    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @Override
    public boolean performOk()
    {
        boolean ok = super.performOk();

        mPlugin.savePreferences();

        return ok;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            if (mViewer != null && !mViewer.getTextWidget().isDisposed() && mViewer.mustProcessPropertyQueue())
            {
                mViewer.processPropertyQueue();
            }
        }
        super.setVisible(visible);
    }

    @Override
    protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable)
    {
        NSISTemplateEditorDialog dialog = new NSISTemplateEditorDialog(getShell(), template, edit, isNameModifiable,
                getContextTypeRegistry());
        if (dialog.open() == Window.OK)
        {
            return dialog.getTemplate();
        }
        return null;
    }
}
