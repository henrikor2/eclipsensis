package net.sf.eclipsensis.help;

import java.io.*;

public class NSISHelpInputStream extends InputStream
{
    private int state = 0;
    private PushbackInputStream pbis;

    public NSISHelpInputStream(InputStream in)
    {
        pbis = new PushbackInputStream(in);
    }

    @Override
    public void close() throws IOException
    {
        pbis.close();
    }

    @Override
    public int available() throws IOException
    {
        return pbis.available();
    }

    @Override
    public int read() throws IOException
    {
        int c = pbis.read();
        switch(state)
        {
        case 0:
            if(c == '<')
            {
                state = 1;
            }
            break;
        case 1:
            switch(c)
            {
            case 'h':
            case 'H':
            case 'b':
            case 'B':
                state = 2;
                break;
            default:
                state = 0;
            }
            break;
        case 2:
            switch(c)
            {
            case 'r':
            case 'R':
                state = 3;
                break;
            default:
                state = 0;
            }
            break;
        case 3:
            switch(c)
            {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case '>':
                pbis.unread(c);
                c = '/';
                //$FALL-THROUGH$
            default:
                state = 0;
            }
            break;
        default:
            break;
        }
        return c;
    }
}
