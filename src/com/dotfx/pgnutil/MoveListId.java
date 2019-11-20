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

import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

/**
 *
 * @author Mark Chen
 */
public class MoveListId implements Comparable<MoveListId>
{
    public static final int HASHSEED = 0x6a83fcd5;
    public static final XXHashFactory FACTORY;
    
    private final long value;
    
    static
    {
        FACTORY = XXHashFactory.fastestInstance();
    }
    
    public MoveListId(byte b[])
    {
        StreamingXXHash64 hashFunc = FACTORY.newStreamingHash64(HASHSEED);
//        hashFunc.reset(); // does not reset the seed
        hashFunc.update(b, 0, b.length);
        value = hashFunc.getValue();
    }
    
    public MoveListId(String s) { this(s.getBytes()); }
    
    public static MoveListId fromString(String s)
    {
        return new MoveListId(NumberUtils.hexToLong(s));
    }
    
    public MoveListId(long value) { this.value = value; }
    
    @Override
    public String toString() { return NumberUtils.longToHex(value, false); }
    
    @Override
    public boolean equals(Object other)
    {
        try { return value == ((MoveListId)other).value; }
        catch (ClassCastException | NullPointerException e) { return false; }
    }
    
    @Override
    public int hashCode()
    {
        return HASHSEED ^ Long.hashCode(value);
    }
    
    @Override
    public int compareTo(MoveListId other)
    {
        long diff = value - other.value;
        if (diff > 0) return 1;
        if (diff < 0) return -1;
        return 0;
    }
}
