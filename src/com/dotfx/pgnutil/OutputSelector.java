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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mark Chen
 */
public class OutputSelector
{
    public static enum Value
    {
        // standard seven tags
        BLACK("black"),
        DATE("date"),
        EVENT("event"),
        RESULT("result"),
        ROUND("round"),
        SITE("site"),
        WHITE("white"),
        
        // Aquarium
        BLACKELO("blackelo"),
        CLASSES("classes"),
        TIMECONTROL("timecontrol"),
        WHITEELO("whiteelo"),
        
        // Arena extras
        BLACKTYPE("blacktype"),
        ECO("eco"),
        PLYCOUNT("plycount"),
        TERMINATION("termination"),
        TIME("time"),
        VARIATION("variation"),
        WHITETYPE("whitetype"),
        
        // special
        FULLMOVES("fullmoves"),
        GAMENO("gameno"),
        LOSER("loser"),
        MOVES("moves"),
        OID("oid"),
        OPENINGMOVES("openingmoves"),
        OPPONENT("opponent"),
        PLIES("plies"),
        TEXTSIZE("textsize"),
        TIMECTRL("timectrl"),
        WINNER("winner"),
        
        // additional opening-stat selectors
        BWINPCT("bwinpct"),
        BWINS("bwins"),
        COUNT("count"), // also applies to player results
        DIFF("diff"),
        DIFFPCT("diffpct"),
        DRAWPCT("drawpct"),
        DRAWS("draws"), // also applies to player results
        WWINPCT("wwinpct"),
        WWINS("wwins"),
        
        // additional event selectors
        LASTROUND("lastround"),
        ROUNDCOUNT("roundcount"),
        
        // additional player-results selectors
        PLAYER("player"),
        WINS("wins"),
        LOSSES("losses"),
        NORESULTS("noresults"),
        WINPCT("winpct"),
        
        OTHER("");
        
        private static final Map<String,Value> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (Value v : Value.values()) sigMap.put(v.toString(), v);
        }
        
        private Value(String signifier) { this.signifier = signifier; }
        @Override public String toString() { return signifier; }
        
        public static Value get(String signifier)
        {
            if (signifier == null) return null;
            return sigMap.get(signifier.toLowerCase());
        }
    }
    
    public static final OutputSelector ECO = new OutputSelector("ECO");
    
    public static final OutputSelector TIMECONTROL =
        new OutputSelector("TimeControl");
    
    private final Value value;
    private final String literal;
    
    public OutputSelector(Value value)
    {
        this.value = value == null ? Value.OTHER : value;
        literal = this.value.toString();
    }
    
    public OutputSelector(String literal)
    {
        Value v = Value.get(literal);
        value = v == null ? Value.OTHER : v;
        this.literal = literal;
    }
    
    public Value getValue() { return value; }
    
    @Override
    public String toString() { return literal; }
}
