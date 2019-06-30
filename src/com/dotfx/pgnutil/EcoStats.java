/*
 * The MIT License
 *
 * Copyright 2019 Mark Chen <chen@dotfx.com>.
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
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class EcoStats implements Tallier
{
    private class Iterator implements java.util.Iterator<String>
    {
        private final List<EcoTree.NodeSet> selectedOpenings;
        private final java.util.Iterator<EcoTree.NodeSet> iterator;
        private final OutputSelector selectors[];
        
        private Iterator(OutputSelector selectors[])
            throws InvalidSelectorException
        {
            selectedOpenings = new ArrayList<>();
            
            nextOpening:
            for (EcoTree.NodeSet nodeSet : openingsMap.keySet())
            { 
                for (OpeningProcessors.Processor processor : openingProcessors)
                    if (!processor.processOpening(openingsMap.get(nodeSet)))
                        continue nextOpening;

                selectedOpenings.add(nodeSet);
            }
            
            iterator = selectedOpenings.iterator();
            
            this.selectors = (selectors == null || selectors.length == 0) ?
                null : selectors;
        }
        
        @Override public boolean hasNext() { return iterator.hasNext(); }
        
        @Override public String next()
        {
            StringBuilder ret = new StringBuilder();
            EcoTree.NodeSet opening = iterator.next();
            OpeningScore score = openingsMap.get(opening);
            
            if (selectors == null)
                ret.append(opening.toString()).append(CLOptions.outputDelim).
                    append(opening.getDesc()).append(CLOptions.outputDelim).
                    append(score.toString());

            else for (int i = 0; i < selectors.length; i++)
            {
                switch (selectors[i].getValue())
                {
                    case CODE: ret.append(opening.toString()); break;
                    case DESC: ret.append(opening.getDesc()); break;
                    case MOVES: ret.append(opening.getMoveString()); break;
                    case COUNT: ret.append(score.getGameCount()); break;
                    case WWINS: ret.append(score.getWhiteWins()); break;
                    case BWINS: ret.append(score.getBlackWins()); break;
                    case DRAWS: ret.append(score.getDraws()); break;

                    case WWINPCT:
                        ret.append(Formats.DECIMAL.format(score.getWhiteWinPct()));
                        break;

                    case BWINPCT:
                        ret.append(Formats.DECIMAL.format(score.getBlackWinPct()));
                        break;

                    case DIFF:
                        ret.append(score.getWhiteWins() - score.getBlackWins());
                        break;

                    case DIFFPCT:
                        ret.append(Formats.DECIMAL.format(score.getWhiteWinPct() -
                            score.getBlackWinPct()));

                        break;

                    case DRAWPCT:
                        ret.append(Formats.DECIMAL.format(score.getDrawPct()));
                }

                if (i < selectors.length - 1) ret.append(CLOptions.outputDelim);
            }

            return ret.toString();
        }
    }
    
    private static final List<OpeningProcessors.Processor> openingProcessors =
        new ArrayList<>();
    
    private final TreeMap<EcoTree.NodeSet,OpeningScore> openingsMap;
    private final EcoTree ecoTree;
    private final boolean transpose;

    public EcoStats(EcoTree.Type type, boolean transpose)
    {
        openingsMap = new TreeMap<>();
        this.transpose = transpose;
        
        if (type == EcoTree.Type.SCID) ecoTree = EcoTree.getScidInstance();
        else ecoTree = EcoTree.getInstance();
    }
    
    @Override
    public void init() throws InvalidSelectorException
    {
        if (PGNUtil.outputSelectors == null) return;
        
        for (OutputSelector selector : PGNUtil.outputSelectors)
        {
            switch (selector.getValue())
            {
                case CODE:
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
    }
    
    static void addOpeningProcessor(OpeningProcessors.Processor op)
    {
        openingProcessors.add(op);
    }

    @Override
    public void tally(PgnGame game) throws IllegalMoveException
    {
        if (CLOptions.maxEloDiff != null)
        {
            Integer whiteElo = PGNUtil.eloMap.get(game.getWhite().trim());
            Integer blackElo = PGNUtil.eloMap.get(game.getBlack().trim());

            if (whiteElo == null || blackElo == null ||
                Math.abs(whiteElo - blackElo) > CLOptions.maxEloDiff)
                return;
        }

        EcoTree.NodeSet nodeSet = transpose ?
            ecoTree.getDeepestTranspositionSet(game) :
            new EcoTree.NodeSet(ecoTree.getDeepestDefined(game));
        
        OpeningScore os = openingsMap.get(nodeSet);

        if (os == null)
        {
            os = new OpeningScore();
            openingsMap.put(nodeSet, os);
        }

        switch (game.getResult())
        {
            case WHITEWIN: os.incWhiteWin(); break;
            case BLACKWIN: os.incBlackWin(); break;
            case DRAW: os.incDraw(); break;
            default: os.incNoResult();
        }
    }

    @Override
    public java.util.Iterator<String> getOutputIterator(OutputSelector selectors[])
        throws InvalidSelectorException
    {
        return new Iterator(selectors);
    }
}
