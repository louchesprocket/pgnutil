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
 *
 * @author Mark Chen
 */
public class Clock implements Comparable<Clock>
{
    private final int seconds;
    
    public Clock(String clockSt) throws InvalidClockException
    {
        String clockParts[] = clockSt.split(":");
        int partCount = clockParts.length;
        if (partCount > 3) throw new InvalidClockException(clockSt);

        try
        {
            if (clockParts[0].charAt(0) == '-') // engine has faulty time management
            {
                clockParts[0] = clockParts[0].substring(1);

                seconds = -(Integer.valueOf(clockParts[partCount - 1]) +
                            (partCount >= 2 ? Integer.valueOf(clockParts[partCount - 2]) * 60 : 0) +
                            (partCount == 3 ? Integer.valueOf(clockParts[0]) * 3600 : 0));
            }

            else seconds = Integer.valueOf(clockParts[partCount - 1]) +
                            (partCount >= 2 ? Integer.valueOf(clockParts[partCount - 2]) * 60 : 0) +
                            (partCount == 3 ? Integer.valueOf(clockParts[0]) * 3600 : 0);
        }

        catch (NumberFormatException e)
        {
            throw new InvalidClockException("invalid time: " + "'" + clockSt + "'");
        }
    }
    
    public int getHrs() { return seconds / 3600; }
    public int getMins() { return (seconds % 3600) / 60; }
    public int getSecs() { return seconds % 60; }

    public int inSecs() { return seconds; }

    @Override
    public boolean equals(Object that) { return seconds == ((Clock)that).seconds; }
    
    @Override
    public int compareTo(Clock that) { return seconds - that.seconds; }
    
    @Override
    public String toString()
    {
        if (seconds < 0)
            return String.format("-%01d:%02d:%02d", Math.abs(getHrs()), Math.abs(getMins()), Math.abs(getSecs()));

        return String.format("%01d:%02d:%02d", getHrs(), getMins(), getSecs());
    }
}
