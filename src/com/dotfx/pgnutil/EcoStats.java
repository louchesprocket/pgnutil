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

import com.dotfx.pgnutil.eco.EcoTree;
import com.dotfx.pgnutil.eco.TreeNodeSet;

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
        private final java.util.Iterator<TreeNodeSet> iterator;
        
        private Iterator()
        {
            List<TreeNodeSet> selectedOpenings = new ArrayList<>();
            
            nextOpening:
            for (TreeNodeSet treeNodeSet : openingsMap.keySet())
            { 
                for (OpeningProcessors.Processor processor : OpeningProcessors.getOpeningProcessors())
                    if (!processor.processOpening(openingsMap.get(treeNodeSet)))
                        continue nextOpening;

                selectedOpenings.add(treeNodeSet);
            }
            
            iterator = selectedOpenings.iterator();
        }
        
        @Override public boolean hasNext() { return iterator.hasNext(); }
        
        @Override public String next()
        {
            StringBuilder ret = new StringBuilder();
            TreeNodeSet opening = iterator.next();
            OpeningScore score = openingsMap.get(opening);
            
            if (selectors == null)
                ret.append(opening.toString()).append(CLOptions.outputDelim).
                    append(opening.getDesc()).append(CLOptions.outputDelim).
                    append(score.toString());

            else
            {
                for (EcoStatsOutputSelector selector : selectors)
                {
                    ret.append(CLOptions.outputDelim);
                    selector.appendOutput(opening, score, ret);
                }

                ret.delete(0, CLOptions.outputDelim.length());
            }

            return ret.toString();
        }
    }

    private static EcoStatsOutputSelector selectors[];
    private static EcoStats instance;
    
    private final TreeMap<TreeNodeSet,OpeningScore> openingsMap;
    private final EcoTree ecoTree;
    private final boolean transpose;

    private EcoStats(EcoTree.FileType type, boolean transpose)
    {
        openingsMap = new TreeMap<>();
        ecoTree = type.getEcoTree();
        this.transpose = transpose;
    }

    public static EcoStats getInstance(EcoTree.FileType type, boolean transpose)
    {
        if (instance == null) instance = new EcoStats(type, transpose);
        return instance;
    }
    
    @Override
    public void init(OutputSelector selectors[]) throws SelectorException
    {
        if (selectors != null && selectors.length > 0)
        {
            EcoStats.selectors = new EcoStatsOutputSelector[selectors.length];
            for (int i = 0; i < selectors.length; i++) EcoStats.selectors[i] = new EcoStatsOutputSelector(selectors[i]);
        }
    }

    @Override
    public void tally(PgnGame game) throws IllegalMoveException
    {
        TreeNodeSet treeNodeSet = transpose ? ecoTree.getDeepestTranspositionSet(game.getOpeningMoveList()) :
                new TreeNodeSet(ecoTree.getDeepestDefined(game.getOpeningMoveList()));

        OpeningScore os = openingsMap.computeIfAbsent(treeNodeSet, k -> new OpeningScore());

        switch (game.getResult())
        {
            case WHITEWIN: os.incWhiteWin(); break;
            case BLACKWIN: os.incBlackWin(); break;
            case DRAW: os.incDraw(); break;
            default: os.incNoResult();
        }
    }

    @Override
    public java.util.Iterator<String> getOutputIterator()
    {
        return new Iterator();
    }
}
