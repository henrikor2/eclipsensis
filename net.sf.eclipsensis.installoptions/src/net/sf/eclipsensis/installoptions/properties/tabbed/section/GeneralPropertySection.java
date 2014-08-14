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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.installoptions.properties.descriptors.PropertyDescriptorHelper;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.*;

public class GeneralPropertySection extends InstallOptionsElementPropertySection
{
    @Override
    protected Control createSection(InstallOptionsElement element, Composite parent, TabbedPropertySheetPage page, InstallOptionsCommandHelper commandHelper)
    {
        if(element instanceof InstallOptionsWidget ) {
            InstallOptionsWidget widget = (InstallOptionsWidget)element;
            int cols = 2;
            IPropertySectionCreator customSectionCreator = widget.getPropertySectionCreator();
            if(customSectionCreator != null) {
                cols++;
            }

            Composite parent2 = createSectionComposite(parent);
            GridLayout layout = new GridLayout(cols,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent2.setLayout(layout);

            Control c = createGeneralSection(widget, parent2, commandHelper);
            GridData data = new GridData(SWT.FILL,SWT.FILL,false,true);
            c.setLayoutData(data);

            if(customSectionCreator != null) {
                Composite composite = getWidgetFactory().createComposite(parent2);
                layout = new GridLayout(1,false);
                layout.marginHeight = layout.marginWidth = 0;
                composite.setLayout(layout);
                composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
                c = customSectionCreator.createPropertySection(composite, getWidgetFactory(), commandHelper);
                if (c != null) {
                    c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
                }
            }
            if(widget.getTypeDef().getFlags().size() > 0 || widget.getFlags().size() > 0) {
                c = createFlagsSection(widget, parent2, commandHelper);
                data = new GridData(SWT.FILL,SWT.FILL,false,true);
                c.setLayoutData(data);
            }
            return parent2;
        }
        return null;
    }

    private Control createFlagsSection(final InstallOptionsWidget widget, Composite parent, final InstallOptionsCommandHelper commandHelper)
    {
        final String displayName = widget.getPropertyDescriptor(InstallOptionsModel.PROPERTY_FLAGS).getDisplayName();

        TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        Composite group = factory.createGroup(parent,displayName);
        GridLayout layout = new GridLayout(2,true);
        group.setLayout(layout);

        final boolean[] nonUserChange = {false};

        final Table table = factory.createTable(group,SWT.FLAT|SWT.CHECK|SWT.SINGLE|SWT.HIDE_SELECTION);
        GC gc = new GC(table);
        gc.setFont(JFaceResources.getDialogFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalSpan = 2;
        data.widthHint = fontMetrics.getAverageCharWidth()*30;
        data.heightHint = fontMetrics.getHeight()*10;
        table.setLayoutData(data);

        final CheckboxTableViewer viewer = new CheckboxTableViewer(table);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new LabelProvider());
        viewer.setComparer(new IElementComparer() {
            public boolean equals(Object a, Object b)
            {
                return Common.objectsAreEqual(a, b);
            }

            public int hashCode(Object element)
            {
                if(element != null) {
                    return element.hashCode();
                }
                return 0;
            }
        });
        table.addListener(SWT.EraseItem, new Listener() {
            public void handleEvent(Event event) {
                event.detail &= ~(SWT.SELECTED|SWT.FOCUSED);
            }
        });
        final List<String> flags = new ArrayList<String>(widget.getFlags());
        final Collection<String> availableFlags = widget.getTypeDef().getFlags();
        final IPropertyDescriptor descriptor = widget.getPropertyDescriptor(InstallOptionsModel.PROPERTY_FLAGS);
        final ICellEditorValidator validator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) descriptor);
        final Runnable runnable = new Runnable() {
            public void run()
            {
                nonUserChange[0]=true;
                try {
                    if(validator != null) {
                        String error = validator.isValid(flags);
                        if(error != null) {
                            Common.openError(table.getShell(), error, InstallOptionsPlugin.getShellImage());
                            viewer.setCheckedElements(widget.getFlags().toArray());
                            return;
                        }
                    }
                    commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_FLAGS,
                                    descriptor.getDisplayName(),
                                    widget, flags);
                }
                finally {
                    nonUserChange[0]=false;
                }
            }
        };
        viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event)
            {
                String flag = (String)event.getElement();
                boolean checked = event.getChecked();
                if(checked) {
                    flags.add(flag);
                }
                else {
                    flags.remove(flag);
                }
                runnable.run();
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if(!event.getSelection().isEmpty()) {
                    String flag = (String)((IStructuredSelection)event.getSelection()).getFirstElement();
                    viewer.setSelection(StructuredSelection.EMPTY);
                    boolean checked = viewer.getChecked(flag);
                    viewer.setChecked(flag,!checked);
                    if(!checked) {
                        flags.add(flag);
                    }
                    else {
                        flags.remove(flag);
                    }
                    runnable.run();
                }
            }
        });

        Button b = factory.createButton(group, InstallOptionsPlugin.getResourceString("select.all.label"), SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                flags.clear();
                flags.addAll(availableFlags);
                viewer.setCheckedElements(availableFlags.toArray());
                runnable.run();
            }
        });
        b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        b = factory.createButton(group, InstallOptionsPlugin.getResourceString("deselect.all.label"), SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                flags.clear();
                viewer.setCheckedElements(Common.EMPTY_STRING_ARRAY);
                runnable.run();
            }
        });
        b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        viewer.setInput(availableFlags==null?Collections.EMPTY_LIST:availableFlags);
        viewer.setCheckedElements(flags.toArray());
        final PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
                    if(!nonUserChange[0]) {
                        List<String> newFlags = widget.getFlags();
                        if (Common.isValid(viewer.getControl())) {
                            viewer.setCheckedElements(newFlags==null?Common.EMPTY_STRING_ARRAY:newFlags.toArray());
                            flags.clear();
                            flags.addAll(newFlags);
                        }
                    }
                }
            }
        };
        widget.addPropertyChangeListener(listener);
        group.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                widget.removePropertyChangeListener(listener);
            }
        });
        return group;
    }

    private Control createGeneralSection(final InstallOptionsWidget widget, Composite parent, final InstallOptionsCommandHelper commandHelper)
    {
        TabbedPropertySheetWidgetFactory factory = getWidgetFactory();

        Composite parent2 = factory.createGroup(parent, InstallOptionsPlugin.getResourceString("basic.section.label")); //$NON-NLS-1$
        parent2.setLayout(new GridLayout(1,false));

        final boolean[] nonUserChange = { false };
        Composite composite = factory.createComposite(parent2);
        composite.setLayout(new GridLayout(2,false));
        final String indexName = widget.getPropertyDescriptor(InstallOptionsModel.PROPERTY_INDEX).getDisplayName();
        CLabel l = factory.createCLabel(composite, indexName);
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

        final CCombo indexCombo = factory.createCCombo(composite, SWT.FLAT|SWT.READ_ONLY|SWT.DROP_DOWN|SWT.BORDER);
        indexCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        for (int i = 0; i < widget.getParent().getChildren().size(); i++) {
            indexCombo.add(Integer.toString(i+1));
        }
        int index = widget.getIndex();
        if(index >= 0 && index < indexCombo.getItemCount()) {
            indexCombo.select(index);
        }
        indexCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if(!nonUserChange[0]) {
                    commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_INDEX, indexName,
                                    widget, new Integer(indexCombo.getSelectionIndex()));
                }
            }
        });

        final String typeName = widget.getPropertyDescriptor(InstallOptionsModel.PROPERTY_TYPE).getDisplayName();
        l = factory.createCLabel(composite, typeName);
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        final CCombo typeCombo = factory.createCCombo(composite, SWT.FLAT|SWT.DROP_DOWN|SWT.BORDER);
        typeCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

        final Collection<InstallOptionsModelTypeDef> coll = InstallOptionsModel.INSTANCE.getControlTypeDefs();
        String type = widget.getType();
        int selected = -1;
        index = 0;
        for (Iterator<InstallOptionsModelTypeDef> iter = coll.iterator(); iter.hasNext();) {
            InstallOptionsModelTypeDef typeDef = iter.next();
            final String t = typeDef.getType();
            typeCombo.add(t);
            if(t.equalsIgnoreCase(type)) {
                selected = index;
            }
            index++;
        }
        if(selected >= 0) {
            typeCombo.select(selected);
        }
        else {
            typeCombo.setText(type);
        }

        final Runnable r = new Runnable() {
            public void run()
            {
                if(!nonUserChange[0]) {
                    String type = typeCombo.getText();
                    for (Iterator<InstallOptionsModelTypeDef> iter = coll.iterator(); iter.hasNext();) {
                        InstallOptionsModelTypeDef typeDef = iter.next();
                        final String t = typeDef.getType();
                        if(t.equalsIgnoreCase(type)) {
                            type = t;
                            break;
                        }
                    }
                    if(!Common.stringsAreEqual(type, widget.getType())) {
                        commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_TYPE, indexName,
                                        widget, type);
                    }
                }
            }
        };

        typeCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.character == '\t') {
                    r.run();
                }
            }
        });

        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                r.run();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event)
            {
                r.run();
            }
        });

        typeCombo.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e)
            {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                                || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        typeCombo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                r.run();
            }
        });

        final String positionName = widget.getPropertyDescriptor(InstallOptionsModel.PROPERTY_POSITION).getDisplayName();
        Group group = factory.createGroup(parent2, positionName);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setLayout(new GridLayout(2,false));
        final Position position = widget.getPosition().getCopy();
        final Text[] positionTexts = new Text[4];
        String[] names = {"left.property.name","top.property.name", //$NON-NLS-1$ //$NON-NLS-2$
                        "right.property.name","bottom.property.name"}; //$NON-NLS-1$ //$NON-NLS-2$
        int[] values = {position.left,position.top,position.right,position.bottom};
        VerifyListener verifyListener = new NumberVerifyListener(true);
        TraverseListener traverseListener = new TraverseListener() {
            public void keyTraversed(TraverseEvent e)
            {
                if(e.widget instanceof Text) {
                    Text text = (Text)e.widget;
                    if(Common.isEmpty(text.getText())) {
                        Common.openError(text.getShell(), InstallOptionsPlugin.getResourceString("empty.numeric.value.error"), InstallOptionsPlugin.getShellImage()); //$NON-NLS-1$
                        e.doit = false;
                    }
                }
            }
        };
        final String indexDataName = "INDEX"; //$NON-NLS-1$
        TextChangeHelper helper = new TextChangeHelper(new ICellEditorValidator() {
            public String isValid(Object value)
            {
                if(value instanceof String && Common.isEmpty((String)value)) {
                    return InstallOptionsPlugin.getResourceString("empty.numeric.value.error"); //$NON-NLS-1$
                }
                return null;
            }
        }) {
            @Override
            protected String getResetValue(Text text)
            {
                Integer index = (Integer)text.getData(indexDataName);
                if(index != null) {
                    String value;
                    switch(index.intValue()) {
                        case 0:
                            value = Integer.toString(position.left);
                            break;
                        case 1:
                            value = Integer.toString(position.top);
                            break;
                        case 2:
                            value = Integer.toString(position.right);
                            break;
                        default:
                            value = Integer.toString(position.bottom);
                    }
                    return value;
                }
                return null;
            }

            @Override
            protected void handleTextChange(Text text)
            {
                Integer index = (Integer)text.getData(indexDataName);
                if(index != null) {
                    int value = Integer.parseInt(text.getText());
                    int oldValue;
                    switch(index.intValue()) {
                        case 0:
                            oldValue = position.left;
                            position.left = value;
                            break;
                        case 1:
                            oldValue = position.top;
                            position.top = value;
                            break;
                        case 2:
                            oldValue = position.right;
                            position.right = value;
                            break;
                        default:
                            oldValue = position.bottom;
                            position.bottom = value;
                    }

                    if(oldValue != value) {
                        commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_POSITION,
                                        positionName, widget, position);
                    }
                }
            }
        };

        for (int i = 0; i < positionTexts.length; i++) {
            l = factory.createCLabel(group, InstallOptionsPlugin.getResourceString(names[i]));
            l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
            positionTexts[i] = factory.createText(group, Integer.toString(values[i]),SWT.FLAT|SWT.SINGLE);
            positionTexts[i].setEditable(!widget.isLocked());
            positionTexts[i].setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
            positionTexts[i].setData(indexDataName,new Integer(i));
            positionTexts[i].addVerifyListener(verifyListener);
            positionTexts[i].addTraverseListener(traverseListener);
            helper.connect(positionTexts[i]);
        }

        final String lockedName = widget.getPropertyDescriptor(InstallOptionsWidget.PROPERTY_LOCKED).getDisplayName();
        final Button lockedButton = factory.createButton(parent2, lockedName, SWT.FLAT|SWT.CHECK);
        lockedButton.setSelection(widget.isLocked());
        lockedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if(!nonUserChange[0]) {
                    commandHelper.propertyChanged(InstallOptionsWidget.PROPERTY_LOCKED,
                                    lockedName, widget, (lockedButton.getSelection()?Boolean.TRUE:Boolean.FALSE));
                }
            }
        });

        final PropertyChangeListener propertyListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                nonUserChange[0]=true;
                try {
                    if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_INDEX)) {
                        int index = ((Integer)evt.getNewValue()).intValue();
                        if(Common.isValid(indexCombo) && indexCombo.getSelectionIndex() != index) {
                            indexCombo.select(index);
                        }
                    }
                    else if(evt.getPropertyName().equals(InstallOptionsWidget.PROPERTY_LOCKED)) {
                        boolean locked = ((Boolean)evt.getNewValue()).booleanValue();
                        if(Common.isValid(lockedButton) && lockedButton.getSelection() != locked) {
                            lockedButton.setSelection(locked);
                        }
                        for (int i = 0; i < positionTexts.length; i++) {
                            if (Common.isValid(positionTexts[i])) {
                                positionTexts[i].setEditable(!locked);
                            }
                        }
                    }
                    else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_CHILDREN)) {
                        if(Common.isValid(indexCombo) && evt.getOldValue() == widget && evt.getNewValue() instanceof Integer) {
                            Integer index = (Integer)evt.getNewValue();
                            indexCombo.select(index.intValue());
                        }
                    }
                    else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_POSITION)) {
                        Position pos = (Position)evt.getNewValue();
                        if(!position.equals(pos)) {
                            if (Common.isValid(positionTexts[0])) {
                                positionTexts[0].setText(Integer.toString(pos.left));

                            }
                            if (Common.isValid(positionTexts[1])) {
                                positionTexts[1].setText(Integer.toString(pos.top));

                            }
                            if (Common.isValid(positionTexts[2])) {
                                positionTexts[2].setText(Integer.toString(pos.right));

                            }
                            if (Common.isValid(positionTexts[3])) {
                                positionTexts[3].setText(Integer.toString(pos.bottom));

                            }
                            position.set(pos);
                        }
                    }
                    else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_TYPE)) {
                        String type = (String)evt.getNewValue();
                        for (Iterator<InstallOptionsModelTypeDef> iter = coll.iterator(); iter.hasNext();) {
                            InstallOptionsModelTypeDef typeDef = iter.next();
                            final String t = typeDef.getType();
                            if(t.equalsIgnoreCase(type)) {
                                type = t;
                                break;
                            }
                        }
                        if (Common.isValid(typeCombo)) {
                            typeCombo.setText(type);
                        }
                    }
                }
                finally {
                    nonUserChange[0]=false;
                }
            }
        };
        widget.addPropertyChangeListener(propertyListener);
        widget.getParent().addPropertyChangeListener(propertyListener);
        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                InstallOptionsDialog dialog = widget.getParent();
                if(dialog != null) {
                    dialog.removePropertyChangeListener(propertyListener);
                }
                widget.removePropertyChangeListener(propertyListener);
            }
        });

        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        return parent2;
    }
}
