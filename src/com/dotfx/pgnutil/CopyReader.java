/*
 * The MIT License
 *
 * Copyright 2018 Mark Chen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dotfx.pgnutil;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 *
 * @author Mark Chen
 */
public class CopyReader extends Reader
{
    private final Reader in;
    private int markLimit;
    private long charsRead; // count since last mark
    private long prevCharsRead; // count up to last mark
    
    /**
     * 
     * @param in must support mark() and reset() operations
     * @param outBufSize
     * @throws IOException 
     */
    public CopyReader(Reader in, int outBufSize) throws IOException
    {
        this.in = in;
        markLimit = outBufSize;
        in.mark(markLimit);
    }
    
    /**
     * This is only called rarely.
     * 
     * @param minRequired smallest required read-ahead limit
     */
    private void increaseMarkLimit(long minRequired) throws IOException
    {
        in.reset();
        markLimit += Math.max(minRequired - markLimit, markLimit);
        in.mark(markLimit);
        in.skip(charsRead);
    }
    
    @Override public boolean ready() throws IOException { return in.ready(); }
    @Override public void close() throws IOException { in.close(); }
    @Override public boolean markSupported() { return in.markSupported(); }
    
    @Override public int read() throws IOException
    {
        long span = charsRead + 1;
        if (span >= markLimit) increaseMarkLimit(span);

        int ret = in.read();
        if (ret > -1) charsRead++;
        return ret;
    }
    
    @Override public int read(char buf[]) throws IOException
    {
        long span = charsRead + buf.length;
        if (span >= markLimit) increaseMarkLimit(span);

        int ret = in.read(buf);
        if (ret > 0) charsRead += ret;
        return ret;
    }
    
    @Override public int read(char buf[], int off, int len) throws IOException
    {
        long span = charsRead + len;
        if (span >= markLimit) increaseMarkLimit(span);

        int ret = in.read(buf, off, len);
        if (ret > 0) charsRead += ret;
        return ret;
    }
    
    @Override public int read(CharBuffer target)
        throws IOException
    {
        long span = charsRead + target.length();
        if (span >= markLimit) increaseMarkLimit(span);

        int ret = in.read(target);
        if (ret > 0) charsRead += ret;
        return ret;
    }
    
    @Override public void mark(int limit) throws IOException
    {
        prevCharsRead += charsRead;
        charsRead = 0L;
        markLimit = Math.max(limit, markLimit);
        in.mark(markLimit);
    }
    
    @Override public void reset() throws IOException
    {
        in.reset();
        charsRead = 0L;
    }
    
    @Override public long skip(long n) throws IOException
    {
        long ret = in.skip(n);
        charsRead += ret;
        return ret;
    }
    
    public String getCopy() throws EOFException, IOException
    {
        char buf[] = new char[(int)charsRead];
        int total = 0;
        in.reset();
        
        do
        {
            int read = in.read(buf, total, (int)charsRead - total);
            if (read == -1) throw new EOFException();
            total += read;
        }
        while (total < charsRead);
        
        prevCharsRead += charsRead;
        charsRead = 0L;
        in.mark(markLimit);
        
        return new String(buf);
    }
    
    public long charsRead() { return charsRead; }
    public long totalCharsRead() { return prevCharsRead + charsRead; }
}
