/*
 * The MIT License
 *
 * Copyright (c) 2023 Mark Chen <chen@dotfx.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED
 * "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dotfx.pgnutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class EloResolver
{
    private static final Map<String,Integer> eloMap = new HashMap<>();

    public static final void readEloMap(File eloFile)
    {
        final Pattern playerPattern = Pattern.compile("^(\\S.*\\S)\\s+-?\\d+$");
        final Pattern eloPattern = Pattern.compile("^.*\\s+(-?\\d+)$");

        try (Scanner fileScanner = new Scanner(eloFile))
        {
            while (fileScanner.hasNextLine())
            {
                String s = fileScanner.nextLine().trim();
                if (s.length() == 0) continue;

                eloMap.put(playerPattern.matcher(s).replaceAll("$1"),
                        Integer.valueOf(eloPattern.matcher(s).replaceAll("$1")));
            }
        }

        catch (FileNotFoundException e)
        {
            System.err.println("File '" + eloFile + "' not found.");
            System.exit(-1);
        }

        catch (NumberFormatException e)
        {
            System.err.println("Invalid integer value.  " + e.getMessage());
            System.exit(-1);
        }
    }

    public static Integer getWhiteElo(PgnGame game)
    {
        Integer ret = eloMap.get(game.getWhite());
        try { return ret == null ? Integer.valueOf(game.getValue("WhiteElo")) : ret; }
        catch (NumberFormatException e) { return null; }
    }

    public static Integer getBlackElo(PgnGame game)
    {
        Integer ret = eloMap.get(game.getBlack());
        try { return ret == null ? Integer.valueOf(game.getValue("BlackElo")) : ret; }
        catch (NumberFormatException e) { return null; }
    }
}
