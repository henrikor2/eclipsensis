/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.*;

import net.sf.eclipsensis.editor.codeassist.NSISAnnotationHover;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.annotation.INIProblemAnnotation;
import net.sf.eclipsensis.installoptions.editor.text.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.presentation.*;
import org.eclipse.jface.text.quickassist.*;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;

public class InstallOptionsSourceViewerConfiguration extends SourceViewerConfiguration
{
    private NSISAnnotationHover mAnnotationHover;

    public InstallOptionsSourceViewerConfiguration()
    {
        mAnnotationHover = new NSISAnnotationHover(new String[]{IInstallOptionsConstants.INSTALLOPTIONS_ERROR_ANNOTATION_NAME, IInstallOptionsConstants.INSTALLOPTIONS_WARNING_ANNOTATION_NAME});
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return mAnnotationHover;
    }

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
    {
        return mAnnotationHover;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(InstallOptionsPartitionScanner.INSTALLOPTIONS_PARTITIONING);
        DefaultDamagerRepairer dr = new InstallOptionsDamagerRepairer(new InstallOptionsCommentScanner());
        reconciler.setDamager(dr, InstallOptionsPartitionScanner.INSTALLOPTIONS_COMMENT);
        reconciler.setRepairer(dr, InstallOptionsPartitionScanner.INSTALLOPTIONS_COMMENT);

        dr = new InstallOptionsDamagerRepairer(new InstallOptionsRuleBasedScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        return reconciler;
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer)
    {
        QuickAssistAssistant quickAssistAssistant = new QuickAssistAssistant();
        quickAssistAssistant.setQuickAssistProcessor(new IQuickAssistProcessor() {
            public boolean canAssist(IQuickAssistInvocationContext invocationContext)
            {
                return false;
            }

            public boolean canFix(Annotation annotation)
            {
                if(annotation instanceof INIProblemAnnotation) {
                    return ((INIProblemAnnotation)annotation).isQuickFixable();
                }
                return false;
            }

            public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext)
            {
                if(invocationContext.getOffset() >= 0 && invocationContext.getLength() <= 0) {
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    if(workbench != null) {
                        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                        if(window != null) {
                            IWorkbenchPage page = window.getActivePage();
                            if(page != null) {
                                IEditorPart editor = page.getActiveEditor();
                                if(editor instanceof InstallOptionsSourceEditor) {
                                    INIFile inifile = ((InstallOptionsSourceEditor)editor).getINIFile();
                                    if(inifile != null) {
                                        INILine line = inifile.getLineAtOffset(invocationContext.getOffset());
                                        if(line != null) {
                                            List<INIProblem> problems = new ArrayList<INIProblem>(line.getProblems());
                                            problems.addAll(inifile.getProblems(false));
                                            if(!Common.isEmptyCollection(problems)) {
                                                List<INIProblemQuickFixProposal> proposals = new ArrayList<INIProblemQuickFixProposal>();
                                                for (Iterator<INIProblem> iter = problems.iterator(); iter.hasNext();) {
                                                    INIProblem problem = iter.next();
                                                    if(problem.canFix()) {
                                                        proposals.add(new INIProblemQuickFixProposal(problem));
                                                    }
                                                }
                                                if(proposals.size() > 0) {
                                                    return proposals.toArray(new ICompletionProposal[proposals.size()]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }

            public String getErrorMessage()
            {
                return null;
            }
        });
        return quickAssistAssistant;
    }

    private static Image FIX_ERROR_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("fix.errors.action.icon")); //$NON-NLS-1$
    private static Image FIX_WARNING_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("fix.warnings.action.icon")); //$NON-NLS-1$
    private class INIProblemQuickFixProposal implements ICompletionProposal
    {
        private INIProblem mProblem;

        public INIProblemQuickFixProposal(INIProblem problem)
        {
            mProblem = problem;
        }

        public void apply(IDocument document)
        {
            mProblem.fix(document);
        }

        public String getAdditionalProposalInfo()
        {
            return null;
        }

        public IContextInformation getContextInformation()
        {
            return null;
        }

        public String getDisplayString()
        {
            return mProblem.getFixDescription();
        }

        public Image getImage()
        {
            if(INIProblem.TYPE_ERROR.equals(mProblem.getType())) {
                return FIX_ERROR_ICON;
            }
            if(INIProblem.TYPE_WARNING.equals(mProblem.getType())) {
                return FIX_WARNING_ICON;
            }
            return null;
        }

        public Point getSelection(IDocument document)
        {
            return null;
        }
    }
}
