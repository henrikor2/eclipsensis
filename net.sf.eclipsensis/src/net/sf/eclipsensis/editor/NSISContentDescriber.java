package net.sf.eclipsensis.editor;

import java.io.*;

import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.*;

public class NSISContentDescriber implements ITextContentDescriber
{
    private final static QualifiedName[] SUPPORTED_OPTIONS = {IContentDescription.BYTE_ORDER_MARK};

    /*
     *  (non-Javadoc)
     * @see org.eclipse.core.runtime.content.ITextContentDescriber#describe(java.io.Reader, org.eclipse.core.runtime.content.IContentDescription)
     */
    public int describe(Reader contents, IContentDescription description) throws IOException
    {
        if (description == null || !description.isRequested(IContentDescription.BYTE_ORDER_MARK))
        {
            return INDETERMINATE;
        }
        byte[] bom = getByteOrderMark(contents);
        boolean unicodeSupported = NSISPreferences.getInstance().isUnicode();
        if (bom != null)
        {
            description.setProperty(IContentDescription.BYTE_ORDER_MARK, bom);
            if(!unicodeSupported)
            {
                return INVALID;
            }
            return VALID;
        }
        // we want to be pretty loose on detecting the text content type
        return INDETERMINATE;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.core.runtime.content.IContentDescriber#describe(java.io.InputStream, org.eclipse.core.runtime.content.IContentDescription)
     */
    public int describe(InputStream contents, IContentDescription description) throws IOException
    {
        return describe(new InputStreamReader(contents), description);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.core.runtime.content.IContentDescriber#getSupportedOptions()
     */
    public QualifiedName[] getSupportedOptions()
    {
        return SUPPORTED_OPTIONS;
    }

    byte[] getByteOrderMark(Reader input) throws IOException
    {
        int first = input.read();
        if (first == 0xEF) {
            //look for the UTF-8 Byte Order Mark (BOM)
            int second = input.read();
            int third = input.read();
            if (second == 0xBB && third == 0xBF)
            {
                return IContentDescription.BOM_UTF_8;
            }
        } else if (first == 0xFE) {
            //look for the UTF-16 BOM
            if (input.read() == 0xFF)
            {
                return IContentDescription.BOM_UTF_16BE;
            }
        } else if (first == 0xFF) {
            if (input.read() == 0xFE)
            {
                return IContentDescription.BOM_UTF_16LE;
            }
        }
        return null;
    }
}
