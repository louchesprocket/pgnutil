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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Mark Chen
 * 
 * This class is not thread safe.
 */
public class OpeningStats implements Tallier
{
    private static interface OpeningProcessor
    {
        /**
         * 
         * @return true if we should continue processing this opening, false if
         * we should skip it
         */
        public boolean processOpening(Opening opening);
    }
    
    static class MinGamesProcessor implements OpeningProcessor
    {
        private final int minGames;
        
        public MinGamesProcessor(int minGames) { this.minGames = minGames; }
        
        @Override public boolean processOpening(Opening opening)
        {
            return opening.getGameCount() >= minGames;
        }
    }
    
    static class MaxWinDiffProcessor implements OpeningProcessor
    {
        private final double maxDiff;
        
        public MaxWinDiffProcessor(double maxDiff) { this.maxDiff = maxDiff; }
        
        @Override public boolean processOpening(Opening opening)
        {
            return opening.getWhiteWinPct() - opening.getBlackWinPct() <= maxDiff;
        }
    }
    
    static class MinWinDiffProcessor implements OpeningProcessor
    {
        private final double minDiff;
        
        public MinWinDiffProcessor(double minDiff) { this.minDiff = minDiff; }
        
        @Override public boolean processOpening(Opening opening)
        {
            return opening.getWhiteWinPct() - opening.getBlackWinPct() >= minDiff;
        }
    }
    
    static class MinDrawProcessor implements OpeningProcessor
    {
        private final double minDraw;
        
        public MinDrawProcessor(double minDraw) { this.minDraw = minDraw; }
        
        @Override public boolean processOpening(Opening opening)
        {
            return opening.getDrawPct() >= minDraw;
        }
    }
    
    static class MaxDrawProcessor implements OpeningProcessor
    {
        private final double maxDraw;
        
        public MaxDrawProcessor(double maxDraw) { this.maxDraw = maxDraw; }
        
        @Override public boolean processOpening(Opening opening)
        {
            return opening.getDrawPct() <= maxDraw;
        }
    }
    
    private static class Opening
    {
        private final MoveListId oid;
        private final String eco;
        private int whiteWins;
        private int blackWins;
        private int draws;
        private int noResult;
        
        Opening(MoveListId oid, String eco)
        {
            this.oid = oid;
            this.eco = eco;
            whiteWins = blackWins = draws = noResult = 0;
        }

        public void incWhiteWin() { whiteWins++; }
        public void incBlackWin() { blackWins++; }
        public void incDraw() { draws++; }
        public void incNoResult() { noResult++; }
        public MoveListId getId() { return oid; }
        public String getEco() { return eco; }
        public int getWhiteWins() { return whiteWins; }
        public int getBlackWins() { return blackWins; }
        public int getDraws() { return draws; }
        public int getNoResults() { return noResult; }

        public int getGameCount()
        {
            return whiteWins + blackWins + draws;
        }

        public double getWhiteWinPct()
        {
            return (double)whiteWins / (whiteWins + blackWins + draws);
        }

        public double getBlackWinPct()
        {
            return (double)blackWins / (whiteWins + blackWins + draws);
        }

        public double getDrawPct()
        {
            return (double)draws / (whiteWins + blackWins + draws);
        }

        @Override
        public String toString()
        {
            double whiteWinPct = getWhiteWinPct();
            double blackWinPct = getBlackWinPct();

            return "ECO: " + eco + "; " + "opening: " + oid + "; " + 
                "games: " + getGameCount() + "; " +
                "white: " + Formats.PERCENT.format(whiteWinPct) + "; " +
                "black: " + Formats.PERCENT.format(blackWinPct) + "; " +
                "diff: " + Formats.PERCENT.format(whiteWinPct - blackWinPct) + "; " +
                "draw: " + Formats.PERCENT.format(getDrawPct());
        }
    }
    
    private class Iterator implements java.util.Iterator<String>
    {
        private final List<Opening> selectedOpenings;
        private final java.util.Iterator<Opening> iterator;
        private final OutputSelector selectors[];
        
