/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.config;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISConfigSelectionPage extends WizardSelectionPage
{
    public static final String ID = "nsisConfigSelectionPage"; //$NON-NLS-1$
    private static ImageDescriptor cImage = EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("wizard.title.image")); //$NON-NLS-1$

    public NSISConfigSelectionPage()
    {
        super(ID);
        setTitle(EclipseNSISPlugin.getResourceString("config.wizard.title")); //$NON-NLS-1$
        setImageDescriptor(cImage);
        setDescription(EclipseNSISPlugin.getResourceString("config.wizard.description")); //$NON-NLS-1$
    }

    public void createControl(Composite parent)
    {
        Composite parent2 = new Composite(parent,SWT.NONE);
        parent2.setLayout(new GridLayout(1,false));
        Font wizardFont = parent2.getFont();

        Label l = new Label(parent2,SWT.NONE);
        l.setFont(wizardFont);
        l.setText(EclipseNSISPlugin.getResourceString("config.wizard.welcome.message")); //$NON-NLS-1$

        Group g = new Group(parent2,SWT.NONE);
        g.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        g.setLayout(new GridLayout(1,false));
        l = new Label(g,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("available.config.wizards.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        l.setFont(wizardFont);

        Table table = new Table(g,SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
        table.setFont(wizardFont);
        table.setLinesVisible(false);
        table.setHeaderVisible(false);
        table.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

        TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new LabelProvider() {
            ImageManager mImageManager = EclipseNSISPlugin.getImageManager();
            @Override
            public Image getImage(Object element)
            {
                if(element instanceof NSISConfigWizardNode) {
                    NSISConfigWizardDescriptor descriptor = ((NSISConfigWizardNode)element).getDescriptor();
                    ImageDescriptor icon = descriptor.getIcon();
                    if(icon != null) {
                        if(!mImageManager.containsImage(descriptor.getId())) {
                            mImageManager.putImageDescriptor(descriptor.getId(), icon);
                        }
                        return mImageManager.getImage(descriptor.getId());
                    }
                    return null;
                }
                return super.getImage(element);
            }

            @Override
            public String getText(Object element)
            {
                if(element instanceof NSISConfigWizardNode) {
                    return ((NSISConfigWizardNode)element).getDescriptor().getName();
                }
                return super.getText(element);
            }

        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                if(sel.isEmpty()) {
                    setSelectedNode(null);
                }
                else {
                    NSISConfigWizardNode node = (NSISConfigWizardNode)sel.getFirstElement();
                    setDescription(node.getDescriptor().getDescription());
                    setSelectedNode(node);
                }
            }
        });
        viewer.getTable().addSelectionListener(new SelectionAdapter() {
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
        NSISConfigWizardDescriptor[] wizardDescriptors = NSISConfigWizardRegistry.INSTANCE.getWizardDescriptors();
        Collection<NSISConfigWizardNode> input = new ArrayList<NSISConfigWizardNode>();
        for (int i = 0; i < wizardDescriptors.length; i++) {
            input.add(new NSISConfigWizardNode(this,wizardDescriptors[i]));
        }
        viewer.setInput(input);
        if(input.size() > 0) {
            viewer.setSelection(new StructuredSelection(input.iterator().next()));
        }
        setControl(parent2);
    }

    public boolean canFinishEarly()
    {
        NSISConfigWizardNode node = (NSISConfigWizardNode)getSelectedNode();
        if(node != null) {
            return node.getDescriptor().canFinishEarly();
        }
        return false;
    }
}
