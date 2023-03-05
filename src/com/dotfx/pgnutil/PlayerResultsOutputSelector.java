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

import java.util.HashMap;
import java.util.Map;

public class PlayerResultsOutputSelector
{
    interface OutputHandler
    {
        void appendOutput(String player, PlayerResults.Score score, StringBuilder sb);
    }

    private static final class PlayerOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(player);
        }
    }

    private static final class WinsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(score.getWins());
        }
    }

    private static final class LossesOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(score.getLosses());
        }
    }

    private static final class DrawsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(score.getDraws());
        }
    }

    private static final class NoResultsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(score.getNoResults());
        }
    }

    private static final class CountOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(score.getGameCount());
        }
    }

    private static final class WinPctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(score.getWinPct()));
        }
    }

    public enum Value
    {
        PLAYER(OutputSelector.Value.PLAYER, new PlayerOutputHandler()),
        WINS(OutputSelector.Value.WINS, new WinsOutputHandler()),
        LOSSES(OutputSelector.Value.LOSSES, new LossesOutputHandler()),
        DRAWS(OutputSelector.Value.DRAWS, new DrawsOutputHandler()),
        NORESULTS(OutputSelector.Value.NORESULTS, new NoResultsOutputHandler()),
        COUNT(OutputSelector.Value.COUNT, new CountOutputHandler()),
        WINPCT(OutputSelector.Value.WINPCT, new WinPctOutputHandler());

        private static final Map<OutputSelector.Value, Value> sigMap = new HashMap<>();
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

    private final PlayerResultsOutputSelector.Value value;
    private final PlayerResultsOutputSelector.OutputHandler handler;

    public PlayerResultsOutputSelector(OutputSelector.Value selector) throws InvalidSelectorException
    {
        Value v = Value.get(selector);

        if (v != null)
        {
            value = v;
            handler = value.getOutputHandler();
        }

        else throw new InvalidSelectorException("output selector '" + selector + "' is invalid in this context");
    }

    public Value getValue() { return value; }

    public void appendOutput(String player, PlayerResults.Score score, StringBuilder sb)
    {
        handler.appendOutput(player, score, sb);
    }
}