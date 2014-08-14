/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;

public class INIFile implements IDocumentListener, IINIContainer, IINIProblemConstants
{
    private static String STRING_CR = new String("\r"); //$NON-NLS-1$
    private static String STRING_LF = new String("\n"); //$NON-NLS-1$
    private static String STRING_CRLF = new String("\r\n"); //$NON-NLS-1$

    public static final int INIFILE_CONNECTED = 0;
    public static final int INIFILE_MODIFIED = 1;
    public static final int INIFILE_DISCONNECTED = 2;

    public static final String INIFILE_CATEGORY = "__installoptions_inifile"; //$NON-NLS-1$

    private List<INILine> mChildren = new ArrayList<INILine>();
    private IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(INIFILE_CATEGORY);
    private List<INILine> mLines = new ArrayList<INILine>();
    private List<IINIFileListener> mListeners = new ArrayList<IINIFileListener>();
    private int mChangeStartLine = -1;
    private int mChangeEndLine = -1;
    private IDocument mDocument = null;

    private boolean mDirty = false;
    private List<INIProblem> mProblems = new ArrayList<INIProblem>();
    private boolean mErrors = false;
    private boolean mWarnings = false;
    private boolean mUpdatingDocument = false;

    private int mValidateFixMode = INILine.VALIDATE_FIX_NONE;

    public boolean isDirty()
    {
        return mDirty;
    }

    public void setDirty(boolean dirty)
    {
        mDirty = dirty;
    }

