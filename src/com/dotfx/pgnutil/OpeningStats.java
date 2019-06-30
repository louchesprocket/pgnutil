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

/**
 *
 * @author Mark Chen
 * 
 * This class is not thread safe.
 */
public class OpeningStats implements Tallier
{
    private static class Opening extends OpeningScore
    {
        private final MoveListId oid;
        private final EcoTree.Node eco;
        
        Opening(MoveListId oid, EcoTree.Node eco)
        {
            super();
            this.oid = oid;
            this.eco = eco;
        }

        public MoveListId getId() { return oid; }
        public EcoTree.Node getEco() { return eco; }

        @Override
        public String toString()
        {
            return "ECO: " + eco.getCode() + CLOptions.outputDelim +
                "opening: " + oid + CLOptions.outputDelim + super.toString();
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
                
                for (OpeningProcessors.Processor processor : openingProcessors)
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
                    case ECODESC:
                    case ECOMOVES:
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
                    case ECO: ret.append(opening.getEco().getCode()); break;
                    case ECODESC: ret.append(opening.getEco().getDesc()); break;
                    case OID: ret.append(opening.getId()); break;
                    case COUNT: ret.append(opening.getGameCount()); break;
                    case WWINS: ret.append(opening.getWhiteWins()); break;
                    case BWINS: ret.append(opening.getBlackWins()); break;
                    case DRAWS: ret.append(opening.getDraws()); break;
                    
                    case ECOMOVES:
                        for (EcoTree.Node node : opening.getEco().getPath())
                        {
                            int ply = node.getPly();
                            if (ply != 1) ret.append(" ");

                            if (ply % 2 == 1)
                                ret.append(((ply + 1)/2)).append(".");

                            ret.append(node.getMove());
                        }
                        
                        break;

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
    
    private static final List<OpeningProcessors.Processor> openingProcessors =
        new ArrayList<>();
    
    private final Map<MoveListId,Opening> openingsMap;
    private final EcoTree ecoTree;

    public OpeningStats()
    {
        openingsMap = new HashMap<>(10000);
        ecoTree = EcoTree.getInstance();
    }
    
    static void addOpeningProcessor(OpeningProcessors.Processor op)
    {
        openingProcessors.add(op);
    }

    @Override
    public void tally(PgnGame game)
    {
        if (CLOptions.maxEloDiff != null)
        {
            Integer whiteElo = PGNUtil.eloMap.get(game.getWhite().trim());
            Integer blackElo = PGNUtil.eloMap.get(game.getBlack().trim());

            if (whiteElo == null || blackElo == null ||
                Math.abs(whiteElo - blackElo) > CLOptions.maxEloDiff)
                return;
        }

        MoveListId openingId = game.openingId();
        Opening opening = openingsMap.get(openingId);

        if (opening == null)
        {
            PgnGame.Move firstOob = game.getFirstOobMove();
            if (firstOob == null) return;
            
            opening = new Opening(openingId,
                ecoTree.get(game, firstOob.getPly() - 1));
            
            openingsMap.put(openingId, opening);
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
