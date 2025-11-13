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

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class OpeningProcessors
{
    public interface Processor
    {
        /**
         * 
         * @param score
         * @return true if we should continue processing this opening, false if
         *         we should skip it
         */
        public boolean processOpening(AggregateScore score);
    }

    static class MinGamesProcessor implements Processor
    {
        private final int minGames;
        
        public MinGamesProcessor(int minGames) { this.minGames = minGames; }
        
        @Override public boolean processOpening(AggregateScore opening)
        {
            return opening.getGameCount() >= minGames;
        }
    }
    
    static class MaxWinDiffProcessor implements Processor
    {
        private final double maxDiff;
        
        public MaxWinDiffProcessor(double maxDiff) { this.maxDiff = maxDiff; }
        
        @Override public boolean processOpening(AggregateScore opening)
        {
            return opening.getWhiteWinPct() - opening.getBlackWinPct() <= maxDiff;
        }
    }
    
    static class MinWinDiffProcessor implements Processor
    {
        private final double minDiff;
        
        public MinWinDiffProcessor(double minDiff) { this.minDiff = minDiff; }
        
        @Override public boolean processOpening(AggregateScore opening)
        {
            return opening.getWhiteWinPct() - opening.getBlackWinPct() >= minDiff;
        }
    }
    
    static class MinDrawProcessor implements Processor
    {
        private final double minDraw;
        
        public MinDrawProcessor(double minDraw) { this.minDraw = minDraw; }
        
        @Override public boolean processOpening(AggregateScore opening)
        {
            return opening.getDrawPct() >= minDraw;
        }
    }
    
    static class MaxDrawProcessor implements Processor
    {
        private final double maxDraw;
        
        public MaxDrawProcessor(double maxDraw) { this.maxDraw = maxDraw; }
        
        @Override public boolean processOpening(AggregateScore opening)
        {
            return opening.getDrawPct() <= maxDraw;
        }
    }

    private static final List<Processor> openingProcessors = new ArrayList<>();

    static void addOpeningProcessor(OpeningProcessors.Processor op)
    {
        openingProcessors.add(op);
    }

    public static List<Processor> getOpeningProcessors() { return openingProcessors; }
}
