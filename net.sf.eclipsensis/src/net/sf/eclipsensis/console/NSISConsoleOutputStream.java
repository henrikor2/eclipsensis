/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.ui.console.IOConsoleOutputStream
 *
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.io.*;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;

public class NSISConsoleOutputStream extends OutputStream
{
    private boolean mClosed = false;
    private NSISConsolePartitioner mPartitioner;
    private NSISConsole mConsole;
    private Color mColor;
    private int mFontStyle;

    NSISConsoleOutputStream(NSISConsole console)
    {
        this.mConsole = console;
        this.mPartitioner = (NSISConsolePartitioner) console.getPartitioner();
    }

    public int getFontStyle()
    {
        return mFontStyle;
    }

    public void setFontStyle(int newFontStyle)
    {
        if (newFontStyle != mFontStyle) {
            mFontStyle = newFontStyle;
        }
    }

    public void setColor(Color newColor)
    {
        Color old = mColor;
        if (old == null || !old.equals(newColor)) {
            mColor = newColor;
        }
    }

    public Color getColor()
    {
        return mColor;
    }

    public synchronized boolean isClosed()
    {
        return mClosed;
    }

    @Override
    public synchronized void close() throws IOException
    {
        if(mClosed) {
            throw new IOException(EclipseNSISPlugin.getResourceString("console.outputstream.closed.error")); //$NON-NLS-1$
        }
        mClosed = true;
        mPartitioner = null;
    }

    @Override
    public void flush() throws IOException
    {
        if(mClosed) {
            throw new IOException(EclipseNSISPlugin.getResourceString("console.outputstream.closed.error")); //$NON-NLS-1$
        }
    }

    public void print(String message)
    {
        try {
            write(message);
        } catch (IOException e) {
            ConsolePlugin.log(e);
        }
    }

    public void println()
    {
        try {
            write("\n"); //$NON-NLS-1$
        } catch (IOException e) {
            ConsolePlugin.log(e);
        }
    }

    public void println(String message)
    {
        print(message);
        println();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        checkWrite(new String(b, off, len));
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException
    {
        write(new byte[] {(byte)b}, 0, 1);
    }

    public synchronized void write(String str) throws IOException
    {
        checkWrite(str);
    }

    private void checkWrite(String string) throws IOException
    {
        if(mClosed) {
            throw new IOException(EclipseNSISPlugin.getResourceString("console.outputstream.closed.error")); //$NON-NLS-1$
        }
        notifyParitioner(string);
    }

    private void notifyParitioner(String string) throws IOException
    {
        try {
            mPartitioner.streamAppended(this, string);
            ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(mConsole);
        }
        catch (IOException e) {
            if (!mClosed) {
                close();
            }
            throw e;
        }
    }
}