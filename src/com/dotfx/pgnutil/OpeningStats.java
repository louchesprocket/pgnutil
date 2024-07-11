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
import com.dotfx.pgnutil.eco.TreeNodeSet;

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
    public static class Opening extends OpeningScore
    {
        private final String openingSt;
        private final MoveListId oid;
        private final TreeNode eco;
        private final TreeNode scidEco;
        private final TreeNodeSet xEcoSet;
        private final TreeNodeSet xScidEcoSet;
        
        Opening(String openingSt, MoveListId oid, TreeNode eco, TreeNode scidEco, TreeNodeSet xEcoSet,
                TreeNodeSet xScidEcoSet)
        {
            super();
            this.openingSt = openingSt;
            this.oid = oid;
            this.eco = eco;
            this.scidEco = scidEco;
            this.xEcoSet = xEcoSet;
            this.xScidEcoSet = xScidEcoSet;
        }

        public String getOpeningSt() { return openingSt; }

        public MoveListId getId() { return oid; }

        public TreeNode getEco(EcoTree.FileType type)
        {
            if (type == EcoTree.FileType.STD) return eco;
            if (type == EcoTree.FileType.SCIDDB) return scidEco;
            return null;
        }

        public TreeNodeSet getXEcoSet(EcoTree.FileType type)
        {
            if (type == EcoTree.FileType.STD) return xEcoSet;
            if (type == EcoTree.FileType.SCIDDB) return xScidEcoSet;
            return null;
        }

        @Override
        public String toString()
        {
            return "ECO: " + eco.getCode() + CLOptions.outputDelim + "opening: " + oid + CLOptions.outputDelim +
                    super.toString();
        }
    }
    
    private class Iterator implements java.util.Iterator<String>
    {
        private final List<Opening> selectedOpenings;
        private final java.util.Iterator<Opening> iterator;
        
        private Iterator()
        {
            selectedOpenings = new ArrayList<>();
            
            nextOpening:
            for (MoveListId oid : openingsMap.keySet())
            {
                Opening opening = openingsMap.get(oid);
                
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
            Opening opening = iterator.next();
            
            if (selectors == null) ret.append(opening.toString());

            else
            {
                for (OpeningStatsOutputSelector selector : selectors)
                {
                    ret.append(CLOptions.outputDelim);
                    selector.appendOutput(opening, ret);
                }

                ret.delete(0, CLOptions.outputDelim.length());
            }

            return ret.toString();
        }
    }

    private static boolean saveOpeningMoves = false;
    private static OpeningStatsOutputSelector selectors[];
    private static OpeningStats instance;
    
    private final Map<MoveListId,Opening> openingsMap;
    private EcoTree ecoTree;
    private EcoTree scidEcoTree;
    private boolean useXStdEco;
    private boolean useXScidEco;

    private OpeningStats() { openingsMap = new HashMap<>(10000); }

    public static OpeningStats getInstance()
    {
        if (instance == null) instance = new OpeningStats();
        return instance;
    }

    /**
     * Output selectors must be initialized here so that the tallier knows what to do.
     *
     * @throws InvalidSelectorException
     */
    @Override
    public void init(OutputSelector selectors[]) throws InvalidSelectorException
    {
        if (selectors == null || selectors.length == 0) ecoTree = EcoTree.FileType.STD.getEcoTree();

        else
        {
            this.selectors = new OpeningStatsOutputSelector[selectors.length];

            for (int i = 0; i < selectors.length; i++)
            {
                this.selectors[i] = new OpeningStatsOutputSelector(selectors[i], this);

                if (this.selectors[i].getValue() == OpeningStatsOutputSelector.Value.OPENINGMOVES)
                    saveOpeningMoves = true;
            }
        }
    }

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

    @Override
    public void tally(PgnGame game) throws IllegalMoveException
    {
        MoveListId openingId = game.openingId();
        Opening opening = openingsMap.get(openingId);

        if (opening == null)
        {
            List<PgnGame.Move> openingMoveList = game.getOpeningMoveList();
            if (openingMoveList.isEmpty()) return; // no book moves
            TreeNode ecoNode = ecoTree != null ? ecoTree.getDeepestDefined(openingMoveList) : null;
            TreeNode scidNode = scidEcoTree != null ? scidEcoTree.getDeepestDefined(openingMoveList) : null;

            opening = new Opening(saveOpeningMoves ? game.getFullOpeningString() : null, openingId, ecoNode, scidNode,
                useXStdEco ? ecoTree.getDeepestTranspositionSet(openingMoveList) : null,
                useXScidEco ? scidEcoTree.getDeepestTranspositionSet(openingMoveList) : null);

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
    public java.util.Iterator<String> getOutputIterator()
    {
        return new Iterator();
    }
}
