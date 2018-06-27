/*
 */

package com.dotfx.pgnutil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mark Chen
 */
public class PGNOpeningMapper extends PGNFile
{
    private final Map<OpeningID,OpeningStats> openingsMap;
    
    public PGNOpeningMapper(String file)
        throws FileNotFoundException, IOException, PGNException
    {
        super(file);
        openingsMap = new HashMap(10000);
        scan(openingsMap);
    }
    
    public PGNOpeningMapper(InputStream is)
        throws IOException, PGNException
    {
        super(is);
        openingsMap = new HashMap(10000);
        scan(openingsMap);
    }
    
    public PGNOpeningMapper(Reader reader)
        throws IOException, PGNException
    {
        super(reader);
        openingsMap = new HashMap(10000);
        scan(openingsMap);
    }
    
    private void scan(Map<OpeningID,OpeningStats> openingsMap)
        throws IOException, PGNException
    {
        for (Game game = nextGame(); game != null; game = nextGame())
        {
            OpeningID openingID = game.getOpeningID();
            OpeningStats stats = openingsMap.get(openingID);

            if (stats == null)
            {
                stats = new OpeningStats(openingID, game.get("ECO"));
                openingsMap.put(openingID, stats);
            }

            stats.count(game);
        }
    }
    
    public Map<OpeningID,OpeningStats> getStats() { return openingsMap; }
}
