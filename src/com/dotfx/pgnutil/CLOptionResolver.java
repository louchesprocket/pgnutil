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

import java.util.*;
import java.util.stream.Collectors;

public class CLOptionResolver
{
    private interface OptHandler
    {
        default void handleOpts(Collection<OptId> setOpts, Collection<OptId> checkOpts) {}
        default void handleIfAny() {}
        default void handleIfNone() {}
    }

    private static class MutexHandler implements OptHandler
    {
        @Override
        public void handleOpts(Collection<OptId> setOpts, Collection<OptId> checkOpts)
        {
            Set<OptId> conflicts = checkOpts.stream().filter(setOpts::contains).collect(Collectors.toSet());

            if (conflicts.size() > 1)
            {
                StringJoiner sj = new StringJoiner(",' '", "'", "'");
                for (OptId opt : conflicts) sj.add(opt.toString());
                System.err.println("Only one of " + sj + " may be set at a time.");
                System.exit(-1);
            }
        }
    }

    private static class OpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfNone()
        {
            Tallier os = OpeningStats.getInstance();
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    private static class MatchPlayerHandler implements OptHandler
    {
        @Override
        public void handleIfAny()
        {
            final OutputSelector.Value[] v = new OutputSelector.Value[1];

            if (CLOptions.getCount(OptId.MATCHPLAYER) > 1 &&
                    // hack to save the offending selector
                Arrays.stream(PGNUtil.outputSelectors).anyMatch(os -> ((v[0] = os.getValue()) != null) &&
                        os.getValue() == OutputSelector.Value.OPPONENT ||
                        os.getValue() == OutputSelector.Value.OPPONENTELO ||
                        os.getValue() == OutputSelector.Value.PLAYER ||
                        os.getValue() == OutputSelector.Value.PLAYERELO))
            {
                System.err.println("Output selector '" + v[0] + "' only works while matching one player ('" +
                        CLOptions.MP + "')!");

                System.exit(-1);
            };
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

        private void handle(final Set<OptId> setOpts)
        {
            if (!checkOpts.stream().anyMatch(setOpts::contains)) return;
            handler.handleOpts(setOpts, checkOpts);
            if (ifAnyOf.stream().anyMatch(setOpts::contains)) handler.handleIfAny();
            if (!ifNoneOf.stream().anyMatch(setOpts::contains)) handler.handleIfNone();
        }
    }

    public static final void resolveOpts(final Set<OptId> setOpts)
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

        new ConditionSet(new OptId[] {OptId.MATCHPLAYER}, new OptId[] {OptId.SELECTORS}, null,
                new MatchPlayerHandler()).handle(setOpts);
    }
}
