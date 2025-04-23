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
        default void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects) {}
        default void handleIfAny(Map<OptId,Integer> setOpts, Set<OptId> ifAnyIntersects) {}
        default void handleIfNone(Map<OptId,Integer> setOpts) {}
    }

    public static class PrintPositionHandler implements OptHandler
    {
        private final String moveSt;

        public PrintPositionHandler(String moveSt) { this.moveSt = moveSt; }

        @Override
        public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects)
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
        public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects)
        {
            if (setIntersects.size() > 1)
            {
                StringJoiner sj = new StringJoiner(",' '", "'", "'");
                for (OptId opt : setIntersects) sj.add(opt.toString());
                System.err.println("Only one of " + sj + " may be set at a time!");
                System.exit(-1);
            }
        }
    }

    private static class SingletonHandler implements OptHandler
    {
        @Override
        public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects)
        {
            for (OptId opt : setIntersects)
            {
                if (setOpts.get(opt) > 1)
                {
                    System.err.println("Option '" + opt + "' may only be set once!");
                    System.exit(-1);
                }
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
        public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects)
        {
            PGNUtil.addReplaceProcessor(new PGNUtil.ReplaceProcessor(searchPattern, replacement));
        }
    }

    private static class OpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfNone(Map<OptId,Integer> setOpts)
        {
            Tallier os = OpeningStats.getInstance();
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    public static class GameNumHandler implements OptHandler
    {
        public void handleIfAny(Map<OptId,Integer> setOpts, Set<OptId> ifAnyIntersects)
        {
            if (CLOptions.getCount(OptId.INPUTFILE) > 1)
            {
                System.err.println("Option '" +
                        (setOpts.containsKey(OptId.GAMENUM) ? OptId.GAMENUM : OptId.GAMENUMFILE) +
                        "' is only valid with a single input file!");

                System.exit(-1);
            }
        }
    }

    public static class PlayerHandler implements OptHandler
    {
        private final Pattern playerPattern;

        public PlayerHandler(Pattern playerPattern) { this.playerPattern = playerPattern; }

        @Override
        public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects)
        {
            if (CLOptions.getCount(OptId.MATCHPLAYER) > 2)
            {
                System.err.println("Option '" + OptId.MATCHPLAYER + "' may not be set more than twice!");
                System.exit(-1);
            }
        }

        @Override
        public void handleIfAny(Map<OptId,Integer> setOpts, Set<OptId> ifAnyIntersects)
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
        public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> setIntersects)
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
        public void handleIfAny(Map<OptId,Integer> setOpts, Set<OptId> ifAnyIntersects)
        {
            Arrays.stream(PGNUtil.outputSelectors).filter(selector ->
                    selector.getValue() == OutputSelector.Value.CBPLAYERS).forEach(selector ->
                    selector.setOutputHandler(new OutputSelector.ClockBelowPlayersOutputHandler(clock)));
        }
    }

    private static class DefaultSelectorsHandler implements OptHandler
    {
        @Override
        public void handleIfNone(Map<OptId,Integer> setOpts)
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

        private void handle(final Map<OptId, Integer> setOpts)
        {
            Set<OptId> intersects = setOpts.keySet().stream().filter(checkOpts::contains).collect(Collectors.toSet());
            if (intersects.isEmpty()) return;
            handler.handleOpts(setOpts, intersects);

            Set<OptId> anyIntersects = setOpts.keySet().stream().filter(ifAnyOf::contains).collect(Collectors.toSet());
            if (!anyIntersects.isEmpty()) handler.handleIfAny(setOpts, anyIntersects);

            if (setOpts.keySet().stream().noneMatch(ifNoneOf::contains)) handler.handleIfNone(setOpts);
        }

        @Override
        public final boolean equals(Object other)
        {
            ConditionSet that = (ConditionSet)other;

            return that != null && checkOpts.equals(that.checkOpts) && ifAnyOf.equals(that.ifAnyOf) &&
                    ifNoneOf.equals(that.ifNoneOf);
        }

        @Override
        public final int hashCode()
        {
            return ConditionSet.class.hashCode() ^ checkOpts.hashCode() ^ ifAnyOf.hashCode() ^ ifNoneOf.hashCode();
        }
    }

    private static final Set<ConditionSet> condSetSet = new HashSet<>();

    public static void addCondition(OptId checkOpts[], OptId ifAnyOf[], OptId ifNoneOf[], OptHandler handler)
    {
        condSetSet.add(new ConditionSet(checkOpts, ifAnyOf, ifNoneOf, handler));
    }

    public static void resolveOpts(final Map<OptId,Integer> setOpts)
    {
        final OptId topLevelOpts[] = new OptId[] {OptId.get(CLOptions.D), OptId.get(CLOptions.DM),
                OptId.get(CLOptions.DO), OptId.get(CLOptions.DOOB), OptId.get(CLOptions.DOOBM), OptId.get(CLOptions.O),
                OptId.get(CLOptions.E), OptId.get(CLOptions.P), OptId.get(CLOptions.CSR)};

        // sub-options under "-o"
        final OptId ecoOpts[] = new OptId[] {OptId.get(CLOptions.ECO), OptId.get(CLOptions.SECO),
                OptId.get(CLOptions.XECO), OptId.get(CLOptions.XSECO)};

        final OptId matchOpeningOpts[] = new OptId[] {OptId.get(CLOptions.MO), OptId.get(CLOptions.NMO),
                OptId.get(CLOptions.OF), OptId.get(CLOptions.NOF)};

        final OptId matchPositionOpts[] = new OptId[] {OptId.get(CLOptions.MPOS), OptId.get(CLOptions.POSF),
                OptId.get(CLOptions.MFEN), OptId.get(CLOptions.FF)};

        final OptId singletonOpts[] = new OptId[] {OptId.get(CLOptions.MPOS), OptId.get(CLOptions.POSF),
                OptId.get(CLOptions.MFEN), OptId.get(CLOptions.FF), OptId.get(CLOptions.PP),
                OptId.get(CLOptions.GN), OptId.get(CLOptions.GNF), OptId.get(CLOptions.MW), OptId.get(CLOptions.ML),
                OptId.get(CLOptions.TF), OptId.get(CLOptions.LELO), OptId.get(CLOptions.HELO), OptId.get(CLOptions.LED),
                OptId.get(CLOptions.HED), OptId.get(CLOptions.CB), OptId.get(CLOptions.CNB), OptId.get(CLOptions.ELO),
                OptId.get(CLOptions.MO), OptId.get(CLOptions.NMO), OptId.get(CLOptions.OF), OptId.get(CLOptions.NOF),
                OptId.get(CLOptions.EF), OptId.get(CLOptions.ME), OptId.get(CLOptions.MED), OptId.get(CLOptions.MSE),
                OptId.get(CLOptions.MSED), OptId.get(CLOptions.MXE), OptId.get(CLOptions.MXED),
                OptId.get(CLOptions.MXSE), OptId.get(CLOptions.MXSED), OptId.get(CLOptions.APF),
                OptId.get(CLOptions.PF), OptId.get(CLOptions.NPF), OptId.get(CLOptions.TC), OptId.get(CLOptions.LPC),
                OptId.get(CLOptions.HPC), OptId.get(CLOptions.BM), OptId.get(CLOptions.LOOB), OptId.get(CLOptions.HOOB),
                OptId.get(CLOptions.R), OptId.get(CLOptions.RW), OptId.get(CLOptions.RL), OptId.get(CLOptions.RO),
                OptId.get(CLOptions.D), OptId.get(CLOptions.DM), OptId.get(CLOptions.DO), OptId.get(CLOptions.DOOB),
                OptId.get(CLOptions.DOOBM), OptId.get(CLOptions.E), OptId.get(CLOptions.CSR), OptId.get(CLOptions.O),
                OptId.get(CLOptions.ECO), OptId.get(CLOptions.SECO), OptId.get(CLOptions.XECO),
                OptId.get(CLOptions.XSECO), OptId.get(CLOptions.CMIN), OptId.get(CLOptions.LWD),
                OptId.get(CLOptions.HWD), OptId.get(CLOptions.HDRAW), OptId.get(CLOptions.LDRAW),
                OptId.get(CLOptions.P), OptId.get(CLOptions.S)};

        new ConditionSet(topLevelOpts, null, null, new MutexHandler()).handle(setOpts);
        new ConditionSet(ecoOpts, null, null, new MutexHandler()).handle(setOpts);
        new ConditionSet(matchOpeningOpts, null, null, new MutexHandler()).handle(setOpts);
        new ConditionSet(matchPositionOpts, null, null, new MutexHandler()).handle(setOpts);
        new ConditionSet(singletonOpts, null, null, new SingletonHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.get(CLOptions.O)}, null, ecoOpts,
                new OpeningsHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.get(CLOptions.S)}, null, topLevelOpts,
                new DefaultSelectorsHandler()).handle(setOpts);

        // anything else requiring delayed initialization
        for (ConditionSet cs : condSetSet) cs.handle(setOpts);
    }
}
