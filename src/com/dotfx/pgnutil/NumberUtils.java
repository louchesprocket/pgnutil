/*
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
        
        if (hex.length > 16)
            throw new IllegalArgumentException("input too long (max 16 hex digits)");
        
        long ret = 0;
        
        for (int i = 0; i < hex.length; i++)
        {
            byte b = (byte)(hex[i] & 0xFF);

            b -= 48;
            if (b > 9) b -= 7;
            if (b > 15) b -= 32;

            if (b < 0 || b > 15)
                throw new IllegalArgumentException("illegal hex value: " + hex[i]);
            
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
        byte[] ret;
        
        Arrays.fill(result, 0, result.length, (byte)0);

        for (int i = 0; i < result.length; i++)
        {
            byte q = (byte)(((v & 0xF000000000000000L) >> 60) & 0x0F);
            
            if (q > 9) q += 39;
            q += 48;
            
            result[i] = (byte)(q & 0xFF);
            
            v <<= 4;
        }
        
        if (trimLeadingZeroes) ret = trimLeadingZeroes(result);
        else ret = result;

        return new String(ret);
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

        for (int i = 0; i < length; i++) ret[i] = input[i + skip];
        
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

        for (int i = 3; i >= diff; i--) input[i] = intBytes[i - diff];
        for (int i = 0; i < diff; i++) input[i] = 0x00;
        
        ByteBuffer bb = ByteBuffer.wrap(input);
        return bb.getInt();
    }

//    public static void main(String[] args)
//    {
//        System.out.println((byte)0xff);
//        System.out.println(bytesToInt(new byte[] {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}));
//    }
}