    public INIFile copy()
    {
        INIFile copy = new INIFile();
        for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = iter.next().copy();
            copy.addChild(line);
        }
        return copy;
    }

    public void addListener(IINIFileListener listener)
    {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(IINIFileListener listener)
    {
        mListeners.remove(listener);
    }

    private void notifyListeners(int event)
    {
        IINIFileListener[] listeners = mListeners.toArray(new IINIFileListener[mListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].iniFileChanged(this, event);
        }
    }

    public INILine getLineAtOffset(int offset)
    {
        int start = 0;
        for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = iter.next();
            if(line instanceof INISection) {
                Position pos = ((INISection)line).getPosition();
                line = ((INISection)line).getLineAtOffset(offset);
                if(line != null) {
                    return line;
                }
                start += pos.length;
            }
            else {
                if(offset >= start && offset < start+line.getLength()) {
                    return line;
                }
                start += line.getLength();
            }
        }
        return null;
    }

    public Position getChildPosition(INILine child)
    {
        if(mChildren.contains(child)) {
            if(child instanceof INISection) {
                Position pos = ((INISection)child).getPosition();
                return new Position(pos.offset,child.getLength());
            }
            else {
                int offset = 0;
                for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
                    INILine line = iter.next();
                    if(line instanceof INISection) {
                        break;
                    }
                    else if(line == child) {
                        return new Position(offset, line.getLength());
                    }
                    else {
                        offset += line.getLength();
                    }
                }
            }
        }
        return null;
    }

    public void addChild(INILine line)
    {
        int index = mChildren.size();
        if(line instanceof INISection) {
            Position pos = ((INISection)line).getPosition();
            if(pos != null) {
                for(int i=0; i<mChildren.size(); i++) {
                    INILine child = mChildren.get(i);
                    if(child instanceof INISection) {
                        Position pos2 = ((INISection)child).getPosition();
                        if(pos2 == null || pos.getOffset() < pos2.getOffset() ||
                                (pos.getOffset()==pos2.getOffset() && pos.getLength() < pos2.getLength())) {
                            index = i;
                            break;
                        }
                    }
                }
            }
        }
        addChild(index,line);
    }

    public void addChild(int index, INILine line)
    {
        mChildren.add(index, line);
        line.setParent(this);
        setDirty(true);
    }

    public void removeChild(INILine line)
    {
        mChildren.remove(line);
        line.setParent(null);
        setDirty(true);
    }

    public List<INILine> getChildren()
    {
        return mChildren;
    }

    public INISection[] findSections(String name)
    {
        List<INILine> list = new ArrayList<INILine>();
        for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = iter.next();
            if(line instanceof INISection && ((INISection)line).getName().equalsIgnoreCase(name)) {
                list.add(line);
            }
        }
        return list.toArray(new INISection[0]);
    }

    public INISection[] getSections()
    {
        List<INILine> list = new ArrayList<INILine>();
        for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = iter.next();
            if(line instanceof INISection) {
                list.add(line);
            }
        }
        return list.toArray(new INISection[0]);
    }

    public IDocument getDocument()
    {
        return mDocument;
    }

    private static INIComment parseComment(String text, String delimiter)
    {
        if(text.trim().startsWith(";")) { //$NON-NLS-1$
            return new INIComment(text, delimiter);
        }
        return null;
    }

    private static INISection parseSection(String text, String delimiter)
    {
        if(text.trim().startsWith("[")) { //$NON-NLS-1$
            int m = text.indexOf('[');
            int n = text.indexOf(']',m+1);
            if(n > 0) {
                String name = text.substring(m+1,n).trim();
                if(Character.isLetter(name.charAt(0))) {
                    INISection section = new INISection(text, delimiter, name.trim());
                    return section;
                }
            }
        }
        return null;
    }

    private static INIKeyValue parseKeyValue(String text, String delimiter)
    {
        String text2 = text.trim();
        if(text2.length() > 0) {
            int n = text.indexOf('=');
            if (Character.isLetter(text2.charAt(0)) && n > 0) {
                INIKeyValue keyValue = new INIKeyValue(text, delimiter,
                                                       text.substring(0,n).trim(),
                                                       text.substring(n+1).trim());
                return keyValue;
            }
        }
        return null;
    }

    public void update()
    {
        update(INILine.VALIDATE_FIX_NONE);
    }

    public void update(int fixFlag)
    {
        mLines.clear();
        for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
            INILine child = iter.next();
            mLines.add(child);
            if(child instanceof INISection) {
                mLines.addAll(((INISection)child).getChildren());
            }
            child.update();
        }
        validate(fixFlag);
        setDirty(false);
    }

    public void updateDocument()
    {
        if(mDocument != null) {
            try {
                mUpdatingDocument = true;
                String content = toString();
                if(!Common.stringsAreEqual(content,mDocument.get())) {
                    mDocument.set(content);
                }
            }
            finally {
                mUpdatingDocument = false;
            }
        }
    }

    public void connect(IDocument doc)
    {
        if(mDocument != null) {
            disconnect(mDocument);
        }
        setDirty(true);
        mDocument = doc;
        doc.addPositionCategory(INIFILE_CATEGORY);
        doc.addDocumentListener(this);
        doc.addPositionUpdater(mPositionUpdater);
        int lineCount = doc.getNumberOfLines();
        IINIContainer container = this;
        List<INILine> lines = parseLines(doc,0,lineCount-1);
        for(Iterator<INILine> iter=lines.iterator(); iter.hasNext();) {
            INILine line = iter.next();
            mLines.add(line);
            if(line instanceof IINIContainer) {
                addChild(line);
                container = (IINIContainer)line;
            }
            else {
                container.addChild(line);
            }
        }
        validate();
        notifyListeners(INIFILE_CONNECTED);
    }

    public void save(File file)
    {
       BufferedWriter writer = null;
       try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(toString());
        }
        catch (IOException e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(writer);
        }
    }

    public static INIFile load(File file)
    {
        try {
            return load(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return null;
        }
    }

    public static INIFile load(Reader r)
    {
        INIFile iniFile = new INIFile();
        BufferedReader br = null;
        try {
            if(r instanceof BufferedReader) {
                br = (BufferedReader)r;
            }
            else {
                br = new BufferedReader(r);
            }
            String delimiter;
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            IINIContainer container = iniFile;
            int n = br.read();
            while(n != -1) {
                char c = (char)n;
                n = br.read();
                switch(c) {
                    case SWT.CR:
                        if((char)n != SWT.LF) {
                            delimiter = STRING_CR;
                        }
                        else {
                            delimiter = STRING_CRLF;
                            n = br.read();
                        }
                        break;
                    case SWT.LF:
                        delimiter = STRING_LF;
                        break;
                    default:
                        buf.append(c);
                        continue;
                }
                container = loadLine(iniFile, container, buf.toString(),delimiter);
                buf.setLength(0);
                delimiter = null;
            }
            if(buf.length() > 0) {
                container = loadLine(iniFile, container, buf.toString(),null);
            }
        }
        catch (Exception e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(br);
        }

        return iniFile;
    }

    private static IINIContainer loadLine(INIFile iniFile, IINIContainer container, String text, String delimiter)
    {
        IINIContainer container2 = container;
        INILine line = parse(text, delimiter);
        iniFile.mLines.add(line);
        if(line instanceof IINIContainer) {
            iniFile.addChild(line);
            container2 = (IINIContainer)line;
        }
        else {
            container2.addChild(line);
        }
        return container2;
    }

    private List<INILine> parseLines(IDocument doc, int startLine, int endLine)
    {
        List<INILine> lines = new ArrayList<INILine>();
        for(int i=startLine; i<= endLine; i++) {
            try {
                IRegion region = doc.getLineInformation(i);
                String text = doc.get(region.getOffset(),region.getLength());
                INILine line = parse(text,doc.getLineDelimiter(i));
                if(line instanceof INISection) {
                    Position pos = new Position(region.getOffset(),line.getLength());
                    ((INISection)line).setPosition(pos);
                    try {
                        doc.addPosition(INIFILE_CATEGORY,pos);
                    }
                    catch (BadPositionCategoryException e1) {
                        InstallOptionsPlugin.getDefault().log(e1);
                    }
                }
                lines.add(line);
            }
            catch (BadLocationException e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }
        return lines;
    }

    private static INILine parse(String text, String delimiter)
    {
        INILine line;
        line = parseComment(text, delimiter);
        if(line == null) {
            line = parseSection(text, delimiter);
            if(line == null) {
                line = parseKeyValue(text, delimiter);
                if(line == null) {
                    line = new INILine(text, delimiter);
                }
            }
        }
        return line;
    }

    public void disconnect(IDocument doc)
    {
        if(mDocument == doc) {
            mDocument = null;
            doc.removePositionUpdater(mPositionUpdater);
            doc.removeDocumentListener(this);
            if(doc.containsPositionCategory(INIFILE_CATEGORY)) {
                try {
                    doc.removePositionCategory(INIFILE_CATEGORY);
                }
                catch (BadPositionCategoryException e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
            mChildren.clear();
            mLines.clear();
            mProblems.clear();
            mErrors = false;
            mWarnings = false;
            setDirty(false);
            notifyListeners(INIFILE_DISCONNECTED);
        }
    }

    private int[] getLineRange(IDocument doc, int offset, int length)
    {
        int startLine = -1;
        int endLine = -1;
        try {
            startLine = doc.getLineOfOffset(offset);
            endLine = startLine;
            if(length > 0) {
                endLine = doc.getLineOfOffset(offset+length);
            }
        }
        catch (BadLocationException e) {
            startLine = -1;
            endLine = -1;
        }
        return new int[]{startLine,endLine};
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        if(!mUpdatingDocument) {
            mChangeStartLine = -1;
            mChangeEndLine = -1;
            int[] lineRange = getLineRange(event.getDocument(),event.getOffset(),event.getLength());
            mChangeStartLine = lineRange[0];
            mChangeEndLine = lineRange[1];
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event)
    {
        if(!mUpdatingDocument) {
            setDirty(true);
            int start = mChangeStartLine;
            int end = mChangeEndLine;
            for(int i=start; i <= end; i++) {
                INILine line = mLines.get(i);
                mLines.remove(i);
                line.getParent().removeChild(line);
                i--;
                end--;
            }
            int index = 0;
            int childIndex = 0;
            IINIContainer container = this;
            if(mChangeStartLine > 0) {
                INILine previous = mLines.get(mChangeStartLine-1);
                if(previous instanceof IINIContainer) {
                    childIndex = 0;
                    index = mChildren.indexOf(previous)+1;
                    container = (IINIContainer)previous;
                }
                else {
                    container = previous.getParent();
                    childIndex = container.getChildren().indexOf(previous)+1;
                    index = (container == this?childIndex:mChildren.indexOf(container)+1);
                }
            }

            String text = event.getText();
            int[] lineRange = getLineRange(event.getDocument(),event.getOffset(),(text==null?0:text.length()));
            List<INILine> newLines = parseLines(event.getDocument(),lineRange[0],lineRange[1]);
            for (Iterator<INILine> iter = newLines.iterator(); iter.hasNext();) {
                INILine line = iter.next();
                if(line instanceof IINIContainer) {
                    addChild(index++,line);
                    container = (IINIContainer)line;
                    childIndex = 0;
                }
                else {
                    if(container == this) {
                        addChild(index++,line);
                    }
                    else {
                        container.addChild(childIndex++,line);
                    }
                }
                mLines.add(mChangeStartLine++,line);
            }
            for(int i=mChangeStartLine; i<mLines.size(); i++) {
                INILine line = mLines.get(i);
                if(line instanceof IINIContainer) {
                    break;
                }
                else if(line.getParent() != container) {
                    line.getParent().removeChild(line);
                    container.addChild(line);
                }
            }
            notifyListeners(INIFILE_MODIFIED);
        }
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
            buf.append(iter.next());
        }
        return buf.toString();
    }

    public INISection findSection(int offset, int length)
    {
        INISection[] sections = getSections();
        if(!Common.isEmptyArray(sections)) {
            int low = 0;
            int high = sections.length-1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                INISection midVal = sections[mid];
                Position p = midVal.calculatePosition();
                if(p.includes(offset)) {
                    if(positionContains(p,offset,length)) {
                        return midVal;
                    }
                    break;
                }
                else if (p.getOffset() > offset) {
                    high = mid - 1;
                }
                else if (p.getOffset() < offset) {
                    low = mid + 1;
                }
                else {
                    break;
                }
            }
        }
        return null;
    }

    public int getValidateFixMode()
    {
        return mValidateFixMode;
    }

    public void setValidateFixMode(int validateFixMode)
    {
        mValidateFixMode = validateFixMode;
    }

    public void validate()
    {
        validate(false);
    }

    public void validate(boolean force)
    {
        validate(mValidateFixMode,force);
    }

    public void validate(int fixFlag)
    {
        validate(fixFlag, false);
    }

    private void addProblem(INIProblem problem)
    {
        if(!mProblems.contains(problem)) {
            mProblems.add(problem);
            mErrors = mErrors || INIProblem.TYPE_ERROR.equals(problem.getType());
            mWarnings = mWarnings || INIProblem.TYPE_WARNING.equals(problem.getType());
        }
    }

    public void validate(int fixFlag, boolean force)
    {
        if(mDirty || force) {
            mProblems.clear();
            mErrors = false;
            mWarnings = false;
            for (int i=0; i < mChildren.size(); i++) {
                mChildren.get(i).validate(fixFlag);
            }
            INISection[] sections = getSections();
            INISection[] fieldSections;
            int[] indexes;
            if(!Common.isEmptyArray(sections)) {
                Map<INISection, Integer> map = new HashMap<INISection, Integer>();
                for (int i = 0; i < sections.length; i++) {
                    Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(sections[i].getName());
                    if(m.matches()) {
                        map.put(sections[i],Integer.valueOf(m.group(1)));
                    }
                }
                List<Map.Entry<INISection, Integer>> entries = new ArrayList<Map.Entry<INISection, Integer>>(map.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<INISection, Integer>>(){
                    public int compare(Map.Entry<INISection, Integer> e1, Map.Entry<INISection, Integer> e2)
                    {
                        return (e1.getValue()).compareTo(e2.getValue());
                    }
                });
                fieldSections = new INISection[entries.size()];
                indexes = new int[entries.size()];
                int i=0;
                for(Map.Entry<INISection, Integer> entry : entries) {
                    fieldSections[i] = entry.getKey();
                    indexes[i] = (entry.getValue()).intValue();
                    i++;
                }
            }
            else {
                indexes = Common.EMPTY_INT_ARRAY;
                fieldSections = new INISection[0];
            }
            final int n = fieldSections.length;
            int numFields = -1;
            sections = findSections(InstallOptionsModel.SECTION_SETTINGS);
            final INISection settingsSection;
            if(sections.length == 0) {
                if(n > 0) {
                    INILine line = null;
                    int index = 0;
                    for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
                        INILine l = iter.next();
                        if(l instanceof INISection) {
                            break;
                        }
                        if(!(l instanceof INIComment)) {
                            line = l;
                            break;
                        }
                        index++;
                    }
                    sections = new INISection[] {new INISection()};
                    sections[0].setName(InstallOptionsModel.SECTION_SETTINGS);
                    final INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_NUMFIELDS);
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        keyValue.setValue(Integer.toString(n));

                        addChild(index,sections[0]);
                        sections[0].addChild(keyValue);
                        if(line == null) {
                            line = new INILine(""); //$NON-NLS-1$
                            sections[0].addChild(line);
                        }
                    }
                    else {
                        INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("settings.section.missing", //$NON-NLS-1$
                                                        new String[]{InstallOptionsModel.SECTION_SETTINGS}));
                        final INISection section = sections[0];
                        final INILine line2 = line;
                        problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.add.settings.section")) { //$NON-NLS-1$
                            @Override
                            protected INIProblemFix[] createFixes()
                            {
                                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                                buf.append(section.buildText(section.getName())).append(section.getDelimiter()==null?INSISConstants.LINE_SEPARATOR:section.getDelimiter());
                                buf.append(keyValue.buildText(Integer.toString(n))).append(section.getDelimiter()==null?INSISConstants.LINE_SEPARATOR:section.getDelimiter());
                                if(line2 != null) {
                                    buf.append(line2.getText()).append(line2.getDelimiter()==null?"":line2.getDelimiter()); //$NON-NLS-1$
                                }
                                else {
                                    buf.append(INSISConstants.LINE_SEPARATOR);
                                }
                                return new INIProblemFix[] {new INIProblemFix(line2, buf.toString())};
                            }
                        });
                        addProblem(problem);
                    }
                    settingsSection = sections[0];
                }
                else {
                    settingsSection = null;
                }
            }
            else {
                settingsSection = sections[0];
            }

            final INIKeyValue numFieldsKeyVal;
            if(sections.length > 0) {
                INIKeyValue[] keyValues = sections[0].findKeyValues(InstallOptionsModel.PROPERTY_NUMFIELDS);
                if(keyValues.length == 0) {
                    final INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_NUMFIELDS);
                    keyValues = new INIKeyValue[] {keyValue};
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        keyValues[0].setValue(Integer.toString(n));
                        sections[0].addChild(0,keyValues[0]);
                        int index = mLines.indexOf(sections[0]);
                        mLines.add(index+1,keyValues[0]);
                    }
                    else {
                        final INISection section = sections[0];
                        INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("numfields.section.missing", //$NON-NLS-1$
                                                                        new String[]{InstallOptionsModel.PROPERTY_NUMFIELDS}));
                        problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.add.numfields.value")) { //$NON-NLS-1$
                            @Override
                            protected INIProblemFix[] createFixes()
                            {
                                StringBuffer buf = new StringBuffer(section.getText()).append(section.getDelimiter()==null?INSISConstants.LINE_SEPARATOR:section.getDelimiter());
                                buf.append(keyValue.buildText(Integer.toString(n))).append(section.getDelimiter()==null?"":section.getDelimiter()); //$NON-NLS-1$
                                return new INIProblemFix[] {new INIProblemFix(section, buf.toString())};
                            }
                        });
                        sections[0].addProblem(problem);
                    }
                }

                if(keyValues.length > 0) {
                    numFieldsKeyVal = keyValues[0];
                    try {
                        numFields = Integer.parseInt(keyValues[0].getValue());
                    }
                    catch(Exception e) {
                        numFields = -1;
                    }
                    if(numFields != n) {
                        if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                            keyValues[0].setValue(Integer.toString(n));
                            keyValues[0].update();
                            numFields = n;
                        }
                        else {
                            INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("numfields.value.incorrect", //$NON-NLS-1$
                                                                                    new String[]{InstallOptionsModel.PROPERTY_NUMFIELDS,
                                                                                                 InstallOptionsModel.SECTION_FIELD_PREFIX}));
                            final INIKeyValue keyValue = keyValues[0];
                            problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.numfields.value")) { //$NON-NLS-1$
                                @Override
                                protected INIProblemFix[] createFixes()
                                {
                                    return new INIProblemFix[] {new INIProblemFix(keyValue, keyValue.buildText(Integer.toString(n))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                                }
                            });
                            keyValues[0].addProblem(problem);
                        }
                    }
                }
                else {
                    numFieldsKeyVal = null;
                }
            }
            else {
                numFieldsKeyVal = null;
            }
            final int bitsSize = Math.max(fieldSections.length,(indexes.length > 0?indexes[indexes.length-1]:0));
            final BitSet bits = new BitSet(bitsSize);
            for (int i=0; i< indexes.length; i++) {
                if(indexes[i] > 0) {
                    bits.set(indexes[i]-1);
                }
            }

            final Integer numFields2 = new Integer(numFields);
            for (int i=0; i<indexes.length; i++) {
                final int index = indexes[i];
                final int nextIndex = bits.nextClearBit(0)+1;
                if(numFields >= 0 && index > numFields) {
                    final INISection sec = fieldSections[i];
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        if(nextIndex > bitsSize) {
                            if(bits.get(index-1)) {
                                bits.clear(index-1);
                            }
                            removeChild(sec);
                        }
                        else {
                            sec.setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[] {new Integer(nextIndex)}));
                            if(bits.get(index-1)) {
                                bits.clear(index-1);
                            }
                            bits.set(nextIndex-1);
                            sec.update();
                        }
                    }
                    else {
                        INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("field.index.exceeding", //$NON-NLS-1$
                                                                    new Object[]{InstallOptionsModel.SECTION_FIELD_PREFIX,
                                                                                new Integer(indexes[i]),
                                                                                InstallOptionsModel.PROPERTY_NUMFIELDS,
                                                                                numFields2}));
                        problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString(nextIndex > bitsSize?"quick.fix.remove.field":"quick.fix.correct.field.index")) { //$NON-NLS-1$ //$NON-NLS-2$
                            @Override
                            protected INIProblemFix[] createFixes()
                            {
                                if(nextIndex > bitsSize) {
                                    List<INIProblemFix> fixes = new ArrayList<INIProblemFix>();
                                    List<INILine> children = sec.getChildren();
                                    if(!Common.isEmptyCollection(children)) {
                                        ListIterator<INILine> iter = children.listIterator(children.size());
                                        while(iter.hasPrevious()) {
                                            fixes.add(new INIProblemFix(iter.previous()));
                                        }
                                    }
                                    fixes.add(new INIProblemFix(sec));
                                    return fixes.toArray(new INIProblemFix[fixes.size()]);
                                }
                                else {
                                    String newName = InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[] {new Integer(nextIndex)});
                                    if(bits.get(index-1)) {
                                        bits.clear(index-1);
                                    }
                                    bits.set(nextIndex-1);
                                    return new INIProblemFix[] {new INIProblemFix(sec, sec.buildText(newName)+(sec.getDelimiter()==null?"":sec.getDelimiter()))}; //$NON-NLS-1$
                                }
                            }
                        });
                        sec.addProblem(problem);
                    }
                }
                else if(index > nextIndex) {
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        INISection sec = fieldSections[i];
                        sec.setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[] {new Integer(nextIndex)}));
                        if(bits.get(index-1)) {
                            bits.clear(index-1);
                        }
                        bits.set(nextIndex-1);
                        sec.update();
                    }
                }
            }
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) == 0) {
                int missing = 0;
                final StringBuffer missingBuf = new StringBuffer(""); //$NON-NLS-1$
                for(int i=0; i<bitsSize; i++) {
                    if(!bits.get(i)) {
                        if(missing > 0) {
                            missingBuf.append(","); //$NON-NLS-1$
                        }
                        missingBuf.append(i+1);
                        missing++;
                    }
                }
                if (missing > 0) {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("field.sections.missing", //$NON-NLS-1$
                            new Object[]{InstallOptionsModel.SECTION_FIELD_PREFIX, new Integer(missing), missingBuf.toString()}));
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.add.missing.fields")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            List<INIProblemFix> fixes = new ArrayList<INIProblemFix>();
                            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                            INILine lastLine = (mLines.size() == 0?null:mLines.get(mLines.size() - 1));
                            INILine previous = lastLine;
                            if (previous != null) {
                                buf.append(previous.getText());
                                if (!Common.isEmpty(previous.getText())) {
                                    buf.append(previous.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:previous.getDelimiter());
                                    previous = new INILine(""); //$NON-NLS-1$
                                    buf.append(previous.getText());
                                }
                            }
                            final Map<String,String> requiredSettings = InstallOptionsModel.INSTANCE.getControlRequiredSettings();
                            String[] indexes = Common.tokenize(missingBuf.toString(), ',', true);
                            for (int i = 0; i < indexes.length; i++) {
                                INISection section = new INISection();
                                section.setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[]{new Integer(indexes[i])}));
                                buf.append(previous == null || previous.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:previous.getDelimiter());
                                buf.append(section.buildText(section.getName()));
                                previous = section;
                                INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_TYPE);
                                buf.append(previous.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:previous.getDelimiter());
                                buf.append(keyValue.buildText(InstallOptionsModel.TYPE_UNKNOWN));
                                previous = keyValue;
                                for (Iterator<String> iter = requiredSettings.keySet().iterator(); iter.hasNext();) {
                                    String name = iter.next();
                                    keyValue = new INIKeyValue(name);
                                    buf.append(previous.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:previous.getDelimiter());
                                    buf.append(keyValue.buildText(requiredSettings.get(name)));
                                    previous = keyValue;
                                }
                                if (i < indexes.length - 1) {
                                    buf.append(previous.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:previous.getDelimiter());
                                    previous = new INILine(""); //$NON-NLS-1$
                                    buf.append(previous.getText());
                                }
                            }
                            fixes.add(new INIProblemFix(lastLine, buf.toString()));
                            int numFields = n + indexes.length;
                            if(numFields != numFields2.intValue()) {
                                if(settingsSection == null) {
                                    INISection section = new INISection();
                                    section.setName(InstallOptionsModel.SECTION_SETTINGS);
                                    INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_NUMFIELDS);
                                    buf = new StringBuffer(""); //$NON-NLS-1$
                                    buf.append(section.buildText(section.getName())).append(section.getDelimiter()==null?INSISConstants.LINE_SEPARATOR:section.getDelimiter());
                                    buf.append(keyValue.buildText(Integer.toString(n))).append(section.getDelimiter()==null?INSISConstants.LINE_SEPARATOR:section.getDelimiter());
                                    buf.append(INSISConstants.LINE_SEPARATOR);
                                    fixes.add(new INIProblemFix(null, buf.toString()));
                                }
                                else {
                                    if(numFieldsKeyVal == null) {
                                        INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_NUMFIELDS);
                                        buf = new StringBuffer(settingsSection.getText()).append(settingsSection.getDelimiter()==null?INSISConstants.LINE_SEPARATOR:settingsSection.getDelimiter());
                                        buf.append(keyValue.buildText(Integer.toString(n))).append(settingsSection.getDelimiter()==null?"":settingsSection.getDelimiter()); //$NON-NLS-1$
                                        fixes.add(new INIProblemFix(settingsSection, buf.toString()));
                                    }
                                    else {
                                        fixes.add(new INIProblemFix(numFieldsKeyVal, numFieldsKeyVal.buildText(Integer.toString(numFields))+(numFieldsKeyVal.getDelimiter()==null?"":numFieldsKeyVal.getDelimiter()))); //$NON-NLS-1$
                                    }
                                }
                            }
                            return fixes.toArray(new INIProblemFix[fixes.size()]);
                        }
                    });
                    mProblems.add(problem);
                }
            }
            if(force) {
                notifyListeners(INIFILE_MODIFIED);
            }
        }
    }

    public INIProblem[] getProblems()
    {
        List<INIProblem> problems = getProblems(true);
        return problems.toArray(new INIProblem[problems.size()]);
    }

    /**
     * @param recurse
     * @return
     */
    public List<INIProblem> getProblems(boolean recurse)
    {
        validate();
        List<INIProblem> problems = new ArrayList<INIProblem>(mProblems);
        if(recurse) {
            int n=1;
            INILine[] lines = mLines.toArray(new INILine[mLines.size()]);
            for (int i=0; i<lines.length; i++) {
                for (Iterator<INIProblem> iterator = lines[i].getProblems().iterator(); iterator.hasNext();) {
                    INIProblem problem = iterator.next();
                    problem.setLine(n);
                    problems.add(problem);
                }
                n++;
            }
        }
        return problems;
    }

    public boolean hasErrors()
    {
        validate();
        if(!mErrors) {
            for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
                if(iter.next().hasErrors()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean hasWarnings()
    {
        validate();
        if(!mWarnings) {
            for (Iterator<INILine> iter = mChildren.iterator(); iter.hasNext();) {
                if(iter.next().hasWarnings()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean positionContains(Position position, int offset, int length)
    {
        return (offset >= position.getOffset() && offset+length <= position.getOffset()+position.getLength());
    }
}
