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
public class TimeCtrl implements Comparable<TimeCtrl>
{
    public static class Segment implements Comparable<Segment>
    {
        private int moves;
        private Clock time;
        private Clock timeIncr;
        
        /**
         * 
         * @param ctrlStr example: "40/1200" or "0/300" or "0+6" or "20/300+6"
         * @throws InvalidTimeCtrlException 
         */
        public Segment(String ctrlStr) throws InvalidTimeCtrlException
        {
            try
            {
                String parts[] = ctrlStr.split("\\+");
                
                if (parts.length == 2) timeIncr = new Clock(parts[1]);
                
                else if (parts.length > 2)
                    throw new InvalidTimeCtrlException(ctrlStr);

                parts = parts[0].split("/");
                
                if (parts.length == 1)
                {
                    time = new Clock(parts[0]);
                    return;
                }
                
                if (parts.length != 2) throw new InvalidTimeCtrlException(ctrlStr);
            
                moves = Integer.valueOf(parts[0]);
                time = new Clock(parts[1]);
            }
            
            catch (NumberFormatException | InvalidClockException e)
            {
                throw new InvalidTimeCtrlException(ctrlStr);
            }
        }
        
        public int getMoves() { return moves; }
        public Clock getTime() { return time; }
        
        @Override
        public int compareTo(Segment that)
        {
            if (moves != that.moves) return moves - that.moves;
            if (time != that.time) return time.compareTo(that.time);
            return timeIncr.compareTo(that.timeIncr);
        }
        
        @Override
        public String toString()
        {
            return moves + "/" + time.inSecs() +
                (timeIncr != null ? "+" + timeIncr.inSecs() : "");
        }
    }
    
    private final Segment segments[];
    private final boolean official; // if this came from the "TimeControl" tag
    
    public TimeCtrl(String ctrlString, boolean official)
        throws InvalidTimeCtrlException
    {
        this.official = official;
        String parts[] = ctrlString.split("[: ]");
        
        if (parts.length > 3) throw new InvalidTimeCtrlException(ctrlString);
        segments = new Segment[parts.length];
        
        for (int i = 0; i < parts.length; i++)
            segments[i] = new Segment(parts[i]);
    }
    
    public Segment[] getSegments() { return segments; }
    public boolean isOfficial() { return official; }
    
    @Override
    public int compareTo(TimeCtrl that)
    {
        if (segments.length != that.segments.length)
            return segments.length - that.segments.length;
        
        for (int i = 0; i < segments.length; i++)
        {
            int comp = segments[i].compareTo(that.segments[i]);
            if (comp != 0) return comp;
        }
        
        return 0;
    }
    
    @Override
    public boolean equals(Object other)
    {
        try
        {
            TimeCtrl that = (TimeCtrl)other;
            return compareTo(that) == 0;
        }
        
        catch (ClassCastException e) { return false; }
    }
    
    @Override
    public String toString()
    {
        String ret = segments[0].toString();
        if (segments.length >= 2) ret += ":" + segments[1].toString();
        if (segments.length == 3) ret += ":" + segments[2].toString();
        return ret;
    }
}
