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

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class OpeningScore
{
    private int whiteWins;
    private int blackWins;
    private int draws;
    private int noResult;
    private long totalOobPlies;
    private long totalDisagree;

    OpeningScore() {}

    public void incWhiteWin() { whiteWins++; }
    public void incBlackWin() { blackWins++; }
    public void incDraw() { draws++; }
    public void incNoResult() { noResult++; }
    public void addOobPlies(int plies) { totalOobPlies += plies; }
    public void addDisagree(int plies) { totalDisagree += plies; }
    public int getWhiteWins() { return whiteWins; }
    public int getBlackWins() { return blackWins; }
    public int getDraws() { return draws; }
    public int getNoResults() { return noResult; }
    public long getTotalOobPlies() { return totalOobPlies; }
    public long getTotalDisagree() { return totalDisagree; }
    public int getGameCount() { return whiteWins + blackWins + draws; }
    public double getWhiteWinPct()
    {
        return (double)whiteWins / (whiteWins + blackWins + draws);
    }
    public double getBlackWinPct() { return (double)blackWins / (whiteWins + blackWins + draws); }
    public double getDrawPct()
    {
        return (double)draws / (whiteWins + blackWins + draws);
    }

    public double getDisagreePct()
    {
        // subtract the last move of every game from the denominator, since we don't know if it was disagreed
        long denominator = totalOobPlies - (whiteWins + blackWins + draws + noResult);
        return denominator < 1 ? 0 : (double)totalDisagree / denominator;
    }

    public double getAvgOobPlies()
    {
        long denominator = whiteWins + blackWins + draws + noResult;
        return denominator < 1 ? 0 : (double)totalOobPlies / denominator;
    }

    @Override
    public String toString()
    {
        double whiteWinPct = getWhiteWinPct();
        double blackWinPct = getBlackWinPct();

        return "games: " + getGameCount() + CLOptions.outputDelim +
            "white: " + Formats.PERCENT.format(whiteWinPct) + CLOptions.outputDelim +
            "black: " + Formats.PERCENT.format(blackWinPct) + CLOptions.outputDelim +
            "diff: " + Formats.PERCENT.format(whiteWinPct - blackWinPct) + CLOptions.outputDelim +
            "draw: " + Formats.PERCENT.format(getDrawPct());
    }
}
