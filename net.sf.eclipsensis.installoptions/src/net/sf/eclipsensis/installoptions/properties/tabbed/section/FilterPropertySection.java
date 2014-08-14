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

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.installoptions.properties.descriptors.PropertyDescriptorHelper;
import net.sf.eclipsensis.installoptions.properties.labelproviders.FileFilterLabelProvider;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.*;

public class FilterPropertySection extends InstallOptionsElementPropertySection
{
    @Override
    protected Control createSection(final InstallOptionsElement element, Composite parent, TabbedPropertySheetPage page, final InstallOptionsCommandHelper commandHelper)
    {
        if(element instanceof InstallOptionsFileRequest) {
            final FileFilter[] current = { null };
            final boolean[] nonUserChange = { false };
            final IPropertyDescriptor descriptor = element.getPropertyDescriptor(InstallOptionsModel.PROPERTY_FILTER);
            final ICellEditorValidator validator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) descriptor);

            final TabbedPropertySheetWidgetFactory widgetFactory = getWidgetFactory();

            Composite parent2 = createSectionComposite(parent);
            GridLayout layout = new GridLayout(2, false);
            layout.marginHeight = layout.marginWidth = 0;
            parent2.setLayout(layout);

            final Group summaryGroup = widgetFactory.createGroup(parent2, InstallOptionsPlugin.getResourceString("filter.summary.group.name")); //$NON-NLS-1$
            summaryGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            summaryGroup.setLayout(new GridLayout(2, false));

            Table summaryTable = widgetFactory.createTable(summaryGroup,SWT.FLAT|SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
            summaryTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            summaryTable.setLinesVisible(true);
            summaryTable.setHeaderVisible(true);

            TableColumn[] columns = new TableColumn[2];
            columns[0] = new TableColumn(summaryTable,SWT.LEFT);
            columns[0].setText(InstallOptionsPlugin.getResourceString("filter.description")); //$NON-NLS-1$
            columns[1] = new TableColumn(summaryTable,SWT.LEFT);
            columns[1].setText(InstallOptionsPlugin.getResourceString("filter.patterns")); //$NON-NLS-1$
            summaryTable.addControlListener(new TableResizer());

            final TableViewer summaryViewer = new TableViewer(summaryTable);
            summaryViewer.setContentProvider(new CollectionContentProvider());
            summaryViewer.setLabelProvider(new FileFilterLabelProvider());

            final Composite summaryButtons = widgetFactory.createComposite(summaryGroup);
            summaryButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            layout= new GridLayout();
            layout.marginHeight= 0;
            layout.marginWidth= 0;
            summaryButtons.setLayout(layout);

            final Button summaryAdd = widgetFactory.createButton(summaryButtons,"",SWT.PUSH); //$NON-NLS-1$
            summaryAdd.setImage(CommonImages.ADD_ICON);
            summaryAdd.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
            summaryAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            summaryAdd.addListener(SWT.Selection, new Listener() {
                @SuppressWarnings("unchecked")
                public void handleEvent(Event e) {
                    if(!nonUserChange[0]) {
                        List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                        if(list != null) {
                            String desc = InstallOptionsPlugin.getFormattedString("default.filter.description",new Object[]{""}).trim(); //$NON-NLS-1$ //$NON-NLS-2$
                            int counter = 1;
                            for(ListIterator<FileFilter> iter=list.listIterator(); iter.hasNext(); ) {
                                if(iter.next().getDescription().equals(desc)) {
                                    while(iter.hasPrevious()) {
                                        iter.previous();
                                    }
                                    desc = InstallOptionsPlugin.getFormattedString("default.filter.description",new Object[]{new Integer(counter++)}); //$NON-NLS-1$
                                    continue;
                                }
                            }
                            FileFilter f = new FileFilter(desc, new FilePattern[]{new FilePattern(InstallOptionsPlugin.getResourceString("default.filter.pattern"))}); //$NON-NLS-1$
                            list.add(f);
                            String error = validator.isValid(list);
                            if(Common.isEmpty(error)) {
                                summaryViewer.refresh(false);
                                summaryViewer.setSelection(new StructuredSelection(f));
                                commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_FILTER, descriptor.getDisplayName(), element, list);
                            }
                            else {
                                Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                                list.remove(f);
                            }
                        }
                    }
                }
            });

