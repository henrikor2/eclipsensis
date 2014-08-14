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

import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.commands.*;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;

public class NSISInformationUtility implements INSISConstants
{
    public static final ICompletionProposal[] EMPTY_COMPLETION_PROPOSAL_ARRAY = new ICompletionProposal[0];

    private static final char[] COMPLETION_AUTO_ACTIVATION_CHARS = { '.', '/','$','!',':' };
    private static final Image KEYWORD_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("keyword.icon")); //$NON-NLS-1$
    private static final Image PLUGIN_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("plugin.icon")); //$NON-NLS-1$
    private static final Comparator<ICompletionProposal> cCompletionProposalComparator = new Comparator<ICompletionProposal>() {
        public int compare(ICompletionProposal o1, ICompletionProposal o2)
        {
            return (o1).getDisplayString().compareToIgnoreCase((o2).getDisplayString());
        }
    };

    private static IBindingService cBindingService = (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
    private static ICommandService cCommandService = (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);

    public static IRegion getInformationRegionAtOffset(ITextViewer textViewer, int offset, boolean forUsage)
    {
        IDocument doc = textViewer.getDocument();
        ITypedRegion[][] nsisLine = NSISTextUtility.getNSISLines(doc, offset);
        return getInformationRegionAtOffset(doc, offset, nsisLine, forUsage);
    }

    public static ParameterizedCommand getCommand(String commandId)
    {
        Command command = cCommandService.getCommand(commandId);
        if(command != null) {
            ParameterizedCommand pc = new ParameterizedCommand(command, null);
            return pc;
        }
        return null;
    }

    public static KeySequence[] getKeySequences(ParameterizedCommand command)
    {
        List<TriggerSequence> list = new ArrayList<TriggerSequence>();
        TriggerSequence[] sequences = cBindingService.getActiveBindingsFor(command);
        if (!Common.isEmptyArray(sequences)) {
            for (int j = 0; j < sequences.length; j++) {
                if(sequences[j] instanceof KeySequence) {
                    list.add(sequences[j]);
                }
            }
        }
        return list.toArray(new KeySequence[list.size()]);
    }

    public static String buildStatusText(String description, KeySequence[] sequences)
    {
        String statusText = null;
        if (!Common.isEmptyArray(sequences)) {
            List<String> params = new ArrayList<String>();
            for (int j = 0; j < sequences.length; j++) {
                try {
                    String keyText = sequences[j].format();
                    params.add(keyText);
                    params.add(description);
                }
                catch(Exception e) {
                }
            }
            if(params.size() > 0) {
                String format = EclipseNSISPlugin.getResourceString("information.status.format."+params.size()/2); //$NON-NLS-1$
                if(!Common.isEmpty(format)) {
                    statusText = MessageFormat.format(format, params.toArray());
                }
            }
        }

        return statusText;
    }

    public static String buildStatusText(ParameterizedCommand[] commands)
    {
        String statusText = null;
        if(!Common.isEmptyArray(commands)) {
            List<String> params = new ArrayList<String>();
            for (int i = 0; i < commands.length; i++) {
                KeySequence[] sequences = getKeySequences(commands[i]);
                if (!Common.isEmptyArray(sequences)) {
                    for (int j = 0; j < sequences.length; j++) {
                        try {
                            String keyText = sequences[j].format();
                            String description = commands[i].getCommand().getDescription();
                            params.add(keyText);
                            params.add(description);
                        }
                        catch(Exception e) {
                        }
                    }
                }
            }
            if(params.size() > 0) {
                String format = EclipseNSISPlugin.getResourceString("information.status.format."+params.size()/2); //$NON-NLS-1$
                if(!Common.isEmpty(format)) {
                    statusText = MessageFormat.format(format, params.toArray());
                }
            }
        }

        return statusText;
    }

    /**
     * @param doc
     * @param offset
     * @param nsisLine
     * @param forUsage
     * @return
     */
    private static IRegion getInformationRegionAtOffset(IDocument doc, int offset, ITypedRegion[][] nsisLine, boolean forUsage)
    {
        if(!Common.isEmptyArray(nsisLine)) {
            if(!Common.isEmptyArray(nsisLine[0])) {
                NSISTextProcessorRule rule = new NSISTextProcessorRule();
                for (int i = 0; i < nsisLine[0].length; i++) {
                    NSISRegionScanner scanner = new NSISRegionScanner(doc, nsisLine[0][i]);
                    String type = nsisLine[0][i].getType();
                    if(forUsage) {
                        if(NSISTextUtility.contains(nsisLine[0][i],offset)) {
                            if(type.equals(NSISPartitionScanner.NSIS_STRING)) {
                                rule.setTextProcessor(new EntireStringProcessor());
                                IToken token = rule.evaluate(scanner);
                                if(!token.isUndefined()) {
                                    return (IRegion)token.getData();
                                }
                                break;
                            }
                            else if(type.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                                rule.setTextProcessor(new UsageWordProcessor(offset));
                                IToken token = rule.evaluate(scanner);
                                if(!token.isUndefined()) {
                                    return (IRegion)token.getData();
                                }
                                break;
                            }
                            else {
                                break;
                            }
                        }
                        else if(nsisLine[0][i].getOffset() > offset) {
                            break;
                        }
                        else {
                            rule.setTextProcessor(new OnlyWhitespaceProcessor());
                            IToken token = rule.evaluate(scanner);
                            if(token.isWhitespace()) {
                                continue;
                            }
                            break;
                        }
                    }
                    else {
                        if(NSISTextUtility.contains(nsisLine[0][i],offset)) {
                            rule.setTextProcessor(new VariablesAndSymbolsProcessor(offset));
                            IToken token = rule.evaluate(scanner);
                            if(token.isUndefined()) {
                                scanner.reset();
                                if(type.equals(NSISPartitionScanner.NSIS_STRING)) {
                                    rule.setTextProcessor(new EntireStringProcessor());
                                    token = rule.evaluate(scanner);
                                    if(token.isUndefined()) {
                                        break;
                                    }
                                }
                                else {
                                    rule.setTextProcessor(new AnyWordProcessor(offset));
                                    token = rule.evaluate(scanner);
                                    if(token.isUndefined()) {
                                        scanner.reset();
                                        rule.setTextProcessor(new PluginProcessor(offset));
                                        token = rule.evaluate(scanner);
                                        if(token.isUndefined()) {
                                            break;
                                        }
                                    }
                                }
                            }
                            return (IRegion)token.getData();
                        }
                        else if(nsisLine[0][i].getOffset() > offset) {
                            break;
                        }
                    }
                }
            }
        }
        return NSISTextUtility.EMPTY_REGION;
    }

    public static char[] getCompletionProposalAutoActivationCharacters()
    {
        return COMPLETION_AUTO_ACTIVATION_CHARS;
    }

    public static ICompletionProposal[] getCompletionsAtOffset(ITextViewer viewer, int offset)
    {
        if(offset > 0) {
            IDocument doc = viewer.getDocument();
            ITypedRegion[][] nsisLine = NSISTextUtility.getNSISLines(doc, offset);
            IRegion region = getInformationRegionAtOffset(doc, offset, nsisLine, true);
            if(region == null || region.equals(NSISTextUtility.EMPTY_REGION)) {
                region = getInformationRegionAtOffset(doc, offset, nsisLine, false);
                if(region == null || region.equals(NSISTextUtility.EMPTY_REGION)) {
                    try {
                        //Last ditch
                        region = doc.getLineInformationOfOffset(offset);
                        char[] chars = doc.get(region.getOffset(),offset-region.getOffset()).toCharArray();
                        int i = chars.length-1;
                        while (i >= 0) {
                            if(Character.isWhitespace(chars[i])) {
                                break;
                            }
                            i--;
                        }
                        int offset3 = region.getOffset()+i+1;
                        region = new Region(offset3,offset-offset3);
                    }
                    catch (BadLocationException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
            if(region != null && !region.equals(NSISTextUtility.EMPTY_REGION)) {
                if(region.getOffset()+region.getLength() > offset) {
                    region = new Region(region.getOffset(),offset-region.getOffset()+1);
                }
                String text = NSISTextUtility.getRegionText(doc,region);
                if(!Common.isEmpty(text)) {
                    List<CompletionProposal> list = new ArrayList<CompletionProposal>();
                    int pos = text.indexOf("::"); //$NON-NLS-1$
                    if(pos > 0) {
                        String pluginName = text.substring(0,pos);
                        text = text.substring(pos+2);
                        String[] exports = NSISPluginManager.INSTANCE.getDefaultPluginExports(pluginName);
                        if(!Common.isEmptyArray(exports)) {
                            int textlen = text.length();
                            for (int i = 0; i < exports.length; i++) {
                                if(!Common.isEmpty(text)) {
                                    if((exports[i].compareToIgnoreCase(text) < 0) ||
                                        !exports[i].regionMatches(true,0,text,0,textlen)) {
                                        continue;
                                    }
                                }
                                list.add(new CompletionProposal(exports[i],
                                        region.getOffset()+pos+2,
                                        textlen,
                                        exports[i].length(),
                                        PLUGIN_IMAGE,
                                        null, null, null));

                            }
                        }
                    }
                    else {
                        int textlen = text.length();
                        String[] allKeywords = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.ALL_KEYWORDS);
                        for(int i=0; i<allKeywords.length; i++) {
                            int n = allKeywords[i].compareToIgnoreCase(text);
                            if(n >= 0) {
                                if(allKeywords[i].regionMatches(true,0,text,0,textlen)) {
                                    list.add(new CompletionProposal(allKeywords[i],
                                                                    region.getOffset(),
                                                                    offset-region.getOffset(),
                                                                    allKeywords[i].length(),
                                                                    KEYWORD_IMAGE,
                                                                    null, null, null));
                                }
                                else {
                                    break;
                                }
                            }
                            continue;
                        }
                        String[] plugins = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PLUGINS);
                        for(int i=0; i<plugins.length; i++) {
                            int n = plugins[i].compareToIgnoreCase(text);
                            if(n >= 0) {
                                if(plugins[i].regionMatches(true,0,text,0,textlen)) {
                                    list.add(new CompletionProposal(plugins[i],
                                                                    region.getOffset(),
                                                                    offset-region.getOffset(),
                                                                    plugins[i].length(),
                                                                    PLUGIN_IMAGE,
                                                                    null, null, null));
                                }
                                else {
                                    break;
                                }
                            }
                            continue;
                        }
                    }
                    ICompletionProposal[] completionProposals = list.toArray(EMPTY_COMPLETION_PROPOSAL_ARRAY);
                    Arrays.sort(completionProposals, cCompletionProposalComparator);
                    return completionProposals;
                }
            }
        }
        return EMPTY_COMPLETION_PROPOSAL_ARRAY;
    }

    private static class VariablesAndSymbolsProcessor extends AnyWordProcessor
    {
        protected boolean mIsSymbol = false;
        protected NSISKeywords.VariableMatcher mVariableMatcher = NSISKeywords.getInstance().createVariableMatcher();
        private int mMatchOffset = -1;
        private boolean mIsComplete = false;

        /**
          * @param offset
          */
        public VariablesAndSymbolsProcessor(int offset)
        {
            super(offset);
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.text.INSISTextProcessor#isValid(int)
         */
        @Override
        public boolean isValid(int c)
        {
            if(testComplete()) {
                return false;
            }
            if(mBuffer.length() == 0) {
                if(c == '$') {
                    mFirstNonWhitespaceOffset = ((NSISScanner)mScanner).getOffset()-1;
                    mBuffer.append((char)c);
                }
                return true;
            }
            else {
                if(mBuffer.length() == 1 && c == '{') {
                    mIsSymbol = true;
                    mBuffer.append((char)c);
                    return true;
                }
                if(mIsSymbol && c == '}') {
                    mBuffer.append((char)c);
                    return true;
                }
                if(!Character.isLetterOrDigit((char)c) && c != '_' && c != '.') {
                    return false;
                }
                mBuffer.append((char)c);
            }
            return true;
        }

        @Override
        protected boolean testComplete()
        {
            boolean isComplete = false;
            if(mBuffer.length() > 1) {
                if(mIsSymbol) {
                    if(mBuffer.charAt(mBuffer.length()-1) == '}') {
                        isComplete = true;
                    }
                }
                else {
                    if(mIsComplete) {
                        isComplete = true;
                    }
                    else {
                        mVariableMatcher.setText(mBuffer.toString());
                        if(mVariableMatcher.hasPotentialMatch()) {
                            if(mVariableMatcher.isMatch()) {
                                mMatchOffset = ((NSISScanner)mScanner).getOffset();
                            }
                        }
                        else {
                            if(mMatchOffset >= 0) {
                                isComplete = true;
                            }
                        }
                    }
                }
                if(isComplete) {
                    isComplete = super.testComplete();
                    if(!isComplete) {
                        mIsSymbol = false;
                        mMatchOffset = -1;
                        mVariableMatcher.reset();
                    }
                    else {
                        if(mMatchOffset >= 0) {
                            NSISTextUtility.unread(mScanner,((NSISScanner)mScanner).getOffset()-mMatchOffset);
                        }
                    }
                }
            }
            return isComplete;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.text.INSISTextProcessor#setScanner(org.eclipse.jface.text.rules.ICharacterScanner)
         */
        @Override
        public void setScanner(ICharacterScanner scanner)
        {
            super.setScanner(scanner);
            mIsSymbol = false;
        }
    }

    private static class EntireStringProcessor extends DefaultTextProcessor
    {
        private char mStringChar = (char)0;
        private int mStartOffset = -1;

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#createToken()
         */
        @Override
        public IToken createToken()
        {
            if(mStringChar != (char)0) {
                return new Token(new Region(mStartOffset+1,(((NSISScanner)mScanner).getOffset()-mStartOffset-1)));
            }
            else {
                return Token.UNDEFINED;
            }
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#isValid(int)
         */
        @Override
        public boolean isValid(int c)
        {
            if(mStringChar == 0) {
                if(c == '"' || c == '\'' || c == '`') {
                    mStringChar = (char)c;
                    mStartOffset = ((NSISScanner)mScanner).getOffset()-1;
                    return true;
                }
            }
            else {
                if(c != mStringChar) {
                    for(int i=0; i<QUOTE_ESCAPE_SEQUENCES.length; i++) {
                        if(c == QUOTE_ESCAPE_SEQUENCES[i][0] && NSISTextUtility.sequenceDetected(mScanner,QUOTE_ESCAPE_SEQUENCES[i],true,false)) {
                            for(int j=0;j<QUOTE_ESCAPE_SEQUENCES[i].length;j++) {
                                mBuffer.append(QUOTE_ESCAPE_SEQUENCES[i][j]);
                            }
                            return true;
                        }
                    }
                    mBuffer.append((char)c);
                    return true;
                }
            }
            return false;
        }
    }

    private static class AnyWordProcessor extends UsageWordProcessor
    {
        /**
          * @param offset
          */
        public AnyWordProcessor(int offset)
        {
            super(offset);
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.text.INSISTextProcessor#isValid(int)
         */
        @Override
        public boolean isValid(int c)
        {
            boolean b = super.isValid(c);
            if(!b) {
                b = (c == '_' || c == '/');
                if(!b) {
                    if(!testComplete()) {
                        return super.isValid(c);
                    }
                }
                else {
                    mBuffer.append((char)c);
                }
            }
            return b;
        }

        protected boolean testComplete()
        {
            if(mOffset >=0 && mFirstNonWhitespaceOffset >= 0 && mBuffer.length() > 0) {
                int offset = ((NSISScanner)mScanner).getOffset();
                if(mOffset >= mFirstNonWhitespaceOffset && mOffset < offset) {
                    return true;
                }
            }
            mBuffer.setLength(0);
            mFirstNonWhitespaceOffset = -1;
            return false;
        }
    }

    private static class UsageWordProcessor extends DefaultTextProcessor
    {
        protected int mOffset;
        protected int mFirstNonWhitespaceOffset;

        /**
         * @param offset
         */
        public UsageWordProcessor(int offset)
        {
            mOffset = offset;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#createToken()
         */
        @Override
        public IToken createToken()
        {
            if(mOffset >=0 && mFirstNonWhitespaceOffset >= 0 && mBuffer.length() > 0) {
                int offset = ((NSISScanner)mScanner).getOffset();
                if(mOffset >= mFirstNonWhitespaceOffset && mOffset < offset) {
                    return new Token(new Region(mFirstNonWhitespaceOffset,mBuffer.length()));
                }
            }
            return Token.UNDEFINED;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#isValid(int)
         */
        @Override
        public boolean isValid(int c)
        {
            if(!Character.isWhitespace((char)c)) {
                if(mFirstNonWhitespaceOffset < 0) {
                    if(Character.isLetterOrDigit((char)c) || c == '!' || c == '.' ) {
                        mFirstNonWhitespaceOffset = ((NSISScanner)mScanner).getOffset()-1;
                    }
                    else {
                        return false;
                    }
                }
                else if(!Character.isLetterOrDigit((char)c) && c != '.') {
                    return false;
                }
                mBuffer.append((char)c);
                return true;
            }
            return (mFirstNonWhitespaceOffset < 0);
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#setScanner(org.eclipse.jface.text.rules.ICharacterScanner)
         */
        @Override
        public void setScanner(ICharacterScanner scanner)
        {
            super.setScanner(scanner);
            mFirstNonWhitespaceOffset = -1;
        }
    }

    private static class PluginProcessor extends UsageWordProcessor
    {
        /**
         * @param offset
         */
        public PluginProcessor(int offset)
        {
            super(offset);
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#isValid(int)
         */
        @Override
        public boolean isValid(int c)
        {
            if(!Character.isWhitespace((char)c)) {
                if(mFirstNonWhitespaceOffset < 0) {
                    if(Character.isLetterOrDigit((char)c)) {
                        mFirstNonWhitespaceOffset = ((NSISScanner)mScanner).getOffset()-1;
                    }
                    else {
                        return false;
                    }
                }
                else if(!Character.isLetterOrDigit((char)c)) {
                    if(c == ':') {
                        String temp=mBuffer.toString();
                        return ((temp.indexOf(c) < 0) || (temp.endsWith(":") && !temp.endsWith("::"))); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    return false;
                }
                mBuffer.append((char)c);
                return true;
            }
            return (mFirstNonWhitespaceOffset < 0);
        }
    }

    private static class OnlyWhitespaceProcessor implements INSISTextProcessor
    {
        protected boolean mFoundNonWhitespace = false;

        public void setScanner(ICharacterScanner scanner)
        {
        }

        public boolean isValid(int c)
        {
            boolean temp = Character.isWhitespace((char)c);
            if(!mFoundNonWhitespace && !temp) {
                mFoundNonWhitespace = true;
            }
            return temp;
        }

        public IToken createToken()
        {
            return (mFoundNonWhitespace?Token.UNDEFINED:Token.WHITESPACE);
        }
    }
}
