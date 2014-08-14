/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.ui.internal.console.IOConsolePartitioner
 *
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.*;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.*;
import org.eclipse.ui.progress.WorkbenchJob;

public class NSISConsolePartitioner implements IConsoleDocumentPartitioner, IDocumentPartitionerExtension
{
    private PendingPartition mConsoleClosedPartition;
    private IDocument mDocument;
    private List<NSISConsolePartition> mPartitions;
    private List<PendingPartition> mPendingPartitions;
    private List<PendingPartition> mUpdatePartitions;
    private NSISConsolePartition mLastPartition;
    private QueueProcessingJob mQueueJob;
    private boolean mUpdateInProgress;
    private int mFirstOffset;
    private int mHighWaterMark = -1;
    private int mLowWaterMark = -1;
    private boolean mConnected = false;
    private NSISConsole mConsole;
    private TrimJob mTrimJob = new TrimJob();
    private Object mOverflowLock = new Object();
    private int mBuffer;

    public NSISConsolePartitioner(NSISConsole console)
    {
        this.mConsole = console;
        mTrimJob.setRule(console.getSchedulingRule());
    }

    public IDocument getDocument()
    {
        return mDocument;
    }

    public void connect(IDocument doc)
    {
        mDocument = doc;
        mDocument.setDocumentPartitioner(this);
        mPartitions = new ArrayList<NSISConsolePartition>();
        mPendingPartitions = new ArrayList<PendingPartition>();
        mQueueJob = new QueueProcessingJob();
        mQueueJob.setSystem(true);
        mQueueJob.setPriority(Job.INTERACTIVE);
        mQueueJob.setRule(mConsole.getSchedulingRule());
        mConnected = true;
    }

    public int getHighWaterMark()
    {
        return mHighWaterMark;
    }

    public int getLowWaterMark()
    {
        return mLowWaterMark;
    }

