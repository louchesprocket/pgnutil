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

import com.dotfx.pgnutil.eco.TreeNodeSet;

import java.util.*;

public class EcoStatsOutputSelector
{
    interface OutputHandler
    {
        void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb);
    }

    private static final class BlackWinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(os.getBlackWinPct()));
        }
    }

    private static final class BlackWinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(os.getBlackWins());
        }
    }

    private static final class CountOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(os.getGameCount());
        }
    }

    private static final class DiffOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(os.getWhiteWins() - os.getBlackWins());
        }
    }

    private static final class DiffPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(os.getWhiteWinPct() - os.getBlackWinPct()));
        }
    }

    private static final class DrawPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(os.getDrawPct()));
        }
    }

    private static final class DrawsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(os.getDraws());
        }
    }

    private static final class WhiteWinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(os.getWhiteWinPct()));
        }
    }

    private static final class WhiteWinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(os.getWhiteWins());
        }
    }

    private static final class EcoOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(opening.getCode());
        }
    }

    private static final class EcoDescOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(opening.getDesc());
        }
    }

    private static final class EcoMovesOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
        {
            sb.append(opening.getMoveString());
        }
    }

    public enum Value
    {
        BWINPCT(OutputSelector.Value.BWINPCT, new BlackWinPctOutputHandler()),
        BWINS(OutputSelector.Value.BWINS, new BlackWinsOutputHandler()),
        COUNT(OutputSelector.Value.COUNT, new CountOutputHandler()), // also applies to player results
        DIFF(OutputSelector.Value.DIFF, new DiffOutputHandler()),
        DIFFPCT(OutputSelector.Value.DIFFPCT, new DiffPctOutputHandler()),
        DRAWPCT(OutputSelector.Value.DRAWPCT, new DrawPctOutputHandler()),
        DRAWS(OutputSelector.Value.DRAWS, new DrawsOutputHandler()), // also applies to player results
        WWINPCT(OutputSelector.Value.WWINPCT, new WhiteWinPctOutputHandler()),
        WWINS(OutputSelector.Value.WWINS, new WhiteWinsOutputHandler()),

        ECO(OutputSelector.Value.ECO, new EcoOutputHandler()),
        ECODESC(OutputSelector.Value.ECODESC, new EcoDescOutputHandler()),
        ECOMOVES(OutputSelector.Value.ECOMOVES, new EcoMovesOutputHandler());

        private static final Map<OutputSelector.Value,Value> sigMap = new HashMap<>();
        private final OutputSelector.Value signifier;
        private final OutputHandler outputHandler;

        static { for (Value v : Value.values()) sigMap.put(v.signifier, v); }

        Value(OutputSelector.Value signifier, OutputHandler outputHandler)
        {
            this.signifier = signifier;
            this.outputHandler = outputHandler;
        }

        @Override public String toString() { return signifier.toString(); }

        public static Value get(OutputSelector.Value signifier)
        {
            if (signifier == null) return null;
            return sigMap.get(signifier);
        }

        public OutputHandler getOutputHandler() { return outputHandler; }
    }

    private final Value value;
    private final OutputHandler handler;

    public EcoStatsOutputSelector(OutputSelector selector) throws SelectorException
    {
        value = Value.get(selector.getValue());

        if (value == null)
            throw new SelectorException("output selector '" + selector + "' is invalid in this context");

        handler = value.getOutputHandler();
    }

    public Value getValue() { return value; }

    public void appendOutput(TreeNodeSet opening, AggregateScore os, StringBuilder sb)
    {
        handler.appendOutput(opening, os, sb);
    }
}
