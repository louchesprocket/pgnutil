/*
 */

package com.dotfx.pgnutil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mark Chen
 */
public class PGNEventMapper extends PGNFile
{
    private final Map<String,List<Integer>> eventMap;
    
    public PGNEventMapper(String file)
        throws FileNotFoundException, IOException, PGNException
    {
        super(file);
        eventMap = new HashMap();
        scan(eventMap);
    }
    
    public PGNEventMapper(InputStream is)
        throws IOException, PGNException
    {
        super(is);
        eventMap = new HashMap();
        scan(eventMap);
    }
    
    public PGNEventMapper(Reader reader)
        throws IOException, PGNException
    {
        super(reader);
        eventMap = new HashMap();
        scan(eventMap);
    }
    
    private void scan(Map<String,List<Integer>> eventMap)
        throws IOException, PGNException
    {
        for (Game game = nextGame(); game != null; game = nextGame())
        {
            String event = game.getValue("Event");
            List<Integer> eventGames = eventMap.get(event);

            if (eventGames == null)
            {
                eventGames = new ArrayList();
                eventMap.put(event, eventGames);
            }

            eventGames.add((int)game.getNumber());
        }
    }
    
    public Map<String,List<Integer>> getEventMap() { return eventMap; }
}
