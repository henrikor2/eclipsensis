/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import org.eclipse.jface.text.*;

public class NSISTextHover extends NSISAnnotationHover
{
    private NSISInformationProvider mInformationProvider;
    private boolean mUseSuper = false;

    public NSISTextHover(String[] annotationTypes)
    {
        super(annotationTypes);
        mInformationProvider = new NSISInformationProvider();
        mInformationProvider.setInformationPresenterControlCreator(new NSISHelpInformationControlCreator(new String[]{STICKY_HELP_COMMAND_ID, GOTO_HELP_COMMAND_ID}));
    }

    /*
     * (non-Javadoc) Method declared on ITextHover
     */
    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
    {
        Object info1 = super.getHoverInfo2(textViewer, hoverRegion);
        Object info2 = mInformationProvider.getInformation2(textViewer, hoverRegion);
        mUseSuper = (info2 == null && info1 != null);
        return info2 != null?info2:info1;
    }

    /*
     * (non-Javadoc) Method declared on ITextHover
     */
    @Override
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        IRegion region1 = super.getHoverRegion(textViewer, offset);
        IRegion region2 = mInformationProvider.getSubject(textViewer, offset);
        return region2 != null && region2.getLength() > 0?region2:region1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
     */
    @Override
    public IInformationControlCreator getHoverControlCreator()
    {
        if(mUseSuper) {
            mUseSuper = false;
            return super.getHoverControlCreator();
        }
        return mInformationProvider.getInformationPresenterControlCreator();
    }
}