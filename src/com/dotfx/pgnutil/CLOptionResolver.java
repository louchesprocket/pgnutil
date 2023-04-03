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

public class CLOptionResolver
{
    private interface OptHandler
    {
        default void handleIfAny() {}
        default void handleIfNone() {}
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
        private final Set<OptId> opts;
        private final Set<OptId> ifAnyOf;
        private final Set<OptId> ifNoneOf;
        private final OptHandler handler;

        private ConditionSet(OptId opts[], OptId ifAnyOf[], OptId ifNoneOf[], OptHandler handler)
        {
            this.opts = new HashSet<>(Arrays.asList(opts));
            this.ifAnyOf = ifAnyOf == null ? new HashSet<>() : new HashSet<>(Arrays.asList(ifAnyOf));
            this.ifNoneOf = ifNoneOf == null ? new HashSet<>() : new HashSet<>(Arrays.asList(ifNoneOf));
            this.handler = handler;
        }

        private void handle(final Set<OptId> setOpts)
        {
            // https://stackoverflow.com/questions/11796371/check-if-one-list-contains-element-from-the-other
            if (!opts.stream().anyMatch(setOpts::contains)) return;
            if (ifAnyOf.stream().anyMatch(setOpts::contains)) handler.handleIfAny();
            if (!ifNoneOf.stream().anyMatch(setOpts::contains)) handler.handleIfNone();
        }
    }

    public static final void resolveOpts(final Set<OptId> setOpts)
    {
        new ConditionSet(new OptId[] {OptId.OPENINGS}, null,
                new OptId[] {OptId.STDECO, OptId.SCIDECO, OptId.XSTDECO, OptId.XSCIDECO},
                new OpeningsHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.MATCHPLAYER}, new OptId[] {OptId.SELECTORS}, null,
                new MatchPlayerHandler()).handle(setOpts);
    }
}
