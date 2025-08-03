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
        void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb);
        default void configTallier(OpeningStatsTallier os) {}
    }

    private static final class OpeningStOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getOpeningSt());
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setSaveOpeningMoves(true); }
    }

    private static final class OidOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getId());
        }
    }

    private static final class BlackWinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(opening.getBlackWinPct()));
        }
    }

    private static final class BlackWinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getBlackWins());
        }
    }

    private static final class CountOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getGameCount());
        }
    }

    private static final class DiffOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getWhiteWins() - opening.getBlackWins());
        }
    }

    private static final class DiffPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(opening.getWhiteWinPct() - opening.getBlackWinPct()));
        }
    }

    private static final class DrawPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(opening.getDrawPct()));
        }
    }

    private static final class DrawsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getDraws());
        }
    }

    private static final class WhiteWinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(opening.getWhiteWinPct()));
        }
    }

    private static final class WhiteWinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getWhiteWins());
        }
    }

    private static final class DisagreeOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(opening.getDisagreePct()));
        }

        @Override
        public void configTallier(OpeningStatsTallier os)
        {
            os.setTrackDisagree(true);
            os.setTrackPlies(true);
        }
    }

    private static final class AvgPliesOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(Formats.DECIMAL.format(opening.getAvgOobPlies()));
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setTrackPlies(true); }
    }

    private static final class EcoOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        EcoOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getEco(type).getCode());
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setUseEco(type); }
    }

    private static final class EcoDescOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        EcoDescOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getEco(type).getDesc());
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setUseEco(type); }
    }

    private static final class EcoMovesOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        EcoMovesOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(new TreeNodeSet(opening.getEco(type)).getMoveString());
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setUseEco(type); }
    }

    private static final class XEcoOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getXEcoSet(type));
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setUseXEco(type); }
    }

    private static final class XEcoDescOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoDescOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getXEcoSet(type).getDesc());
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setUseXEco(type); }
    }

    private static final class XEcoMovesOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoMovesOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            sb.append(opening.getXEcoSet(type).getMoveString());
        }

        @Override
        public void configTallier(OpeningStatsTallier os) { os.setUseXEco(type); }
    }

    public enum Value
    {
        OPENINGMOVES(OutputSelector.Value.OPENINGMOVES, new OpeningStOutputHandler()),
        OID(OutputSelector.Value.OID, new OidOutputHandler()),
        BWINPCT(OutputSelector.Value.BWINPCT, new BlackWinPctOutputHandler()),
        BWINS(OutputSelector.Value.BWINS, new BlackWinsOutputHandler()),
        COUNT(OutputSelector.Value.COUNT, new CountOutputHandler()), // also applies to player results
        DIFF(OutputSelector.Value.DIFF, new DiffOutputHandler()),
        DIFFPCT(OutputSelector.Value.DIFFPCT, new DiffPctOutputHandler()),
        DRAWPCT(OutputSelector.Value.DRAWPCT, new DrawPctOutputHandler()),
        DRAWS(OutputSelector.Value.DRAWS, new DrawsOutputHandler()), // also applies to player results
        WWINPCT(OutputSelector.Value.WWINPCT, new WhiteWinPctOutputHandler()),
        WWINS(OutputSelector.Value.WWINS, new WhiteWinsOutputHandler()),
        DISAGREEPCT(OutputSelector.Value.DISAGREEPCT, new DisagreeOutputHandler()),
        AVGPLIES(OutputSelector.Value.AVGPLIES, new AvgPliesOutputHandler()),

        STDECO(OutputSelector.Value.STDECO, new EcoOutputHandler(EcoTree.FileType.STD)),
        STDECODESC(OutputSelector.Value.STDECODESC, new EcoDescOutputHandler(EcoTree.FileType.STD)),
        STDECOMOVES(OutputSelector.Value.STDECOMOVES, new EcoMovesOutputHandler(EcoTree.FileType.STD)),

        SCIDECO(OutputSelector.Value.SCIDECO, new EcoOutputHandler(EcoTree.FileType.SCIDDB)),
        SCIDECODESC(OutputSelector.Value.SCIDECODESC, new EcoDescOutputHandler(EcoTree.FileType.SCIDDB)),
        SCIDECOMOVES(OutputSelector.Value.SCIDECOMOVES, new EcoMovesOutputHandler(EcoTree.FileType.SCIDDB)),

        XSTDECO(OutputSelector.Value.XSTDECO, new XEcoOutputHandler(EcoTree.FileType.STD)),
        XSTDECODESC(OutputSelector.Value.XSTDECODESC, new XEcoDescOutputHandler(EcoTree.FileType.STD)),
        XSTDECOMOVES(OutputSelector.Value.XSTDECOMOVES, new XEcoMovesOutputHandler(EcoTree.FileType.STD)),

        XSCIDECO(OutputSelector.Value.XSCIDECO, new XEcoOutputHandler(EcoTree.FileType.SCIDDB)),
        XSCIDECODESC(OutputSelector.Value.XSCIDECODESC, new XEcoDescOutputHandler(EcoTree.FileType.SCIDDB)),
        XSCIDECOMOVES(OutputSelector.Value.XSCIDECOMOVES, new XEcoMovesOutputHandler(EcoTree.FileType.SCIDDB));

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

        public void configTallier(OpeningStatsTallier os) { outputHandler.configTallier(os); }

        public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
        {
            outputHandler.appendOutput(opening, sb);
        }
    }

    private final Value value;

    public OpeningStatsOutputSelector(OutputSelector selector, OpeningStatsTallier os) throws SelectorException
    {
        value = Value.get(selector.getValue());

        if (value == null)
            throw new SelectorException("output selector '" + selector + "' is invalid in this context");

        value.configTallier(os);
    }

    public Value getValue() { return value; }

    public void appendOutput(OpeningStatsTallier.Opening opening, StringBuilder sb)
    {
        value.appendOutput(opening, sb);
    }
}
