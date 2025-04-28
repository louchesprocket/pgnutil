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

import net.openhft.hashing.LongTupleHashFunction;

public class UniqueId128 implements Comparable<UniqueId128>
{
    public static final long HASHSEED = 0x6a830fe67ba5892cL;
    private static final LongTupleHashFunction hashFunc = LongTupleHashFunction.xx128(HASHSEED);
    private final long[] value = new long[2];

    private UniqueId128(long v1, long v2)
    {
        this.value[0] = v1;
        this.value[1] = v2;
    }

    public UniqueId128(byte[] b) { hashFunc.hashBytes(b, value); }
    public UniqueId128(long value) { this.value[0] = value; }

    public UniqueId128(long[] value)
    {
        this.value[0] = value[0];
        this.value[1] = value[1];
    }

    public long[] getValue() { return value; }

    public static UniqueId128 fromString(String s)
    {
        return new UniqueId128(NumberUtils.hexToLong(s.substring(0,16)),
                NumberUtils.hexToLong(s.substring(16,32)));
    }

    @Override
    public String toString()
    {
        return NumberUtils.longToHex(value[0], false) +
                NumberUtils.longToHex(value[1], false);
    }

    @Override
    public int hashCode()
    {
        return UniqueId128.class.hashCode() ^ Long.hashCode(value[0]) ^ Long.hashCode(value[1]);
    }

    @Override
    public boolean equals(Object other)
    {
        return value[0] == ((UniqueId128)other).value[0] && value[1] == ((UniqueId128)other).value[1];
    }

    @Override
    public int compareTo(UniqueId128 other)
    {
        long diff0 = value[0] - other.value[0];

        if (diff0 > 0) return 1;
        if (diff0 < 0) return -1;

        long diff1 = value[1] - other.value[1];

        if (diff1 > 0) return 1;
        if (diff1 < 0) return -1;
        return 0;
    }
}
