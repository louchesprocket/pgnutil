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

import com.dynatrace.hash4j.hashing.HashValue128;
import com.dynatrace.hash4j.hashing.Hasher128;
import com.dynatrace.hash4j.hashing.Hashing;

public class UniqueId128 implements Comparable<UniqueId128>
{
    public static final long HASHSEED = 0x6a830fe67ba5892cL;

    // re-usable persistent instance
    private static final Hasher128 hasher = Hashing.xxh3_128(HASHSEED);

    private final HashValue128 value;

    private UniqueId128(long leastSig, long mostSig) { value = new HashValue128(mostSig, leastSig); }
    public UniqueId128(byte[] b) { value = hasher.hashBytesTo128Bits(b); }
    public UniqueId128(long v1) { value = new HashValue128(0L, v1); }

    /**
     *
     * @param value two-element array, lsb first
     */
    public UniqueId128(long[] value) { this.value = new HashValue128(value[1], value[0]); }

    public long[] getValue() { return new long[] { value.getLeastSignificantBits(), value.getMostSignificantBits()}; }

    public static UniqueId128 fromString(String s)
    {
        return new UniqueId128(NumberUtils.hexToLong(s.substring(0,16)),
                NumberUtils.hexToLong(s.substring(16,32)));
    }

    @Override
    public String toString()
    {
        return NumberUtils.longToHex(value.getLeastSignificantBits(), false) +
                NumberUtils.longToHex(value.getMostSignificantBits(), false);
    }

    @Override
    public int hashCode()
    {
        return UniqueId128.class.hashCode() ^ Long.hashCode(value.getMostSignificantBits()) ^
                Long.hashCode(value.getLeastSignificantBits());
    }

    @Override
    public boolean equals(Object other) { return value.equals(((UniqueId128)other).value); }

    @Override
    public int compareTo(UniqueId128 other)
    {
        long diff0 = value.getMostSignificantBits() - other.value.getMostSignificantBits();

        if (diff0 > 0) return 1;
        if (diff0 < 0) return -1;

        long diff1 = value.getLeastSignificantBits() - other.value.getLeastSignificantBits();

        if (diff1 > 0) return 1;
        if (diff1 < 0) return -1;
        return 0;
    }

//    public static void main(String[] args)
//    {
//        UniqueId128 hash0 = new UniqueId128("hello world".getBytes());
//        System.out.println("from String: " + hash0);
//
//        UniqueId128 hash1 = new UniqueId128(hash0.getValue());
//        System.out.println("from value: " + hash1);
//
//        UniqueId128 hash2 = UniqueId128.fromString(hash1.toString());
//
//        System.out.println(hash0.equals(hash1));
//        System.out.println(hash1.equals(hash2));
//        System.out.println(hash0.compareTo(hash1));
//
////        byte[] preimage = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
////        LongTupleHashFunction hashFunc = LongTupleHashFunction.xx128(HASHSEED);
////        Hasher128 hasher = Hashing.xxh3_128(HASHSEED);
////        long[] value = new long[2];
////
////        long start = System.currentTimeMillis();
////
////        for (int i = 0; i < 10000000; i++) hashFunc.hashBytes(preimage, value);
////        System.out.println("old: " + (System.currentTimeMillis() - start));
////
////        start = System.currentTimeMillis();
////
////        for (int i = 0; i < 10000000; i++) hasher.hashBytesTo128Bits(preimage);
////        System.out.println("new: " + (System.currentTimeMillis() - start));
//    }
}
