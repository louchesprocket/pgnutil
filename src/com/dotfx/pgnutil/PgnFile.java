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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 *
 * @author Mark Chen
 */
public class PgnFile
{
    public static final int COPY_BUF_INIT_SIZE = 32768;
    private final CopyReader reader;
    private final String fileName;
    private int gameCounter;
    
    public PgnFile(String file) throws FileNotFoundException, IOException
    {
        fileName = file;

        reader = new CopyReader(new BufferedReader(new FileReader(file)),
            COPY_BUF_INIT_SIZE);
    }
    
    public PgnFile(InputStream is) throws IOException
    {
        fileName = null;

        reader = new CopyReader(new BufferedReader(new InputStreamReader(is)),
            COPY_BUF_INIT_SIZE);
    }
    
    /**
     * This constructor uses the exact Reader supplied by the caller (except for wrapping it in a CopyReader so that
     * the original game text can be retrieved).
     * 
     * @param reader Reader containing PGN-formatted text
     * @throws IOException 
     */
    public PgnFile(String fileName, BufferedReader reader) throws IOException
    {
        this.fileName = fileName;
        this.reader = new CopyReader(reader, COPY_BUF_INIT_SIZE);
    }
    
    public PgnGame nextGame() throws IOException, PGNException
    {
        PgnGame ret = PgnGame.gameParser.parseNext(fileName, gameCounter + 1, reader);
        if (ret != null) gameCounter++;
        return ret;
    }
    
    public int getGamesRead() { return gameCounter; }
    public long getTotalCharsRead() { return reader.totalCharsRead(); }
}
