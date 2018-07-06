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

import java.text.NumberFormat;

/**
 *
 * @author Mark Chen
 * 
 * This class is not thread safe.
 */
public class OpeningStats
{
    private final NumberFormat PCT_FORMAT;
    private final NumberFormat DECIMAL_FORMAT;
    private final OpeningID oid;
    private final String eco;
    private int whiteWins;
    private int blackWins;
    private int draws;
    private int noResult;

    public OpeningStats(OpeningID oid, String eco)
    {
        this.oid = oid;
        this.eco = eco;
        whiteWins = blackWins = draws = noResult = 0;

        PCT_FORMAT = NumberFormat.getPercentInstance();
        PCT_FORMAT.setMinimumFractionDigits(1);
        PCT_FORMAT.setMaximumFractionDigits(1);

        DECIMAL_FORMAT = NumberFormat.getInstance();
        DECIMAL_FORMAT.setMinimumIntegerDigits(0);
        DECIMAL_FORMAT.setMinimumIntegerDigits(1);
        DECIMAL_FORMAT.setMinimumFractionDigits(3);
        DECIMAL_FORMAT.setMaximumFractionDigits(3);
    }

    public void count(Game game)
    {
        switch (game.getResult())
        {
            case WHITEWIN: incWhiteWin(); break;
            case BLACKWIN: incBlackWin(); break;
            case DRAW: incDraw(); break;
            default: incNoResult();
        }
    }

    public void incWhiteWin() { whiteWins++; }
    public void incBlackWin() { blackWins++; }
    public void incDraw() { draws++; }
    public void incNoResult() { noResult++; }
    public OpeningID getId() { return oid; }
    public String getEco() { return eco; }
    public int getWhiteWins() { return whiteWins; }
    public int getBlackWins() { return blackWins; }
    public int getDraws() { return draws; }
    public int getNoResults() { return noResult; }

    public int getGameCount()
    {
        return whiteWins + blackWins + draws;
    }
        
    public double getWhiteWinPct()
    {
        return (double)whiteWins / (whiteWins + blackWins + draws);
    }

    public double getBlackWinPct()
    {
        return (double)blackWins / (whiteWins + blackWins + draws);
    }

    public double getDrawPct()
    {
        return (double)draws / (whiteWins + blackWins + draws);
    }
    
    /**
     * 
     * @param selectors
     * @return
     * @throws InvalidSelectorException 
     */
    public String get(OutputSelector selectors[])
        throws InvalidSelectorException
    {
        StringBuilder ret = new StringBuilder();
        
        for (int i = 0; i < selectors.length; i++)
        {
            switch (selectors[i].getValue())
            {
                case ECO: ret.append(getEco()); break;
                case OID: ret.append(getId()); break;
                case COUNT: ret.append(getGameCount()); break;
                case WWINS: ret.append(getWhiteWins()); break;
                case BWINS: ret.append(getBlackWins()); break;
                case DRAWS: ret.append(getDraws()); break;
                
                case WWINPCT:
                    ret.append(DECIMAL_FORMAT.format(getWhiteWinPct()));
                    break;
                    
                case BWINPCT:
                    ret.append(DECIMAL_FORMAT.format(getBlackWinPct()));
                    break;
            
                case DIFF:
                    ret.append(getWhiteWins() - getBlackWins());
                    break;
                    
                case DIFFPCT:
                    ret.append(DECIMAL_FORMAT.format(getWhiteWinPct() -
                        getBlackWinPct()));
                    
                    break;
                    
                case DRAWPCT:
                    ret.append(DECIMAL_FORMAT.format(getDrawPct()));
                    break;
                    
                default:
                    throw new InvalidSelectorException("'" + selectors[i] +
                        "' is not a valid selector in this context");
            }
            
            if (i < selectors.length - 1) ret.append(CLOptions.outputDelim);
        }
        
        return ret.toString();
    }
        
    @Override
    public String toString()
    {
        double whiteWinPct = getWhiteWinPct();
        double blackWinPct = getBlackWinPct();

        return "ECO: " + eco + "; " + "opening: " + oid + "; " + 
            "games: " + getGameCount() + "; " +
            "white: " + PCT_FORMAT.format(whiteWinPct) + "; " +
            "black: " + PCT_FORMAT.format(blackWinPct) + "; " +
            "diff: " + PCT_FORMAT.format(whiteWinPct - blackWinPct) + "; " +
            "draw: " + PCT_FORMAT.format(getDrawPct());
    }
}
