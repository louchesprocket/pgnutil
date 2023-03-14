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

import java.util.Arrays;
import java.util.Set;

public class CLOptionResolver
{
    private static int cmin;
    private static final OptId[] ECOOPTS = new OptId[] {OptId.STDECO, OptId.SCIDECO, OptId.XSTDECO, OptId.XSCIDECO};

    private interface OptHandler
    {
        void handleIfAny();
        void handleIfNone();
    }

    private static class CminHandler implements OptHandler
    {
        @Override
        public void handleIfAny()
        {
            OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MinGamesProcessor(cmin));
        }

        @Override
        public void handleIfNone()
        {
            OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MinGamesProcessor(cmin));
        }
    }

    private static class ConditionSet
    {
        private final OptId opt;
        private final OptId ifAnyOf[];
        private final OptId ifNoneOf[];
        private final OptHandler handler;

        private ConditionSet(OptId opt, OptId ifAnyOf[], OptId ifNoneOf[], OptHandler handler)
        {
            this.opt = opt;
            this.ifAnyOf = ifAnyOf;
            this.ifNoneOf = ifNoneOf;
            this.handler = handler;
        }

        OptId[] getIfAnyOf() { return ifAnyOf; }
        OptId[] getIfNoneOf() { return ifNoneOf; }

        void handle(Set<OptId> setOpts)
        {
            // https://stackoverflow.com/questions/11796371/check-if-one-list-contains-element-from-the-other
            if (Arrays.stream(getIfAnyOf()).anyMatch(setOpts::contains)) handler.handleIfAny();
            if (!Arrays.stream(getIfNoneOf()).anyMatch(setOpts::contains)) handler.handleIfNone();
        }
    }

    public static final void resolveOpts(Set<OptId> setOpts)
    {
//        new ConditionSet(OptId.MINGAMECOUNT, ECOOPTS, ECOOPTS, new CminHandler()).handle(setOpts);
    }

    static void setCmin(int n) { cmin = n; }
}
