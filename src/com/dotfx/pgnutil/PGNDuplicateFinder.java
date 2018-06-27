/*
 */

package com.dotfx.pgnutil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.hash.HashCode;

/**
 *
 * @author Mark Chen
 */
public class PGNDuplicateFinder extends PGNFile
{
    private final Map<HashCode,List<Integer>> gameMap;
    private final Set<HashCode> duplicates;
    
    public PGNDuplicateFinder(String file)
        throws FileNotFoundException, IOException, PGNException
    {
        super(file);
        gameMap = new HashMap(100000);
        duplicates = new HashSet();
        scan(gameMap, duplicates);
    }
    
    public PGNDuplicateFinder(InputStream is)
        throws IOException, PGNException
    {
        super(is);
        gameMap = new HashMap(100000);
        duplicates = new HashSet();
        scan(gameMap, duplicates);
    }
    
    public PGNDuplicateFinder(Reader reader)
        throws IOException, PGNException
    {
        super(reader);
        gameMap = new HashMap(100000);
        duplicates = new HashSet();
        scan(gameMap, duplicates);
    }
    
    private void scan(Map<HashCode,List<Integer>> gameMap,
        Set<HashCode> duplicates)
        throws IOException, PGNException
    {
        for (Game game = nextGame(); game != null; game = nextGame())
        {
            HashCode gameHash = game.getHash();
            List<Integer> gameIdxes = gameMap.get(gameHash);

            if (gameIdxes != null) duplicates.add(gameHash);

            else
            {
                gameIdxes = new ArrayList();
                gameMap.put(gameHash, gameIdxes);
            }

            gameIdxes.add((int)game.getNumber());
        }
    }
    
    public List<List<Integer>> getDuplicates()
    {
        List<List<Integer>> ret = new ArrayList();
        for (HashCode duplicate : duplicates) ret.add(gameMap.get(duplicate));
        return ret;
    }
}
