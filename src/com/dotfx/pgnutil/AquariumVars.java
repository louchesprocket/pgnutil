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

import com.cedarsoftware.util.StringUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mark Chen
 */
public class AquariumVars
{
    public static class Eval implements Comparable<Eval>
    {
        private int eval; // in centipawns
        private short depth;

        public Eval(String st)
        {
            int comma = st.indexOf(',');

            if (comma > -1)
            {
                try
                {
                    eval = Integer.parseInt(st.substring(0, comma));
                    depth = Short.parseShort(st.substring(comma + 1));
                }

                catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {}
            }
        }

        public int getEval() { return eval; }
        public short getDepth() { return depth; }

        @Override
        public int compareTo(Eval other) { return eval - other.eval; }
    }

    private Clock clk; // my time remaining until time control after this move
    private Clock clko; // opponent's time remaining
    private String emt; // elapsed time for this move
    private String eval; // position evaluation
    private String meval; // similar to emt? plus egtb hits. E.g., "15s|TB:3m"
    private String expectedResponse;
    private final Map<String,String> otherVars;
    
    public AquariumVars(PgnGame.Move move)
    {
        otherVars = new HashMap<>();

        for (String comment : move.getComments())
        {
            int varNameStartIdx, pos = 0, varNameEndIdx;
            int commentLen = comment.length();

            while ((varNameStartIdx = comment.indexOf("[%", pos)) >= 0 ||
                    (varNameStartIdx = comment.indexOf("(", pos)) >= 0)
            {
                int i;

                if (comment.charAt(varNameStartIdx) == '[')
                {
                    String oneVarName;
                    int varValStartIdx, varValEndIdx;

                    varNameStartIdx += 2;

                    for (i = varNameStartIdx + 1; i < commentLen; i++)
                    {
                        char oneChar = comment.charAt(i);
                        if (oneChar == ' ' || oneChar == '\t') break;
                    }

                    varNameEndIdx = i;
                    oneVarName = comment.substring(varNameStartIdx, varNameEndIdx);

                    for (i = varNameEndIdx; i < commentLen; i++)
                    {
                        char oneChar = comment.charAt(i);
                        if (oneChar != ' ' && oneChar != '\t') break;
                    }

                    varValStartIdx = i;

                    for (i = varValStartIdx + 1; i < commentLen; i++)
                    {
                        if (comment.charAt(i) == '\"') do i++; while (comment.charAt(i) != '\"');
                        if (comment.charAt(i) == ']') break;
                    }

                    varValEndIdx = i;
                    pos = varValEndIdx + 1;

                    try
                    {
                        switch (oneVarName)
                        {
                            case "clk":
                                clk = new Clock(comment.substring(varValStartIdx, varValEndIdx));
                                break;

                            case "clko":
                                clko = new Clock(comment.substring(varValStartIdx, varValEndIdx));
                                break;

                            case "emt":
                                emt = comment.substring(varValStartIdx, varValEndIdx);
                                break;

                            case "eval":
                                eval = comment.substring(varValStartIdx, varValEndIdx);
                                break;

                            case "meval":
                                meval = comment.substring(varValStartIdx, varValEndIdx);
                                break;

                            default:
                                otherVars.put(oneVarName, comment.substring(varValStartIdx, varValEndIdx));
                        }
                    } catch (InvalidClockException e) {} // best effort
                }

                else // "("
                {
                    for (pos = ++varNameStartIdx; pos < commentLen; pos++) if (comment.charAt(pos) == ')') break;
                    expectedResponse = comment.substring(varNameStartIdx, pos);
                }
            }
        }
    }
    
    public Clock getClk() { return clk; }
    public Clock getClko() { return clko; }
    public String getEmt() { return emt; }
    public Eval getEval() { return new Eval(eval); }
    public String getMeval() { return meval; }
    public String getExpectedResponse() { return expectedResponse; }
    public String get(String tag) { return otherVars.get(tag); }
    
    public String getTbHits()
    {
        if (meval == null) return null;
        int tbVarStart = meval.indexOf("|TB:");
        if (tbVarStart > -1) return meval.substring(tbVarStart + 4);
        return null;
    }
}
