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

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author Mark Chen
 */public class NumberUtils
{
    public static long hexToLong(byte[] input)
    {
        byte[] hex;
        
        if (input.length > 16) hex = trimLeadingZeroes(input);
        else hex = input;
        
        if (hex.length > 16) throw new IllegalArgumentException("input too long (max 16 hex digits)");
        
        long ret = 0;
        
        for (int i = 0; i < hex.length; i++)
        {
            byte b = (byte)(hex[i] & 0xFF);

            b -= 48;
            if (b > 9) b -= 7;
            if (b > 15) b -= 32;
            if (b < 0 || b > 15) throw new IllegalArgumentException("illegal hex value: " + hex[i]);
            
            ret |= b & 0x000000000000000FL;
            
            if (i + 1 < hex.length) ret <<= 4;
        }

        return ret;
    }
    
    public static long hexToLong(String hexString)
    {
        return hexToLong(hexString.getBytes());
    }

    public static String longToHex(long input, boolean trimLeadingZeroes)
    {
        long v = input & 0xFFFFFFFFFFFFFFFFL;
        byte[] result = new byte[16];

        for (int i = 0; i < result.length; i++)
        {
            byte q = (byte)(((v & 0xF000000000000000L) >> 60) & 0x0F);
            
            if (q > 9) q += 39;
            q += 48;
            
            result[i] = (byte)(q & 0xFF);
            v <<= 4;
        }

        return new String(trimLeadingZeroes ? trimLeadingZeroes(result) : result);
    }
    
    public static String longToHex(long input)
    {
        return longToHex(input, true);
    }
    
    public static byte[] trimLeadingZeroes(byte[] input)
    {
        int length = input.length;
        int skip;

        for (skip = 0; skip < input.length; skip++)
        {
            if (input[skip] == (byte)'0') length--;
            else break;
        }

        byte[] ret = new byte[length];
        System.arraycopy(input, skip, ret, 0, length);
        return ret;
    }

    public static byte[] intToBytes(int i)
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE);
        bb.putInt(i);
        return bb.array();
    }

    public static int bytesToInt(byte[] intBytes)
    {
        byte[] input = new byte[4];
        int diff = 4 - intBytes.length;
        if (diff < 0) diff = 0;

        System.arraycopy(intBytes, 0, input, diff, 4 - diff);
        for (int i = 0; i < diff; i++) input[i] = 0x00;
        
        ByteBuffer bb = ByteBuffer.wrap(input);
        return bb.getInt();
    }

//    public static void main(String[] args)
//    {
////        System.out.println((byte)0xff);
////        System.out.println(bytesToInt(new byte[] {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}));
//        System.out.println(longToHex(hexToLong(longToHex(0x0123456789abcdeL, false)), false));
//    }
}
