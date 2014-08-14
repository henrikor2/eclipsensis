/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;

public class UnicodeReader extends Reader
{
    private PushbackInputStream mPushbackStream;

    private InputStreamReader mInternalReader = null;

    private boolean mClosed = false;

    private static final int BOM_SIZE = 4;

    public UnicodeReader(InputStream in)
    {
        mPushbackStream = new PushbackInputStream(in, BOM_SIZE);
    }

    public String getEncoding()
    {
        if (mInternalReader == null) {
            return null;
        }
        return mInternalReader.getEncoding();
    }

    private void detectEncoding() throws IOException
    {
        /*
         * Read-ahead four bytes and check for BOM marks. Extra bytes are unread
         * back to the stream, only BOM bytes are skipped.
         */
        byte bom[] = new byte[BOM_SIZE];
        int n;
        n = mPushbackStream.read(bom, 0, bom.length);

        /***********************************************************************
         * http://www.unicode.org/unicode/faq/utf_bom.html#25
         * BOMs:
         *   00 00 FE FF = UTF-32, big-endian
         *   FF FE 00 00 = UTF-32, little-endian
         *   FE FF       = UTF-16, big-endian
         *   FF FE       = UTF-16, little-endian
         *   EF BB BF    = UTF-8
         **********************************************************************/
        String encoding = null;
        int unread = n;

        switch(bom[0]) {
            case (byte)0x00:
                switch(bom[1]) {
                    case (byte)0x00:
                        switch(bom[2]) {
                            case (byte)0xFE:
                                switch(bom[3]) {
                                    case (byte)0xFF:
                                        encoding = "UTF-32BE"; //$NON-NLS-1$
                                        unread -= 4;
                                }
                        }
                }
                break;
            case (byte)0xEF:
                switch(bom[1]) {
                    case (byte)0xBB:
                        switch(bom[2]) {
                            case (byte)0xBF:
                                encoding = "UTF-8"; //$NON-NLS-1$
                                unread -= 3;
                        }
                }
                break;
            case (byte)0xFE:
                switch(bom[1]) {
                    case (byte)0xFF:
                        encoding = "UTF-16BE"; //$NON-NLS-1$
                        unread -= 2;
                }
                break;
            case (byte)0xFF:
                switch(bom[1]) {
                    case (byte)0xFE:
                        switch(bom[2]) {
                            case (byte)0x00:
                                if(bom[3] == (byte)0x00) {
                                    encoding = "UTF-32LE"; //$NON-NLS-1$
                                    unread -= 4;
                                    break;
                                }
                                //$FALL-THROUGH$
                            default:
                                encoding = "UTF-16LE"; //$NON-NLS-1$
                                unread -= 2;
                        }
                }
                break;
        }

        if (unread > 0) {
            mPushbackStream.unread(bom, (n - unread), unread);
        }
        else if (unread < -1) {
            mPushbackStream.unread(bom, 0, 0);
        }

        // Use given encoding
        if (encoding == null) {
            mInternalReader = new InputStreamReader(mPushbackStream);
        }
        else {
            mInternalReader = new InputStreamReader(mPushbackStream, encoding);
        }
    }

    @Override
    public void close() throws IOException
    {
        if(mInternalReader != null) {
            mInternalReader.close();
            mInternalReader = null;
        }
        else if (mPushbackStream != null){
            mPushbackStream.close();
            mPushbackStream = null;
        }
        mClosed = true;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        if(mClosed) {
            throw new IOException("Reader is closed"); //$NON-NLS-1$
        }
        if (mInternalReader == null) {
            detectEncoding();
        }

        return mInternalReader.read(cbuf, off, len);
    }
}
