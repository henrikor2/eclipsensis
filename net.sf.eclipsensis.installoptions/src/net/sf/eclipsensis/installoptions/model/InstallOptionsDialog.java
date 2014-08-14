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
import java.util.regex.Matcher;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomComboBoxPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.validators.*;
import net.sf.eclipsensis.installoptions.rulers.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsDialog extends InstallOptionsElement implements IInstallOptionsConstants
{
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_SELECTION = "net.sf.eclipsensis.installoptions.selection"; //$NON-NLS-1$

    private static final String GUIDES_PREFIX = "guides="; //$NON-NLS-1$
    private static final String LOCKED_PREFIX = "locked="; //$NON-NLS-1$

    public static final String NODE_NAME = "dialog"; //$NON-NLS-1$

    public static final int DEFAULT_OPTION = 0;
    public static final Integer[] OPTION_DATA = {InstallOptionsModel.OPTION_DEFAULT,
                                                 InstallOptionsModel.OPTION_NO,
                                                 InstallOptionsModel.OPTION_YES};
    public static final String[] OPTION_DISPLAY = {InstallOptionsPlugin.getResourceString("option.default"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("option.no"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("option.yes")}; //$NON-NLS-1$
    public static final Image INSTALLOPTIONS_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.dialog.icon")); //$NON-NLS-1$
    private static LabelProvider cDefaultLabelProvider = new LabelProvider(){
        @Override
        public String getText(Object element)
        {
            if(element instanceof String) {
                if(Common.isEmpty((String)element)) {
                    return InstallOptionsPlugin.getResourceString("value.default"); //$NON-NLS-1$
                }
                else {
                    return (String)element;
                }
            }
            return super.getText(element);
        }
    };

    protected List<InstallOptionsWidget> mChildren = new ArrayList<InstallOptionsWidget>();
    protected transient InstallOptionsRuler mVerticalRuler;
    protected transient InstallOptionsRuler mHorizontalRuler;

    private String mTitle;
    private Integer mCancelEnabled;
    private Integer mCancelShow;
    private Integer mBackEnabled;
    private String mCancelButtonText;
    private String mNextButtonText;
    private String mBackButtonText;
    private Integer mRect;
    private Integer mRTL;
    private transient int[] mSelectedIndices;
    private transient UpDownMover<InstallOptionsWidget> mUpDownMover;
    private transient INIFile mINIFile;
    private transient Map<INISection, InstallOptionsElement> mINISectionMap;
    private transient DialogSize mDialogSize;
    private transient boolean mShowDialogSize;

    public InstallOptionsDialog(INISection section)
    {
        super(section);
        if(section != null) {
            mINISectionMap.put(section,this);
        }
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("dialogSize"); //$NON-NLS-1$
        skippedProperties.add("showDialogSize"); //$NON-NLS-1$
        skippedProperties.add("selection"); //$NON-NLS-1$
    }

    public String getNodeName()
    {
        return NODE_NAME;
    }

    @Override
    protected void init()
    {
        super.init();
        mChildren = new ArrayList<InstallOptionsWidget>();
        mTitle=""; //$NON-NLS-1$
        mCancelEnabled=null;
        mCancelShow=null;
        mBackEnabled=null;
        mCancelButtonText=""; //$NON-NLS-1$
        mNextButtonText=""; //$NON-NLS-1$
        mBackButtonText=""; //$NON-NLS-1$
        mRect=null;
        mRTL=null;
        mSelectedIndices = null;
        mINIFile = null;
        mINISectionMap = new HashMap<INISection, InstallOptionsElement>();
        mDialogSize = IInstallOptionsConstants.DEFAULT_DIALOG_SIZE;
        mShowDialogSize = true;
        reset();
    }

    protected void reset()
    {
        final PropertyChangeListener guideListener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt)
            {
                setDirty(true);
            }
        };

        final PropertyChangeListener rulerListener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt)
            {
                setDirty(true);
                InstallOptionsRuler ruler = (InstallOptionsRuler)evt.getSource();
                if(evt.getPropertyName() == InstallOptionsRuler.PROPERTY_CHILDREN) {
                    InstallOptionsGuide guide = (InstallOptionsGuide)evt.getNewValue();
                    if(ruler.getGuides().contains(guide)) {
                        guide.addPropertyChangeListener(guideListener);
                    }
                    else {
                        guide.removePropertyChangeListener(guideListener);
                    }
                }
            }
        };

        mVerticalRuler = new InstallOptionsRuler(false, InstallOptionsRulerProvider.UNIT_DLU);
        mVerticalRuler.addPropertyChangeListener(rulerListener);
        mHorizontalRuler = new InstallOptionsRuler(true, InstallOptionsRulerProvider.UNIT_DLU);
        mHorizontalRuler.addPropertyChangeListener(rulerListener);
        mUpDownMover = new UpDownMover<InstallOptionsWidget>() {
            @Override
            protected int[] getSelectedIndices()
            {
                return mSelectedIndices;
            }

            @Override
            protected List<InstallOptionsWidget> getAllElements()
            {
                return mChildren;
            }

            @Override
            protected void updateElements(List<InstallOptionsWidget> elements, List<InstallOptionsWidget> move, boolean isDown)
            {
                setChildren(elements);
            }
        };
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_DIALOG;
    }

    public DialogSize getDialogSize()
    {
        return mDialogSize;
    }

    public void setDialogSize(DialogSize dialogSize)
    {
        mDialogSize = dialogSize;
    }

    public boolean isShowDialogSize()
    {
        return mShowDialogSize;
    }

    public void setShowDialogSize(boolean showDialogSize)
    {
        mShowDialogSize = showDialogSize;
    }

    public boolean canMove(int type, List<InstallOptionsWidget> selection)
    {
        boolean flag = false;
        int[] oldSelection = mSelectedIndices;
        try {
            mSelectedIndices = parseSelection(selection);
            switch(type) {
                case IInstallOptionsConstants.ARRANGE_SEND_BACKWARD:
                    flag = canSendBackward();
                    break;
                case IInstallOptionsConstants.ARRANGE_SEND_TO_BACK:
                    flag = canSendToBack();
                    break;
                case IInstallOptionsConstants.ARRANGE_BRING_FORWARD:
                    flag = canBringForward();
                    break;
                case IInstallOptionsConstants.ARRANGE_BRING_TO_FRONT:
                default:
                    return canBringToFront();
            }
        }
        finally {
            mSelectedIndices = oldSelection;
        }

        return flag;
    }

    public void move(int type)
    {
        switch(type) {
            case IInstallOptionsConstants.ARRANGE_SEND_BACKWARD:
                sendBackward();
                break;
            case IInstallOptionsConstants.ARRANGE_SEND_TO_BACK:
                sendToBack();
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_FORWARD:
                bringForward();
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_TO_FRONT:
            default:
                bringToFront();
        }
    }

    public void setChildren(List<InstallOptionsWidget> children)
    {
        mChildren.clear();
        mChildren.addAll(children);
        updateChildIndices(0);
        mListeners.firePropertyChange(InstallOptionsModel.PROPERTY_CHILDREN, null, null);
        setDirty(true);
    }

    public boolean canSendBackward()
    {
        return mUpDownMover.canMoveDown();
    }

    public boolean canSendToBack()
    {
        return mUpDownMover.canMoveDown();
    }

    public boolean canBringForward()
    {
        return mUpDownMover.canMoveUp();
    }

    public boolean canBringToFront()
    {
        return mUpDownMover.canMoveUp();
    }

    public void sendBackward()
    {
        mUpDownMover.moveDown();
    }

    public void bringForward()
    {
        mUpDownMover.moveUp();
    }

    public void sendToBack()
    {
        mUpDownMover.moveToBottom();
    }

    public void bringToFront()
    {
        mUpDownMover.moveToTop();
    }

    public void setSelection(List<InstallOptionsWidget> selection)
    {
        mSelectedIndices = parseSelection(selection);
        List<InstallOptionsWidget> list = new ArrayList<InstallOptionsWidget>();
        if(!Common.isEmptyArray(mSelectedIndices)) {
            for (int i = 0; i < mSelectedIndices.length; i++) {
                list.add(mChildren.get(mSelectedIndices[i]));
            }
        }
        firePropertyChange(PROPERTY_SELECTION,null,list);
    }

    @SuppressWarnings("null")
    private int[] parseSelection(List<InstallOptionsWidget> list)
    {
        if (list != null) {
            list.retainAll(mChildren);
        }
        if(Common.isEmptyCollection(list)) {
            return new int[0];
        }
        int[] selectedIndices = new int[list.size()];
        for (int i = 0; i < selectedIndices.length; i++) {
            selectedIndices[i] = mChildren.indexOf(list.get(i));
        }
        Arrays.sort(selectedIndices);
        return selectedIndices;
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        PropertyDescriptor descriptor = null;
        if(name.equals(InstallOptionsModel.PROPERTY_TITLE)) {
            String propertyName = InstallOptionsPlugin.getResourceString("title.property.name"); //$NON-NLS-1$
            descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_TITLE, propertyName);
            descriptor.setLabelProvider(cDefaultLabelProvider);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_CANCEL_ENABLED)) {
            descriptor = new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_ENABLED, InstallOptionsPlugin.getResourceString("cancel.enabled.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION); //$NON-NLS-1$
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_CANCEL_SHOW)) {
            descriptor = new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_SHOW, InstallOptionsPlugin.getResourceString("cancel.show.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION); //$NON-NLS-1$
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_BACK_ENABLED)) {
            descriptor = new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_BACK_ENABLED, InstallOptionsPlugin.getResourceString("back.enabled.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION); //$NON-NLS-1$
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("cancel.button.text.property.name"); //$NON-NLS-1$
            descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT, propertyName);
            descriptor.setLabelProvider(cDefaultLabelProvider);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("back.button.text.property.name"); //$NON-NLS-1$
            descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT, propertyName);
            descriptor.setLabelProvider(cDefaultLabelProvider);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("next.button.text.property.name"); //$NON-NLS-1$
            descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT, propertyName);
            descriptor.setLabelProvider(cDefaultLabelProvider);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_RECT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("rect.property.name"); //$NON-NLS-1$
            descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_RECT, propertyName);
            descriptor.setValidator(new NumberCellEditorValidator(propertyName, 1,Integer.MAX_VALUE,true));
            descriptor.setLabelProvider(cDefaultLabelProvider);
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_RTL)) {
            descriptor = new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_RTL, InstallOptionsPlugin.getResourceString("rtl.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION); //$NON-NLS-1$
        }
        return descriptor;
    }

    @Override
    public void setPropertyValue(Object id, Object value)
    {
        if(InstallOptionsModel.PROPERTY_TITLE.equals(id)) {
            setTitle((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_ENABLED.equals(id)) {
            setCancelEnabled((Integer)value);
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_SHOW.equals(id)) {
            setCancelShow((Integer)value);
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT.equals(id)) {
            setCancelButtonText((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_BACK_ENABLED.equals(id)) {
            setBackEnabled((Integer)value);
        }
        else if(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT.equals(id)) {
            setBackButtonText((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT.equals(id)) {
            setNextButtonText((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_RECT.equals(id)) {
            setRect((Integer)value);
        }
        else if(InstallOptionsModel.PROPERTY_RTL.equals(id)) {
            setRTL((Integer)value);
        }
    }

    public String getBackButtonText()
    {
        return mBackButtonText;
    }

    public void setBackButtonText(String backButtonText)
    {
        if(!Common.stringsAreEqual(mBackButtonText,backButtonText)) {
            mBackButtonText = backButtonText;
            setDirty(true);
        }
    }

    public Integer getBackEnabled()
    {
        return mBackEnabled;
    }

    public void setBackEnabled(Integer backEnabled)
    {
        if(!Common.objectsAreEqual(mBackEnabled,backEnabled)) {
            mBackEnabled = backEnabled;
            setDirty(true);
        }
    }

    public String getCancelButtonText()
    {
        return mCancelButtonText;
    }

    public void setCancelButtonText(String cancelButtonText)
    {
        if(!Common.stringsAreEqual(mCancelButtonText,cancelButtonText)) {
            mCancelButtonText = cancelButtonText;
            setDirty(true);
        }
    }

    public Integer getCancelEnabled()
    {
        return mCancelEnabled;
    }

    public void setCancelEnabled(Integer cancelEnabled)
    {
        if(!Common.objectsAreEqual(mCancelEnabled,cancelEnabled)) {
            mCancelEnabled = cancelEnabled;
            setDirty(true);
        }
    }

    public Integer getCancelShow()
    {
        return mCancelShow;
    }

    public void setCancelShow(Integer cancelShow)
    {
        if(!Common.objectsAreEqual(mCancelShow,cancelShow)) {
            mCancelShow = cancelShow;
            setDirty(true);
        }
    }

    public String getNextButtonText()
    {
        return mNextButtonText;
    }

    public void setNextButtonText(String nextButtonText)
    {
        if(!Common.stringsAreEqual(mNextButtonText,nextButtonText)) {
            mNextButtonText = nextButtonText;
            setDirty(true);
        }
    }

    public Integer getRect()
    {
        return mRect;
    }

    public void setRect(Integer rect)
    {
        if(!Common.objectsAreEqual(mRect,rect)) {
            mRect = rect;
            setDirty(true);
        }
    }

    public Integer getRTL()
    {
        return mRTL;
    }

    public void setRTL(Integer rtl)
    {
        if(!Common.objectsAreEqual(mRTL,rtl)) {
            Integer oldRTL = mRTL;
            mRTL = rtl;
            setDirty(true);
            firePropertyChange(InstallOptionsModel.PROPERTY_RTL,oldRTL,mRTL);
        }
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String title)
    {
        if(!Common.stringsAreEqual(mTitle,title)) {
            mTitle = title;
            setDirty(true);
        }
    }

    @Override
    public Object getPropertyValue(Object id)
    {
        if (InstallOptionsModel.PROPERTY_NUMFIELDS.equals(id)) {
            return new Integer(Common.isEmptyCollection(mChildren)?0:mChildren.size());
        }
        else if(InstallOptionsModel.PROPERTY_TITLE.equals(id)) {
            return getTitle();
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_ENABLED.equals(id)) {
            return getCancelEnabled();
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_SHOW.equals(id)) {
            return getCancelShow();
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT.equals(id)) {
            return getCancelButtonText();
        }
        else if(InstallOptionsModel.PROPERTY_BACK_ENABLED.equals(id)) {
            return getBackEnabled();
        }
        else if(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT.equals(id)) {
            return getBackButtonText();
        }
        else if(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT.equals(id)) {
            return getNextButtonText();
        }
        else if(InstallOptionsModel.PROPERTY_RECT.equals(id)) {
            return getRect();
        }
        else if(InstallOptionsModel.PROPERTY_RTL.equals(id)) {
            return getRTL();
        }
        return super.getPropertyValue(id);
    }

    protected void fireChildReplaced(String prop, Object oldChild, Object newChild)
    {
        mListeners.firePropertyChange(prop, oldChild, newChild);
        setDirty(true);
    }

    protected void fireChildAdded(String prop, Object child, Object index)
    {
        mListeners.firePropertyChange(prop, index, child);
        setDirty(true);
    }

    protected void fireChildMoved(String prop, Object child, Object index)
    {
        mListeners.firePropertyChange(prop, child, index);
        setDirty(true);
    }

    protected void fireChildRemoved(String prop, Object child)
    {
        mListeners.firePropertyChange(prop, child, null);
        setDirty(true);
    }

    public void addChild(InstallOptionsWidget child)
    {
        addChild(child, -1);
    }

    public void addChild(InstallOptionsWidget child, int index)
    {
        int index2 = index;
        if (index2 >= 0) {
            mChildren.add(index2,child);
        }
        else {
            mChildren.add(child);
            index2 = mChildren.indexOf(child);
        }
        updateChildIndices(index2);
        child.setParent(this);
        INISection section = child.getSection();
        if(section != null) {
            mINISectionMap.put(section,child);
        }
        fireChildAdded(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(index2));
        setDirty(true);
    }

    public void replaceChild(InstallOptionsWidget oldChild, InstallOptionsWidget newChild)
    {
        int index = mChildren.indexOf(oldChild);
        if(index >= 0 && newChild != null && !mChildren.contains(newChild)) {
            mChildren.remove(oldChild);
            oldChild.setParent(null);
            mChildren.add(index,newChild);
            updateChildIndices(index);
            newChild.setParent(this);
            INISection section = newChild.getSection();
            if(section != null) {
                mINISectionMap.put(section,newChild);
            }
            fireChildReplaced(InstallOptionsModel.PROPERTY_CHILDREN, oldChild, newChild);
            setDirty(true);
        }
    }

    public void setChild(InstallOptionsWidget child, int index)
    {
        if (index >= mChildren.size()) {
            addChild(child,index);
        }
        else {
            InstallOptionsWidget oldChild = mChildren.get(index);
            mChildren.set(index,child);
            child.setIndex(index);
            child.setParent(this);
            if(oldChild != null) {
                fireChildRemoved(InstallOptionsModel.PROPERTY_CHILDREN, child);
            }
            INISection section = child.getSection();
            if(section != null) {
                mINISectionMap.put(section,child);
            }
            fireChildAdded(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(index));
            setDirty(true);
        }
    }

    private void updateChildIndices(int index)
    {
        updateChildIndices(index,mChildren.size()-1);
    }

    private void updateChildIndices(int startIndex, int endIndex)
    {
        if(startIndex <= endIndex) {
            for(int i=startIndex; i<=endIndex; i++) {
                InstallOptionsWidget widget = mChildren.get(i);
                if(widget != null) {
                    widget.setIndex(i);
                }
            }
            setDirty(true);
        }
    }

    public List<InstallOptionsWidget> getChildren(){
        return Collections.unmodifiableList(mChildren);
    }

    public void removeChild(InstallOptionsWidget child){
        int index = mChildren.indexOf(child);
        if(index >= 0) {
            mChildren.remove(child);
            child.setParent(null);
            updateChildIndices(index);
            fireChildRemoved(InstallOptionsModel.PROPERTY_CHILDREN, child);
            setDirty(true);
        }
    }

    public void moveChild(InstallOptionsWidget child, int newIndex){
        int oldIndex = mChildren.indexOf(child);
        if(oldIndex >= 0 && newIndex >= 0 && oldIndex != newIndex) {
            mChildren.remove(child);
            mChildren.add(newIndex,child);
            updateChildIndices(Math.min(oldIndex,newIndex),Math.max(oldIndex,newIndex));
            fireChildMoved(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(newIndex));
            setDirty(true);
        }
    }

    public void removeChild(int index){
        removeChild(mChildren.get(index));
    }

    @Override
    public Image getIconImage()
    {
        return INSTALLOPTIONS_ICON;
    }

    public InstallOptionsRuler getRuler(int orientation)
    {
        InstallOptionsRuler result = null;
        switch (orientation)
        {
            case PositionConstants.NORTH:
                result = mHorizontalRuler;
                break;
            case PositionConstants.WEST:
                result = mVerticalRuler;
                break;
        }
        return result;
    }

    public InstallOptionsElement getElement(INISection section)
    {
        if(mINISectionMap != null) {
            return mINISectionMap.get(section);
        }
        return null;
    }

    @Override
    public Object clone()
    {
        InstallOptionsDialog dialog = (InstallOptionsDialog)super.clone();
        if(dialog.mSelectedIndices != null) {
            dialog.mSelectedIndices = dialog.mSelectedIndices.clone();
        }
        dialog.reset();
        List<InstallOptionsWidget> list = new ArrayList<InstallOptionsWidget>();
        for (Iterator<InstallOptionsWidget> iter = mChildren.iterator(); iter.hasNext();) {
            InstallOptionsWidget child = (InstallOptionsWidget)iter.next().clone();
            child.setParent(dialog);
            list.add(child);
        }
        dialog.setChildren(list);
        dialog.setBackButtonText(getBackButtonText());
        dialog.setBackEnabled(getBackEnabled());
        dialog.setCancelButtonText(getCancelButtonText());
        dialog.setCancelEnabled(getCancelEnabled());
        dialog.setCancelShow(getCancelShow());
        dialog.setNextButtonText(getNextButtonText());
        dialog.setRect(getRect());
        dialog.setRTL(getRTL());
        dialog.setTitle(getTitle());
        dialog.setDialogSize(getDialogSize().getCopy());
        dialog.setShowDialogSize(isShowDialogSize());
        dialog.mINIFile = null;
        dialog.mINISectionMap = new HashMap<INISection, InstallOptionsElement>();
        return dialog;
    }

    @Override
    protected Collection<String> doGetPropertyNames()
    {
        return InstallOptionsModel.INSTANCE.getDialogSettings();
    }

    @Override
    public String toString()
    {
        return InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
    }

    private void setINIFile(INIFile file)
    {
        mINIFile = file;
    }

    public static InstallOptionsDialog loadINIFile(INIFile file)
    {
        INISection[] sections = file.getSections();
        InstallOptionsDialog dialog  = null;
        if(!Common.isEmptyArray(sections)) {
            List<InstallOptionsWidget> children = new ArrayList<InstallOptionsWidget>();
            for (int i = 0; i < sections.length; i++) {
                if(sections[i].getName().equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
                    dialog = new InstallOptionsDialog(sections[i]);
                }
                else {
                    Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(sections[i].getName());
                    if(m.matches()){
                        int index = Integer.parseInt(m.group(1))-1;
                        INIKeyValue[] types = sections[i].findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                        InstallOptionsElementFactory factory = null;
                        if(!Common.isEmptyArray(types)) {
                            factory =  InstallOptionsElementFactory.getFactory(types[0].getValue());
                        }
                        else {
                            factory =  InstallOptionsElementFactory.getFactory(InstallOptionsModel.TYPE_UNKNOWN);
                        }
                        if(factory != null) {
                            InstallOptionsWidget widget = (InstallOptionsWidget)factory.getNewObject(sections[i]);
                            if(index >= children.size()) {
                                int diff = index-children.size()+1;
                                for(int j=0; j<diff; j++) {
                                    children.add(null);
                                }
                            }
                            children.set(index, widget);
                        }
                    }
                }
            }
            if(dialog == null) {
                dialog = new InstallOptionsDialog(null);
            }
            int index = 0;
            for (Iterator<InstallOptionsWidget> iter = children.iterator(); iter.hasNext(); index++) {
                InstallOptionsWidget widget = iter.next();
                dialog.mChildren.add(null);
                if(widget != null) {
                    dialog.setChild(widget,index);
                    widget.setDirty(false);
                }
            }

            //Guides
            boolean hasGuides = false;
            if(dialog.getMetadataComment() != null) {
                String comment = dialog.getMetadataComment().getText().trim();
                String prefix = METADATA_PREFIX;
                int n = comment.indexOf(METADATA_PREFIX);
                if(n < 0) {
                    prefix = OLD_METADATA_PREFIX;
                    n = comment.indexOf(OLD_METADATA_PREFIX);
                }
                if(n >= 0) {
                    comment = comment.substring(n+prefix.length()).trim();
                    String[] metadata = Common.tokenize(comment,',');
                    if(!Common.isEmptyArray(metadata)) {
                        if(prefix.equals(METADATA_PREFIX)) {
                            for (int i = 0; i < metadata.length; i++) {
                                if(metadata[i].startsWith(GUIDES_PREFIX)) {
                                    hasGuides = loadGuides(dialog,metadata[i].substring(GUIDES_PREFIX.length()).trim());
                                    break;
                                }
                            }
                        }
                        else {
                            hasGuides = loadGuides(dialog,metadata[0]);
                        }
                    }
                }
            }
            for(Iterator<InstallOptionsWidget> iter=dialog.getChildren().iterator(); iter.hasNext(); ) {
                InstallOptionsWidget child = iter.next();
                if(child != null && child.getMetadataComment() != null) {
                    String comment = child.getMetadataComment().getText().trim();
                    if(comment != null) {
                        String prefix = METADATA_PREFIX;
                        int n = comment.indexOf(METADATA_PREFIX);
                        if(n < 0) {
                            prefix = OLD_METADATA_PREFIX;
                            n = comment.indexOf(OLD_METADATA_PREFIX);
                        }
                        if(n >= 0) {
                            comment = comment.substring(n+prefix.length()).trim();
                            if(hasGuides && prefix.equals(OLD_METADATA_PREFIX)) {
                                loadChildGuides(dialog, child, comment);
                            }
                            else {
                                String[] metadata = Common.tokenize(comment,',');
                                for (int i = 0; i < metadata.length; i++) {
                                    if(hasGuides && metadata[i].startsWith(GUIDES_PREFIX)) {
                                        loadChildGuides(dialog, child, metadata[i].substring(GUIDES_PREFIX.length()));
                                    }
                                    else if(metadata[i].startsWith(LOCKED_PREFIX)) {
                                        child.setLocked(Boolean.valueOf(metadata[i].substring(LOCKED_PREFIX.length())).booleanValue());
                                        child.setDirty(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            dialog.setDirty(false);

            //Remove null entries
            int oldSize = dialog.mChildren.size();
            for (Iterator<InstallOptionsWidget> iter = dialog.mChildren.iterator(); iter.hasNext(); ) {
                if(iter.next() == null) {
                    iter.remove();
                }
            }
            if(oldSize != dialog.mChildren.size()) {
                dialog.updateChildIndices(0);
            }
        }
        else {
            dialog = new InstallOptionsDialog(null);
        }
        dialog.setINIFile(file);
        return dialog;
    }

    private static boolean loadGuides(InstallOptionsDialog dialog, String guidesMetadata)
    {
        boolean loaded = false;
        String[] tokens = Common.tokenize(guidesMetadata,'#');
        for (int i = 0; i < tokens.length; i++) {
            String[] tokens2 = Common.tokenize(tokens[i],'|');
            if(tokens2.length > 0) {
                int orientation;
                try {
                    orientation = Integer.parseInt(tokens2[0]);
                }
                catch(Exception ex) {
                    orientation = -1;
                }
                if(orientation == PositionConstants.NORTH || orientation == PositionConstants.WEST) {
                    int offset = 0;
                    if(tokens2.length > 1) {
                        try {
                            offset = Integer.parseInt(tokens2[1]);
                        }
                        catch(Exception ex) {
                            offset = 0;
                        }
                    }
                    InstallOptionsRuler ruler = dialog.getRuler(orientation);
                    InstallOptionsGuide guide = new InstallOptionsGuide(orientation == PositionConstants.WEST);
                    guide.setPosition(offset);
                    ruler.addGuide(guide);
                    loaded = true;
                }
            }
        }
        return loaded;
    }

    private static void loadChildGuides(InstallOptionsDialog parent, InstallOptionsWidget child, String guidesMetadata)
    {
        String[] tokens = Common.tokenize(guidesMetadata,'#');
        if(tokens.length > 0) {
            for (int i = 0; i < tokens.length; i++) {
                String[] tokens2 = Common.tokenize(tokens[i],'|');
                if(tokens2.length >= 2) {
                    int orientation;
                    try {
                        orientation = Integer.parseInt(tokens2[0]);
                    }
                    catch(Exception ex) {
                        orientation = -1;
                    }
                    if(orientation == PositionConstants.NORTH || orientation == PositionConstants.WEST) {
                        int index;
                        try {
                            index = Integer.parseInt(tokens2[1]);
                        }
                        catch(Exception ex) {
                            index = -1;
                        }
                        if(index >= 0) {
                            int alignment = 0;
                            if(tokens2.length > 2) {
                                try {
                                    alignment = Integer.parseInt(tokens2[2]);
                                }
                                catch(Exception ex) {
                                    alignment = 0;
                                }
                            }
                            if(Math.abs(alignment) <= 1) {
                                List<InstallOptionsGuide> guides = parent.getRuler(orientation).getGuides();
                                if(!Common.isEmptyCollection(guides) && guides.size() > index) {
                                    InstallOptionsGuide guide = guides.get(index);
                                    int offset = guide.getPosition();
                                    Position p = child.toGraphical(child.getPosition(), false);
                                    int offset2 = -1;
                                    switch(alignment) {
                                        case -1:
                                            offset2 = (orientation == PositionConstants.NORTH?p.left:p.top);
                                            break;
                                        case 0:
                                            offset2 = (orientation == PositionConstants.NORTH?p.left+p.right:p.top+p.bottom)/2;
                                            break;
                                        case 1:
                                            offset2 = (orientation == PositionConstants.NORTH?p.right:p.bottom);
                                    }
                                    if(offset == offset2) {
                                        guide.attachWidget(child,alignment);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean canUpdateINIFile()
    {
        if(!isDirty()) {
            for (Iterator<InstallOptionsWidget> iter = mChildren.iterator(); iter.hasNext();) {
                InstallOptionsWidget child = iter.next();
                if(child.isDirty()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public INISection getSection()
    {
        INISection section = super.getSection();
        if(section.getPosition() == null) {
            section.setPosition(new org.eclipse.jface.text.Position(0,0));
        }
        return section;
    }


    public INIFile updateINIFile()
    {
        if(canUpdateINIFile()) {
            HashMap<INISection, InstallOptionsElement> tempMap = new HashMap<INISection, InstallOptionsElement>();
            INISection previousSection = null;
            List<InstallOptionsElement> children = new ArrayList<InstallOptionsElement>(mChildren);
            children.add(this);
            Collections.sort(children,new Comparator<InstallOptionsElement>() {
                private org.eclipse.jface.text.Position getPosition(InstallOptionsElement element)
                {
                    INISection sec = element.getSection();
                    if(sec != null) {
                        org.eclipse.jface.text.Position p = sec.getPosition();
                        if(p != null) {
                            return p;
                        }
                    }
                    return MAX_POSITION;
                }

                public int compare(InstallOptionsElement o1, InstallOptionsElement o2)
                {
                    org.eclipse.jface.text.Position p1 = getPosition(o1);
                    org.eclipse.jface.text.Position p2 = getPosition(o2);
                    int n = p1.getOffset()-p2.getOffset();
                    if(n == 0) {
                        n = p1.getLength()-p2.getLength();
                    }
                    return n;
                }
            });

            for (Iterator<InstallOptionsElement> iter = children.iterator(); iter.hasNext();) {
                if(previousSection != null) {
                    int n = previousSection.getSize();
                    if(n > 0) {
                        INILine lastChild = previousSection.getChild(n-1);
                        if(lastChild.getDelimiter() == null) {
                            lastChild.setDelimiter(INSISConstants.LINE_SEPARATOR);
                        }
                        while(n > 0) {
                            if(lastChild instanceof INIComment) {
                                n--;
                                lastChild = previousSection.getChild(n-1);
                                continue;
                            }
                            if(!lastChild.getClass().equals(INILine.class) || !Common.isEmpty(lastChild.getText())) {
                                previousSection.addChild(new INILine("",lastChild.getDelimiter())); //$NON-NLS-1$
                            }
                            break;
                        }
                    }
                }
                InstallOptionsElement element = iter.next();
                INISection section = element.updateSection();
                if(!mINISectionMap.containsKey(section)) {
                    mINISectionMap.put(section,element);
                }
                if(!mINIFile.getChildren().contains(section)) {
                    mINIFile.addChild(section);
                }
                tempMap.put(section, element);
                previousSection = section;
            }
            if(previousSection != null) {
                int n = previousSection.getSize();
                if(n > 0) {
                    INILine lastChild = previousSection.getChild(n-1);
                    if(lastChild.getClass().equals(INILine.class) && Common.isEmpty(lastChild.getText()) && lastChild.getDelimiter() != null) {
                        previousSection.removeChild(lastChild);
                    }
                }
            }
            for (Iterator<INISection> iter = mINISectionMap.keySet().iterator(); iter.hasNext();) {
                INISection section = iter.next();
                if(!tempMap.containsKey(section)) {
                    iter.remove();
                    mINIFile.removeChild(section);
                }
            }

            boolean saveGuides = updateGuidesMetadata();
            for(Iterator<InstallOptionsWidget> iter = getChildren().iterator(); iter.hasNext(); ) {
                InstallOptionsWidget child = iter.next();
                updateChildMetadata(child, saveGuides);
            }

            mINIFile.update();
            setDirty(false);
        }
        return mINIFile;
    }

    private boolean updateGuidesMetadata()
    {
        INISection section = getSection();
        List<InstallOptionsGuide> verticalGuides = getRuler(PositionConstants.NORTH).getGuides();
        List<InstallOptionsGuide> horizontalGuides = getRuler(PositionConstants.WEST).getGuides();
        INIComment comment = getMetadataComment();
        if(verticalGuides.size() > 0 || horizontalGuides.size() > 0) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            if(comment == null) {
                buf.append(METADATA_PREFIX).append(" ").append(GUIDES_PREFIX); //$NON-NLS-1$
            }
            else {
                String text = comment.getText();
                int n = text.indexOf(METADATA_PREFIX);
                if(n < 0) {
                    n = text.indexOf(OLD_METADATA_PREFIX);
                }
                buf.append(text.substring(0,n)).append(METADATA_PREFIX).append(" ").append(GUIDES_PREFIX); //$NON-NLS-1$
            }
            int count = 0;
            for (Iterator<InstallOptionsGuide> iter = horizontalGuides.iterator(); iter.hasNext();) {
                InstallOptionsGuide guide = iter.next();
                if(count > 0) {
                    buf.append('#');
                }
                buf.append(PositionConstants.WEST).append('|').append(guide.getPosition());
                count++;
            }
            for (Iterator<InstallOptionsGuide> iter = verticalGuides.iterator(); iter.hasNext();) {
                InstallOptionsGuide guide = iter.next();
                if(count > 0) {
                    buf.append('#');
                }
                buf.append(PositionConstants.NORTH).append('|').append(guide.getPosition());
                count++;
            }
            if(comment == null) {
                comment = new INIComment(buf.toString());
                section.addChild(0,comment);
            }
            else {
                comment.setText(buf.toString());
            }
            return true;
        }
        else {
            if(comment != null) {
                section.removeChild(comment);
            }
            return false;
        }
    }

    @Override
    protected TypeConverter<?> loadTypeConverter(String property, Object value)
    {
        if (InstallOptionsModel.PROPERTY_NUMFIELDS.equals(property)||
            InstallOptionsModel.PROPERTY_CANCEL_ENABLED.equals(property)||
            InstallOptionsModel.PROPERTY_CANCEL_SHOW.equals(property)||
            InstallOptionsModel.PROPERTY_BACK_ENABLED.equals(property)||
            InstallOptionsModel.PROPERTY_RTL.equals(property)||
            InstallOptionsModel.PROPERTY_RECT.equals(property)) {
            if(value instanceof String) {
                if(((String)value).regionMatches(true,0,"0x",0,2)) { //$NON-NLS-1$
                    return TypeConverter.HEX_CONVERTER;
                }
            }
            return TypeConverter.INTEGER_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }

    private void updateChildMetadata(InstallOptionsWidget child, boolean saveGuides)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(child.isLocked()) {
            buf.append(LOCKED_PREFIX).append(Boolean.TRUE.toString());
        }
        if(saveGuides) {
            InstallOptionsGuide horizontalGuide = child.getHorizontalGuide();
            InstallOptionsGuide verticalGuide = child.getVerticalGuide();
            if(horizontalGuide != null || verticalGuide != null) {
                if(buf.length() > 0) {
                    buf.append(","); //$NON-NLS-1$
                }
                buf.append(GUIDES_PREFIX);
                if(horizontalGuide != null) {
                    buf.append(PositionConstants.WEST).append('|');
                    buf.append(getRuler(PositionConstants.WEST).getGuides().indexOf(horizontalGuide)).append('|');
                    buf.append(horizontalGuide.getAlignment(child));
                }
                if(verticalGuide != null) {
                    if(horizontalGuide != null) {
                        buf.append('#');
                    }
                    buf.append(PositionConstants.NORTH).append('|');
                    buf.append(getRuler(PositionConstants.NORTH).getGuides().indexOf(verticalGuide)).append('|');
                    buf.append(verticalGuide.getAlignment(child));
                }
            }
        }
        if(buf.length() > 0) {
            INIComment comment = child.getMetadataComment();
            if(comment == null) {
                comment = new INIComment(new StringBuffer(METADATA_PREFIX).append(" ").append(buf.toString()).toString()); //$NON-NLS-1$
                child.getSection().addChild(0, comment);
            }
            else {
                String text = comment.getText();
                int n = text.indexOf(METADATA_PREFIX);
                if(n < 0) {
                    n = text.indexOf(OLD_METADATA_PREFIX);
                }
                text = new StringBuffer(text.substring(0,n)).append(METADATA_PREFIX).append(" ").append(buf.toString()).toString(); //$NON-NLS-1$
                comment.setText(text);
            }
        }
        else {
            if(child.getMetadataComment() != null) {
                child.getSection().removeChild(child.getMetadataComment());
            }
        }
    }

    @Override
    protected String getSectionName()
    {
        return InstallOptionsModel.SECTION_SETTINGS;
    }

    @Override
    public void modelChanged()
    {
        super.modelChanged();
        if(!Common.isEmptyCollection(mChildren)) {
            for (ListIterator<InstallOptionsWidget> iter = mChildren.listIterator(); iter.hasNext();) {
                InstallOptionsWidget child = iter.next();
                InstallOptionsWidget newChild = null;
                InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(child.getType());
                boolean isDirty = isDirty();
                boolean childIsDirty = child.isDirty();
                if((child instanceof InstallOptionsUnknown && !InstallOptionsModel.TYPE_UNKNOWN.equals(typeDef.getType())) ||
                   (!(child instanceof InstallOptionsUnknown) && InstallOptionsModel.TYPE_UNKNOWN.equals(typeDef.getType()))) {
                    if(childIsDirty) {
                        child.updateSection();
                    }
                    newChild = (InstallOptionsWidget)typeDef.createModel(child.getSection());
                    InstallOptionsGuide verticalGuide = child.getVerticalGuide();
                    int verticalAlign = 0;
                    InstallOptionsGuide horizontalGuide = child.getHorizontalGuide();
                    int horizontalAlign = 0;

                    if (verticalGuide != null) {
                        verticalAlign = verticalGuide.getAlignment(child);
                        verticalGuide.detachWidget(child);
                    }
                    if (horizontalGuide != null) {
                        horizontalAlign = horizontalGuide.getAlignment(child);
                        horizontalGuide.detachWidget(child);
                    }

                    child.setParent(null);
                    newChild.setIndex(child.getIndex());
                    iter.set(newChild);
                    newChild.setParent(this);
                    mINISectionMap.put(newChild.getSection(),newChild);
                    newChild.setDirty(childIsDirty);
                    setDirty(isDirty);
                    fireChildReplaced(InstallOptionsModel.PROPERTY_CHILDREN, child, newChild);

                    if (verticalGuide != null) {
                        verticalGuide.attachWidget(newChild, verticalAlign);
                    }
                    if (horizontalGuide != null) {
                        horizontalGuide.attachWidget(newChild, horizontalAlign);
                    }
                }
                else {
                    child.modelChanged();
                }
            }
        }
    }
}