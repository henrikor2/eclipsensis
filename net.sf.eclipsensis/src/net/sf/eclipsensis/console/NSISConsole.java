/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.ui.console.IOConsole
 *
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.*;
import org.eclipse.ui.part.IPageBookViewPage;

public class NSISConsole extends TextConsole implements INSISConsole, IPropertyChangeListener,
                                                        INSISPreferenceConstants, IDocumentListener
{
    public static final String TYPE = "net.sf.eclipsensis.console.NSISConsole"; //$NON-NLS-1$

    private Color mInfoColor;
    private Color mWarningColor;
    private Color mErrorColor;

    private Image mErrorImage;
    private Image mWarningImage;

    private int mAutoShowLevel;

    private IConsoleManager mConsoleManager;

    private NSISConsoleOutputStream mInfoStream;
    private NSISConsoleOutputStream mWarningStream;
    private NSISConsoleOutputStream mErrorStream;

    private NSISConsolePartitioner mPartitioner;
    private List<NSISConsoleLine> mPending = new ArrayList<NSISConsoleLine>();
    private boolean mVisible = false;
    private boolean mInitialized = false;
    private IPreferenceStore mPreferenceStore;

    private AnnotationModel mAnnotationModel;
    private int mOffset;
    private List<NSISConsoleAnnotation> mPendingAnnotations = new ArrayList<NSISConsoleAnnotation>();

    private IConsoleListener mLifecycleListener = new IConsoleListener() {
        public void consolesAdded(IConsole[] consoles)
        {
            for (int i = 0; i < consoles.length; i++) {
                IConsole console = consoles[i];
                if (console == NSISConsole.this) {
                    init();
                }
            }

        }
        public void consolesRemoved(IConsole[] consoles)
        {
            for (int i = 0; i < consoles.length; i++) {
                IConsole console = consoles[i];
                if (console == NSISConsole.this) {
                    mConsoleManager.removeConsoleListener(this);
                    dispose();
                }
            }
        }
    };

    public NSISConsole()
    {
        super(EclipseNSISPlugin.getResourceString("console.name"), TYPE, EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("nsis.icon")), true); //$NON-NLS-1$ //$NON-NLS-2$
        getDocument().addDocumentListener(this);
        mOffset = getDocument().getLength();
        mPreferenceStore = EclipseNSISPlugin.getDefault().getPreferenceStore();
        mConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
        mAutoShowLevel = NSISPreferences.getInstance().getAutoShowConsole();
        mPartitioner = new NSISConsolePartitioner(this);
        mPartitioner.connect(getDocument());
        mPreferenceStore.addPropertyChangeListener(this);
        mErrorImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("error.icon")); //$NON-NLS-1$
        mWarningImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("warning.icon")); //$NON-NLS-1$
        mAnnotationModel = new AnnotationModel();
    }

    public AnnotationModel getAnnotationModel()
    {
        return mAnnotationModel;
    }

    @Override
    protected void init()
    {
        super.init();
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                JFaceResources.getColorRegistry().addListener(NSISConsole.this);
                JFaceResources.getFontRegistry().addListener(NSISConsole.this);
                initializeStreams();
                dump();
            }
        });
    }

    public void documentAboutToBeChanged(DocumentEvent event)
    {
    }

    public void documentChanged(DocumentEvent event)
    {
        EclipseNSISPlugin.getDefault().getJobScheduler().scheduleUIJob(getClass(), EclipseNSISPlugin.getResourceString("console.annotations.job.name"), new IJobStatusRunnable() { //$NON-NLS-1$
            public IStatus run(IProgressMonitor monitor)
            {
                int offset = getDocument().getLength();
                for(ListIterator<NSISConsoleAnnotation> iter = mPendingAnnotations.listIterator(); iter.hasNext(); ) {
                    NSISConsoleAnnotation annotation = iter.next();
                    Position pos = annotation.getPosition();
                    if(pos.overlapsWith(0, offset) && !pos.includes(offset)) {
                        mAnnotationModel.addAnnotation(annotation, pos);
                        try {
                            addHyperlink(new ConsoleHyperlink(annotation.getLine()), pos.getOffset(), pos.getLength());
                        }
                        catch (BadLocationException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                        iter.remove();
                    }
                    else {
                        break;
                    }
                }
                return Status.OK_STATUS;
            }
        });
    }

    private void initializeStreams()
    {
        synchronized(mPending) {
            if (!mInitialized) {
                mInfoStream = new NSISConsoleOutputStream(this);
                mErrorStream = new NSISConsoleOutputStream(this);
                mWarningStream = new NSISConsoleOutputStream(this);
                // install colors
                mInfoColor = createColor(CONSOLE_INFO_COLOR);
                mInfoStream.setColor(mInfoColor);
                mWarningColor = createColor(CONSOLE_WARNING_COLOR);
                mWarningStream.setColor(mWarningColor);
                mErrorColor = createColor(CONSOLE_ERROR_COLOR);
                mErrorStream.setColor(mErrorColor);
                // install font
                Font f = JFaceResources.getFontRegistry().get(CONSOLE_FONT);
                setFont(f);
                mInitialized = true;
            }
        }
    }

    private void dump()
    {
        synchronized(mPending) {
            mVisible = true;
            for (Iterator<NSISConsoleLine> iter = mPending.iterator(); iter.hasNext();) {
                NSISConsoleLine line = iter.next();
                appendLine(line);
                iter.remove();
            }
        }
    }

    public void appendLine(NSISConsoleLine line)
    {
        if((mAutoShowLevel & line.getType()) > 0) {
            show();
        }
        synchronized(mPending) {
            if(mVisible) {
                Image image = null;
                String text = line.toString();
                int length = text.length();
                switch(line.getType()) {
                    case NSISConsoleLine.TYPE_INFO:
                        mInfoStream.println(text);
                        break;
                    case NSISConsoleLine.TYPE_WARNING:
                        mWarningStream.println(text);
                        image = mWarningImage;
                        break;
                    case NSISConsoleLine.TYPE_ERROR:
                        mErrorStream.println(text);
                        image = mErrorImage;
                        break;
                }
                if(line.getSource() != null && line.getLineNum() > 0) {
                    NSISConsoleAnnotation annotation = new NSISConsoleAnnotation(image,new Position(mOffset, length),
                                                                                 line);
                    mPendingAnnotations.add(annotation);
                }
                mOffset += (length + 1); // + 1 for the LF
            }
            else {
                mPending.add(line);
            }
        }
    }

    @Override
    protected void dispose()
    {
        // Here we can't call super.dispose() because we actually want the partitioner to remain
        // connected, but we won't show lines until the console is added to the console manager
        // again.

        // Called when console is removed from the console view
        synchronized (mPending) {
            mVisible = false;
            JFaceResources.getColorRegistry().removeListener(this);
            JFaceResources.getFontRegistry().removeListener(this);
        }
    }

    public void shutdown()
    {
        super.dispose();
        getDocument().removeDocumentListener(this);
        mPreferenceStore.removePropertyChangeListener(this);
        IOUtility.closeIO(mInfoStream);
        IOUtility.closeIO(mWarningStream);
        IOUtility.closeIO(mErrorStream);
        mPartitioner.streamsClosed();
        mPartitioner.disconnect();
        if (mInfoColor != null) {
            mInfoColor.dispose();
        }
        if (mWarningColor != null) {
            mWarningColor.dispose();
        }
        if (mErrorColor != null) {
            mErrorColor.dispose();
        }
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        String property = event.getProperty();
        // colors
        if (mVisible) {
            if (property.equals(CONSOLE_INFO_COLOR)) {
                Color newColor = createColor(CONSOLE_INFO_COLOR);
                mInfoStream.setColor(newColor);
                mInfoColor.dispose();
                mInfoColor = newColor;
            }
            else if (property.equals(CONSOLE_WARNING_COLOR)) {
                Color newColor = createColor(CONSOLE_WARNING_COLOR);
                mWarningStream.setColor(newColor);
                mWarningColor.dispose();
                mWarningColor = newColor;
            }
            else if (property.equals(CONSOLE_ERROR_COLOR)) {
                Color newColor = createColor(CONSOLE_ERROR_COLOR);
                mErrorStream.setColor(newColor);
                mErrorColor.dispose();
                mErrorColor = newColor;

            } // font
            else if (property.equals(CONSOLE_FONT)) {
                setFont(((FontRegistry) event.getSource()).get(CONSOLE_FONT));
            }
        }
        if (property.equals(AUTO_SHOW_CONSOLE)) {
            mAutoShowLevel = NSISPreferences.getInstance().getAutoShowConsole();
        }
    }

    private Color createColor(String preference)
    {
        RGB rgb = JFaceResources.getColorRegistry().getRGB(preference);
        return new Color(Display.getDefault(), rgb);
    }

    public void show()
    {
        if(!mVisible) {
            NSISConsoleFactory.showConsole();
        }
        else {
            mConsoleManager.showConsoleView(this);
        }
    }

    @Override
    public IPageBookViewPage createPage(IConsoleView view)
    {
        return new NSISConsolePage(this, view);
    }

    @Override
    protected IConsoleDocumentPartitioner getPartitioner()
    {
        return mPartitioner;
    }

    public int getHighWaterMark()
    {
        return mPartitioner.getHighWaterMark();
    }

    public int getLowWaterMark()
    {
        return mPartitioner.getLowWaterMark();
    }

    public void setWaterMarks(int low, int high)
    {
        if (low >= 0) {
            if (low >= high) {
                throw new IllegalArgumentException(EclipseNSISPlugin.getResourceString("partitioner.watermarks.error")); //$NON-NLS-1$
            }
        }
        mPartitioner.setWaterMarks(low, high);
    }

    @Override
    public void clearConsole()
    {
        synchronized (mPending) {
            if (mPartitioner != null) {
                mPartitioner.clearBuffer();
                mOffset = 0;
                mPendingAnnotations.clear();
            }
        }
    }

    public IConsoleListener getLifecycleListener()
    {
        return mLifecycleListener;
    }

    private class ConsoleHyperlink implements IHyperlink
    {
        private NSISConsoleLine mLine;

        public ConsoleHyperlink(NSISConsoleLine line)
        {
            mLine = line;
        }

        public void linkEntered()
        {
        }

        public void linkExited()
        {
        }

        public void linkActivated()
        {
            NSISEditorUtilities.gotoConsoleLineProblem(mLine);
        }
    }
}