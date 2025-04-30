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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class Events implements Tallier
{
    private interface IteratorFactory
    {
        java.util.Iterator getIterator(Map<String,List<GameInfo>> eventMap, EventsOutputSelector selectors[])
                throws SelectorException;
    }

    private static class EventIteratorFactory implements IteratorFactory
    {
        @Override
        public java.util.Iterator getIterator(Map<String,List<GameInfo>> eventMap, EventsOutputSelector selectors[])
        {
            return new Iterator(eventMap, selectors);
        }
    }

    private static class ErrorCheckIteratorFactory implements IteratorFactory
    {
        @Override
        public java.util.Iterator getIterator(Map<String,List<GameInfo>> eventMap, EventsOutputSelector selectors[])
                throws SelectorException
        {
            return new RoundErrorIterator(eventMap, selectors);
        }
    }

    private static class Iterator implements java.util.Iterator<String>
    {
        private final Map<String,List<GameInfo>> eventMap;
        private final EventsOutputSelector selectors[];
        private final java.util.Iterator<String> eventNameIterator;

        private Iterator(Map<String,List<GameInfo>> eventMap, EventsOutputSelector selectors[])
        {
            this.eventMap = eventMap;
            eventNameIterator = eventMap.keySet().iterator();
            this.selectors = selectors;
        }

        @Override
        public boolean hasNext() { return eventNameIterator.hasNext(); }

        @Override
        public String next()
        {
            String eventName = eventNameIterator.next();
            List<GameInfo> gameList = eventMap.get(eventName);
            StringBuilder ret = new StringBuilder();

            if (selectors == null || selectors.length == 0)
            {
                ret.append(eventName).append(CLOptions.outputDelim).
                        append(guessTimeCtrl(gameList)).append(CLOptions.outputDelim);

                int size = gameList.size();
                if (size > 0) ret.append(gameList.get(0).getGameNum());

                for (int i = 1; i < size; i++)
                    ret.append(CLOptions.valueDelim).append(gameList.get(i).getGameNum());
            }

            else
            {
                for (EventsOutputSelector selector : selectors)
                {
                    ret.append(CLOptions.outputDelim);
                    selector.appendOutput(eventName, gameList, ret);
                }

                ret.delete(0, CLOptions.outputDelim.length());
            }

            return ret.toString();
        }
    }

    private static class RoundErrorIterator implements java.util.Iterator<String>
    {
        private final NormalizedRound FIRST_ROUND = new NormalizedRound("1");
        private final Map<String,List<GameInfo>> eventMap;
        private final java.util.Iterator<String> eventNameIterator;
        private String next;

        private RoundErrorIterator(Map<String,List<GameInfo>> eventMap, EventsOutputSelector selectors[])
                throws SelectorException
        {
            this.eventMap = eventMap;
            eventNameIterator = eventMap.keySet().iterator();

            if (selectors != null && selectors.length > 0)
                throw new SelectorException("output selectors are not allowed for this function");
        }

        private void genNext()
        {
            StringBuilder ret = new StringBuilder();
            String eventName = eventNameIterator.next();
            List<GameInfo> gameList = eventMap.get(eventName);

            gameList.sort(Comparator.comparing(GameInfo::getRound));
            NormalizedRound thisRound = gameList.get(0).getRound();
            if (!FIRST_ROUND.equals(thisRound)) ret.append(thisRound);

            for (int j = 1; j < gameList.size(); j++)
            {
                NormalizedRound prevRound = thisRound;
                thisRound = gameList.get(j).getRound();

                if (!thisRound.canFollow(prevRound))
                {
                    if (ret.length() > 0) ret.append(CLOptions.valueDelim);
                    ret.append(prevRound).append("-").append(thisRound);
                }
            }

            if (thisRound.ordinalValue() != gameList.size())
            {
                if (ret.length() > 0) ret.append(CLOptions.valueDelim);
                ret.append(thisRound).append("(").append(gameList.size()).append(")");
            }

            if (ret.length() > 0)
            {
                ret.insert(0, eventName + CLOptions.outputDelim);
                next = ret.toString();
            }

            else next = null;
        }

        @Override
        public boolean hasNext()
        {
            while (next == null && eventNameIterator.hasNext()) genNext();
            return next != null;
        }

        @Override
        public String next()
        {
            if (hasNext())
            {
                String ret = next;
                next = null;
                return ret;
            }

            throw new NoSuchElementException();
        }
    }

    private static Events instance;
    private final Map<String,List<GameInfo>> eventMap;
    private final IteratorFactory iteratorFactory;
    private EventsOutputSelector selectors[];

    private Events(IteratorFactory iteratorFactory)
    {
        eventMap = new HashMap<>();
        this.iteratorFactory = iteratorFactory;
    }

    public static Events getInstance()
    {
        if (instance == null) instance = new Events(new EventIteratorFactory());
        return instance;
    }

    public static Events getEventErrorInstance()
    {
        if (instance == null) instance = new Events(new ErrorCheckIteratorFactory());
        return instance;
    }

    @Override
    public void init(OutputSelector selectors[]) throws SelectorException
    {
        if (selectors != null && selectors.length > 0)
        {
            this.selectors = new EventsOutputSelector[selectors.length];
            for (int i = 0; i < selectors.length; i++) this.selectors[i] = new EventsOutputSelector(selectors[i]);
        }
    }

    public static TimeCtrl guessTimeCtrl(List<GameInfo> games)
    {
        TimeCtrl best = null;

        for (GameInfo gi : games)
        {
            TimeCtrl current = gi.getTimeCtrl();
            if (current == null) continue;
            if (current.isOfficial()) return current;
            if (best == null || current.compareTo(best) > 0) best = current;
        }

        return best;
    }

    @Override
    public void tally(PgnGame game)
    {
        String event = game.getValue("Event");
        List<GameInfo> eventGames = eventMap.computeIfAbsent(event, k -> new ArrayList<>());
        eventGames.add(new GameInfo(game.getNumber(), game.getNormalizedRound(), game.getTimeCtrl()));
    }

    @Override
    public java.util.Iterator<String> getOutputIterator() throws SelectorException
    {
        return iteratorFactory.getIterator(eventMap, selectors);
    }
}
