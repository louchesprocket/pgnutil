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
public class GameInfo implements Comparable<GameInfo>
{
    private final int gameNum;
    private final NormalizedRound round;
    private final TimeCtrl timeCtrl;

    public GameInfo(int gameNum, NormalizedRound round, TimeCtrl timeCtrl)
    {
        this.gameNum = gameNum;
        this.round = round;
        this.timeCtrl = timeCtrl;
    }

    public int getGameNum() { return gameNum; }
    public NormalizedRound getRound() { return round; }
    public TimeCtrl getTimeCtrl() { return timeCtrl; }

    @Override
    public boolean equals(Object other)
    {
        try
        {
            GameInfo that = (GameInfo)other;
            return getGameNum() == that.getGameNum();
        }

        catch (ClassCastException e) { return false; }
    }
    
    @Override
    public int hashCode()
    {
        return GameInfo.class.hashCode() ^ gameNum;
    }

    @Override
    public int compareTo(GameInfo that)
    {
        return getGameNum() - that.getGameNum();
    }
}