        private Iterator(Map<MoveListId,Opening> openingsMap,
            OutputSelector selectors[])
            throws InvalidSelectorException
        {
            selectedOpenings = new ArrayList<>();
            
            nextOpening:
            for (MoveListId oid : openingsMap.keySet())
            {
                Opening opening = openingsMap.get(oid);
                
                for (OpeningProcessor processor : openingProcessors)
                    if (!processor.processOpening(opening))
                        continue nextOpening;

                selectedOpenings.add(opening);
            }
            
            iterator = selectedOpenings.iterator();
            
            if (selectors == null || selectors.length == 0)
            {
                this.selectors = null;
                return;
            }
            
            for (OutputSelector selector : selectors)
            {
                switch (selector.getValue())
                {
                    case ECO:
                    case OID:
                    case COUNT:
                    case WWINS:
                    case BWINS:
                    case DRAWS:
                    case WWINPCT:
                    case BWINPCT:
                    case DIFF:
                    case DIFFPCT:
                    case DRAWPCT:
                        break;

                    default:
                        throw new InvalidSelectorException("'" + selector +
                            "' is not a valid selector in this context");
                }
            }
            
            this.selectors = selectors;
        }
        
        @Override public boolean hasNext() { return iterator.hasNext(); }
        
        @Override public String next()
        {
            StringBuilder ret = new StringBuilder();
            Opening opening = iterator.next();
            
            if (selectors == null) ret.append(opening.toString());

            else for (int i = 0; i < selectors.length; i++)
            {
                switch (selectors[i].getValue())
                {
                    case ECO: ret.append(opening.getEco()); break;
                    case OID: ret.append(opening.getId()); break;
                    case COUNT: ret.append(opening.getGameCount()); break;
                    case WWINS: ret.append(opening.getWhiteWins()); break;
                    case BWINS: ret.append(opening.getBlackWins()); break;
                    case DRAWS: ret.append(opening.getDraws()); break;

                    case WWINPCT:
                        ret.append(Formats.DECIMAL.format(opening.getWhiteWinPct()));
                        break;

                    case BWINPCT:
                        ret.append(Formats.DECIMAL.format(opening.getBlackWinPct()));
                        break;

                    case DIFF:
                        ret.append(opening.getWhiteWins() - opening.getBlackWins());
                        break;

                    case DIFFPCT:
                        ret.append(Formats.DECIMAL.format(opening.getWhiteWinPct() -
                            opening.getBlackWinPct()));

                        break;

                    case DRAWPCT:
                        ret.append(Formats.DECIMAL.format(opening.getDrawPct()));
                }

                if (i < selectors.length - 1) ret.append(CLOptions.outputDelim);
            }

            return ret.toString();
        }
    }
    
    private static final List<OpeningProcessor> openingProcessors =
        new ArrayList<>();
    
    private final Map<MoveListId,Opening> openingsMap;

    public OpeningStats()
    {
        openingsMap = new HashMap<>(10000);
    }
    
    static void addOpeningProcessor(OpeningProcessor op)
    {
        openingProcessors.add(op);
    }

    @Override
    public void tally(Game game)
    {
        if (CLOptions.maxEloDiff != null)
        {
            Integer whiteElo = PGNUtil.eloMap.get(game.getWhite().trim());
            Integer blackElo = PGNUtil.eloMap.get(game.getBlack().trim());

            if (whiteElo == null || blackElo == null ||
                Math.abs(whiteElo - blackElo) > CLOptions.maxEloDiff)
                return;
        }

        MoveListId openingID = game.getOpeningID();
        Opening opening = openingsMap.get(openingID);

        if (opening == null)
        {
            try { opening = new Opening(openingID, game.get(OutputSelector.ECO)); }
            
            catch (InvalidSelectorException e) // can't happen
            {
                e.printStackTrace();
                System.exit(-1);
            }
            
            openingsMap.put(openingID, opening);
        }

        switch (game.getResult())
        {
            case WHITEWIN: opening.incWhiteWin(); break;
            case BLACKWIN: opening.incBlackWin(); break;
            case DRAW: opening.incDraw(); break;
            default: opening.incNoResult();
        }
    }

    @Override
    public java.util.Iterator<String> getOutputIterator(OutputSelector selectors[])
        throws InvalidSelectorException
    {
        return new Iterator(openingsMap, selectors);
    }
}
