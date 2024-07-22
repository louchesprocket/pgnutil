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

import com.dotfx.pgnutil.CLOptions.OptId;
import com.dotfx.pgnutil.eco.EcoTree;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CLOptionResolver
{
    public interface OptHandler
    {
        default void handleOpts(Collection<OptId> setOpts, Set<OptId> setIntersects) {}
        default void handleIfAny(Collection<OptId> setOpts, Set<OptId> ifAnyIntersects) {}
        default void handleIfNone(Collection<OptId> setOpts) {}
    }

    public static class PrintPositionHandler implements OptHandler
    {
        private final String moveSt;

        public PrintPositionHandler(String moveSt) { this.moveSt = moveSt; }

        @Override
        public void handleOpts(Collection<OptId> setOpts, Set<OptId> setIntersects)
        {
            if (setOpts.size() > 1)
            {
                System.err.println("Option '" + OptId.PRINTPOS + "' cannot be used with other options.");
                System.exit(-1);
            }

            try
            {
                System.out.println(new Board(true).goTo(PgnGame.parseMoveString(moveSt)));
                System.exit(0);
            }

            catch (IllegalMoveException e)
            {
                System.err.println("illegal move: " + e.getLocalizedMessage());
                System.exit(-1);
            }
        }
    }

    private static class MutexHandler implements OptHandler
    {
        @Override
        public void handleOpts(Collection<OptId> setOpts, Set<OptId> setIntersects)
        {
            if (setIntersects.size() > 1)
            {
                StringJoiner sj = new StringJoiner(",' '", "'", "'");
                for (OptId opt : setIntersects) sj.add(opt.toString());
                System.err.println("Only one of " + sj + " may be set at a time.");
                System.exit(-1);
            }
        }
    }

    public static class ReplaceHandler implements OptHandler
    {
        private final Pattern searchPattern;
        private final String replacement;

        public ReplaceHandler(Pattern searchPattern, String replacement)
        {
            this.searchPattern = searchPattern;
            this.replacement = replacement;
        }

        @Override
        public void handleOpts(Collection<OptId> setOpts, Set<OptId> setIntersects)
        {
            PGNUtil.addReplaceProcessor(new PGNUtil.ReplaceProcessor(searchPattern, replacement));
        }
    }

    private static class OpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfNone(Collection<OptId> setOpts)
        {
            Tallier os = OpeningStats.getInstance();
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    public static class PlayerHandler implements OptHandler
    {
        private final Pattern playerPattern;

        public PlayerHandler(Pattern playerPattern) { this.playerPattern = playerPattern; }

        @Override
        public void handleIfAny(Collection<OptId> setOpts, Set<OptId> ifAnyIntersects)
        {
            List<OutputSelector.Value> checkValues = Arrays.asList(OutputSelector.Value.OPPONENT,
                    OutputSelector.Value.OPPONENTELO, OutputSelector.Value.PLAYER, OutputSelector.Value.PLAYERELO);

            List<OutputSelector> osList = Arrays.stream(PGNUtil.outputSelectors).filter(os ->
                    checkValues.contains(os.getValue())).collect(Collectors.toList());

            if (CLOptions.getCount(OptId.MATCHPLAYER) > 1 && !osList.isEmpty())
            {
                System.err.println("Output selector '" + osList.get(0) + "' only works while matching " +
                        "one player ('" + CLOptions.MP + "')!");

                System.exit(-1);
            };

            for (OutputSelector os : osList)
            {
                switch (os.getValue())
                {
                    case OPPONENT:
                        os.setOutputHandler(new OutputSelector.OpponentOutputHandler(playerPattern));
                        break;

                    case OPPONENTELO:
                        os.setOutputHandler(new OutputSelector.OpponentEloOutputHandler(playerPattern));
                        break;

                    case PLAYER:
                        os.setOutputHandler(new OutputSelector.PlayerOutputHandler(playerPattern));
                        break;

                    case PLAYERELO:
                        os.setOutputHandler(new OutputSelector.PlayerEloOutputHandler(playerPattern));
                        break;
                }
            }
        }
    }

    public static class StdEcoHandler implements OptHandler
    {
        private final boolean transpose;

        public StdEcoHandler(boolean transpose) { this.transpose = transpose; }

        @Override
        public void handleOpts(Collection<OptId> setOpts, Set<OptId> setIntersects)
        {
            Tallier os = EcoStats.getInstance(EcoTree.FileType.STD, transpose);
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    public static class ClockBelowHandler implements OptHandler
    {
        private final Clock clock;

        public ClockBelowHandler(Clock clock) { this.clock = clock; }

        @Override
        public void handleIfAny(Collection<OptId> setOpts, Set<OptId> ifAnyIntersects)
        {
            for (int i = 0; i < PGNUtil.outputSelectors.length; i++)
            {
                if (PGNUtil.outputSelectors[i].getValue() == OutputSelector.Value.CBPLAYERS)
                    PGNUtil.outputSelectors[i].
                            setOutputHandler(new OutputSelector.ClockBelowPlayersOutputHandler(clock));
            }
        }
    }

    private static class DefaultSelectorsHandler implements OptHandler
    {
        @Override
        public void handleIfNone(Collection<OptId> setOpts)
        {
            PGNUtil.setHandler(new PGNUtil.SelectGameHandler(PGNUtil.outputSelectors));
        }
    }

    private static class ConditionSet
    {
        private final Set<OptId> checkOpts;
        private final Set<OptId> ifAnyOf;
        private final Set<OptId> ifNoneOf;
        private final OptHandler handler;

        private ConditionSet(OptId checkOpts[], OptId ifAnyOf[], OptId ifNoneOf[], OptHandler handler)
        {
            this.checkOpts = new HashSet<>(Arrays.asList(checkOpts));
            this.ifAnyOf = ifAnyOf == null ? new HashSet<>() : new HashSet<>(Arrays.asList(ifAnyOf));
            this.ifNoneOf = ifNoneOf == null ? new HashSet<>() : new HashSet<>(Arrays.asList(ifNoneOf));
            this.handler = handler;
        }

        private final void handle(final Set<OptId> setOpts)
        {
            Set<OptId> intersects = checkOpts.stream().filter(setOpts::contains).collect(Collectors.toSet());
            if (intersects.isEmpty()) return;
            handler.handleOpts(setOpts, intersects);

            Set<OptId> anyIntersects = ifAnyOf.stream().filter(setOpts::contains).collect(Collectors.toSet());
            if (!anyIntersects.isEmpty()) handler.handleIfAny(setOpts, anyIntersects);

            if (ifNoneOf.stream().noneMatch(setOpts::contains)) handler.handleIfNone(setOpts);
        }
    }

    private static final List<ConditionSet> conditionList = new ArrayList<>();

    public static void addCondition(OptId checkOpts[], OptId ifAnyOf[], OptId ifNoneOf[], OptHandler handler)
    {
        conditionList.add(new ConditionSet(checkOpts, ifAnyOf, ifNoneOf, handler));
    }

    public static void resolveOpts(final Set<OptId> setOpts)
    {
        final OptId topLevelOpts[] =
                new OptId[] {OptId.DUPLICATES, OptId.DUPLICATEMOVES, OptId.DUPLICATEOPENINGS, OptId.OPENINGS,
                        OptId.EVENTS, OptId.PLAYERRESULTS, OptId.CHECKSEQUENTIALROUNDS};

        final OptId openingsOpts[] =
                new OptId[] {OptId.STDECO, OptId.SCIDECO, OptId.XSTDECO, OptId.XSCIDECO};

        final OptId matchOpeningOpts[] =
                new OptId[] {OptId.MATCHOPENING, OptId.NOTMATCHOPENING, OptId.OPENINGFILE, OptId.NOTOPENINGFILE};

        new ConditionSet(topLevelOpts, null, null, new MutexHandler()).handle(setOpts);
        new ConditionSet(openingsOpts, null, null, new MutexHandler()).handle(setOpts);
        new ConditionSet(matchOpeningOpts, null, null, new MutexHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.OPENINGS}, null, openingsOpts,
                new OpeningsHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.SELECTORS}, null, topLevelOpts,
                new DefaultSelectorsHandler()).handle(setOpts);

        // anything else requiring delayed initialization
        for (ConditionSet cs : conditionList) cs.handle(setOpts);
    }
}
