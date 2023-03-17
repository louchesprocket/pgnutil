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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Mark Chen
 */
public class PlayerResults implements Tallier
{
    public static class Score
    {
        private int wins;
        private int losses;
        private int draws;
        private int noResults;

        public Score() {}

        public void incWins() { wins++; }
        public void incLosses() { losses++; }
        public void incDraws() { draws++; }
        public void incNoResults() { noResults++; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getDraws() { return draws; }
        public int getNoResults() { return noResults; }
        public int getGameCount()
        {
            return wins + losses + draws + noResults;
        }
        public double getWinPct()
        {
            return ((double)wins + ((double)draws) / 2) / getGameCount();
        }

        @Override
        public String toString()
        {
            return String.format("+%d=%d-%d", wins, draws, losses);
        }
    }
    
    private class ResultsIterator implements Iterator<String>
    {
        private final Iterator<String> iterator;
        
        private ResultsIterator(Map<String,Score> resultsMap)
        {
            iterator = resultsMap.keySet().iterator();
        }
        
        @Override public boolean hasNext() { return iterator.hasNext(); }
        
        @Override public String next()
        {
            StringBuilder ret = new StringBuilder();
            String player = iterator.next();
            Score score = resultsMap.get(player);
            
            if (selectors == null)
                ret.append(player).append(CLOptions.outputDelim).
                    append(score.toString()).append(CLOptions.outputDelim).
                    append("games: ").append(score.getGameCount()).
                    append(CLOptions.outputDelim).append("win: " ).
                    append(Formats.PERCENT.format(score.getWinPct()));

            else
            {
                for (PlayerResultsOutputSelector selector : selectors)
                {
                    ret.append(CLOptions.outputDelim);
                    selector.appendOutput(player, score, ret);
                }

                ret.delete(0, CLOptions.outputDelim.length());
            }

            return ret.toString();
        }
    }

    private static PlayerResults instance;
    private static PlayerResultsOutputSelector selectors[];

    private final Map<String,Score> resultsMap;
    
    private PlayerResults()
    {
        resultsMap = new TreeMap<>();
    }

    public static PlayerResults getInstance()
    {
        if (instance == null) instance = new PlayerResults();
        return instance;
    }

    @Override
    public void init(OutputSelector selectors[]) throws InvalidSelectorException
    {
        if (selectors != null && selectors.length > 0)
        {
            this.selectors = new PlayerResultsOutputSelector[selectors.length];

            for (int i = 0; i < selectors.length; i++)
                this.selectors[i] = new PlayerResultsOutputSelector(selectors[i]);
        }
    }
    
    @Override
    public void tally(PgnGame game)
    {
        String white = game.getWhite();
        String black = game.getBlack();
        Score whiteScore = resultsMap.get(white);
        Score blackScore = resultsMap.get(black);
        
        if (whiteScore == null)
        {
            whiteScore = new Score();
            resultsMap.put(white, whiteScore);
        }
        
        if (blackScore == null)
        {
            blackScore = new Score();
            resultsMap.put(black, blackScore);
        }
        
        switch (game.getResult())
        {
            case WHITEWIN:
                whiteScore.incWins();
                blackScore.incLosses();
                break;
                
            case BLACKWIN:
                whiteScore.incLosses();
                blackScore.incWins();
                break;
                
            case DRAW:
                whiteScore.incDraws();
                blackScore.incDraws();
                break;
                
            default:
                whiteScore.incNoResults();
                blackScore.incNoResults();
        }
    }

    @Override
    public Iterator<String> getOutputIterator()
    {
        return new ResultsIterator(resultsMap);
    }
}
