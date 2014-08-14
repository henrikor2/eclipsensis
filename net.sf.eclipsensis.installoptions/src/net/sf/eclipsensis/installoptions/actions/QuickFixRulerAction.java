/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.annotation.INIProblemAnnotation;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.*;

public class QuickFixRulerAction extends AbstractRulerActionDelegate
{
    @Override
    protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo)
    {
        return new SelectQuickFixRulerAction(InstallOptionsPlugin.getDefault().getResourceBundle(),
                "quick.fix.ruler.action.",editor,rulerInfo); //$NON-NLS-1$
    }

    public class SelectQuickFixRulerAction extends SelectMarkerRulerAction
    {
        private ITextEditor mTextEditor;
        private Position mPosition;
        private AnnotationPreferenceLookup mAnnotationPreferenceLookup;
        private IPreferenceStore mStore;
        private boolean mCanFix;

        public SelectQuickFixRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler)
        {
            super(bundle, prefix, editor, ruler);
            mTextEditor= editor;

            mAnnotationPreferenceLookup= EditorsUI.getAnnotationPreferenceLookup();
            mStore= InstallOptionsPlugin.getDefault().getCombinedPreferenceStore();

            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, "installoptions_quickfix_context"); //$NON-NLS-1$
        }

        @Override
        public void run()
        {
            runWithEvent(null);
        }

        @Override
        public void runWithEvent(Event event)
        {
            if (mCanFix) {
                ITextOperationTarget operation= (ITextOperationTarget) mTextEditor.getAdapter(ITextOperationTarget.class);
                final int opCode= ISourceViewer.QUICK_ASSIST;
                if (operation != null && operation.canDoOperation(opCode)) {
                    mTextEditor.selectAndReveal(mPosition.getOffset(), mPosition.getLength());
                    operation.doOperation(opCode);
                }
                return;
            }

            super.run();
        }

        @Override
        public void update()
        {
            findAnnotation();
            setEnabled(true); // super.update() might change this later

            if (!mCanFix) {
                super.update();
            }
        }

        private void findAnnotation()
        {
            mPosition= null;
            mCanFix= false;

            IDocumentProvider provider= mTextEditor.getDocumentProvider();
            IAnnotationModel model= provider.getAnnotationModel(mTextEditor.getEditorInput());
            IAnnotationAccessExtension annotationAccess= getAnnotationAccessExtension();

            IDocument document= getDocument();
            if (model == null) {
                return ;
            }

            Iterator<?> iter= model.getAnnotationIterator();
            int layer= Integer.MIN_VALUE;

            while (iter.hasNext()) {
                Annotation annotation= (Annotation) iter.next();
                if (annotation.isMarkedDeleted()) {
                    continue;
                }

                int annotationLayer = Integer.MAX_VALUE;
                if (annotationAccess != null) {
                    annotationLayer= annotationAccess.getLayer(annotation);
                    if (annotationLayer < layer) {
                        continue;
                    }
                }

                Position position= model.getPosition(annotation);
                if (!includesRulerLine(position, document)) {
                    continue;
                }

                boolean isReadOnly= mTextEditor instanceof ITextEditorExtension && ((ITextEditorExtension)mTextEditor).isEditorInputReadOnly();
                if (!isReadOnly && annotation instanceof INIProblemAnnotation && ((INIProblemAnnotation)annotation).isQuickFixable()) {
                    mPosition= position;
                    mCanFix= true;
                    layer= annotationLayer;
                    continue;
                }
                else {
                    AnnotationPreference preference= mAnnotationPreferenceLookup.getAnnotationPreference(annotation);
                    if (preference == null) {
                        continue;
                    }

                    String key= preference.getVerticalRulerPreferenceKey();
                    if (key == null) {
                        continue;
                    }

                    if (mStore.getBoolean(key)) {
                        mPosition= position;
                        mCanFix= false;
                        layer= annotationLayer;
                    }
                }
            }
        }
    }
}
