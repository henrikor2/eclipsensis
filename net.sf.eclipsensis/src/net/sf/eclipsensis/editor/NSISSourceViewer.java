/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.RegistryValueSelectionDialog;
import net.sf.eclipsensis.editor.codeassist.NSISInformationUtility;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.settings.IPropertyAdaptable;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.markers.MarkerViewUtil;


public class NSISSourceViewer extends ProjectionViewer implements IPropertyChangeListener, INSISKeywordsListener
{
    public static final int INSERT_TEMPLATE = 1000;
    public static final int GOTO_HELP = INSERT_TEMPLATE + 1;
    public static final int INSERT_FILE = GOTO_HELP + 1;
    public static final int INSERT_DIRECTORY = INSERT_FILE + 1;
    public static final int INSERT_COLOR = INSERT_DIRECTORY + 1;
    public static final int IMPORT_REGFILE = INSERT_COLOR + 1;
    public static final int IMPORT_REGKEY = IMPORT_REGFILE + 1;
    public static final int IMPORT_REGVAL = IMPORT_REGKEY + 1;
    public static final int TABS_TO_SPACES = IMPORT_REGVAL + 1;
    public static final int TOGGLE_COMMENT = TABS_TO_SPACES + 1;
    public static final int ADD_BLOCK_COMMENT = TOGGLE_COMMENT + 1;
    public static final int REMOVE_BLOCK_COMMENT = ADD_BLOCK_COMMENT + 1;

    private IPreferenceStore mPreferenceStore = null;
    private NSISAutoIndentStrategy mAutoIndentStrategy = null;
    private ILineTracker mLineTracker = null;
    private String[] mConfiguredContentTypes = null;
    private Set<String> mPropertyQueue = new HashSet<String>();
    private NSISScrollTipHelper mScrollTipHelper = null;
    private IContentAssistant mInsertTemplateAssistant = null;
    private boolean mInsertTemplateAssistantInstalled = false;
    private RegistryValue mRegValue = null;

   /**
     * @param parent
     * @param ruler
     * @param overviewRuler
     * @param showsAnnotationOverview
     * @param styles
     */
    public NSISSourceViewer(Composite parent, IVerticalRuler ruler,
            IOverviewRuler overviewRuler, boolean showsAnnotationOverview,
            int styles)
    {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
        mLineTracker = new DefaultLineTracker();
        mScrollTipHelper = new NSISScrollTipHelper(this);
    }

    public boolean mustProcessPropertyQueue()
    {
        return mPropertyQueue.size() > 0;
    }

