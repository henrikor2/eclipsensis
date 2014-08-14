/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.text.Collator;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;
import net.sf.eclipsensis.wizard.template.*;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardWelcomePage extends AbstractNSISWizardStartPage
{
    public static final String NAME = "nsisWizardWelcome"; //$NON-NLS-1$

    private NSISWizardTemplate mTemplate = null;
    private boolean mCreateFromTemplate = false;
    private boolean mUsingTemplate = false;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardWelcomePage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.welcome.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.welcome.description")); //$NON-NLS-1$
    }

    boolean isCreateFromTemplate()
    {
        return mCreateFromTemplate;
    }

    boolean isUsingTemplate()
    {
        return mUsingTemplate;
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return true;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizwelcome_context"; //$NON-NLS-1$
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        final Label header = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.header", true, null, false); //$NON-NLS-1$
        header.setFont(JFaceResources.getBannerFont());
        header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label text = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.text", true, null, false); //$NON-NLS-1$
        final GridData gridData = (GridData)text.getLayoutData();
        Dialog.applyDialogFont(text);
        gridData.widthHint = Common.calculateControlSize(text,80,0).x;

        createTemplatesGroup(composite);

        composite.addListener (SWT.Resize,  new Listener () {
            boolean init = false;

            public void handleEvent (Event e) {
                if(init) {
                    Point size = composite.getSize();
                    gridData.widthHint = size.x - 2*layout.marginWidth;
                    composite.layout();
                }
                else {
                    init=true;
                }
            }
        });

        validatePage(1);

        return composite;
    }

    private Group createTemplatesGroup(Composite parent)
    {
        Group templatesGroup = NSISWizardDialogUtil.createGroup(parent, 1, null,null,false);
        ((GridLayout)templatesGroup.getLayout()).makeColumnsEqualWidth = true;
        GridData data = (GridData)templatesGroup.getLayoutData();
        data.grabExcessVerticalSpace = true;
        data.verticalAlignment = GridData.FILL;

        final Button createFromTemplate = NSISWizardDialogUtil.createCheckBox(templatesGroup,"create.from.template.button.text",false,true,null,false); //$NON-NLS-1$

        MasterSlaveController m = new MasterSlaveController(createFromTemplate);
        SashForm templatesSashForm = new SashForm(templatesGroup,SWT.HORIZONTAL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        templatesSashForm.setLayoutData(data);

        MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                return true;
            }

            public void enabled(Control control, boolean flag)
            {
                int id = (flag?SWT.COLOR_LIST_BACKGROUND:SWT.COLOR_WIDGET_BACKGROUND);
                control.setBackground(getShell().getDisplay().getSystemColor(id));
            }
        };

        Composite composite = new Composite(templatesSashForm,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Label availableTemplatesLabel = NSISWizardDialogUtil.createLabel(composite,"available.templates.label",createFromTemplate.getSelection(),m,true); //$NON-NLS-1$
        data = (GridData)availableTemplatesLabel.getLayoutData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        final List list = new List(composite,SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        list.setLayoutData(data);
        m.addSlave(list, mse);

        composite = new Composite(templatesSashForm,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        Label templateDescLabel = NSISWizardDialogUtil.createLabel(composite,"template.description.label",true,m,false); //$NON-NLS-1$
        templateDescLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final StyledText description = new StyledText(composite,SWT.BORDER|SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
        description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        description.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        description.setCursor(null);
        description.setCaret(null);
        m.addSlave(description, mse);

        final ListViewer templatesViewer = new ListViewer(list);
        templatesViewer.setContentProvider(new CollectionContentProvider());
        templatesViewer.setLabelProvider(new CollectionLabelProvider());
        final NSISWizardTemplateManager templateManager = ((NSISScriptWizard)mWizard).getTemplateManager();
        templatesViewer.setInput(templateManager.getTemplates());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        templatesViewer.setSorter(new ViewerSorter(collator));

        ViewerFilter filter = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element instanceof NSISWizardTemplate) {
                    NSISWizardTemplate template = (NSISWizardTemplate)element;
                    return template.isAvailable() && template.isEnabled() && !template.isDeleted();
                }
                return true;
            }
        };
        templatesViewer.addFilter(filter);

        templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                ISelection sel = event.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof NSISWizardTemplate) {
                        mTemplate = (NSISWizardTemplate)obj;
                        description.setText(mTemplate.getDescription());
                    }
                }
                else {
                    mTemplate = null;
                }
                validatePage(VALIDATE_ALL);
            }
        });

        templatesViewer.getList().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                if(canFlipToNextPage()) {
                    IWizardPage nextPage = getNextPage();
                    if(nextPage != null) {
                        getContainer().showPage(nextPage);
                    }
                }
            }
        });

        createFromTemplate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mCreateFromTemplate = createFromTemplate.getSelection();
                validatePage(VALIDATE_ALL);
            }
        });

        m.updateSlaves();

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if(isPreviousPage() && !isCurrentPage()) {
                    if(getSelectedPage() instanceof AbstractNSISWizardPage) {
                        NSISScriptWizard scriptWizard = (NSISScriptWizard)mWizard;
                        if(!mCreateFromTemplate) {
                            if(mUsingTemplate) {
                                scriptWizard.initSettings();
                                scriptWizard.setTemplate(null);
                                mUsingTemplate = false;
                            }
                        }
                        else {
                            if(mTemplate != null) {
                                scriptWizard.loadTemplate(mTemplate);
                                mTemplate = null;
                                mUsingTemplate = true;
                            }
                        }
                    }
                }
            }
        });
        return templatesGroup;
    }

    @Override
    public boolean validatePage(int flag)
    {
        boolean b = !mCreateFromTemplate || mTemplate != null;
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("select.template.error")); //$NON-NLS-1$
        }
        return b;
    }
}
