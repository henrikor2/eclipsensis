/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class NSISTextUtility implements INSISConstants
{
    public static final IRegion EMPTY_REGION = new Region(0,0);
    private static final String[] cValidPartitionTypes = {IDocument.DEFAULT_CONTENT_TYPE,
                                                          NSISPartitionScanner.NSIS_STRING};
    public static final int COMPUTE_OFFSET_HOVER_LOCATION = 1;
    public static final int COMPUTE_OFFSET_CURSOR_LOCATION = 2;
    public static final int COMPUTE_OFFSET_CARET_LOCATION = 4;
    public static final int COMPUTE_OFFSET_ANY = COMPUTE_OFFSET_HOVER_LOCATION|COMPUTE_OFFSET_CURSOR_LOCATION|COMPUTE_OFFSET_CARET_LOCATION;

    private NSISTextUtility()
    {
    }

    public static void setupPartitioning(IDocument document)
    {
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3= (IDocumentExtension3) document;
            IDocumentPartitioner partitioner= new FastPartitioner(new NSISPartitionScanner(), NSISPartitionScanner.NSIS_PARTITION_TYPES);
            extension3.setDocumentPartitioner(NSISPartitionScanner.NSIS_PARTITIONING, partitioner);
            partitioner.connect(document);
        }
    }

    public static int computeOffset(ISourceViewer sourceViewer, int type)
    {
        if (!(sourceViewer instanceof ITextViewerExtension2)) {
            return -1;
        }

        ITextViewerExtension2 textViewerExtension2= (ITextViewerExtension2) sourceViewer;

        StyledText widget = sourceViewer.getTextWidget();
        int offset = -1;
        if((type & COMPUTE_OFFSET_HOVER_LOCATION) > 0) {
           // does a text hover exist?
            ITextHover textHover= textViewerExtension2.getCurrentTextHover();
            if (textHover == null) {
                offset = -1;
            }
            else {
                Point hoverEventLocation= textViewerExtension2.getHoverEventLocation();
                offset= computeOffsetAtLocation(sourceViewer, hoverEventLocation.x, hoverEventLocation.y);
            }
        }
        if(offset < 0 && (type & COMPUTE_OFFSET_CARET_LOCATION) > 0) {
            offset = widget.getCaretOffset();
            if(offset >= 0) {
                return offset;
            }
        }
        if(offset < 0 && (type & COMPUTE_OFFSET_CURSOR_LOCATION) > 0) {
            Point p = null;
            p = widget.toControl(widget.getDisplay().getCursorLocation());
            try {
                offset = computeOffsetAtLocation(sourceViewer, p.x, p.y);
            }
            catch (Exception e) {
                offset = -1;
            }
        }
        return offset;
    }

    public static int computeOffsetAtLocation(ISourceViewer sourceViewer, int x, int y)
    {
        StyledText styledText= sourceViewer.getTextWidget();
        IDocument document= sourceViewer.getDocument();

        if (document == null) {
            return -1;
        }

        try {
            int widgetLocation= styledText.getOffsetAtLocation(new Point(x, y));
            if (sourceViewer instanceof ITextViewerExtension5) {
                ITextViewerExtension5 extension= (ITextViewerExtension5) sourceViewer;
                return extension.widgetOffset2ModelOffset(widgetLocation);
            }
            else {
                IRegion visibleRegion= sourceViewer.getVisibleRegion();
                return widgetLocation + visibleRegion.getOffset();
            }
        }
        catch (IllegalArgumentException e) {
            return -1;
        }
    }

    public static IRegion intersection(IRegion region1, IRegion region2)
    {
        int start1 = region1.getOffset();
        int end1 = start1 + region1.getLength() - 1;
        int start2 = region2.getOffset();
        int end2 = start2 + region2.getLength() - 1;
        if(start1 >= 0 && end1 >= 0 && start2 >= 0 && end2 >= 0) {
            if(start1 < start2)
            {
                if(end1 >= start2) {
                    return (end2 <= end1 ? region2 : new Region(start2, (end1-start2+1)));
                }
            }
            else {
                if(start1 <= end2) {
                    return (end2 < end1 ? new Region(start1, (end2-start1+1)) : region1);
                }
            }

        }
        return EMPTY_REGION;
    }

    public static ITypedRegion[][] getNSISLines(IDocument doc)
    {
        return getNSISLines(doc, getNSISPartitions(doc));
    }

    public static boolean contains(IRegion region, int offset)
    {
        return (region !=null && offset >= region.getOffset() && offset < region.getOffset()+region.getLength());
    }

    public static int findRegion(IRegion[] regions, int offset)
    {
        if(!Common.isEmptyArray(regions)) {
            int low = 0;
            int high = regions.length-1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                IRegion midVal = regions[mid];
                if(contains(midVal,offset)) {
                    return mid;
                }
                else if (midVal.getOffset() > offset) {
                    high = mid - 1;
                }
                else if (midVal.getOffset() < offset) {
                    low = mid + 1;
                }
                else {
                    break;
                }
            }
        }
        return -1;
    }

    public static ITypedRegion[][] getNSISLines(IDocument doc, int offset)
    {
        ITypedRegion[] typedRegions = null;
        try {
            int linenum = doc.getLineOfOffset(offset);
            IRegion line = doc.getLineInformation(linenum);
            ITypedRegion typedRegion = getNSISPartitionAtOffset(doc, line.getOffset());
            if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                int startOffset = typedRegion.getOffset()+typedRegion.getLength();
                while(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                    if(contains(typedRegion,offset)) {
                        return new ITypedRegion[0][];
                    }
                    startOffset = typedRegion.getOffset()+typedRegion.getLength();
                    linenum = doc.getLineOfOffset(offset);
                    line = doc.getLineInformation(linenum);
                    typedRegion = getNSISPartitionAtOffset(doc, startOffset);
                }
                line = new Region(startOffset, line.getOffset()+line.getLength()-startOffset);
            }
            else {
                int linenum2 = linenum-1;
                while(linenum2 >= 0) {
                    IRegion line2 = doc.getLineInformation(linenum2);
                    int endOffset = line2.getOffset()+line2.getLength()-1;
                    if(endOffset >= 0) {
                        typedRegion = getNSISPartitionAtOffset(doc, endOffset);
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            break;
                        }
                        if(doc.get(endOffset,1).charAt(0)!=LINE_CONTINUATION_CHAR) {
                            break;
                        }
                        typedRegion = getNSISPartitionAtOffset(doc, line2.getOffset());
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            int startOffset = typedRegion.getOffset()+typedRegion.getLength();
                            line = new Region(startOffset, line.getOffset()+line.getLength()-startOffset);
                            break;
                        }
                        else {
                            line = new Region(line2.getOffset(), line.getOffset()+line.getLength()-line2.getOffset());
                            linenum2--;
                        }
                    }
                    else {
                        break;
                    }
                }
            }
            typedRegion = getNSISPartitionAtOffset(doc, line.getOffset()+line.getLength()-1);
            if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                if(contains(typedRegion,offset)) {
                    return new ITypedRegion[0][];
                }
                line = new Region(line.getOffset(),typedRegion.getOffset()-line.getOffset());
            }
            else {
                if(doc.get(line.getOffset()+line.getLength()-1,1).charAt(0)==LINE_CONTINUATION_CHAR) {
                    int linenum2 = linenum+1;
                    int numlines = doc.getNumberOfLines();
                    while(linenum2 < numlines) {
                        IRegion line2 = doc.getLineInformation(linenum2);
                        typedRegion = getNSISPartitionAtOffset(doc, line2.getOffset());
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            break;
                        }
                        int endOffset = line2.getOffset()+line2.getLength()-1;
                        typedRegion = getNSISPartitionAtOffset(doc, endOffset);
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            line = new Region(line.getOffset(),typedRegion.getOffset()-line.getOffset());
                            break;
                        }
                        else {
                            line = new Region(line.getOffset(), endOffset-line2.getOffset()+1);
                            if(doc.get(endOffset,1).charAt(0)!=LINE_CONTINUATION_CHAR) {
                                break;
                            }
                            else {
                                linenum2++;
                            }
                        }
                    }
                }
            }
            ITypedRegion[] partitions = getNSISPartitions(doc);
            if(!Common.isEmptyArray(partitions)) {
                int startIndex = findRegion(partitions,line.getOffset());
                if(startIndex >= 0) {
                    int endIndex = findRegion(partitions,line.getOffset()+line.getLength()-1);
                    if(endIndex >= 0 && endIndex >= startIndex) {
                        typedRegions = new ITypedRegion[endIndex-startIndex+1];
                        for(int i=startIndex; i<endIndex+1; i++) {
                            typedRegions[i-startIndex] = partitions[i];
                        }
                        typedRegions[0] = new TypedRegion(line.getOffset(),typedRegions[0].getOffset()+typedRegions[0].getLength()-line.getOffset(),typedRegions[0].getType());
                        endIndex -= startIndex;
                        typedRegions[endIndex] = new TypedRegion(typedRegions[endIndex].getOffset(),line.getOffset()+line.getLength()-typedRegions[endIndex].getOffset(),typedRegions[endIndex].getType());
                    }
                }
            }
        }
        catch (BadLocationException e) {
        }
        return getNSISLines(doc, typedRegions);
    }

    public static ITypedRegion[][] getNSISLines(IDocument doc, ITypedRegion[] typedRegions)
    {
        List<ITypedRegion[]> regions = new ArrayList<ITypedRegion[]>();
        if(doc != null && doc.getLength() > 0) {
            try {
                if(!Common.isEmptyArray(typedRegions)) {
                    String[] delims = doc.getLegalLineDelimiters().clone();
                    for(int i=0; i<delims.length; i++) {
                        delims[i] = "\\"+delims[i]; //$NON-NLS-1$
                    }

                    int firstLine = doc.getLineOfOffset(typedRegions[0].getOffset());
                    ITypedRegion lastRegion = typedRegions[typedRegions.length-1];
                    int lastLine = doc.getLineOfOffset(lastRegion.getOffset()+lastRegion.getLength()-1);
                    for(int i=firstLine, index = 0; index < typedRegions.length && i<= lastLine; i++) {
                        List<TypedRegion> lineRegions = new ArrayList<TypedRegion>();
                        IRegion line = doc.getLineInformation(i);
                        String lineDelim = doc.getLineDelimiter(i);
//                        int start = line.getOffset();
                        int start = Math.max(line.getOffset(),typedRegions[index].getOffset());
                        int end = line.getOffset() + line.getLength() - 1 + (lineDelim != null?lineDelim.length():0);

                        int partitionEnd = typedRegions[index].getOffset() + typedRegions[index].getLength() - 1;
                        String type = typedRegions[index].getType();
                        boolean validPartition = type.equals(NSISPartitionScanner.NSIS_STRING) ||
                                                 type.equals(IDocument.DEFAULT_CONTENT_TYPE);

                        while(end >= start) {
                            if(partitionEnd > end) {
                                if(validPartition) {
                                    String text = doc.get(start, (end-start+1));
                                    boolean found = false;
                                    for(int j=0; j<delims.length; j++) {
                                        if(text.endsWith(delims[j])) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(found) {
                                        i++;
                                        line = doc.getLineInformation(i);
                                        lineDelim = doc.getLineDelimiter(i);
                                        end = line.getOffset() + line.getLength() - 1 + (lineDelim != null?lineDelim.length():0);
                                    }
                                    else {
                                        lineRegions.add(new TypedRegion(start,(end-start+1),type));
                                        break;
                                    }
                                }
                                else {
                                    break;
                                }
                            }
                            else if (partitionEnd < end) {
                                if(validPartition) {
                                    lineRegions.add(new TypedRegion(start,(partitionEnd-start+1),type));
                                }
                                start = partitionEnd+1;
                                index++;
                                if(index < typedRegions.length) {
                                    partitionEnd = typedRegions[index].getOffset() + typedRegions[index].getLength() - 1;
                                    type = typedRegions[index].getType();
                                    validPartition = type.equals(NSISPartitionScanner.NSIS_STRING) ||
                                                     type.equals(IDocument.DEFAULT_CONTENT_TYPE);
                                }
                                else {
                                    break;
                                }
                            }
                            else {
                                if(validPartition) {
                                    lineRegions.add(new TypedRegion(start,(end-start+1),type));
                                }
                                index++;
                                break;
                            }
                        }

                        if(lineRegions.size() > 0) {
                            regions.add(lineRegions.toArray(new ITypedRegion[0]));
                        }
                    }
                }
            }
            catch(BadLocationException e) {
            }
        }
        return regions.toArray(new ITypedRegion[0][]);
    }

    /**
     * @param doc
     * @return
     * @throws BadLocationException
     */
    public static ITypedRegion[] getNSISPartitions(IDocument doc)
    {
        ITypedRegion[] typedRegions;
        try {
            if(doc != null) {
                if (doc instanceof IDocumentExtension3) {
                    try {
                        typedRegions = ((IDocumentExtension3)doc).computePartitioning(NSISPartitionScanner.NSIS_PARTITIONING,0, doc.getLength(),false);
                    }
                    catch (BadPartitioningException e) {
                        typedRegions = doc.computePartitioning(0, doc.getLength());
                    }
                }
                else {
                    typedRegions = doc.computePartitioning(0, doc.getLength());
                }
            }
            else {
                typedRegions = new ITypedRegion[0];
            }
        }
        catch(BadLocationException ex) {
            typedRegions = new ITypedRegion[0];
        }
        return typedRegions;
    }

    /**
     * @param regionType
     * @param partitionTypes
     * @return
     */
    private static boolean isValidRegionType(String regionType, String[] partitionTypes)
    {
        boolean found = false;
        for (int i = 0; i < partitionTypes.length; i++) {
            if(partitionTypes[i].equals(regionType)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * @param offset
     * @param doc
     * @param typedRegion
     * @return
     * @throws BadLocationException
     */
    public static ITypedRegion getNSISPartitionAtOffset(IDocument doc, int offset) throws BadLocationException
    {
        ITypedRegion typedRegion;
        if (doc instanceof IDocumentExtension3) {
            try {
                typedRegion = ((IDocumentExtension3)doc).getPartition(NSISPartitionScanner.NSIS_PARTITIONING,offset,false);
            }
            catch (BadPartitioningException e) {
                typedRegion = doc.getPartition(offset);
            }
        }
        else {
            typedRegion = doc.getPartition(offset);
        }
        return typedRegion;
    }

    public static boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean lineContinuationAllowed, boolean eofAllowed)
    {
        int c;
        int offset = ((NSISScanner)scanner).getOffset();
        for (int i= 1; i < sequence.length; i++) {
            c = scanner.read();
            if (c == ICharacterScanner.EOF && eofAllowed) {
                return true;
            }

            if (c == LINE_CONTINUATION_CHAR) {
                int c2 = scanner.read();
                if(delimitersDetected(scanner, c2)) {
                    if(lineContinuationAllowed) {
                        i--;
                        continue;
                    }
                    else {
                        unread(scanner, ((NSISScanner)scanner).getOffset() - offset);
                        return false;
                    }
                }
                else {
                    scanner.unread();
                }
            }

            if (c != sequence[i]) {
                unread(scanner, ((NSISScanner)scanner).getOffset() - offset);
                return false;
            }
        }

        return true;
    }

    public static void unread(ICharacterScanner scanner, int count)
    {
        for (int i= 0; i < count; i++) {
            scanner.unread();
        }
    }

    public static boolean delimitersDetected(ICharacterScanner scanner, int c)
    {
        char[][] delimiters= scanner.getLegalLineDelimiters();
        for (int i= 0; i < delimiters.length; i++) {
            if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i], false, true)) {
                return true;
            }
        }
        return false;
    }

    private static boolean stringEscapeSequencesDetected(ICharacterScanner scanner, int c, char[][] specialSequences, boolean lineContinuationAllowed)
    {
        for (int i= 0; i < specialSequences.length; i++) {
            if (c == specialSequences[i][0] && sequenceDetected(scanner, specialSequences[i], lineContinuationAllowed, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean stringEscapeSequencesDetected(ICharacterScanner scanner, int c)
    {
        if(!(stringEscapeSequencesDetected(scanner, c, QUOTE_ESCAPE_SEQUENCES, true))) {
            stringEscapeSequencesDetected(scanner, c, WHITESPACE_ESCAPE_SEQUENCES, false);
        }

        return true;
    }

    public static String getRegionText(IDocument document, IRegion region)
    {
        String text = null;
        if(document != null && region != null && region.getLength() > 0) {
            NSISTextProcessorRule rule = new NSISTextProcessorRule();
            NSISRegionScanner scanner = new NSISRegionScanner(document, region);
            rule.setTextProcessor(new DefaultTextProcessor());
            IToken token = rule.evaluate(scanner);
            text = (String)token.getData();
        }
        return text;
    }

    public static int insertTabString(StringBuffer buffer, int offsetInLine, int tabWidth)
    {
        if (tabWidth == 0) {
            buffer.append('\t');
            return 1;
        }
        else {
            int remainder= offsetInLine % tabWidth;
            remainder= tabWidth - remainder;
            for (int i= 0; i < remainder; i++) {
                buffer.append(' ');
            }
            return remainder;
        }
    }

    public static String flattenSyntaxStylesMap(Map<String,NSISSyntaxStyle> map)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmptyMap(map)) {
            Iterator<String> iter = map.keySet().iterator();
            String key = iter.next();
            NSISSyntaxStyle style = map.get(key);
            buf.append(key).append('#').append(style.toString());
            while(iter.hasNext()) {
                key = iter.next();
                style = map.get(key);
                buf.append('\u00FF').append(key).append('#').append(style.toString());
            }
        }
        return buf.toString();
    }

    public static Map<String, NSISSyntaxStyle> parseSyntaxStylesMap(String text)
    {
        Map<String, NSISSyntaxStyle> map = new LinkedHashMap<String, NSISSyntaxStyle>();
        String[] pairs = Common.tokenize(text,'\u00FF');
        if(!Common.isEmptyArray(pairs)) {
            for (int i = 0; i < pairs.length; i++) {
                String[] keyValue = Common.tokenize(pairs[i],'#');
                if(!Common.isEmptyArray(keyValue)) {
                    String key = keyValue[0];
                    if(keyValue.length > 1) {
                        try {
                            NSISSyntaxStyle style = NSISSyntaxStyle.parse(keyValue[1]);
                            map.put(key, style);
                            continue;
                        }
                        catch(Exception ex) {
                            EclipseNSISPlugin.getDefault().log(ex);
                        }
                    }
                    map.put(key,null);
                }
            }
        }
        return map;
    }

    public static void hookSourceViewer(final ISourceViewer viewer)
    {
        final StyledText textWidget = viewer.getTextWidget();

        final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
        textWidget.setFont(fontRegistry.get(JFaceResources.TEXT_FONT));
        final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(JFaceResources.TEXT_FONT)) {
                    textWidget.setFont(fontRegistry.get(JFaceResources.TEXT_FONT));
                }
            }
        };
        fontRegistry.addListener(fontListener);

        final Display display = textWidget.getDisplay();
        final HashMap<String, Color> map = new HashMap<String, Color>();
        final IPreferenceStore store = EditorsUI.getPreferenceStore();

        textWidget.setBackground(createColor(map, store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
                AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, display));
        textWidget.setForeground(createColor(map, store, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,
                AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, display));

        final IPropertyChangeListener colorListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)||
                   event.getProperty().equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
                    textWidget.setBackground(createColor(map, store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
                            AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, display));
                }
                else if(event.getProperty().equals(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND)||
                        event.getProperty().equals(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)) {
                    textWidget.setForeground(createColor(map, store, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,
                            AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, display));
                }
            }
        };
        store.addPropertyChangeListener(colorListener);
        textWidget.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                fontRegistry.removeListener(fontListener);
                store.removePropertyChangeListener(colorListener);
                for(Iterator<Color> iter=map.values().iterator(); iter.hasNext(); ) {
                    Color color = iter.next();
                    if(color != null && !color.isDisposed()) {
                        color.dispose();
                    }
                }
            }
        });
    }

    private static Color createColor(Map<String, Color> map, IPreferenceStore store, String key, String defaultKey, Display display)
    {
        if(!store.getBoolean(defaultKey)) {
            if (store.contains(key)) {
                RGB rgb= null;
                if (store.isDefault(key)) {
                    rgb= PreferenceConverter.getDefaultColor(store, key);
                }
                else {
                    rgb= PreferenceConverter.getColor(store, key);
                }
                Color color = new Color(display, rgb);
                Color oldColor = map.put(key,color);
                if(oldColor != null) {
                    oldColor.dispose();
                }
                return color;
            }
        }

        return null;
    }
}
