/*
 */

package com.dotfx.pgnutil;

import java.io.CharArrayWriter;
import java.nio.CharBuffer;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Mark Chen
 */
public class CopyReader extends Reader
{
    private final Reader in;
    private final CharArrayWriter saver;
    private long totalBytesRead;
    
    public CopyReader(Reader in)
    {
        this.in = in;
        saver = new CharArrayWriter();
        totalBytesRead = 0L;
    }
    
    @Override public void close() throws IOException { in.close(); }
    
    @Override public void mark(int limit) throws IOException
    {
        throw new IOException("mark unsupported");
    }
    
    @Override public boolean markSupported() { return false; }
    
    @Override public int read() throws IOException
    {
        int ret = in.read();
        if (ret > -1) saver.write(ret);
        return ret;
    }
    
    @Override public int read(char buf[]) throws IOException
    {
        int ret = in.read(buf);
        if (ret > 0) saver.write(buf);
        return ret;
    }
    
    @Override public int read(char buf[], int off, int len) throws IOException
    {
        int ret = in.read(buf, off, len);
        if (ret > 0) saver.write(buf, off, ret);
        return ret;
    }
    
    @Override public int read(CharBuffer target)
        throws IOException
    {
        int start = target.position();
        int ret = in.read(target);
        if (ret == -1) return ret;
        int end = target.position();
        saver.write(target.toString().substring(start, end));
        return ret;
    }
    
    @Override public boolean ready() throws IOException { return in.ready(); }
    
    @Override public void reset() throws IOException
    { throw new IOException("reset not supported"); }
    
    @Override public long skip(long n) throws IOException { return in.skip(n); }
    
    public String getCopy() { return saver.toString(); }
    public int bytesRead() { return saver.size(); }
    public long totalBytesRead() { return totalBytesRead + saver.size(); }
    
    public void clear()
    {
        totalBytesRead += saver.size();
        saver.reset();
    }
}
