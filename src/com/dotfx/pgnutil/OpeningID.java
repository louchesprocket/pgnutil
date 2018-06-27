/*
 */

package com.dotfx.pgnutil;

import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

/**
 *
 * @author Mark Chen
 */
public class OpeningID implements Comparable
{
    public static final int HASHSEED = 0x6a83fcd5;
    public static final XXHashFactory FACTORY;
    
    private final Long value;
    
    static
    {
        FACTORY = XXHashFactory.fastestInstance();
    }
    
    public OpeningID(byte b[])
    {
        StreamingXXHash64 hashFunc = FACTORY.newStreamingHash64(HASHSEED);
//        hashFunc.reset(); // does not reset the seed
        hashFunc.update(b, 0, b.length);
        value = hashFunc.getValue();
    }
    
    public OpeningID(String s) { this(s.getBytes()); }
    
    public static OpeningID fromString(String s)
    {
        return new OpeningID(NumberUtils.hexToLong(s));
    }
    
    public OpeningID(long value) { this.value = value; }
    
    @Override
    public String toString() { return NumberUtils.longToHex(value, false); }
    
    @Override
    public boolean equals(Object other)
    {
        try
        {
            OpeningID otherOID = (OpeningID)other;
            return value.equals(otherOID.value);
        }
        
        catch (ClassCastException | NullPointerException e) { return false; }
    }
    
    @Override
    public int hashCode()
    {
        return HASHSEED ^ value.hashCode();
    }
    
    @Override
    public int compareTo(Object other)
    {
        return value.compareTo(((OpeningID)other).value);
    }
    
//    public static void main(String args[])
//    {
//        OpeningID oid1 = new OpeningID("hello world");
//        OpeningID oid2 = OpeningID.fromString("3a4c23e3c695ef34");
//        System.out.println(oid1);
//        System.out.println(oid2);
//        System.out.println(oid1.equals(oid2));
//    }
}
