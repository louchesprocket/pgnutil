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
import com.dotfx.pgnutil.eco.TreeNodeSet;

import java.util.HashMap;
import java.util.Map;

public class OpeningStatsOutputSelector
{
    interface OutputHandler
    {
        void appendOutput(OpeningStats.Opening opening, StringBuilder sb);
        void configTallier(OpeningStats os);
    }

    private static final class BlackWinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.DECIMAL.format(opening.getBlackWinPct()));
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class BlackWinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getBlackWins());
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class CountOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getGameCount());
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class DiffOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getWhiteWins() - opening.getBlackWins());
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class DiffPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.DECIMAL.format(opening.getWhiteWinPct() - opening.getBlackWinPct()));
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class DrawPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.DECIMAL.format(opening.getDrawPct()));
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class DrawsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getDraws());
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class WhiteWinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.DECIMAL.format(opening.getWhiteWinPct()));
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class WhiteWinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getWhiteWins());
        }

        @Override
        public void configTallier(OpeningStats os) {}
    }

    private static final class EcoOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        EcoOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getEco(type).getCode());
        }

        @Override
        public void configTallier(OpeningStats os) { os.setUseEco(type); }
    }

    private static final class EcoDescOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        EcoDescOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getEco(type).getDesc());
        }

        @Override
        public void configTallier(OpeningStats os) { os.setUseEco(type); }
    }

    private static final class EcoMovesOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        EcoMovesOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(new TreeNodeSet(opening.getEco(type)).getMoveString());
        }

        @Override
        public void configTallier(OpeningStats os) { os.setUseEco(type); }
    }

    private static final class XEcoOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getXEcoSet(type));
        }

        @Override
        public void configTallier(OpeningStats os) { os.setUseXEco(type); }
    }

    private static final class XEcoDescOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoDescOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getXEcoSet(type).getDesc());
        }

        @Override
        public void configTallier(OpeningStats os) { os.setUseXEco(type); }
    }

    private static final class XEcoMovesOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoMovesOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getXEcoSet(type).getMoveString());
        }

        @Override
        public void configTallier(OpeningStats os) { os.setUseXEco(type); }
    }

    public enum Value
    {
        BWINPCT("bwinpct", new BlackWinPctOutputHandler()),
        BWINS("bwins", new BlackWinsOutputHandler()),
        COUNT("count", new CountOutputHandler()), // also applies to player results
        DIFF("diff", new DiffOutputHandler()),
        DIFFPCT("diffpct", new DiffPctOutputHandler()),
        DRAWPCT("drawpct", new DrawPctOutputHandler()),
        DRAWS("draws", new DrawsOutputHandler()), // also applies to player results
        WWINPCT("wwinpct", new WhiteWinPctOutputHandler()),
        WWINS("wwins", new WhiteWinsOutputHandler()),

        STDECO("stdeco", new EcoOutputHandler(EcoTree.FileType.STD)),
        STDECODESC("stdecodesc", new EcoDescOutputHandler(EcoTree.FileType.STD)),
        STDECOMOVES("stdecomoves", new EcoMovesOutputHandler(EcoTree.FileType.STD)),

        SCIDECO("scideco", new EcoOutputHandler(EcoTree.FileType.SCIDDB)),
        SCIDECODESC("scidecodesc", new EcoDescOutputHandler(EcoTree.FileType.SCIDDB)),
        SCIDECOMOVES("scidecomoves", new EcoMovesOutputHandler(EcoTree.FileType.SCIDDB)),

        XSTDECO("xstdeco", new XEcoOutputHandler(EcoTree.FileType.STD)),
        XSTDECODESC("xstdecodesc", new XEcoDescOutputHandler(EcoTree.FileType.STD)),
        XSTDECOMOVES("xstdecomoves", new XEcoMovesOutputHandler(EcoTree.FileType.STD)),

        XSCIDECO("xscideco", new XEcoOutputHandler(EcoTree.FileType.SCIDDB)),
        XSCIDECODESC("xscidecodesc", new XEcoDescOutputHandler(EcoTree.FileType.SCIDDB)),
        XSCIDECOMOVES("xscidecomoves", new XEcoMovesOutputHandler(EcoTree.FileType.SCIDDB));

        private static final Map<String,Value> sigMap = new HashMap<>();
        private final String signifier;
        private final OutputHandler outputHandler;

        static { for (Value v : Value.values()) sigMap.put(v.toString(), v); }

        Value(String signifier, OutputHandler outputHandler)
        {
            this.signifier = signifier;
            this.outputHandler = outputHandler;
        }

        @Override public String toString() { return signifier; }

        public static Value get(String signifier)
        {
            if (signifier == null) return null;
            return sigMap.get(signifier.toLowerCase());
        }

        public OutputHandler getOutputHandler() { return outputHandler; }
    }

    private final Value value;
    private final OutputHandler handler;

    public OpeningStatsOutputSelector(OutputSelector selector, OpeningStats os) throws InvalidSelectorException
    {
        Value v = Value.get(selector.toString());

        if (v != null)
        {
            value = v;
            handler = value.getOutputHandler();
            handler.configTallier(os);
        }

        else throw new InvalidSelectorException("output selector '" + selector.getValue() +
                "' is invalid in this context");
    }

    public Value getValue() { return value; }
    public void appendOutput(OpeningStats.Opening opening, StringBuilder sb)
    {
        handler.appendOutput(opening, sb);
    }
}
