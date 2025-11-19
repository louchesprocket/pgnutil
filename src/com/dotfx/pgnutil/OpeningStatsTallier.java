/*
 * The MIT License
 *
 * Copyright (c) 2023 Mark Chen <chen@dotfx.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED
 * "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dotfx.pgnutil;

import com.dotfx.pgnutil.eco.EcoTree;
import com.dotfx.pgnutil.eco.TreeNode;

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
public class OpeningStatsTallier implements Tallier
{
    private class Iterator implements java.util.Iterator<String>
    {
        private final java.util.Iterator<OpeningStats> iterator;
        
        private Iterator()
        {
            List<OpeningStats> selectedOpenings = new ArrayList<>();
            
            nextOpening:
            for (MoveListId oid : openingsMap.keySet())
            {
                OpeningStats opening = openingsMap.get(oid);
                
                for (OpeningProcessors.Processor processor : OpeningProcessors.getOpeningProcessors())
                    if (!processor.processOpening(opening)) continue nextOpening;

                selectedOpenings.add(opening);
            }
            
            iterator = selectedOpenings.iterator();
        }
        
        @Override public boolean hasNext() { return iterator.hasNext(); }
        
        @Override public String next()
        {
            StringBuilder ret = new StringBuilder();
            OpeningStats stats = iterator.next();
            
            if (selectors == null) ret.append("ECO: ").
                    append(stats.getEco(EcoTree.FileType.STD).getCode()).append(CLOptions.outputDelim).
                    append(tallyPosition ? "position: " + stats.getOpeningSt() : "opening: " + stats.getId()).
                    append(CLOptions.outputDelim).append(stats);

            else
            {
                for (OpeningStatsOutputSelector selector : selectors)
                {
                    ret.append(CLOptions.outputDelim);
                    selector.appendOutput(stats, ret);
                }

                ret.delete(0, CLOptions.outputDelim.length());
            }

            return ret.toString();
        }
    }

    private static OpeningStatsTallier instance;
    private static boolean saveOpeningMoves = false;
    private static boolean trackPlies = false;
    private static boolean trackDisagree = false;
    private static OpeningStatsOutputSelector[] selectors;

    private final boolean tallyPosition;
    private final Map<MoveListId, OpeningStats> openingsMap;

    private EcoTree ecoTree;
    private EcoTree scidEcoTree;
    private boolean useXStdEco;
    private boolean useXScidEco;

    private OpeningStatsTallier(boolean tallyPosition)
    {
        openingsMap = new HashMap<>(10000);
        this.tallyPosition = tallyPosition;
        if (tallyPosition) setSaveOpeningMoves(true);
    }

    public static OpeningStatsTallier getInstance(boolean tallyPosition)
    {
        if (instance == null) instance = new OpeningStatsTallier(tallyPosition);
        return instance;
    }

    /**
     * Output selectors must be initialized here so that the tallier knows what to do.
     *
     * @throws SelectorException
     */
    @Override
    public void init(OutputSelector[] selectors) throws SelectorException
    {
        if (selectors == null || selectors.length == 0) ecoTree = EcoTree.FileType.STD.getEcoTree();

        else
        {
            OpeningStatsTallier.selectors = new OpeningStatsOutputSelector[selectors.length];

            for (int i = 0; i < selectors.length; i++)
                OpeningStatsTallier.selectors[i] = new OpeningStatsOutputSelector(selectors[i], this);
        }
    }

    public void setSaveOpeningMoves(boolean saveOpeningMoves) { OpeningStatsTallier.saveOpeningMoves = saveOpeningMoves; }
    public void setTrackDisagree(boolean trackDisagree) { OpeningStatsTallier.trackDisagree = trackDisagree; }
    public void setTrackPlies(boolean trackPlies) { OpeningStatsTallier.trackPlies = trackPlies; }

    public void setUseEco(EcoTree.FileType type)
    {
        if (type == EcoTree.FileType.STD) ecoTree = type.getEcoTree();
        else if (type == EcoTree.FileType.SCIDDB) scidEcoTree = type.getEcoTree();
    }

    public void setUseXEco(EcoTree.FileType type)
    {
        if (type == EcoTree.FileType.STD)
        {
            useXStdEco = true;
            ecoTree = type.getEcoTree();
        }

        else if (type == EcoTree.FileType.SCIDDB)
        {
            useXScidEco = true;
            scidEcoTree = type.getEcoTree();
        }
    }

    private void countGame(PgnGame game, OpeningStats stats)
    {
        switch (game.getResult())
        {
            case WHITEWIN: stats.incWhiteWin(); break;
            case BLACKWIN: stats.incBlackWin(); break;
            case DRAW: stats.incDraw(); break;
            default: stats.incNoResult();
        }

        if (trackDisagree) stats.addDisagree(game.getDisagreeCount());
        if (trackPlies) stats.addOobPlies(game.getPostOpeningPlyCount());
    }

    @Override
    public void tally(PgnGame game) throws IllegalMoveException
    {
        if (tallyPosition) tallyPosition(game);
        else tallyOpening(game);
    }

    private void tallyPosition(PgnGame game) throws IllegalMoveException
    {
        MoveListId openingId = game.getOpeningId(game.getPosMatchAtPly()); // "opening" = moves up to matched position
        OpeningStats stats = openingsMap.get(openingId);

        if (stats == null)
        {
            List<PgnGame.Move> openingMoveList = game.getMoveList().subList(0, game.getPosMatchAtPly());
            if (openingMoveList.isEmpty()) return; // no matched moves

            TreeNode ecoNode = ecoTree != null ? ecoTree.getDeepestDefined(openingMoveList) : null;
            TreeNode scidNode = scidEcoTree != null ? scidEcoTree.getDeepestDefined(openingMoveList) : null;

            stats = new OpeningStats(game.getFullMoveStringToPly(game.getPosMatchAtPly()),
                    openingId, ecoNode, scidNode,
                    useXStdEco ? ecoTree.getDeepestTranspositionSet(openingMoveList) : null,
                    useXScidEco ? scidEcoTree.getDeepestTranspositionSet(openingMoveList) : null);

            openingsMap.put(openingId, stats);
        }

        countGame(game, stats);
    }

    private void tallyOpening(PgnGame game) throws IllegalMoveException
    {
        MoveListId openingId = game.getOpeningId();
        OpeningStats stats = openingsMap.get(openingId);

        if (stats == null)
        {
            List<PgnGame.Move> openingMoveList = game.getOpeningMoveList();
            if (openingMoveList.isEmpty()) return; // no book moves

            TreeNode ecoNode = ecoTree != null ? ecoTree.getDeepestDefined(openingMoveList) : null;
            TreeNode scidNode = scidEcoTree != null ? scidEcoTree.getDeepestDefined(openingMoveList) : null;

            stats = new OpeningStats(saveOpeningMoves ? game.getFullOpeningString() : null,
                    openingId, ecoNode, scidNode,
                    useXStdEco ? ecoTree.getDeepestTranspositionSet(openingMoveList) : null,
                    useXScidEco ? scidEcoTree.getDeepestTranspositionSet(openingMoveList) : null);

            openingsMap.put(openingId, stats);
        }

        countGame(game, stats);
    }

    @Override
    public java.util.Iterator<String> getOutputIterator()
    {
        return new Iterator();
    }
}