    public void setWaterMarks(int low, int high)
    {
        mLowWaterMark = low;
        mHighWaterMark = high;
        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                checkBufferSize();
            }
        });
    }

    public void streamsClosed()
    {
        mConsoleClosedPartition = new PendingPartition(null, null);
        synchronized (mPendingPartitions) {
            mPendingPartitions.add(mConsoleClosedPartition);
        }
        mQueueJob.schedule(); //ensure that all pending partitions are processed.
    }

    public void disconnect()
    {
        synchronized (mOverflowLock) {
            mDocument = null;
            mPartitions.clear();
            mConnected = false;
        }
    }

    public void documentAboutToBeChanged(DocumentEvent event)
    {
    }

    public boolean documentChanged(DocumentEvent event)
    {
        return documentChanged2(event) != null;
    }

    public String[] getLegalContentTypes()
    {
        return new String[] { NSISConsolePartition.PARTITION_TYPE };
    }

    public String getContentType(int offset)
    {
        return getPartition(offset).getType();
    }

    public ITypedRegion[] computePartitioning(int offset, int length)
    {
        int rangeEnd = offset + length;
        int left= 0;
        int right= mPartitions.size() - 1;
        int mid= 0;
        NSISConsolePartition position= null;
        if (left == right) {
            return new NSISConsolePartition[]{mPartitions.get(0)};
        }
        while (left < right) {
            mid= (left + right) / 2;
            position= mPartitions.get(mid);
            if (rangeEnd < position.getOffset()) {
                if (left == mid) {
                    right= left;
                }
                else {
                    right= mid -1;
                }
            }
            else if (offset > (position.getOffset() + position.getLength() - 1)) {
                if (right == mid) {
                    left= right;
                }
                else {
                    left= mid  +1;
                }
            }
            else {
                left= right= mid;
            }
        }
        List<NSISConsolePartition> list = new ArrayList<NSISConsolePartition>();
        int index = left - 1;
        if (index >= 0) {
            position= mPartitions.get(index);
            while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
                index--;
                if (index >= 0) {
                    position= mPartitions.get(index);
                }
            }
        }
        index++;
        position= mPartitions.get(index);
        while (index < mPartitions.size() && (position.getOffset() < rangeEnd)) {
            list.add(position);
            index++;
            if (index < mPartitions.size()) {
                position= mPartitions.get(index);
            }
        }
        return list.toArray(new NSISConsolePartition[list.size()]);
    }

    public ITypedRegion getPartition(int offset)
    {
        for (int i = 0; i < mPartitions.size(); i++) {
            ITypedRegion partition = mPartitions.get(i);
            int start = partition.getOffset();
            int end = start + partition.getLength();
            if (offset >= start && offset < end) {
                return partition;
            }
        }
        return mLastPartition;
    }

    private void checkBufferSize()
    {
        if (mDocument != null && mHighWaterMark > 0) {
            int length = mDocument.getLength();
            if (length > mHighWaterMark) {
                if (mTrimJob.getState() == Job.NONE) { //if the job isn't already running
                    mTrimJob.setOffset(length - mLowWaterMark);
                    mTrimJob.schedule();
                }
            }
        }
    }

    public void clearBuffer()
    {
        synchronized (mOverflowLock) {
            mTrimJob.setOffset(-1);
            mTrimJob.schedule();
        }
    }

    public IRegion documentChanged2(DocumentEvent event)
    {
        if (mDocument == null) {
            return null; //another thread disconnected the partitioner
        }
        if (mDocument.getLength() == 0) { //document cleared
            mPartitions.clear();
            mPendingPartitions.clear();
            mLastPartition = null;
            return new Region(0, 0);
        }
        if (mUpdateInProgress) {
            synchronized(mPartitions) {
                if (mUpdatePartitions != null) {
                    for (Iterator<PendingPartition> i = mUpdatePartitions.iterator(); i.hasNext(); ) {
                        PendingPartition pp = i.next();
                        if (pp == mConsoleClosedPartition) {
                            continue;
                        }
                        int ppLen = pp.mText.length();
                        if (mLastPartition != null && mLastPartition.getStream() == pp.mStream) {
                            int len = mLastPartition.getLength();
                            mLastPartition.setLength(len + ppLen);
                        }
                        else {
                            NSISConsolePartition partition = new NSISConsolePartition(pp.mStream, ppLen);
                            partition.setOffset(mFirstOffset);
                            mLastPartition = partition;
                            mPartitions.add(partition);
                        }
                        mFirstOffset += ppLen;
                    }
                }
            }
        }
        return new Region(event.fOffset, event.fText.length());
    }

    private void setUpdateInProgress(boolean b)
    {
        mUpdateInProgress = b;
    }

    public void streamAppended(NSISConsoleOutputStream stream, String s) throws IOException
    {
        if (mDocument == null) {
            throw new IOException(EclipseNSISPlugin.getResourceString("console.document.closed.error")); //$NON-NLS-1$
        }

        synchronized(mPendingPartitions)
        {
            PendingPartition last = (mPendingPartitions.size() > 0 ? mPendingPartitions.get(mPendingPartitions.size()-1) : null);
            if (last != null && last.mStream == stream) {
                last.append(s);
            }
            else {
                mPendingPartitions.add(new PendingPartition(stream, s));
                if (mBuffer > 1000) {
                    mQueueJob.schedule();
                }
                else {
                    mQueueJob.schedule(100);
                }
            }
            if (mBuffer > 160000) {
                try {
                    mPendingPartitions.wait();
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    public boolean isReadOnly(int offset)
    {
        return true;
    }

    public StyleRange[] getStyleRanges(int offset, int length)
    {
        if (!mConnected) {
            return new StyleRange[0];
        }
        NSISConsolePartition[] computedPartitions = (NSISConsolePartition[])computePartitioning(offset, length);
        StyleRange[] styles = new StyleRange[computedPartitions.length];
        for (int i = 0; i < computedPartitions.length; i++) {
            int rangeStart = Math.max(computedPartitions[i].getOffset(), offset);
            int rangeLength = computedPartitions[i].getLength();
            styles[i] = computedPartitions[i].getStyleRange(rangeStart, rangeLength);
        }
        return styles;
    }

    private class PendingPartition
    {
        StringBuffer mText = new StringBuffer(8192);
        NSISConsoleOutputStream mStream;

        PendingPartition(NSISConsoleOutputStream stream, String text)
        {
            mStream = stream;
            if (text != null) {
                append(text);
            }
        }

        void append(String moreText)
        {
            mText.append(moreText);
            mBuffer += moreText.length();
        }
    }

    private class QueueProcessingJob extends Job
    {
        QueueProcessingJob()
        {
            super(EclipseNSISPlugin.getResourceString("console.queue.job.name")); //$NON-NLS-1$
        }

        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
            synchronized (mOverflowLock) {
                Display display = ConsolePlugin.getStandardDisplay();
                ArrayList<PendingPartition> pendingCopy = new ArrayList<PendingPartition>();
                StringBuffer buffer = null;
                boolean consoleClosed = false;
                while (display != null && mPendingPartitions.size() > 0) {
                    synchronized(mPendingPartitions) {
                        pendingCopy.addAll(mPendingPartitions);
                        mPendingPartitions.clear();
                        mBuffer = 0;
                        mPendingPartitions.notifyAll();
                    }
                    buffer = new StringBuffer();
                    for (Iterator<PendingPartition> i = pendingCopy.iterator(); i.hasNext(); ) {
                        PendingPartition pp = i.next();
                        if (pp != mConsoleClosedPartition) {
                            buffer.append(pp.mText);
                        }
                        else {
                            consoleClosed = true;
                        }
                    }
                }
                if (display != null && buffer != null) {
                    final ArrayList<PendingPartition> finalCopy = pendingCopy;
                    final String toAppend = buffer.toString();
                    final boolean notifyClosed = consoleClosed;
                    display.asyncExec(new Runnable() {
                        public void run() {
                            if (mConnected) {
                                setUpdateInProgress(true);
                                mUpdatePartitions = finalCopy;
                                mFirstOffset = mDocument.getLength();
                                try {
                                    mDocument.replace(mFirstOffset, 0, toAppend
                                            .toString());
                                } catch (BadLocationException e) {
                                }
                                mUpdatePartitions = null;
                                setUpdateInProgress(false);
                            }
                            if (notifyClosed) {
                                mConsole.partitionerFinished();
                            }
                            checkBufferSize();
                        }
                    });
                }
            }
            return Status.OK_STATUS;
        }

        @Override
        public boolean shouldRun()
        {
            boolean shouldRun = mConnected && mPendingPartitions != null && mPendingPartitions.size() > 0;
            return shouldRun;
        }
    }
    private class TrimJob extends WorkbenchJob
    {
        private int truncateOffset;

        TrimJob()
        {
            super(EclipseNSISPlugin.getResourceString("console.trim.job.name")); //$NON-NLS-1$
            setSystem(true);
        }

        public void setOffset(int offset)
        {
            truncateOffset = offset;
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor)
        {
            if (mDocument == null) {
                return Status.OK_STATUS;
            }
            int length = mDocument.getLength();
            if (truncateOffset < length) {
                synchronized (mOverflowLock) {
                    try {
                        if (truncateOffset < 0) {
                            // clear
                            setUpdateInProgress(true);
                            mDocument.set(""); //$NON-NLS-1$
                            setUpdateInProgress(false);
                            mPartitions.clear();
                        }
                        else {
                            // overflow
                            int cutoffLine = mDocument.getLineOfOffset(truncateOffset);
                            int cutOffset = mDocument.getLineOffset(cutoffLine);
                            // set the new length of the first partition
                            NSISConsolePartition partition = (NSISConsolePartition) getPartition(cutOffset);
                            partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);
                            setUpdateInProgress(true);
                            mDocument.replace(0, cutOffset, ""); //$NON-NLS-1$
                            setUpdateInProgress(false);
                            //remove partitions and reset Partition offsets
                            int index = mPartitions.indexOf(partition);
                            for (int i = 0; i < index; i++) {
                                mPartitions.remove(0);
                            }
                            int offset = 0;
                            for (Iterator<NSISConsolePartition> i = mPartitions.iterator(); i.hasNext(); ) {
                                NSISConsolePartition p = i.next();
                                p.setOffset(offset);
                                offset += p.getLength();
                            }
                        }
                    }
                    catch (BadLocationException e) {
                    }
                }
            }
            return Status.OK_STATUS;
        }
    }
}