    public void processPropertyQueue()
    {
        HashSet<String> contentTypes = new HashSet<String>();
        for(Iterator<String> iter = mPropertyQueue.iterator(); iter.hasNext(); ) {
            String property = iter.next();
            for(int i=0; i<mConfiguredContentTypes.length; i++) {
                IPresentationDamager damager = fPresentationReconciler.getDamager(mConfiguredContentTypes[i]);
                if(damager instanceof IPropertyAdaptable) {
                    IPropertyAdaptable adaptable = (IPropertyAdaptable)damager;
                    if(adaptable.canAdaptToProperty(mPreferenceStore, property)) {
                        contentTypes.add(mConfiguredContentTypes[i]);
                        adaptable.adaptToProperty(mPreferenceStore, property);
                    }
                }
            }
        }

        updatePresentation(contentTypes);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.help.INSISKeywordsListener#keywordsChanged()
     */
    public void keywordsChanged()
    {
        if(fPresentationReconciler != null) {
            final HashSet<String> contentTypes = new HashSet<String>();
            for(int i=0; i<mConfiguredContentTypes.length; i++) {
                IPresentationDamager damager = fPresentationReconciler.getDamager(mConfiguredContentTypes[i]);
                if(damager instanceof NSISDamagerRepairer) {
                    ((NSISDamagerRepairer)damager).reset();
                    contentTypes.add(mConfiguredContentTypes[i]);
                }
            }

            if(Display.getCurrent() == null) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        updatePresentation(contentTypes);
                    }
                });
            }
            else {
                updatePresentation(contentTypes);
            }
        }
    }

    /**
     * @param contentTypes
     */
    private void updatePresentation(Collection<String> contentTypes)
    {
        IDocument doc = getDocument();
        try {
            ITypedRegion[] regions = null;
            if(doc instanceof IDocumentExtension3) {
                regions = ((IDocumentExtension3)doc).computePartitioning(fPartitioning,0,doc.getLength(),false);
            }
            else {
                regions = doc.computePartitioning(0,doc.getLength());
            }
            for (int i = 0; i < regions.length; i++) {
                if(contentTypes.contains(regions[i].getType())) {
                    invalidateTextPresentation(regions[i].getOffset(),regions[i].getLength());
                }
            }
        }
        catch (BadPartitioningException e) {
        }
        catch (BadLocationException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ISourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
     */
    @Override
    public void configure(SourceViewerConfiguration configuration)
    {
        if(configuration instanceof NSISSourceViewerConfiguration) {
            mPreferenceStore = ((NSISSourceViewerConfiguration)configuration).getPreferenceStore();
        }
        super.configure(configuration);
        NSISKeywords.getInstance().addKeywordsListener(this);
        if(configuration instanceof NSISSourceViewerConfiguration) {
            mConfiguredContentTypes = configuration.getConfiguredContentTypes(this);
            mPreferenceStore.addPropertyChangeListener(this);
            if(configuration instanceof NSISEditorSourceViewerConfiguration) {
                mAutoIndentStrategy = new NSISAutoIndentStrategy(mPreferenceStore);
                mAutoIndentStrategy.updateFromPreferences();
                for(int i=0; i<mConfiguredContentTypes.length; i++) {
                    prependAutoEditStrategy(mAutoIndentStrategy,mConfiguredContentTypes[i]);
                }
                mInsertTemplateAssistant = ((NSISEditorSourceViewerConfiguration)configuration).getInsertTemplateAssistant(this);
                if(mInsertTemplateAssistant != null) {
                    mInsertTemplateAssistant.install(this);
                    mInsertTemplateAssistantInstalled = true;
                }
            }
        }
        mScrollTipHelper.connect();

        final IVerticalRuler ruler = getVerticalRuler();
        if(ruler != null) {
            ruler.getControl().addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseUp(MouseEvent e)
                {
                    try {
                        IAnnotationModel model = getAnnotationModel();
                        IDocument document= getDocument();
                        int lineNumber = ruler.toDocumentLineNumber(e.y);
                        IRegion info= document.getLineInformation(lineNumber);

                        if (model != null) {
                            for(Iterator<?> iter= model.getAnnotationIterator(); iter.hasNext(); ) {
                                Annotation a= (Annotation) iter.next();
                                Position p= model.getPosition(a);
                                if (p != null && p.overlapsWith(info.getOffset(), info.getLength())) {
                                    if(a instanceof MarkerAnnotation) {
                                        IMarker marker = ((MarkerAnnotation)a).getMarker();
                                        String type = marker.getType();
                                        if(type.equals(INSISConstants.PROBLEM_MARKER_ID)||type.equals(INSISConstants.TASK_MARKER_ID)) {
                                            setSelectedRange(p.getOffset(),p.getLength());
                                            final IMarker fMarker = marker;
                                            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                                                public void run()
                                                {
                                                    MarkerViewUtil.showMarker(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),fMarker,false);
                                                }
                                            });
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     */
    @Override
    public void unconfigure()
    {
        mScrollTipHelper.disconnect();

        if (mInsertTemplateAssistant != null) {
            mInsertTemplateAssistant.uninstall();
            mInsertTemplateAssistantInstalled= false;
            mInsertTemplateAssistant= null;
        }

        mAutoIndentStrategy = null;
        if(mPreferenceStore != null) {
            mPreferenceStore.removePropertyChangeListener(this);
            mPreferenceStore = null;
        }
        mConfiguredContentTypes = null;
        NSISKeywords.getInstance().removeKeywordsListener(this);
        super.unconfigure();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        String property = event.getProperty();
        if(property.equals(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH)||
           property.equals(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS)) {
            for(Iterator<?> iter=fIndentChars.keySet().iterator(); iter.hasNext(); ) {
                setIndentPrefixes(calculatePrefixes(),(String)iter.next());
            }

            for(Iterator<?> iter=fAutoIndentStrategies.keySet().iterator(); iter.hasNext(); ) {
                String contentType = (String)iter.next();
                List<?> list = (List<?>)fAutoIndentStrategies.get(contentType);
                if(!Common.isEmptyCollection(list)) {
                    for (Iterator<?> iter2 = list.iterator(); iter2.hasNext();) {
                        IAutoEditStrategy autoEditStrategy = (IAutoEditStrategy)iter2.next();
                        if(autoEditStrategy instanceof NSISAutoEditStrategy) {
                            ((NSISAutoEditStrategy)autoEditStrategy).updateFromPreferences();
                        }
                    }
                }
            }
        }
        else {
            for(int i=0; i<mConfiguredContentTypes.length; i++) {
                IPresentationDamager damager = fPresentationReconciler.getDamager(mConfiguredContentTypes[i]);
                if(damager instanceof IPropertyAdaptable) {
                    IPropertyAdaptable adaptable = (IPropertyAdaptable)damager;
                    if(adaptable.canAdaptToProperty(mPreferenceStore, property)) {
                        mPropertyQueue.add(property);
                    }
                }
            }
        }
    }

    public String[] calculatePrefixes()
    {
        List<String> list= new ArrayList<String>();

        // prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces

        int tabWidth= mPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
        boolean useSpaces= mPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);

        for (int i= 0; i <= tabWidth; i++) {
            StringBuffer prefix= new StringBuffer();

            if (useSpaces) {
                for (int j= 0; j + i < tabWidth; j++) {
                    prefix.append(' ');
                }

                if (i != 0) {
                    prefix.append('\t');
                }
            }
            else {
                for (int j= 0; j < i; j++) {
                    prefix.append(' ');
                }

                if (i != tabWidth) {
                    prefix.append('\t');
                }
            }

            list.add(prefix.toString());
        }

        return list.toArray(new String[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTargetExtension#enableOperation(int, boolean)
     */
    @Override
    public void enableOperation(int operation, boolean enable)
    {
        switch(operation) {
            case INSERT_TEMPLATE:
                if (mInsertTemplateAssistant == null) {
                    return;
                }
                if (enable) {
                    if (!mInsertTemplateAssistantInstalled) {
                        mInsertTemplateAssistant.install(this);
                        mInsertTemplateAssistantInstalled= true;
                    }
                }
                else if (mInsertTemplateAssistantInstalled) {
                    mInsertTemplateAssistant.uninstall();
                    mInsertTemplateAssistantInstalled= false;
                }
                break;
            default:
                super.enableOperation(operation, enable);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTarget#canDoOperation(int)
     */
    @Override
    public boolean canDoOperation(int operation)
    {
        switch(operation) {
            case GOTO_HELP:
                return (fInformationPresenter != null);
            case INSERT_TEMPLATE:
                return mInsertTemplateAssistant != null && mInsertTemplateAssistantInstalled && isEditable();
            case INSERT_FILE:
            case INSERT_DIRECTORY:
            case INSERT_COLOR:
            case IMPORT_REGFILE:
            case IMPORT_REGKEY:
            case IMPORT_REGVAL:
            case TABS_TO_SPACES:
            case TOGGLE_COMMENT:
            case REMOVE_BLOCK_COMMENT:
            case ADD_BLOCK_COMMENT:
                return isEditable();
            default:
                return super.canDoOperation(operation);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
     */
    @Override
    public void doOperation(int operation)
    {
        String text = null;
        switch(operation) {
            case GOTO_HELP:
            {
                doGotoHelp();
                return;
            }
            case INSERT_TEMPLATE:
            {
                mInsertTemplateAssistant.showPossibleCompletions();
                return;
            }
            case INSERT_FILE:
            case INSERT_DIRECTORY:
                if(operation == INSERT_FILE) {
                    FileDialog dialog = new FileDialog(getControl().getShell(),SWT.OPEN);
                    dialog.setText(EclipseNSISPlugin.getResourceString("insert.file.description")); //$NON-NLS-1$
                    text = dialog.open();
                }
                else {
                    DirectoryDialog dialog = new DirectoryDialog(getControl().getShell());
                    dialog.setText(EclipseNSISPlugin.getResourceString("insert.directory.description")); //$NON-NLS-1$
                    text = dialog.open();
                }
                if(!Common.isEmpty(text)) {
                    NSISEditor editor = null;
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if(page != null) {
                        IEditorPart ed = page.getActiveEditor();
                        if(ed instanceof NSISEditor) {
                            editor = (NSISEditor)ed;
                        }
                    }
                    text = IOUtility.resolveFileName(text, editor);
                }
                break;
            case INSERT_COLOR:
            {
                ColorDialog dialog = new ColorDialog(getControl().getShell());
                dialog.setText(EclipseNSISPlugin.getResourceString("insert.color.description")); //$NON-NLS-1$
                RGB rgb = dialog.open();
                if(rgb != null) {
                    text = ColorManager.rgbToHex(rgb);
                }
                break;
            }
            case IMPORT_REGFILE:
            {
                NSISEditorRegistryImportStrategy strategy = new NSISEditorRegistryImportStrategy();
                RegistryImporter.INSTANCE.importRegFile(getControl().getShell(), strategy);
                text = strategy.getText();
                break;
            }
            case IMPORT_REGKEY:
            {
                NSISEditorRegistryImportStrategy strategy = new NSISEditorRegistryImportStrategy();
                RegistryImporter.INSTANCE.importRegKey(getControl().getShell(), strategy);
                text = strategy.getText();
                break;
            }
            case IMPORT_REGVAL:
            {
                RegistryValueSelectionDialog dialog = new RegistryValueSelectionDialog(getControl().getShell());
                dialog.setText(EclipseNSISPlugin.getResourceString("select.regval.message")); //$NON-NLS-1$
                if(mRegValue != null) {
                    dialog.setRegistryValue(mRegValue);
                }
                if(dialog.open() == Window.OK) {
                    mRegValue = dialog.getRegistryValue();
                    String regKey = mRegValue.getRegKey().toString();
                    int n = regKey.indexOf("\\"); //$NON-NLS-1$
                    String rootKey;
                    String subKey;
                    if(n > 0) {
                        rootKey = regKey.substring(0,n);
                        subKey = regKey.substring(n+1);
                    }
                    else {
                        rootKey = regKey;
                        subKey = ""; //$NON-NLS-1$
                    }
                    NSISEditorRegistryImportStrategy strategy = new NSISEditorRegistryImportStrategy();
                    String value = mRegValue.getValue();
                    int type = mRegValue.getType();
                    String data = mRegValue.getData();
                    strategy.addRegistryValue(rootKey, subKey, value,type,data);
                    text = strategy.getText();
                }
                break;
            }
            case TABS_TO_SPACES:
            {
                doConvertTabsToSpaces();
                return;
            }
            case TOGGLE_COMMENT:
            {
                doToggleComment();
                return;
            }
            case ADD_BLOCK_COMMENT:
            {
                doAddBlockComment();
                return;
            }
            case REMOVE_BLOCK_COMMENT:
            {
                doRemoveBlockComment();
                return;
            }
            case ISourceViewer.INFORMATION:
            {
                if(!NSISHelpURLProvider.getInstance().isNSISHelpAvailable()) {
                    return;
                }
                //Fall through
            }
                //$FALL-THROUGH$
            default:
            {
                super.doOperation(operation);
                return;
            }
        }
        if(!Common.isEmpty(text)) {
            IDocument doc = getDocument();
            Point p = getSelectedRange();
            try {
                doc.replace(p.x,p.y,text);
                setSelectedRange(p.x+(text==null?0:text.length()), 0);
                revealRange(p.x+(text==null?0:text.length()), 0);
            }
            catch (BadLocationException e) {
            }
        }
    }

    private void doAddBlockComment()
    {
        try {
            Point p = getSelectedRange();
            if(p.y > 0) {
                IDocument doc = getDocument();
                ITypedRegion region = null;
                int startPos = p.x;
                int endPos = p.x+p.y-1;
                StringBuffer newText = new StringBuffer("/*"); //$NON-NLS-1$
                int pos = p.x;
                while(true) {
                    region = NSISTextUtility.getNSISPartitionAtOffset(doc,pos);
                    int regionStart = region.getOffset();
                    int regionLen = region.getLength();
                    int regionEnd = regionStart+regionLen-1;
                    String regionType = region.getType();
                    if(regionType.equals(NSISPartitionScanner.NSIS_SINGLELINE_COMMENT) ||
                            regionType.equals(NSISPartitionScanner.NSIS_STRING)) {
                        if(regionStart < startPos) {
                            startPos = regionStart;
                        }
                        newText.append(doc.get(regionStart,regionLen));
                        if(regionEnd >= endPos) {
                            endPos = regionEnd;
                            break;
                        }
                        else {
                            pos = regionEnd+1;
                        }
                    }
                    else if(regionType.equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                        if(regionStart < startPos) {
                            startPos = regionStart;
                        }
                        newText.append(doc.get(regionStart+2,regionLen-4));
                        if(regionEnd >= endPos) {
                            endPos = regionEnd;
                            break;
                        }
                        else {
                            pos = regionEnd+1;
                        }
                    }
                    else {
                        regionStart = Math.max(startPos,regionStart);
                        if(regionEnd >= endPos) {
                            newText.append(doc.get(regionStart,endPos-regionStart+1));
                            break;
                        }
                        else {
                            newText.append(doc.get(regionStart,regionEnd-regionStart+1));
                            pos = regionEnd+1;
                        }
                    }
                }
                newText.append("*/"); //$NON-NLS-1$
                doc.replace(startPos,endPos-startPos+1,newText.toString());
            }
        }
        catch (BadLocationException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }

    }

    private void doRemoveBlockComment()
    {
        try {
            IDocument doc = getDocument();
            Point p = getSelectedRange();
            int startPos = p.x;
            int endPos = (p.x>0?p.x+p.y-1:startPos);
            int pos = startPos;
            StringBuffer newText = new StringBuffer(""); //$NON-NLS-1$
            ITypedRegion region;
            while(true) {
                region = NSISTextUtility.getNSISPartitionAtOffset(doc,pos);
                int regionStart = region.getOffset();
                int regionLen = region.getLength();
                int regionEnd = regionStart+regionLen-1;
                String regionType = region.getType();
                if(regionType.equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                    newText.append(doc.get(regionStart+2,regionLen-4));
                    if(regionStart < startPos) {
                        startPos = regionStart;
                    }
                    if(regionEnd >= endPos) {
                        endPos = regionEnd;
                        break;
                    }
                    else {
                        pos = regionEnd+1;
                    }
                }
                else {
                    if(regionEnd >= endPos) {
                        newText.append(doc.get(pos,(endPos-pos+1)));
                        break;
                    }
                    else {
                        newText.append(doc.get(pos,(regionEnd-pos+1)));
                        pos = regionEnd+1;
                    }
                }
            }
            doc.replace(startPos,endPos-startPos+1,newText.toString());
        }
        catch (BadLocationException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    private void doToggleComment()
    {
        try {
            IDocument doc = getDocument();
            Point p = getSelectedRange();
            int startLine = doc.getLineOfOffset(p.x);
            int endLine = (p.y==0?startLine:doc.getLineOfOffset(p.x+p.y-1));
            boolean allAreCommented=true;
            IRegion region = null;
            String[] text = new String[endLine-startLine+1];
            for(int i=startLine; i<=endLine; i++) {
                region = doc.getLineInformation(i);
                text[i-startLine] = doc.get(region.getOffset(),region.getLength());
                String text2 = text[i-startLine].trim();
                if(!text2.startsWith(";") && !text2.startsWith("#")) { //$NON-NLS-1$ //$NON-NLS-2$
                    allAreCommented = false;
                }
            }

            if (region != null) {
                int startPos = doc.getLineOffset(startLine);
                int length = region.getOffset() + region.getLength() - startPos;
                StringBuffer newText = new StringBuffer(""); //$NON-NLS-1$
                for (int i = startLine; i <= endLine; i++) {
                    if (i > startLine) {
                        newText.append(doc.getLineDelimiter(i - 1));
                    }
                    if (allAreCommented) {
                        String text2 = text[i - startLine].trim();
                        int n = text[i - startLine].indexOf(text2);
                        newText.append(text[i - startLine].substring(0, n));
                        if (n < (text[i - startLine].length() - 1)) {
                            newText
                                    .append(text[i - startLine]
                                            .substring(n + 1));
                        }
                    } else {
                        newText.append(";").append(text[i - startLine]); //$NON-NLS-1$
                    }
                }
                doc.replace(startPos, length, newText.toString());
            }
        }
        catch (BadLocationException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    private void doConvertTabsToSpaces()
    {
        int tabWidth = mPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
        IDocument doc = getDocument();
        Point p = getSelectedRange();
        String text;
        if(p.y == 0) {
            p.x = 0;
            p.y = doc.getLength();
        }
        try {
            text = doc.get(p.x,p.y);
            text = convertTabsToSpaces(getDocument(),p.x,text,tabWidth);
            doc.replace(p.x,p.y,text);
        }
        catch (BadLocationException e) {
        }
    }

    private void doGotoHelp()
    {
        if(NSISHelpURLProvider.getInstance().isNSISHelpAvailable()) {
            int offset = NSISTextUtility.computeOffset(this,NSISTextUtility.COMPUTE_OFFSET_HOVER_LOCATION);
            if(offset >= 0) {
                if(showHelp(offset)) {
                    return;
                }
            }
            offset = NSISTextUtility.computeOffset(this,NSISTextUtility.COMPUTE_OFFSET_CARET_LOCATION);
            if(offset >= 0) {
                showHelp(offset);
            }
        }
    }

    private boolean showHelp(int offset)
    {
        IRegion region = NSISInformationUtility.getInformationRegionAtOffset(this,offset,false);
        String keyword = NSISTextUtility.getRegionText(getDocument(),region);
        if(keyword != null) {
            if(NSISHelpURLProvider.getInstance().showHelpURL(keyword)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void ensureOverviewHoverManagerInstalled()
    {
        // This is a hack so that the Hover control creator for hover help isn't used in the
        // Overview ruler.
        IInformationControlCreator oldControlCreator = fHoverControlCreator;
        if (fOverviewRulerAnnotationHover instanceof IAnnotationHoverExtension)  {
            IInformationControlCreator controlCreator = ((IAnnotationHoverExtension)fOverviewRulerAnnotationHover).getHoverControlCreator();
            if(controlCreator != null) {
                fHoverControlCreator = controlCreator;
            }
        }
        super.ensureOverviewHoverManagerInstalled();
        fHoverControlCreator = oldControlCreator;
    }

    private String convertTabsToSpaces(IDocument doc, int textOffset, String text, int tabWidth)
    {
        String text2 = text;
        if (text2 != null) {
            int index= text2.indexOf('\t');
            if (index > -1) {
                StringBuffer buffer= new StringBuffer();
                mLineTracker.set(text2);
                int lines= mLineTracker.getNumberOfLines();

                try {
                    for (int i= 0; i < lines; i++) {
                        int offset= mLineTracker.getLineOffset(i);
                        int endOffset= offset + mLineTracker.getLineLength(i);
                        String line= text2.substring(offset, endOffset);

                        int position= 0;
                        if (i == 0) {
                            IRegion firstLine= doc.getLineInformationOfOffset(textOffset);
                            position= 0;
                            int lineOffset = firstLine.getOffset();
                            if(textOffset > lineOffset) {
                                int length = textOffset-lineOffset;
                                String s = doc.get(lineOffset,length);
                                for(int j=0; j<length; j++) {
                                    if(s.charAt(j)=='\t') {
                                        position = tabWidth*((position+1)/tabWidth + 1);
                                    }
                                    else {
                                        position++;
                                    }
                                }
                            }
                        }

                        int length= line.length();
                        for (int j= 0; j < length; j++) {
                            char c= line.charAt(j);
                            if (c == '\t') {
                                position += NSISTextUtility.insertTabString(buffer,position,tabWidth);
                            } else {
                                buffer.append(c);
                                ++position;
                            }
                        }
                    }

                    text2= buffer.toString();
                } catch (BadLocationException x) {
                }
            }
        }
        return text2;
    }
}