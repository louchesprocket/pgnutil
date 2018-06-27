/*
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
            String result = game.getResult();
            
            if (result.equals("1-0")) incWhiteWin();
            else if (result.equals("1/2-1/2")) incDraw();
            else if (result.equals("0-1")) incBlackWin();
            else incNoResult();
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
     * @param spec 
     * 
     * @return a String containing pipe-separated values corresponding to the
     *         requested fields
     */
    public String get(String spec)
    {
        StringBuilder ret = new StringBuilder();
        String tokens[] = spec.split(",\\W*");
        
        for (String token : tokens)
        {
            if (token.equalsIgnoreCase("eco"))
                ret.append(getEco()).append("|");
            
            else if (token.equalsIgnoreCase("oid"))
                ret.append(getId()).append("|");
            
            else if (token.equalsIgnoreCase("count"))
                ret.append(getGameCount()).append("|");
            
            else if (token.equalsIgnoreCase("wwins"))
                ret.append(getWhiteWins()).append("|");
            
            else if (token.equalsIgnoreCase("wwinpct"))
                ret.append(DECIMAL_FORMAT.format(getWhiteWinPct())).append("|");
            
            else if (token.equalsIgnoreCase("bwins"))
                ret.append(getBlackWins()).append("|");
            
            else if (token.equalsIgnoreCase("bwinpct"))
                ret.append(DECIMAL_FORMAT.format(getBlackWinPct())).append("|");
            
            else if (token.equalsIgnoreCase("diff"))
                ret.append(getWhiteWins() - getBlackWins()).append("|");
            
            else if (token.equalsIgnoreCase("diffpct"))
                ret.append(DECIMAL_FORMAT.format(getWhiteWinPct() -
                    getBlackWinPct())).append("|");
            
            else if (token.equalsIgnoreCase("draws"))
                ret.append(getDraws()).append("|");
            
            else if (token.equalsIgnoreCase("drawpct"))
                ret.append(DECIMAL_FORMAT.format(getDrawPct())).append("|");
        }
        
        ret.deleteCharAt(ret.length() - 1);
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
