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
public class PlayerResults
{
    private final String player;
    private int wins;
    private int losses;
    private int draws;
    private int noResults;
    
    public PlayerResults(String player)
    {
        this.player = player;
    }
    
    public String getPlayer() { return player; }
    
    public String getResults()
    {
        return String.format("+%d=%d-%d", wins, draws, losses);
    }
        
    public void incWins() { wins++; }
    public void incLosses() { losses++; }
    public void incDraws() { draws++; }
    public void incNoResults() { noResults++; }

    public void tallyResult(Game game) throws InvalidPlayerException
    {
        if (game.getWhite().equals(player))
        {
            switch (game.getResult())
            {
                case WHITEWIN: incWins(); break;
                case BLACKWIN: incLosses(); break;
                case DRAW: incDraws(); break;
                default: incNoResults();
            }
        }
        
        else if (game.getBlack().equals(player))
        {
            switch (game.getResult())
            {
                case WHITEWIN: incLosses(); break;
                case BLACKWIN: incWins(); break;
                case DRAW: incDraws(); break;
                default: incNoResults();
            }
        }
        
        else throw new InvalidPlayerException("'" + player + "' is not a player");
    }
}
