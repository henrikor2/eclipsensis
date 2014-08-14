/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.ui.internal.console.IOConsoleViewer
 *
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.console.*;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

@SuppressWarnings("restriction")
public class NSISConsoleViewer extends TextConsoleViewer
{
    private static final int REVEAL_SCHEDULE_DELAY = 50;
    private static final int VERTICAL_RULER_WIDTH = 12;
    private static final int VERTICAL_RULER_GAP = 1;

    private boolean mAutoScroll = true;
    private IDocumentListener mDocumentListener;
    private CompositeRuler mRuler;
    private Composite mComposite;
    private AnnotationModel mAnnotationModel;
    private Cursor mHitDetectionCursor;
    private WorkbenchJob mRevealJob;
    private IPropertyChangeListener mPropertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if(property.equals(INSISPreferenceConstants.CONSOLE_ERROR_COLOR) ||
               property.equals(INSISPreferenceConstants.CONSOLE_WARNING_COLOR) ||
               property.equals(INSISPreferenceConstants.CONSOLE_INFO_COLOR)) {
                StyledText textWidget = getTextWidget();
                if(textWidget != null) {
                    textWidget.redraw();
                }
            }
            else if(property.equals(INSISPreferenceConstants.CONSOLE_FONT)) {
                if(mRuler != null) {
                    mRuler.getControl().redraw();
                }
            }
        }
    };

    public NSISConsoleViewer(Composite parent, NSISConsole console)
    {
        super(parent, console);
        mAnnotationModel = console.getAnnotationModel();
        mRuler.setModel(mAnnotationModel);
        mAnnotationModel.connect(getDocument());
        StyledText text = getTextWidget();
        if (text != null) {
            text.setEditable(false);
        }
        JFaceResources.getColorRegistry().addListener(mPropertyChangeListener);
        JFaceResources.getFontRegistry().addListener(mPropertyChangeListener);
    }

    private WorkbenchJob getRevealJob()
    {
        if(mRevealJob == null) {
            mRevealJob = new WorkbenchJob(EclipseNSISPlugin.getResourceString("console.reveal.job.name")){ //$NON-NLS-1$
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    StyledText textWidget = getTextWidget();
                    if (textWidget != null) {
                        int lineCount = textWidget.getLineCount();
                        setTopIndex(lineCount-1);
                    }
                    return Status.OK_STATUS;
                }
            };
            mRevealJob.setSystem(true);
        }
        return mRevealJob;
    }

    @Override
    protected void revealEndOfDocument()
    {
        getRevealJob().schedule(REVEAL_SCHEDULE_DELAY);
    }

    @Override
    protected void createControl(Composite parent, int styles)
    {
        mHitDetectionCursor= parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
        mRuler = new CompositeRuler(VERTICAL_RULER_WIDTH);
        AnnotationRulerColumn rulerColumn = new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, new DefaultMarkerAnnotationAccess()) {
            @Override
            protected void doPaint(GC gc)
            {
                try {
                    super.doPaint(gc);
                }
                catch(Exception ex) {
                }
            }

            @Override
            protected void doPaint1(GC gc)
            {
                try {
                    super.doPaint1(gc);
                }
                catch (Exception ex) {
                }
            }

        };
        rulerColumn.addAnnotationType(NSISConsoleAnnotation.TYPE);
        //rulerColumn
        mRuler.addDecorator(0, rulerColumn);
        int styles2 = (styles & ~SWT.BORDER);
        mComposite= new Canvas(parent, SWT.NONE);
        mComposite.setLayout(new RulerLayout(VERTICAL_RULER_GAP));
        Composite parent2 = mComposite;
        super.createControl(parent2, styles2);
        mRuler.createControl(mComposite, this);
        mRuler.getControl().setBackground(getTextWidget().getBackground());
        rulerColumn.getControl().setBackground(getTextWidget().getBackground());
        mRuler.getControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e)
            {
                NSISConsoleAnnotation a = getAnnotation(e.y);
                if(a != null) {
                    NSISEditorUtilities.gotoConsoleLineProblem(a.getLine());
                }
            }
        });
        mRuler.getControl().addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e)
            {
                if(hasAnnotation(e.y)) {
                    mRuler.getControl().setCursor(mHitDetectionCursor);
                }
                else {
                    mRuler.getControl().setCursor(null);
                }

            }
        });
    }

    private NSISConsoleAnnotation getAnnotation(int y)
    {
        try {
            IDocument document= getDocument();
            int lineNumber = mRuler.toDocumentLineNumber(y);
            IRegion info= document.getLineInformation(lineNumber);

            if (mAnnotationModel != null) {
                for(Iterator<?> iter= mAnnotationModel.getAnnotationIterator(); iter.hasNext(); ) {
                    Annotation a= (Annotation) iter.next();
                    Position p= mAnnotationModel.getPosition(a);
                    if (p != null && p.overlapsWith(info.getOffset(), info.getLength())) {
                        if(a instanceof NSISConsoleAnnotation) {
                            return (NSISConsoleAnnotation)a;
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
        }
        return null;
    }

    private boolean hasAnnotation(int y)
    {
        return getAnnotation(y) != null;
    }

    public boolean isAutoScroll()
    {
        return mAutoScroll;
    }

    public void setAutoScroll(boolean scroll)
    {
        mAutoScroll = scroll;
    }

    @Override
    public Control getControl()
    {
        return mComposite;
    }

    @Override
    public void setDocument(IDocument document)
    {
        IDocument oldDocument= getDocument();

        if (oldDocument != null) {
            oldDocument.removeDocumentListener(getDocumentListener());
        }

        super.setDocument(document);

        if (oldDocument != null && mAnnotationModel != null) {
            mAnnotationModel.disconnect(oldDocument);
        }

        if (document != null) {
            document.addDocumentListener(getDocumentListener());
            if(mAnnotationModel != null) {
                mAnnotationModel.connect(document);
            }
            revealEndOfDocument();
        }
    }

    @Override
    protected void handleDispose()
    {
        JFaceResources.getColorRegistry().removeListener(mPropertyChangeListener);
        JFaceResources.getFontRegistry().removeListener(mPropertyChangeListener);
        if (getDocument() != null) {
            mAnnotationModel.disconnect(getDocument());
        }
        mAnnotationModel= null;
        mRuler = null;
        mComposite = null;
        super.handleDispose();
    }

    private IDocumentListener getDocumentListener()
    {
        if (mDocumentListener == null) {
            mDocumentListener= new IDocumentListener() {
                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                public void documentChanged(DocumentEvent event) {
                    if (mAutoScroll) {
                        revealEndOfDocument();
                    }
                }
            };
        }
        return mDocumentListener;
    }

    //Need to replace the following 3 methods because I want my own link colors.
    @Override
    public void lineGetStyle(LineStyleEvent event)
    {
        IDocument document = getDocument();
        if (document != null && document.getLength() > 0) {
            List<StyleRange> ranges = new ArrayList<StyleRange>();
            int offset = event.lineOffset;
            int length = event.lineText.length();

            StyleRange[] partitionerStyles = ((IConsoleDocumentPartitioner) document.getDocumentPartitioner()).getStyleRanges(event.lineOffset, event.lineText.length());
            if (partitionerStyles != null) {
                for (int i = 0; i < partitionerStyles.length; i++) {
                    ranges.add(partitionerStyles[i]);
                }
            }

            try {
                Position[] positions = getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
                Position[] overlap = findPosition(offset, length, positions);
                if (overlap != null) {
                    Color color = JFaceResources.getColorRegistry().get(JFacePreferences.HYPERLINK_COLOR);
                    for (int i = 0; i < overlap.length; i++) {
                        Position position = overlap[i];
                        StyleRange linkRange = new StyleRange(position.offset, position.length, color, null);
                        linkRange.underline = true;
                        override(ranges, linkRange);
                    }
                }
            } catch (BadPositionCategoryException e) {
            }

            if (ranges.size() > 0) {
                event.styles = ranges.toArray(new StyleRange[ranges.size()]);
            }
        }
    }

    private void override(List<StyleRange> ranges, StyleRange newRange)
    {
        if (ranges.isEmpty()) {
            ranges.add(newRange);
            return;
        }

        List<StyleRange> newRanges = new ArrayList<StyleRange>();
        newRanges.add(newRange);
        for (ListIterator<StyleRange> iter=ranges.listIterator(); iter.hasNext(); ) {
            if(newRanges.size() > 0) {
                StyleRange existingRange = iter.next();
                for(ListIterator<StyleRange> iter2=newRanges.listIterator(); iter2.hasNext(); ) {
                    StyleRange newRange2 = iter2.next();
                    int offset1 = existingRange.start;
                    int offset2 = newRange2.start;
                    int end1 = offset1 + existingRange.length - 1;
                    int end2 = offset2 + newRange2.length - 1;
                    if(offset1 >= 0 && end1 >= offset1 && offset2 >= 0 && end2 >= offset2) {
                        if(offset1 == offset2 && end1 == end2) {
                            newRange2.foreground = existingRange.foreground;
                            iter2.remove();
                            iter.set(newRange2);
                            continue;
                        }
                        if(offset1 < offset2)
                        {
                            if(end1 >= offset2) {
                                existingRange.length = offset2-offset1;
                                if(end1 >= end2) {
                                    iter2.remove();
                                    if(end1 > end2) {
                                        StyleRange range2 = (StyleRange)existingRange.clone();
                                        range2.start = end2+1;
                                        range2.length = end1-end2;
                                        iter.add(range2);
                                    }
                                }
                                else {
                                    StyleRange range2 = (StyleRange)newRange2.clone();
                                    range2.start=end1+1;
                                    range2.length=end2-end1;
                                    iter2.set(range2);
                                    newRange2.length = end1-offset2+1;
                                }
                                newRange2.foreground = existingRange.foreground;
                                iter.add(newRange2);
                            }
                        }
                        else if(offset1 == offset2){
                            if(end1 < end2) {
                                StyleRange range2 = (StyleRange)newRange2.clone();
                                newRange2.length=end1-offset1+1;
                                newRange2.foreground = existingRange.foreground;
                                iter.set(newRange2);
                                range2.start = end1+1;
                                range2.length = end2-end1;
                                iter2.set(range2);
                            }
                            else {
                                iter2.remove();
                                newRange2.foreground = existingRange.foreground;
                                iter.set(newRange2);
                                existingRange.start = end2+1;
                                existingRange.length = end1-end2;
                                iter.add(existingRange);
                            }
                        }
                        else {
                            if(offset1 <= end2) {
                                newRange2.length = offset1-offset2+1;
                                if(end2 < end1) {
                                    StyleRange range2 = (StyleRange)newRange2.clone();
                                    range2.start=offset1;
                                    range2.length=end2-offset1+1;
                                    range2.foreground = existingRange.foreground;
                                    iter.add(range2);
                                    existingRange.start=end2+1;
                                    existingRange.length=end1-end2;
                                }
                                else {
                                    StyleRange range2 = (StyleRange)newRange2.clone();
                                    range2.start=offset1;
                                    range2.length=end1-offset1+1;
                                    range2.foreground = existingRange.foreground;
                                    iter.set(range2);
                                    if(end2 > end1) {
                                        range2 = (StyleRange)newRange2.clone();
                                        range2.start=end1+1;
                                        range2.length=end2-end1;
                                        iter2.add(range2);
                                    }
                                }
                            }
                        }

                    }
                }
            }
            else {
                break;
            }
        }
        ranges.addAll(newRanges);
    }

    private Position[] findPosition(int offset, int length, Position[] positions)
    {
        if (positions.length == 0) {
            return null;
        }

        int rangeEnd = offset + length;
        int left= 0;
        int right= positions.length - 1;
        int mid= 0;
        Position position= null;

        while (left < right) {

            mid= (left + right) / 2;

            position= positions[mid];
            if (rangeEnd < position.getOffset()) {
                if (left == mid) {
                    right= left;
                }
                else {
                    right= mid -1;
                }
            } else if (offset > (position.getOffset() + position.getLength() - 1)) {
                if (right == mid) {
                    left= right;
                }
                else {
                    left= mid  +1;
                }
            } else {
                left= right= mid;
            }
        }


        List<Position> list = new ArrayList<Position>();
        int index = left - 1;
        if (index >= 0) {
            position= positions[index];
            while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
                index--;
                if (index > 0) {
                    position= positions[index];
                }
            }
        }
        index++;
        position= positions[index];
        while (index < positions.length && (position.getOffset() < rangeEnd)) {
            list.add(position);
            index++;
            if (index < positions.length) {
                position= positions[index];
            }
        }

        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new Position[list.size()]);
    }

    protected class RulerLayout extends Layout
    {
        protected int mGap;

        public RulerLayout(int gap)
        {
            mGap= gap;
        }

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache)
        {
            Control[] children= composite.getChildren();
            Point s= children[children.length - 1].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            if (mRuler != null) {
                s.x += mRuler.getWidth() + mGap;
            }
            return s;
        }

        @Override
        protected void layout(Composite composite, boolean flushCache)
        {
            Rectangle clArea= composite.getClientArea();
            Rectangle trim= getTextWidget().computeTrim(0, 0, 0, 0);
            int topTrim= - trim.y;
            int scrollbarHeight= trim.height - topTrim; // scrollbar is only under the client area

            int x= clArea.x;
            int width= clArea.width;

            if (mRuler != null) {
                int verticalRulerWidth= mRuler.getWidth();
                mRuler.getControl().setBounds(clArea.x, clArea.y + topTrim, verticalRulerWidth, clArea.height - scrollbarHeight - topTrim);

                x += verticalRulerWidth + mGap;
                width -= verticalRulerWidth + mGap;
            }

            getTextWidget().setBounds(x, clArea.y, width, clArea.height);
        }
    }
}
