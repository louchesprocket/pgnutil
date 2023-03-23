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

import java.util.Arrays;
import java.util.Set;

public class CLOptionResolver
{
    private static final OptId[] ECOOPTS = new OptId[] {OptId.STDECO, OptId.SCIDECO, OptId.XSTDECO, OptId.XSCIDECO};

    private interface OptHandler
    {
        default void handleIfAny() {}
        default void handleIfNone() {}
    }

    private static class EcoOpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfAny()
        {
            Tallier os = EcoStats.getInstance(EcoTree.FileType.STD, false);
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    private static class ScidEcoOpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfAny()
        {
            Tallier os = EcoStats.getInstance(EcoTree.FileType.SCIDDB, false);
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    private static class XEcoOpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfAny()
        {
            Tallier os = EcoStats.getInstance(EcoTree.FileType.STD, true);
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    private static class XScidEcoOpeningsHandler implements OptHandler
    {
        @Override
        public void handleIfAny()
        {
            Tallier os = EcoStats.getInstance(EcoTree.FileType.SCIDDB, true);
            PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
            PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
        }
    }

    private static class ConditionSet
    {
        private final OptId opts[];
        private final OptId ifAnyOf[];
        private final OptId ifNoneOf[];
        private final OptHandler handler;

        private ConditionSet(OptId opts[], OptId ifAnyOf[], OptId ifNoneOf[], OptHandler handler)
        {
            this.opts = opts;
            this.ifAnyOf = ifAnyOf;
            this.ifNoneOf = ifNoneOf;
            this.handler = handler;
        }

        OptId[] getOpts() { return opts; }
        OptId[] getIfAnyOf() { return ifAnyOf; }
        OptId[] getIfNoneOf() { return ifNoneOf; }

        void handle(final Set<OptId> setOpts)
        {
            if (!Arrays.stream(getOpts()).anyMatch(setOpts::contains)) return;

            // https://stackoverflow.com/questions/11796371/check-if-one-list-contains-element-from-the-other
            if (Arrays.stream(getIfAnyOf()).anyMatch(setOpts::contains)) handler.handleIfAny();
            if (!Arrays.stream(getIfNoneOf()).anyMatch(setOpts::contains)) handler.handleIfNone();
        }
    }

    public static final void resolveOpts(final Set<OptId> setOpts)
    {
        new ConditionSet(new OptId[] {OptId.OPENINGS}, new OptId[] {OptId.STDECO}, new OptId[] {},
                new EcoOpeningsHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.OPENINGS}, new OptId[] {OptId.SCIDECO}, new OptId[] {},
                new ScidEcoOpeningsHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.OPENINGS}, new OptId[] {OptId.XSTDECO}, new OptId[] {},
                new XEcoOpeningsHandler()).handle(setOpts);

        new ConditionSet(new OptId[] {OptId.OPENINGS}, new OptId[] {OptId.XSCIDECO}, new OptId[] {},
                new XScidEcoOpeningsHandler()).handle(setOpts);
    }
}
