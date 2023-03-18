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

import java.util.*;

public class EventsOutputSelector
{
    interface OutputHandler
    {
        void appendOutput(String eventName, List<GameInfo> gameList, StringBuilder sb);
    }

    private static final class CountOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String eventName, List<GameInfo> gameList, StringBuilder sb)
        {
            sb.append(gameList.size());
        }
    }

    private static final class EventOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String eventName, List<GameInfo> gameList, StringBuilder sb)
        {
            sb.append(eventName);
        }
    }

    private static final class LastRoundOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String eventName, List<GameInfo> gameList, StringBuilder sb)
        {
            NormalizedRound lastRound = new NormalizedRound("-");

            for (GameInfo gi : gameList)
            {
                NormalizedRound thisRound = gi.getRound();
                if (thisRound.compareTo(lastRound) > 0) lastRound = thisRound;
            }

            sb.append(lastRound);
        }
    }

    private static final class RoundCountOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(String eventName, List<GameInfo> gameList, StringBuilder sb)
        {
            Set<NormalizedRound> rounds = new HashSet<>(30000);
            for (GameInfo gi : gameList) rounds.add(gi.getRound());
            sb.append(rounds.size());
        }
    }

    public enum Value
    {
        COUNT(OutputSelector.Value.COUNT, new CountOutputHandler()), // count of games
        EVENT(OutputSelector.Value.EVENT, new EventOutputHandler()),
        LASTROUND(OutputSelector.Value.LASTROUND, new LastRoundOutputHandler()),
        ROUNDCOUNT(OutputSelector.Value.ROUNDCOUNT, new RoundCountOutputHandler()); // count of unique round numbers

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

    public EventsOutputSelector(OutputSelector selector) throws InvalidSelectorException
    {
        value = Value.get(selector.getValue());

        if (value == null)
            throw new InvalidSelectorException("output selector '" + selector + "' is invalid in this context");

        handler = value.getOutputHandler();
    }

    public Value getValue() { return value; }

    public void appendOutput(String eventName, List<GameInfo> gameList, StringBuilder sb)
    {
        handler.appendOutput(eventName, gameList, sb);
    }
}
