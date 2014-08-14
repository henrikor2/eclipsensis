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

import java.util.Map;

import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.installoptions.editor.text.InstallOptionsPartitionScanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsSourcePreviewer extends ProjectionViewer
{
    public InstallOptionsSourcePreviewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles)
    {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview,
                styles);
    }

    public void setSyntaxStyles(Map<String, NSISSyntaxStyle> syntaxStyles)
    {
        if(fPresentationReconciler != null) {
            for(int i=0; i<InstallOptionsPartitionScanner.INSTALLOPTIONS_PARTITION_TYPES.length; i++) {
                String contentType = InstallOptionsPartitionScanner.INSTALLOPTIONS_PARTITION_TYPES[i];
                setContentTypeSyntaxStyles(contentType, syntaxStyles);
            }
        }
        setContentTypeSyntaxStyles(IDocument.DEFAULT_CONTENT_TYPE, syntaxStyles);
        invalidateTextPresentation();
    }

    private void setContentTypeSyntaxStyles(String contentType, Map<String, NSISSyntaxStyle> syntaxStyles)
    {
        IPresentationDamager damager = fPresentationReconciler.getDamager(contentType);
        IPresentationRepairer repairer = fPresentationReconciler.getRepairer(contentType);
        if(damager instanceof InstallOptionsDamagerRepairer) {
            ((InstallOptionsDamagerRepairer)damager).setSyntaxStyles(syntaxStyles);
        }
        if(repairer != damager) {
            if(repairer instanceof InstallOptionsDamagerRepairer) {
                ((InstallOptionsDamagerRepairer)repairer).setSyntaxStyles(syntaxStyles);
            }
        }
    }
}
