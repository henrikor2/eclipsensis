/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.text;

import java.util.*;

import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.jface.text.rules.*;

public class InstallOptionsRuleBasedScanner extends InstallOptionsSyntaxScanner
{
    @Override
    protected void reset()
    {
        List<IRule> list = new ArrayList<IRule>();
        list.add(new BeginningOfLineWordPatternRule(new InstallOptionsWordDetector('[',']'),
            "[","]",createToken(IInstallOptionsConstants.SECTION_STYLE))); //$NON-NLS-1$ //$NON-NLS-2$
        list.add(new ExclusiveEndSequenceWordPatternRule(new InstallOptionsWordDetector('\0','='),null,"=", //$NON-NLS-1$
                createToken(IInstallOptionsConstants.KEY_STYLE)));
        list.add(new WordRule(new IWordDetector(){

            public boolean isWordStart(char c)
            {
                return (c == '=');
            }

            public boolean isWordPart(char c)
            {
                return false;
            }
        }
        ,createToken(IInstallOptionsConstants.KEY_VALUE_DELIM_STYLE)));
        IToken numberToken = createToken(IInstallOptionsConstants.NUMBER_STYLE);
        list.add(new NSISHexNumberRule(numberToken));
        list.add(new NumberRule(numberToken));
        list.add(new WhitespaceRule(new NSISWhitespaceDetector()));
        setRules(list.toArray(new IRule[0]));
    }
}
