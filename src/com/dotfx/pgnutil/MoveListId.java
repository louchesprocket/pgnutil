/*
 * The MIT License
 *
 * Copyright 2018 Mark Chen.
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

/**
 * Represents a unique list of moves (implies unique position). Truncates hashes to 64 bits for convenience in
 * external handling.
 *
 * @author Mark Chen
 */
public final class MoveListId extends UniqueId128
{
    public MoveListId(byte[] b) { super(b); }
    public MoveListId(long value) { super(value); }
    public MoveListId(String s) { this(s.getBytes()); }
    
    public static MoveListId fromHexString(String s)
    {
        return new MoveListId(NumberUtils.hexToLong(s));
    }
    
    @Override
    public int hashCode() { return MoveListId.class.hashCode() ^ Long.hashCode(getLsb()); }

    @Override
    public String toString() { return NumberUtils.longToHex(getLsb(), false); }

    @Override
    public boolean equals(Object other) { return getLsb() == ((UniqueId128)other).getLsb(); }

    @Override
    public int compareTo(UniqueId128 other)
    {
        long diff0 = getLsb() - other.getLsb();
        if (diff0 > 0) return 1;
        if (diff0 < 0) return -1;
        return 0;
    }
}
