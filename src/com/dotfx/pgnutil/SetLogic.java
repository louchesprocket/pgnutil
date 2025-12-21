/*
 * The MIT License
 *
 * Copyright (c) 2025 Mark Chen <chen@dotfx.com>.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SetLogic<T>
{
    public interface Handler<T>
    {
        default void handleChecked(Map<T,Integer> setOpts, Set<T> setIntersects) {}
        default void handleIfAny(Map<T,Integer> setOpts, Set<T> ifAnyIntersects) {}
        default void handleIfNone(Map<T,Integer> setOpts, Set<T> checkIntersects) {}
    }

    private final Set<T> checkObjs;
    private final Set<T> ifAnyOf;
    private final Set<T> ifNoneOf;
    private final Handler<T> handler;

    public SetLogic(T[] checkObjs, T[] ifAnyOf, T[] ifNoneOf, Handler<T> handler)
    {
        this.checkObjs = new HashSet<>(Arrays.asList(checkObjs));
        this.ifAnyOf = ifAnyOf == null ? new HashSet<>() : new HashSet<>(Arrays.asList(ifAnyOf));
        this.ifNoneOf = ifNoneOf == null ? new HashSet<>() : new HashSet<>(Arrays.asList(ifNoneOf));
        this.handler = handler;
    }

    public void handle(final Map<T,Integer> setObjs)
    {
        Set<T> checkIntersects =
                setObjs.keySet().stream().filter(checkObjs::contains).collect(Collectors.toSet());

        if (checkIntersects.isEmpty()) return;
        handler.handleChecked(setObjs, checkIntersects);

        Set<T> anyIntersects = setObjs.keySet().stream().filter(ifAnyOf::contains).collect(Collectors.toSet());
        if (!anyIntersects.isEmpty()) handler.handleIfAny(setObjs, anyIntersects);

        if (setObjs.keySet().stream().noneMatch(ifNoneOf::contains)) handler.handleIfNone(setObjs, checkIntersects);
    }

    @Override
    public final boolean equals(Object other)
    {
        SetLogic<T> that = (SetLogic<T>)other;

        return that != null && checkObjs.equals(that.checkObjs) && ifAnyOf.equals(that.ifAnyOf) &&
                ifNoneOf.equals(that.ifNoneOf);
    }

    @Override
    public final int hashCode()
    {
        return SetLogic.class.hashCode() ^ checkObjs.hashCode() ^ ifAnyOf.hashCode() ^ ifNoneOf.hashCode();
    }
}
