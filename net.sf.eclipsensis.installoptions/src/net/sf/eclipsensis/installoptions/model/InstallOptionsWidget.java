/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.PositionPropertySource;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomComboBoxPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.editors.CustomComboBoxCellEditor;
import net.sf.eclipsensis.installoptions.properties.labelproviders.ListLabelProvider;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.IPropertySectionCreator;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.w3c.dom.*;

public abstract class InstallOptionsWidget extends InstallOptionsElement
{
    /**
     *
     */
    private static final long serialVersionUID = -2010939141357319271L;
    public static final String PROPERTY_BOUNDS = "Bounds"; //$NON-NLS-1$
    public static final String PROPERTY_LOCKED = "Locked"; //$NON-NLS-1$

    public static final String NODE_NAME = "widget"; //$NON-NLS-1$
    public static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

    private static final Position cDefaultPosition = new Position(0,0,63,35);

    private static LabelProvider cPositionLabelProvider = new LabelProvider(){
        @Override
        public String getText(Object element)
        {
            if(element instanceof Position) {
                Position pos = (Position)element;
                return new StringBuffer("(").append(pos.left).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                        pos.top).append(",").append(pos.right).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                        pos.bottom).append(")").toString(); //$NON-NLS-1$
            }
            return super.getText(element);
        }
    };

    private static LabelProvider cFlagsLabelProvider = new ListLabelProvider();
    private static LabelProvider cLockedLabelProvider = new LabelProvider() {
        private final String YES = InstallOptionsPlugin.getResourceString("option.yes"); //$NON-NLS-1$
        private final String NO = InstallOptionsPlugin.getResourceString("option.no"); //$NON-NLS-1$

        @Override
        public String getText(Object element)
        {
            if(element instanceof Boolean) {
                return (((Boolean)element).booleanValue()?YES:NO);
            }
            return NO;
        }
    };
    private static final String MISSING_DISPLAY_NAME = InstallOptionsPlugin.getResourceString("missing.outline.display.name"); //$NON-NLS-1$

    private transient InstallOptionsModelTypeDef mTypeDef;
    protected transient InstallOptionsDialog mParent;
    protected int mIndex;
    protected Position mPosition;
    protected transient InstallOptionsGuide mVerticalGuide;
    protected transient InstallOptionsGuide mHorizontalGuide;
    private List<String> mFlags;
    protected boolean mLocked;
    private transient IPropertySectionCreator mPropertySectionCreator = null;
    private transient Collection<String> mPropertyNames;

    protected InstallOptionsWidget(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("defaultPosition"); //$NON-NLS-1$
        skippedProperties.add("displayLabelProvider"); //$NON-NLS-1$
        skippedProperties.add("displayName"); //$NON-NLS-1$
        skippedProperties.add("horizontalGuide"); //$NON-NLS-1$
        skippedProperties.add("parent"); //$NON-NLS-1$
        skippedProperties.add("propertySectionCreator"); //$NON-NLS-1$
        skippedProperties.add("typeDef"); //$NON-NLS-1$
        skippedProperties.add("verticalGuide"); //$NON-NLS-1$
    }

    public String getNodeName()
    {
        return NODE_NAME;
    }

    @Override
    protected void init()
    {
        super.init();
        mIndex = -1;
        mPosition = new Position();
    }

    public InstallOptionsModelTypeDef getTypeDef()
    {
        if(mTypeDef == null) {
            mTypeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(getType());
        }
        return mTypeDef;
    }

    @Override
    protected void setDefaults()
    {
        super.setDefaults();
        mPosition = getDefaultPosition();
    }

    /**
     *
     */
    @Override
    protected final Collection<String> getPropertyNames()
    {
        if(mPropertyNames == null) {
            mPropertyNames = new CaseInsensitiveSet();
            mPropertyNames.add(InstallOptionsModel.PROPERTY_INDEX);
            mPropertyNames.add(InstallOptionsModel.PROPERTY_POSITION);
            mPropertyNames.addAll(super.getPropertyNames());
            mPropertyNames.add(PROPERTY_LOCKED);
        }
        return mPropertyNames;
    }

    /**
     *
     */
    @Override
    protected final Collection<String> doGetPropertyNames()
    {
        List<String> list = new ArrayList<String>();
        list.add(InstallOptionsModel.PROPERTY_TYPE);
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(getType());
        if(typeDef != null) {
            Collection<String> settings = typeDef.getSettings();
            for (Iterator<String> iter=settings.iterator(); iter.hasNext(); ) {
                addPropertyName(list, iter.next());
            }
        }
        return list;
    }

    protected void addPropertyName(List<String> list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_LEFT)) {
            list.add(InstallOptionsModel.PROPERTY_LEFT);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_RIGHT)) {
            list.add(InstallOptionsModel.PROPERTY_RIGHT);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TOP)) {
            list.add(InstallOptionsModel.PROPERTY_TOP);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_BOTTOM)) {
            list.add(InstallOptionsModel.PROPERTY_BOTTOM);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_FLAGS)) {
            list.add(InstallOptionsModel.PROPERTY_FLAGS);
        }
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_TYPE)) {
            return new PropertyDescriptor(InstallOptionsModel.PROPERTY_TYPE, InstallOptionsPlugin.getResourceString("type.property.name")) { //$NON-NLS-1$
                @Override
                public CellEditor createPropertyEditor(Composite parent)
                {
                    Collection<InstallOptionsModelTypeDef> coll = InstallOptionsModel.INSTANCE.getControlTypeDefs();
                    List<String> types = new ArrayList<String>();
                    for (Iterator<InstallOptionsModelTypeDef> iter = coll.iterator(); iter.hasNext();) {
                        InstallOptionsModelTypeDef typeDef = iter.next();
                        types.add(typeDef.getType());
                    }
                    return new CustomComboBoxCellEditor(parent,types);
                }
            };
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_INDEX)) {
            return new IndexPropertyDescriptor();
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_POSITION)) {
            PropertyDescriptor positionPropertyDescriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_POSITION, InstallOptionsPlugin.getResourceString("position.property.name")); //$NON-NLS-1$
            positionPropertyDescriptor.setLabelProvider(cPositionLabelProvider);
            return positionPropertyDescriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_FLAGS)) {
            return new FlagsPropertyDescriptor();
        }
        else if(name.equals(PROPERTY_LOCKED)) {
            String propertyName = InstallOptionsPlugin.getResourceString("locked.property.name"); //$NON-NLS-1$
            CustomComboBoxPropertyDescriptor descriptor = new CustomComboBoxPropertyDescriptor(PROPERTY_LOCKED,
                    propertyName, new Object[] {Boolean.TRUE,Boolean.FALSE},
                    new String[] {cLockedLabelProvider.getText(Boolean.TRUE),
                    cLockedLabelProvider.getText(Boolean.FALSE)}, 1);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
            descriptor.setLabelProvider(cLockedLabelProvider);
            return descriptor;
        }
        else {
            return null;
        }
    }

    public InstallOptionsGuide getHorizontalGuide()
    {
        return mHorizontalGuide;
    }

    public InstallOptionsGuide getVerticalGuide()
    {
        return mVerticalGuide;
    }

    public void setHorizontalGuide(InstallOptionsGuide hGuide)
    {
        mHorizontalGuide = hGuide;
    }

    public void setVerticalGuide(InstallOptionsGuide vGuide)
    {
        mVerticalGuide = vGuide;
    }

    @Override
    public Object getPropertyValue(Object id)
    {
        if (PROPERTY_BOUNDS.equals(id)) {
            return toGraphical(getPosition()).getBounds();
        }
        else if (PROPERTY_LOCKED.equals(id)) {
            return Boolean.valueOf(isLocked());
        }
        else if (InstallOptionsModel.PROPERTY_LEFT.equals(id)) {
            return new Integer(getPosition().left);
        }
        else if (InstallOptionsModel.PROPERTY_TOP.equals(id)) {
            return new Integer(getPosition().top);
        }
        else if (InstallOptionsModel.PROPERTY_RIGHT.equals(id)) {
            return new Integer(getPosition().right);
        }
        else if (InstallOptionsModel.PROPERTY_BOTTOM.equals(id)) {
            return new Integer(getPosition().bottom);
        }
        else if (InstallOptionsModel.PROPERTY_POSITION.equals(id)) {
            return new PositionPropertySource(this);
        }
        else if (InstallOptionsModel.PROPERTY_INDEX.equals(id)) {
            return new Integer(getIndex());
        }
        else if (InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
            return getFlags();
        }
        else {
            return super.getPropertyValue(id);
        }
    }

    @Override
    protected TypeConverter<?> loadTypeConverter(String property, Object value)
    {
        if (InstallOptionsModel.PROPERTY_LEFT.equals(property) ||
            InstallOptionsModel.PROPERTY_TOP.equals(property) ||
            InstallOptionsModel.PROPERTY_RIGHT.equals(property) ||
            InstallOptionsModel.PROPERTY_BOTTOM.equals(property)) {
            if(value instanceof String) {
                if(((String)value).regionMatches(true,0,"0x",0,2)) { //$NON-NLS-1$
                    return TypeConverter.HEX_CONVERTER;
                }
            }
            return TypeConverter.INTEGER_CONVERTER;
        }
        else if(InstallOptionsModel.PROPERTY_FLAGS.equals(property)) {
            return TypeConverter.STRING_LIST_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object id, Object value)
    {
        if(InstallOptionsModel.PROPERTY_POSITION.equals(id)) {
            setPosition((Position)value);
        }
        else if (InstallOptionsModel.PROPERTY_LEFT.equals(id)) {
            getPosition().left = ((Integer)value).intValue();
        }
        else if (InstallOptionsModel.PROPERTY_TOP.equals(id)) {
            getPosition().top = ((Integer)value).intValue();
        }
        else if (InstallOptionsModel.PROPERTY_RIGHT.equals(id)) {
            getPosition().right = ((Integer)value).intValue();
        }
        else if (InstallOptionsModel.PROPERTY_BOTTOM.equals(id)) {
            getPosition().bottom = ((Integer)value).intValue();
        }
        else if (PROPERTY_LOCKED.equals(id)) {
            setLocked(((Boolean)value).booleanValue());
        }
        else if(InstallOptionsModel.PROPERTY_INDEX.equals(id)) {
            if(mIndex != ((Integer)value).intValue()) {
                firePropertyChange(InstallOptionsModel.PROPERTY_INDEX,new Integer(mIndex), value);
                setDirty(true);
            }
        }
        else if(InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
            setFlags((List<String>)value);
        }
        else {
            super.setPropertyValue(id,value);
        }
    }

    public InstallOptionsDialog getParent()
    {
        return mParent;
    }

    void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
        setDirty(true);
    }

    public int getIndex()
    {
        return mIndex;
    }

    void setIndex(int index)
    {
        mIndex = index;
        setDirty(true);
    }

    @Override
    public final String toString()
    {
        String displayName = getDisplayName();
        return InstallOptionsPlugin.getFormattedString("design.outline.display.name.format", //$NON-NLS-1$
                new Object[]{new Integer(getIndex()+1), getType(),
                            (Common.isEmpty(displayName)?MISSING_DISPLAY_NAME:displayName)});
    }

    public List<String> getFlags()
    {
        if(mFlags == null) {
            mFlags = new ArrayList<String>();
        }
        return mFlags;
    }

    private List<String> retainSupportedFlags(List<String> flags)
    {
        List<String> flags2 = new ArrayList<String>(flags);
        Collection<String> supportedFlags = getTypeDef().getFlags();
        for (Iterator<String> iter = flags2.iterator(); iter.hasNext();) {
            String flag = iter.next();
            if(!supportedFlags.contains(flag)) {
                iter.remove();
            }
        }
        return flags2;
    }

    public boolean hasFlag(String flag)
    {
        if(flag != null && mFlags != null) {
            for (Iterator<String> iter = mFlags.iterator(); iter.hasNext();) {
                if(Common.stringsAreEqual(iter.next(), flag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setFlags(List<String> flags)
    {
        List<String> oldFlags = retainSupportedFlags(getFlags());
        List<String> newFlags = retainSupportedFlags(flags);
        boolean dirty = false;
        if (!getFlags().equals(flags)) {
            dirty = true;
            mFlags = new ArrayList<String>(flags);
        }
        if(!oldFlags.equals(newFlags)) {
            firePropertyChange(InstallOptionsModel.PROPERTY_FLAGS,oldFlags,newFlags);
        }
        if(dirty) {
            setDirty(true);
        }
    }

    private int toGraphical(int value, int refValue)
    {
        if(value < 0) {
            return Math.max(value,refValue+value);
        }
        return value;
    }

    public Position toGraphical(Position p)
    {
        return toGraphical(p, true);
    }

    public Position toGraphical(Position p, boolean toPixels)
    {
        InstallOptionsDialog dialog = getParent();
        return toGraphical(p, (dialog==null?null:dialog.getDialogSize().getSize()), toPixels);
    }

    public Position toGraphical(Position p, Dimension size)
    {
        return toGraphical(p, size, true);
    }

    public Position toGraphical(Position p, Dimension size, boolean toPixels)
    {
        return toGraphical(p, size, toPixels, (toPixels?FontUtility.getInstallOptionsFont():null));
    }

    public Position toGraphical(Position p, Font font)
    {
        InstallOptionsDialog dialog = getParent();
        return toGraphical(p, (dialog==null?null:dialog.getDialogSize().getSize()), font);
    }

    public Position toGraphical(Position p, Dimension size, Font font)
    {
        return toGraphical(p, size, true, font);
    }

    private Position toGraphical(Position p, Dimension size, boolean toPixels, Font font)
    {
        Position p2 = p.getCopy();
        if(size == null) {
            p2.set(0,0,0,0);
        }
        else {
            p2.left = toGraphical(p2.left,size.width);
            p2.top = toGraphical(p2.top,size.height);
            p2.right = toGraphical(p2.right,size.width);
            p2.bottom = toGraphical(p2.bottom,size.height);
        }
        if(toPixels) {
            p2 = FigureUtility.dialogUnitsToPixels(p2,font);
        }
        return p2;
    }

    private int toModel(int value, int localValue, int refValue)
    {
        if(localValue < 0 && value < refValue) {
            return Math.max(0,value) - refValue;
        }
        return value;
    }

    public Position toModel(Position p)
    {
        return toModel(p, true);
    }

    public Position toModel(Position p, boolean fromPixels)
    {
        InstallOptionsDialog dialog = getParent();
        return toModel(p, (dialog==null?null:dialog.getDialogSize().getSize()), fromPixels);
    }

    public Position toModel(Position p, Font font)
    {
        InstallOptionsDialog dialog = getParent();
        return toModel(p, (dialog==null?null:dialog.getDialogSize().getSize()), font);
    }

    public Position toModel(Position p, Dimension size)
    {
        return toModel(p, size, true);
    }

    public Position toModel(Position p, Dimension size, Font font)
    {
        return toModel(p, size, true, font);
    }

    public Position toModel(Position p, Dimension size, boolean fromPixels)
    {
        return toModel(p, size, fromPixels, (fromPixels?FontUtility.getInstallOptionsFont():null));
    }

    private Position toModel(Position p, Dimension size, boolean fromPixels, Font font)
    {
        Position p2 = p;
        if(fromPixels) {
            p2 = FigureUtility.pixelsToDialogUnits(p2,font);
        }
        if(size == null) {
            p2.set(0,0,0,0);
        }
        else {
            p2.left = toModel(p2.left, mPosition.left, size.width);
            p2.top = toModel(p2.top, mPosition.top, size.height);
            p2.right = toModel(p2.right, mPosition.right, size.width);
            p2.bottom = toModel(p2.bottom, mPosition.bottom, size.height);
        }
        return p2;
    }

    public Position getPosition()
    {
        return mPosition;
    }

    public void setPosition(Position position)
    {
        Position mOldPosition = mPosition;
        mPosition = position.getCopy();
        if(!mPosition.equals(mOldPosition)) {
            firePropertyChange(InstallOptionsModel.PROPERTY_POSITION,mOldPosition,mPosition);
            setDirty(true);
        }
    }

    public boolean isLocked()
    {
        return mLocked;
    }

    public void setLocked(boolean locked)
    {
        boolean oldLocked = mLocked;
        mLocked = locked;
        if(mLocked != oldLocked) {
            firePropertyChange(PROPERTY_LOCKED,Boolean.valueOf(oldLocked),Boolean.valueOf(mLocked));
            setDirty(true);
        }
    }

    @Override
    public Object clone()
    {
        InstallOptionsWidget element = (InstallOptionsWidget)super.clone();
        element.setPropertySectionCreator(null);
        element.setParent(null);
        element.setHorizontalGuide(null);
        element.setVerticalGuide(null);
        element.setPosition(mPosition.getCopy());
        element.setFlags(new ArrayList<String>(getFlags()));
        element.setIndex(-1);
        element.mPropertyNames = null;
        return element;
    }

    protected Position getDefaultPosition()
    {
        return cDefaultPosition.getCopy();
    }

    protected final String getDisplayName()
    {
        String displayName = (String)getPropertyValue(getTypeDef().getDisplayProperty());
        ILabelProvider labelProvider = getDisplayLabelProvider();
        if(labelProvider != null) {
            displayName = labelProvider.getText(displayName);
        }
        return displayName;
    }

    @Override
    protected String getSectionName()
    {
        return InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[]{new Integer(getIndex()+1)});
    }

    @Override
    public final Image getIconImage()
    {
        return InstallOptionsPlugin.getImageManager().getImage(getTypeDef().getSmallIcon());
    }

    protected ILabelProvider getDisplayLabelProvider()
    {
        return null;
    }

    public void setPropertySectionCreator(IPropertySectionCreator propertySectionCreator)
    {
        mPropertySectionCreator = propertySectionCreator;
    }

    public final IPropertySectionCreator getPropertySectionCreator()
    {
        if(mPropertySectionCreator == null) {
            mPropertySectionCreator = createPropertySectionCreator();
        }
        return mPropertySectionCreator;
    }

    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return null;
    }

    @Override
    public void fromNode(Node node)
    {
        String nodeType = node.getAttributes().getNamedItem(TYPE_ATTRIBUTE).getNodeValue();
        if(nodeType.equals(getType())) {
            super.fromNode(node);
        }
    }

    @Override
    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        XMLUtil.addAttribute(document,node,TYPE_ATTRIBUTE,getType());
        return node;
    }

    @Override
    protected String convertToString(String name, Object obj)
    {
        if(obj instanceof Position) {
            return TypeConverter.POSITION_CONVERTER.asString((Position) obj);
        }
        else {
            return super.convertToString(name, obj);
        }
    }

    @Override
    protected Object convertFromString(String string, Class<?> clasz)
    {
        if(clasz.equals(Position.class)) {
            return TypeConverter.POSITION_CONVERTER.asType(string);
        }
        else {
            return super.convertFromString(string, clasz);
        }
    }

    @Override
    protected boolean isConvertibleAttributeType(Class<?> clasz)
    {
        if(Position.class.equals(clasz))
        {
            return true;
        }
        return super.isConvertibleAttributeType(clasz);
    }

    private class IndexPropertyDescriptor extends ComboBoxPropertyDescriptor
    {
        public IndexPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_INDEX, InstallOptionsPlugin.getResourceString("index.property.name"), new String[0]); //$NON-NLS-1$
            setLabelProvider(new LabelProvider(){
                @Override
                public String getText(Object element)
                {
                    return Integer.toString(((Integer)element).intValue()+1);
                }
            });
        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            CellEditor editor = null;
            InstallOptionsDialog dialog = getParent();
            if(dialog != null) {
                String[] values = new String[dialog.getChildren().size()];
                for (int i = 0; i < values.length; i++) {
                    values[i]=Integer.toString(i+1);
                }
                editor = new ComboBoxCellEditor(parent, values, SWT.READ_ONLY);
            }
            return editor;
        }
    }

    private class FlagsPropertyDescriptor extends PropertyDescriptor
    {
        public FlagsPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_FLAGS, InstallOptionsPlugin.getResourceString("flags.property.name")); //$NON-NLS-1$
            setLabelProvider(cFlagsLabelProvider);
            setValidator(new NSISStringLengthValidator(getDisplayName()));
        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            final FlagsCellEditor cellEditor = new FlagsCellEditor(parent);
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                cellEditor.setValidator(validator);
            }
            return cellEditor;
        }
    }

    private final class FlagsCellEditor extends DialogCellEditor implements PropertyChangeListener
    {
        private FlagsCellEditor(Composite parent)
        {
            super(parent);
            InstallOptionsWidget.this.addPropertyChangeListener(this);
        }

        @Override
        protected void updateContents(Object value)
        {
            Label label = getDefaultLabel();
            if (label != null) {
                label.setText(cFlagsLabelProvider.getText(value));
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object openDialogBox(Control cellEditorWindow)
        {
            FlagsDialog dialog = new FlagsDialog(cellEditorWindow.getShell(), (List<String>)getValue(), getType());
            dialog.setValidator(getValidator());
            dialog.open();
            return dialog.getValues();
        }

        @Override
        public void dispose()
        {
            InstallOptionsWidget.this.removePropertyChangeListener(this);
            super.dispose();
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
                setValue(InstallOptionsWidget.this.getFlags());
            }
        }
    }

    private class FlagsDialog extends Dialog
    {
        private ICellEditorValidator mValidator;
        private List<String> mValues;
        private Table mTable;
        private String mType;
        private Collection<String> mAvailableFlags;

        public FlagsDialog(Shell parent, List<String> values, String type)
        {
            super(parent);
            mValues = new ArrayList<String>(values);
            mType = type;
            InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(getType());
            mAvailableFlags = (typeDef==null?Collections.<String>emptySet():typeDef.getFlags());
        }

        public ICellEditorValidator getValidator()
        {
            return mValidator;
        }

        public void setValidator(ICellEditorValidator validator)
        {
            mValidator = validator;
        }

        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getFormattedString("flags.dialog.name", new String[]{mType})); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getShellImage());
        }

        public List<String> getValues()
        {
            return mValues;
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);
            Composite composite2 = new Composite(composite,SWT.NONE);
            composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite2.setLayout(layout);

            mTable = new Table(composite2,SWT.CHECK| SWT.BORDER | SWT.SINGLE | SWT.HIDE_SELECTION | SWT.V_SCROLL);
            mTable.addListener(SWT.EraseItem, new Listener() {
                public void handleEvent(Event event) {
                    event.detail &= ~(SWT.SELECTED|SWT.FOCUSED);
                }
            });
            mTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    int selectedIndex = mTable.getSelectionIndex();
                    if(selectedIndex != -1) {
                        mTable.deselect(selectedIndex);
                        TableItem item = mTable.getItem(selectedIndex);
                        item.setChecked(!item.getChecked());
                    }
               }
            });
            initializeDialogUnits(mTable);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = convertWidthInCharsToPixels(40);
            data.heightHint = convertHeightInCharsToPixels(10);
            mTable.setLayoutData(data);
            for (Iterator<String> iter=mAvailableFlags.iterator(); iter.hasNext(); ) {
                TableItem ti = new TableItem(mTable,SWT.NONE);
                String flag = iter.next();
                ti.setText(flag);
                if(mValues != null) {
                    ti.setChecked(mValues.contains(flag));
                }
            }
            composite2 = new Composite(composite2,SWT.NONE);
            composite2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            layout = new GridLayout(1,false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            composite2.setLayout(layout);
            Button b = new Button(composite2,SWT.PUSH);
            b.setText(InstallOptionsPlugin.getResourceString("select.all.label")); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    selectAll(true);
                }
            });
            b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            b = new Button(composite2,SWT.PUSH);
            b.setText(InstallOptionsPlugin.getResourceString("deselect.all.label")); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    selectAll(false);
                }
            });
            b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            return composite;
        }

        private void selectAll(boolean state)
        {
            TableItem[] items = mTable.getItems();
            for (int i = 0; i < items.length; i++) {
                items[i].setChecked(state);
            }
        }

        @Override
        protected void okPressed()
        {
            if(mTable != null) {
                mValues.removeAll(mAvailableFlags);
                TableItem[] items = mTable.getItems();
                int counter=0;
                for (int i = 0; i < items.length; i++) {
                    if(items[i].getChecked()) {
                        mValues.add(counter++,items[i].getText());
                    }
                }
            }
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                String error = validator.isValid(mValues);
                if(!Common.isEmpty(error)) {
                    Common.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error, //$NON-NLS-1$
                                     InstallOptionsPlugin.getShellImage());
                    return;
                }
            }
            super.okPressed();
        }
    }
}
