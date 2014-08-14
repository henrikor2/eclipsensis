/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsFileRequest;
import net.sf.eclipsensis.installoptions.properties.labelproviders.FileFilterLabelProvider;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class FileFilterDialog extends Dialog
{
    private List<FileFilter> mFilter;
    private FileFilter mCurrent = null;
    private ICellEditorValidator mValidator;

    /**
     * @param parentShell
     */
    public FileFilterDialog(Shell parentShell, List<FileFilter> filter)
    {
        super(parentShell);
        setShellStyle(getShellStyle()|SWT.RESIZE);
        mFilter = InstallOptionsFileRequest.FILEFILTER_LIST_CONVERTER.makeCopy(filter);
    }

    public ICellEditorValidator getValidator()
    {
        return mValidator;
    }

    public void setValidator(ICellEditorValidator validator)
    {
        mValidator = validator;
    }

    public List<FileFilter> getFilter()
    {
        return mFilter;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(InstallOptionsPlugin.getResourceString("filter.dialog.name")); //$NON-NLS-1$
        newShell.setImage(InstallOptionsPlugin.getShellImage());
    }

    @Override
    protected void okPressed()
    {
        ICellEditorValidator validator = getValidator();
        if(validator != null) {
            String error = validator.isValid(getFilter());
            if(!Common.isEmpty(error)) {
                Common.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error, //$NON-NLS-1$
                                 InstallOptionsPlugin.getShellImage());
                return;
            }
        }
        super.okPressed();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Control createDialogArea(Composite parent)
    {
        GridLayout layout;
        Composite composite = (Composite)super.createDialogArea(parent);

        final Group summaryGroup = new Group(composite,SWT.SHADOW_ETCHED_IN);
        summaryGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        summaryGroup.setLayout(new GridLayout(2, false));
        summaryGroup.setText(InstallOptionsPlugin.getResourceString("filter.summary.group.name")); //$NON-NLS-1$
        Table summaryTable = new Table(summaryGroup,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
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

        final Composite summaryButtons = new Composite(summaryGroup,SWT.NONE);
        summaryButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        summaryButtons.setLayout(layout);

        final Button summaryAdd = new Button(summaryButtons,SWT.PUSH);
        summaryAdd.setImage(CommonImages.ADD_ICON);
        summaryAdd.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        summaryAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        summaryAdd.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                if(list != null) {
                    String desc = InstallOptionsPlugin.getFormattedString("default.filter.description",new Object[]{""}).trim(); //$NON-NLS-1$ //$NON-NLS-2$
                    int counter = 1;
                    for(ListIterator<FileFilter> iter=list.listIterator(); iter.hasNext(); ) {
                        if((iter.next()).getDescription().equals(desc)) {
                            while(iter.hasPrevious()) {
                                iter.previous();
                            }
                            desc = InstallOptionsPlugin.getFormattedString("default.filter.description",new Object[]{new Integer(counter++)}); //$NON-NLS-1$
                            continue;
                        }
                    }
                    FileFilter f = new FileFilter(desc, new FilePattern[]{new FilePattern(InstallOptionsPlugin.getResourceString("default.filter.pattern"))}); //$NON-NLS-1$
                    list.add(f);
                    summaryViewer.refresh(false);
                    summaryViewer.setSelection(new StructuredSelection(f));

                }
            }
        });

        final Button summaryDel = new Button(summaryButtons, SWT.PUSH);
        summaryDel.setImage(CommonImages.DELETE_ICON);
        summaryDel.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        summaryDel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        summaryDel.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                List<FileFilter> list = (List<FileFilter>)summaryViewer.getInput();
                if(list != null) {
                    IStructuredSelection selection= (IStructuredSelection) summaryViewer.getSelection();
                    if(!selection.isEmpty()) {
                        for(Iterator<?> iter=selection.toList().iterator(); iter.hasNext(); ) {
                            list.remove(iter.next());
                        }
                        summaryViewer.refresh(false);
                    }
                }
            }
        });
        summaryDel.setEnabled(!summaryViewer.getSelection().isEmpty());

        final TableViewerUpDownMover<List<FileFilter>, FileFilter> summaryMover = new TableViewerUpDownMover<List<FileFilter>, FileFilter>() {
            @Override
            protected List<FileFilter> getAllElements()
            {
                return (List<FileFilter>)((TableViewer)getViewer()).getInput();
            }

            @Override
            protected void updateStructuredViewerInput(List<FileFilter> input, List<FileFilter> elements, List<FileFilter> move, boolean isDown)
            {
                (input).clear();
                (input).addAll(elements);
            }
        };
        summaryMover.setViewer(summaryViewer);

        final Button summaryUp = new Button(summaryButtons,SWT.PUSH);
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

        final Button summaryDown = new Button(summaryButtons, SWT.PUSH);
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

        final Group detailsGroup = new Group(composite,SWT.SHADOW_ETCHED_IN);
        detailsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        detailsGroup.setLayout(new GridLayout(1, false));
        detailsGroup.setText(InstallOptionsPlugin.getResourceString("filter.detail.group.name")); //$NON-NLS-1$

        boolean isNull = (mCurrent==null);
        Composite detailsComposite = new Composite(detailsGroup,SWT.NONE);
        detailsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        detailsComposite.setLayout(layout);

        final Label descriptionLabel = new Label(detailsComposite,SWT.NONE);
        descriptionLabel.setText(InstallOptionsPlugin.getResourceString("filter.description")); //$NON-NLS-1$
        descriptionLabel.setLayoutData(new GridData());
        descriptionLabel.setEnabled(!isNull);

        final Text descriptionText = new Text(detailsComposite,SWT.BORDER);
        descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if(mCurrent != null) {
                    mCurrent.setDescription(descriptionText.getText());
                    summaryViewer.update(mCurrent,null);
                }
            }
        });
        descriptionText.setEnabled(!isNull);

        final Label patternsLabel = new Label(detailsGroup,SWT.NONE);
        patternsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        patternsLabel.setText(InstallOptionsPlugin.getResourceString("filter.patterns")); //$NON-NLS-1$
        patternsLabel.setEnabled(!isNull);

        detailsComposite = new Composite(detailsGroup,SWT.NONE);
        detailsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        detailsComposite.setLayout(layout);

        final Table patternsTable = new Table(detailsComposite,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
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

            public void modify(Object element, String property, Object value)
            {
                if(value == null) {
                    Common.openError(getShell(),textEditor.getErrorMessage(), InstallOptionsPlugin.getShellImage());
                }
                else {
                    FilePattern pattern = (FilePattern)((TableItem)element).getData();
                    pattern.setPattern((String)value);
                    patternsViewer.update(pattern,null);
                    summaryViewer.update(mCurrent,null);
                }
            }
        });

        final Composite patternsButtons = new Composite(detailsComposite,SWT.NONE);
        patternsButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        patternsButtons.setLayout(layout);

        final Button patternsAdd = new Button(patternsButtons,SWT.PUSH);
        patternsAdd.setImage(CommonImages.ADD_ICON);
        patternsAdd.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        patternsAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        patternsAdd.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if(mCurrent != null) {
                    FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
                    patterns = (FilePattern[])Common.resizeArray(patterns,patterns.length+1);
                    String filter = InstallOptionsPlugin.getResourceString("default.filter.pattern"); //$NON-NLS-1$
                    patterns[patterns.length-1] = new FilePattern(filter);
                    mCurrent.setPatterns(patterns);
                    patternsViewer.setInput(patterns);
                    patternsViewer.setSelection(new StructuredSelection(patterns[patterns.length-1]));
                    summaryViewer.update(mCurrent,null);
                    patternsViewer.editElement(patterns[patterns.length-1],0);
                    ((Text)textEditor.getControl()).setSelection(filter.length());
                }
            }
        });
        patternsAdd.setEnabled(!isNull);

        final Button patternsDel = new Button(patternsButtons, SWT.PUSH);
        patternsDel.setImage(CommonImages.DELETE_ICON);
        patternsDel.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        patternsDel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        patternsDel.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e)
            {
                if(mCurrent != null) {
                    FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
                    int[] indices = patternsTable.getSelectionIndices();
                    FilePattern[] patterns2 = (FilePattern[])Common.resizeArray(patterns, patterns.length-indices.length);
                    int j=0;
                    int k=0;
                    for (int i = 0; i < patterns.length; i++) {
                        if(j >= indices.length || i != indices[j]) {
                            patterns2[k++] = patterns[i];
                        }
                        else {
                            j++;
                        }
                    }
                    mCurrent.setPatterns(patterns2);
                    patternsViewer.setInput(patterns2);
                    summaryViewer.update(mCurrent,null);
                }
            }
        });
        IStructuredSelection sel = (IStructuredSelection)patternsViewer.getSelection();
        FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
        int len = (Common.isEmptyArray(patterns)?0:patterns.length);
        patternsDel.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);

        final TableViewerUpDownMover<FilePattern[], FilePattern> patternsMover = new TableViewerUpDownMover<FilePattern[], FilePattern>() {
            @Override
            protected List<FilePattern> getAllElements()
            {
                if(mCurrent != null) {
                    return Common.makeList((FilePattern[])((TableViewer)getViewer()).getInput());
                }
                return Collections.<FilePattern>emptyList();
            }

            @Override
            protected void updateStructuredViewerInput(FilePattern[] input, List elements, List move, boolean isDown)
            {
                if(mCurrent != null) {
                    for (int i = 0; i < input.length; i++) {
                        input[i] = (FilePattern)elements.get(i);
                    }
                    patternsViewer.refresh();
                    summaryViewer.update(mCurrent,null);
                }
            }
        };
        patternsMover.setViewer(patternsViewer);

        final Button patternsUp = new Button(patternsButtons,SWT.PUSH);
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

        final Button patternsDown = new Button(patternsButtons, SWT.PUSH);
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
                int len = (patterns==null?0:patterns.length);
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
                mCurrent = null;
                if(!sel.isEmpty()) {
                    if(sel.size() == 1) {
                        mCurrent = (FileFilter)sel.getFirstElement();
                    }
                }
                boolean isNull = (mCurrent==null);
                descriptionText.setText((isNull?"":mCurrent.getDescription())); //$NON-NLS-1$
                descriptionText.setSelection(descriptionText.getText().length());
                patternsViewer.setInput((isNull?null:mCurrent.getPatterns()));
                descriptionLabel.setEnabled(!isNull);
                descriptionText.setEnabled(!isNull);
                patternsLabel.setEnabled(!isNull);
                patternsTable.setEnabled(!isNull);
                patternsAdd.setEnabled(!isNull);
                FilePattern[] patterns = (FilePattern[])patternsViewer.getInput();
                int len = (Common.isEmptyArray(patterns)?0:patterns.length);
                patternsDel.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);
                patternsUp.setEnabled(!isNull && patternsMover.canMoveUp());
                patternsDown.setEnabled(!isNull && patternsMover.canMoveDown());
            }
        });

        applyDialogFont(composite);
        ((GridData)composite.getLayoutData()).widthHint = convertWidthInCharsToPixels(80);
        summaryViewer.setInput(mFilter);
        return composite;
    }
}