            final Button summaryDel = widgetFactory.createButton(summaryButtons, "", SWT.PUSH); //$NON-NLS-1$
            summaryDel.setImage(CommonImages.DELETE_ICON);
            summaryDel.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
            summaryDel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            summaryDel.addListener(SWT.Selection, new Listener() {
                @SuppressWarnings("unchecked")
                public void handleEvent(Event e) {
                    List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                    if(list != null) {
                        IStructuredSelection selection= (IStructuredSelection) summaryViewer.getSelection();
                        if(!selection.isEmpty()) {
                            List<FileFilter> old = new ArrayList<FileFilter>(list);
                            for(Iterator<?> iter=selection.toList().iterator(); iter.hasNext(); ) {
                                list.remove(iter.next());
                            }
                            String error = validator.isValid(list);
                            if(Common.isEmpty(error)) {
                                summaryViewer.refresh(false);
                                commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_FILTER, descriptor.getDisplayName(), element, list);
                            }
                            else {
                                Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                                list.clear();
                                list.addAll(old);
                            }
                        }
                    }
                }
            });
            summaryDel.setEnabled(!summaryViewer.getSelection().isEmpty());

            final TableViewerUpDownMover<List<FileFilter>, FileFilter> summaryMover = new TableViewerUpDownMover<List<FileFilter>, FileFilter>() {
                @Override
                @SuppressWarnings("unchecked")
                protected List<FileFilter> getAllElements()
                {
                    return (List<FileFilter>)((TableViewer)getViewer()).getInput();
                }

                @Override
                protected void updateStructuredViewerInput(List<FileFilter> input, List<FileFilter> elements, List<FileFilter> move, boolean isDown)
                {
                    List<FileFilter> old = new ArrayList<FileFilter>(input);
                    input.clear();
                    input.addAll(elements);
                    String error = validator.isValid(input);
                    if(Common.isEmpty(error)) {
                        summaryViewer.refresh(false);
                        commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_FILTER, descriptor.getDisplayName(), element, input);
                    }
                    else {
                        Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                        input.clear();
                        input.addAll(old);
                    }
                }
            };
            summaryMover.setViewer(summaryViewer);

            final Button summaryUp = widgetFactory.createButton(summaryButtons,"",SWT.PUSH); //$NON-NLS-1$
            summaryUp.setImage(CommonImages.UP_ICON);
            summaryUp.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
            summaryUp.setEnabled(summaryMover.canMoveUp());
            summaryUp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            summaryUp.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    summaryMover.moveUp();
                }
            });

            final Button summaryDown = widgetFactory.createButton(summaryButtons,"", SWT.PUSH); //$NON-NLS-1$
            summaryDown.setImage(CommonImages.DOWN_ICON);
            summaryDown.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
            summaryDown.setEnabled(summaryMover.canMoveDown());
            summaryDown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            summaryDown.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    summaryMover.moveDown();
                }
            });

            final Group detailGroup = widgetFactory.createGroup(parent2, InstallOptionsPlugin.getResourceString("filter.detail.group.name")); //$NON-NLS-1$
            detailGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            detailGroup.setLayout(new GridLayout(1, false));

            boolean isNull = current[0]==null;
            Composite composite = widgetFactory.createComposite(detailGroup);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            layout = new GridLayout(2,false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite.setLayout(layout);

            final CLabel descriptionLabel = widgetFactory.createCLabel(composite,InstallOptionsPlugin.getResourceString("filter.description")); //$NON-NLS-1$
            descriptionLabel.setLayoutData(new GridData());
            descriptionLabel.setEnabled(!isNull);

            final Text descriptionText = widgetFactory.createText(composite,"",SWT.FLAT|SWT.BORDER); //$NON-NLS-1$
            descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            final TextChangeHelper helper = new TextChangeHelper() {
                @Override
                protected String getResetValue(Text text)
                {
                    if(current[0] != null) {
                        return current[0].getDescription();
                    }
                    return ""; //$NON-NLS-1$
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void handleTextChange(Text text)
                {
                    if(current[0] != null) {
                        String oldDescription = current[0].getDescription();
                        current[0].setDescription(text.getText());

                        List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                        String error = validator.isValid(list);
                        if(Common.isEmpty(error)) {
                            summaryViewer.update(current[0],null);
                            commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_FILTER, descriptor.getDisplayName(), element, list);
                        }
                        else {
                            Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                            current[0].setDescription(oldDescription);
                        }
                    }
                }
            };
            helper.connect(descriptionText);
            descriptionText.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    helper.disconnect(descriptionText);
                }
            });
            descriptionText.setEnabled(!isNull);

            final CLabel patternsLabel = widgetFactory.createCLabel(detailGroup,InstallOptionsPlugin.getResourceString("filter.patterns")); //$NON-NLS-1$
            patternsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            patternsLabel.setEnabled(!isNull);

            composite = widgetFactory.createComposite(detailGroup);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            layout = new GridLayout(2,false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite.setLayout(layout);

            final Table patternsTable = widgetFactory.createTable(composite,SWT.FLAT|SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
            patternsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            patternsTable.setLinesVisible(true);
            patternsTable.setEnabled(!isNull);
            new TableColumn(patternsTable,SWT.LEFT);
            final TextCellEditor textEditor = new TextCellEditor(patternsTable);
            ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {
                public void verifyText(VerifyEvent e) {
                    e.doit = e.text.indexOf(IInstallOptionsConstants.LIST_SEPARATOR) < 0 && e.text.indexOf(InstallOptionsFileRequest.FILTER_SEPARATOR) < 0;
                    if(!e.doit) {
                        e.display.beep();
                    }
                }
            });
            patternsTable.addControlListener(new TableResizer());

            textEditor.setValidator(new ICellEditorValidator(){
                public String isValid(Object value)
                {
                    if(!Common.isEmpty((String)value)) {
                        return null;
                    }
                    else {
                        return InstallOptionsPlugin.getResourceString("empty.filter.pattern.error"); //$NON-NLS-1$
                    }
                }
            });

            final TableViewer patternsViewer = new TableViewer(patternsTable);
            patternsViewer.setColumnProperties(new String[]{"pattern"}); //$NON-NLS-1$
            patternsViewer.setContentProvider(new ArrayContentProvider());
            patternsViewer.setLabelProvider(new LabelProvider());
            patternsViewer.setCellEditors(new CellEditor[]{textEditor});
            patternsViewer.setCellModifier(new ICellModifier(){
                public boolean canModify(Object element, String property)
                {
                    return true;
                }

                public Object getValue(Object element, String property)
                {
                    return ((FilePattern)element).getPattern();
                }

                @SuppressWarnings("unchecked")
                public void modify(Object item, String property, Object value)
                {
                    if(value == null) {
                        Common.openError(patternsTable.getShell(),textEditor.getErrorMessage(), InstallOptionsPlugin.getShellImage());
                    }
                    else {
                        FilePattern pattern = (FilePattern)((TableItem)item).getData();
                        String oldValue = pattern.getPattern();
                        pattern.setPattern((String)value);

                        List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                        String error = validator.isValid(list);
                        if(Common.isEmpty(error)) {
                            patternsViewer.update(pattern,null);
                            summaryViewer.update(current[0],null);
                            commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_FILTER, descriptor.getDisplayName(), element, list);
                        }
                        else {
                            Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                            pattern.setPattern(oldValue);
                        }
                    }
                }
            });

            final Composite patternsButtons = widgetFactory.createComposite(composite);
            patternsButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            layout= new GridLayout();
            layout.marginHeight= 0;
            layout.marginWidth= 0;
            patternsButtons.setLayout(layout);

            final Button patternsAdd = widgetFactory.createButton(patternsButtons,"",SWT.PUSH); //$NON-NLS-1$
            patternsAdd.setImage(CommonImages.ADD_ICON);
            patternsAdd.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
            patternsAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            patternsAdd.addListener(SWT.Selection, new Listener() {
                @SuppressWarnings("unchecked")
                public void handleEvent(Event e) {
                    if(current[0] != null) {
                        FilePattern[] oldPatterns = (FilePattern[])patternsViewer.getInput();
                        FilePattern[] patterns = (FilePattern[])Common.resizeArray(oldPatterns,oldPatterns.length+1);
                        String filter = InstallOptionsPlugin.getResourceString("default.filter.pattern"); //$NON-NLS-1$
                        FilePattern newPattern = new FilePattern(filter);
                        patterns[patterns.length-1] = newPattern;
                        current[0].setPatterns(patterns);

                        List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                        String error = validator.isValid(list);
                        if(Common.isEmpty(error)) {
                            patternsViewer.setInput(patterns);
                            patternsViewer.setSelection(new StructuredSelection(newPattern));
                            summaryViewer.update(current[0],null);
                            patternsViewer.editElement(newPattern,0);
                            ((Text)textEditor.getControl()).setSelection(filter.length());
                        }
                        else {
                            Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                            current[0].setPatterns(oldPatterns);
                        }
                    }
                }
            });
            patternsAdd.setEnabled(!isNull);

            final Button patternsDel = widgetFactory.createButton(patternsButtons, "", SWT.PUSH); //$NON-NLS-1$
            patternsDel.setImage(CommonImages.DELETE_ICON);
            patternsDel.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
            patternsDel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            patternsDel.addListener(SWT.Selection, new Listener() {
                @SuppressWarnings("unchecked")
                public void handleEvent(Event e)
                {
                    if(current[0] != null) {
                        FilePattern[] oldPatterns = (FilePattern[])patternsViewer.getInput();
                        int[] indices = patternsTable.getSelectionIndices();
                        FilePattern[] newPatterns = (FilePattern[])Common.resizeArray(oldPatterns, oldPatterns.length-indices.length);
                        int j=0;
                        int k=0;
                        for (int i = 0; i < oldPatterns.length; i++) {
                            if(j >= indices.length || i != indices[j]) {
                                newPatterns[k++] = oldPatterns[i];
                            }
                            else {
                                j++;
                            }
                        }
                        current[0].setPatterns(newPatterns);

                        List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                        String error = validator.isValid(list);
                        if(Common.isEmpty(error)) {
                            patternsViewer.setInput(newPatterns);
                            summaryViewer.update(current[0],null);
                        }
                        else {
                            Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                            current[0].setPatterns(oldPatterns);
                        }
                    }
                }
            });
            IStructuredSelection sel = (IStructuredSelection)patternsViewer.getSelection();
            FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
            int len = Common.isEmptyArray(patterns)?0:patterns.length;
            patternsDel.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);

            final TableViewerUpDownMover<FilePattern[], FilePattern> patternsMover = new TableViewerUpDownMover<FilePattern[], FilePattern>() {
                @Override
                protected List<FilePattern> getAllElements()
                {
                    if(current[0] != null) {
                        return Common.makeList((FilePattern[])((TableViewer)getViewer()).getInput());
                    }
                    return Collections.emptyList();
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void updateStructuredViewerInput(FilePattern[] input, List<FilePattern> elements, List<FilePattern> move, boolean isDown)
                {
                    if(current[0] != null) {
                        FilePattern[] oldPatterns = input.clone();
                        for (int i = 0; i < input.length; i++) {
                            input[i] = elements.get(i);
                        }

                        List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                        String error = validator.isValid(list);
                        if(Common.isEmpty(error)) {
                            patternsViewer.refresh();
                            summaryViewer.update(current[0],null);
                        }
                        else {
                            Common.openError(summaryViewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                            System.arraycopy(oldPatterns,0,input,0,input.length);
                        }
                    }
                }
            };
            patternsMover.setViewer(patternsViewer);

            final Button patternsUp = widgetFactory.createButton(patternsButtons,"",SWT.PUSH); //$NON-NLS-1$
            patternsUp.setImage(CommonImages.UP_ICON);
            patternsUp.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
            patternsUp.setEnabled(!isNull && patternsMover.canMoveUp());
            patternsUp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            patternsUp.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    patternsMover.moveUp();
                }
            });

            final Button patternsDown = widgetFactory.createButton(patternsButtons, "", SWT.PUSH); //$NON-NLS-1$
            patternsDown.setImage(CommonImages.DOWN_ICON);
            patternsDown.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
            patternsDown.setEnabled(!isNull && patternsMover.canMoveDown());
            patternsDown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            patternsDown.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    patternsMover.moveDown();
                }
            });

            patternsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
                    int len = patterns==null?0:patterns.length;
                    patternsDel.setEnabled(!sel.isEmpty() && sel.size() != len && len > 1);
                    patternsUp.setEnabled(patternsMover.canMoveUp());
                    patternsDown.setEnabled(patternsMover.canMoveDown());
                }
            });

            summaryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    summaryDel.setEnabled(!sel.isEmpty());
                    summaryUp.setEnabled(summaryMover.canMoveUp());
                    summaryDown.setEnabled(summaryMover.canMoveDown());
                    current[0] = null;
                    if(!sel.isEmpty()) {
                        if(sel.size() == 1) {
                            current[0] = (FileFilter)sel.getFirstElement();
                        }
                    }
                    boolean isNull = current[0]==null;
                    descriptionText.setText((isNull?"":current[0].getDescription())); //$NON-NLS-1$
                    descriptionText.setSelection(descriptionText.getText().length());
                    patternsViewer.setInput((isNull?null:current[0].getPatterns()));
                    descriptionLabel.setEnabled(!isNull);
                    descriptionText.setEnabled(!isNull);
                    patternsLabel.setEnabled(!isNull);
                    patternsTable.setEnabled(!isNull);
                    patternsAdd.setEnabled(!isNull);
                    FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
                    int len = Common.isEmptyArray(patterns)?0:patterns.length;
                    patternsDel.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);
                    patternsUp.setEnabled(!isNull && patternsMover.canMoveUp());
                    patternsDown.setEnabled(!isNull && patternsMover.canMoveDown());
                }
            });

            summaryViewer.setInput(InstallOptionsFileRequest.FILEFILTER_LIST_CONVERTER.makeCopy(((InstallOptionsFileRequest)element).getFilter()));

            final PropertyChangeListener listener = new PropertyChangeListener() {
                @SuppressWarnings("unchecked")
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FILTER)) {
                        List<FileFilter> newFilter = (List<FileFilter>)evt.getNewValue();
                        List<FileFilter> oldFilter = (List<FileFilter>)summaryViewer.getInput();
                        if(!Common.objectsAreEqual(newFilter, oldFilter)) {
                            try {
                                ISelection sel = summaryViewer.getSelection();
                                nonUserChange[0] = true;
                                summaryViewer.setInput(InstallOptionsFileRequest.FILEFILTER_LIST_CONVERTER.makeCopy(newFilter));
                                if(!sel.isEmpty()) {
                                    summaryViewer.setSelection(sel);
                                }
                            }
                            finally {
                                nonUserChange[0] = false;
                            }
                        }
                    }
                }

            };
            element.addPropertyChangeListener(listener);
            parent2.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    element.removePropertyChangeListener(listener);
                }
            });
            return parent2;
        }
        return null;
    }
}
