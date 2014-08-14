/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.search;

import java.io.*;
import java.util.Arrays;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.demo.html.HTMLParser;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

@SuppressWarnings("restriction")
public class NSISHelpIndexer implements INSISHelpSearchConstants
{
    private static final String[] cIndexedFileExtensions;

    static {
        cIndexedFileExtensions = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"nsis.help.indexed.file.extensions"); //$NON-NLS-1$
    }

    private final ISchedulingRule SCHEDULING_RULE = new ISchedulingRule() {
        public boolean contains(ISchedulingRule rule)
        {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule)
        {
            return rule == this;
        }
    };

    final Object JOB_FAMILY = new Object();
    private File mIndexLocation;
    private File mDocumentRoot;
    private NSISHelpIndexerJob mJob;
    private JobScheduler mScheduler;
    private Analyzer mAnalyzer;
    private NSISHelpSearcher mSearcher;

    public NSISHelpIndexer(File indexLocation, File documentRoot, Analyzer analyzer)
    {
        mIndexLocation = indexLocation;
        mDocumentRoot = documentRoot;
        mScheduler = EclipseNSISPlugin.getDefault().getJobScheduler();
        mJob = new NSISHelpIndexerJob();
        mAnalyzer = analyzer;
        mSearcher = new NSISHelpSearcher(this);
    }

    public NSISHelpSearcher getSearcher()
    {
        return mSearcher;
    }

    public void stopIndexing()
    {
        if(mScheduler.isScheduled(JOB_FAMILY)) {
            mScheduler.cancelJobs(JOB_FAMILY);
        }
    }

    public void indexHelp()
    {
        stopIndexing();
        mScheduler.scheduleJob(JOB_FAMILY, EclipseNSISPlugin.getResourceString("nsis.help.indexer.job.title"),SCHEDULING_RULE, mJob); //$NON-NLS-1$
    }

    private class NSISHelpIndexerJob implements IJobStatusRunnable
    {
        public IStatus run(IProgressMonitor monitor)
        {
            IndexWriter writer = null;
            IStatus status = Status.OK_STATUS;
            try {
                monitor.beginTask(EclipseNSISPlugin.getResourceString("nsis.help.indexer.task.title"),IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                if(mIndexLocation.exists()) {
                    mIndexLocation.mkdirs();
                }
                writer = new IndexWriter(mIndexLocation.getAbsolutePath(), mAnalyzer, true);
                writer.setMaxFieldLength(1000000);

                status = indexDocs(monitor, writer, mDocumentRoot);
                if(status.isOK()) {
                    writer.optimize();
                }
            }
            catch(Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
                status = new Status(IStatus.ERROR, INSISConstants.PLUGIN_ID, IStatus.ERROR, ex.getMessage(), ex);
                IOUtility.deleteDirectory(mIndexLocation);
            }
            finally {
                if(writer != null) {
                    try {
                        writer.close();
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
                monitor.done();
                if(monitor.isCanceled()) {
                    status = Status.CANCEL_STATUS;
                    IOUtility.deleteDirectory(mIndexLocation);
                }
            }
            return status;
        }

        private IStatus indexDocs(IProgressMonitor monitor, IndexWriter writer, File file) throws Exception
        {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                Arrays.sort(files);
                for (int i = 0; i < files.length; i++) {
                    if(monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    IStatus status = indexDocs(monitor, writer, files[i]);
                    if(!status.isOK()) {
                        return status;
                    }
                }
            }
            else {
                if(monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                String ext = IOUtility.getFileExtension(file);
                for (int i = 0; i < cIndexedFileExtensions.length; i++) {
                    if(Common.stringsAreEqual(ext,cIndexedFileExtensions[i],true)) {
                        writer.addDocument(makeDocument(file));
                        break;
                    }
                }
            }
            return Status.OK_STATUS;
        }

        private Document makeDocument(File f) throws IOException, InterruptedException
        {
            Document doc = new Document();

            doc.add(new Field(INDEX_FIELD_URL, IOUtility.getFileURLString(f), Field.Store.YES, Field.Index.NO));

            HTMLParser parser = new HTMLParser(new FileInputStream(f));
            parser.parse();

            doc.add(new Field(INDEX_FIELD_CONTENTS, parser.getReader(), Field.TermVector.NO));
            doc.add(new Field(INDEX_FIELD_SUMMARY, parser.getSummary(), Field.Store.YES, Field.Index.NO));
            doc.add(new Field(INDEX_FIELD_TITLE, parser.getTitle(), Field.Store.YES, Field.Index.TOKENIZED));

            return doc;
        }
    }

    public Analyzer getAnalyzer()
    {
        return mAnalyzer;
    }

    public File getDocumentRoot()
    {
        return mDocumentRoot;
    }

    public File getIndexLocation()
    {
        return mIndexLocation;
    }

    public JobScheduler getScheduler()
    {
        return mScheduler;
    }
}
