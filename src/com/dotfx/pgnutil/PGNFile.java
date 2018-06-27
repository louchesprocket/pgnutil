/*
 */

package com.dotfx.pgnutil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Mark Chen
 */
public class PGNFile
{
    private final CopyReader reader;
    private int gameCounter;
    
    public PGNFile(String file) throws FileNotFoundException
    {
        reader = new CopyReader(new BufferedReader(new FileReader(file)));
        gameCounter = 0;
    }
    
    public PGNFile(InputStream is)
    {
        reader = new CopyReader(new BufferedReader(new InputStreamReader(is)));
        gameCounter = 0;
    }
    
    /**
     * This constructor uses the exact Reader supplied by the caller (except
     * for wrapping it in a CopyReader so that the original game text can be
     * retrieved).
     * 
     * @param reader Reader containing PGN-formatted text
     */
    public PGNFile(Reader reader)
    {
        this.reader = new CopyReader(reader);
        gameCounter = 0;
    }
    
    public final Game nextGame() throws IOException, PGNException
    {
        reader.clear();
        return Game.parseNext(++gameCounter, reader);
    }
}
