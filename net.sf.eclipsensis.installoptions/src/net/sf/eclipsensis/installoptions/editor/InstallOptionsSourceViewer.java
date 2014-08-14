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

import net.sf.eclipsensis.editor.NSISScrollTipHelper;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.installoptions.*;

import org.eclipse.jface.text.source.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsSourceViewer extends InstallOptionsSourcePreviewer
{
    private NSISScrollTipHelper mScrollTipHelper;

    public InstallOptionsSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles)
    {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview,
                styles);
        mScrollTipHelper = new NSISScrollTipHelper(this);
    }

    @Override
    protected void createControl(Composite parent, int styles)
    {
        super.createControl(parent, styles);
        final IPropertyChangeListener listener = new IPropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(IInstallOptionsConstants.PREFERENCE_SYNTAX_STYLES)) {
                    setSyntaxStyles(NSISTextUtility.parseSyntaxStylesMap((String)event.getNewValue()));
                }
            }
        };

        InstallOptionsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(listener);
        getControl().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                InstallOptionsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(listener);
            }
        });
    }

    @Override
    public void configure(SourceViewerConfiguration configuration)
    {
        super.configure(configuration);
        mScrollTipHelper.connect();
    }

    @Override
    public void unconfigure()
    {
        mScrollTipHelper.disconnect();
        super.unconfigure();
    }
}
