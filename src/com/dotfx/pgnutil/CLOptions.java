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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Mark Chen
 */
public class CLOptions
{
    private static final String CMIN = "-cmin";
    private static final String D = "-d";
    private static final String E = "-e";
    private static final String ELO = "-elo";
    private static final String GN = "-gn";
    private static final String H = "-h";
    private static final String HDRAW = "-hdraw";
    private static final String HED = "-hed";
    private static final String HWD = "-hwd";
    private static final String I = "-i";
    private static final String LDRAW = "-ldraw";
    private static final String LWD = "-lwd";
    private static final String M = "-m";
    private static final String ML = "-ml";
    private static final String MP = "-mp";
    private static final String MO = "-mo";
    private static final String MW = "-mw";
    private static final String MT = "-mt";
    private static final String NM = "-nm";
    private static final String NMO = "-nmo";
    private static final String NOF = "-nof";
    private static final String O = "-o";
    private static final String OD = "-od";
    private static final String OF = "-of";
    private static final String R = "-r";
    private static final String RL = "-rl";
    private static final String RO = "-ro";
    private static final String RW = "-rw";
    private static final String S = "-s";
    private static final String TC = "-tc";
    private static final String V = "-v";
    
    private static enum OptId
    {
        MINGAMECOUNT(CMIN),
        DUPLICATES(D),
        ELOFILE(ELO),
        EVENTS(E),
        GAMENUM(GN),
        HIWINDIFF(HWD),
        INPUTFILE(I),
        LOWINDIFF(LWD),
        MATCH(M),
        MATCHLOSER(ML),
        MATCHOPENING(MO),
        MATCHPLAYER(MP),
        MATCHTAG(MT),
        MATCHWINNER(MW),
        MAXDRAW(HDRAW),
        MINDRAW(LDRAW),
        NOTMATCH(NM),
        NOTMATCHOPENING(NMO),
        NOTOPENINGFILE(NOF),
        OPENINGFILE(OF),
        OPENINGS(O),
        OUTPUTDELIM(OD),
        REPLACE(R),
        REPLACELOSER(RL),
        REPLACEOPENING(RO),
        REPLACEWINNER(RW),
        SELECTORS(S),
        TIMECONTROL(TC);
        
        private static final Map<String,OptId> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (OptId v : OptId.values()) sigMap.put(v.toString(), v);
        }
        
        private OptId(String signifier) { this.signifier = signifier; }
        @Override public String toString() { return signifier; }
        
        public static OptId get(String signifier)
        {
            if (signifier == null) return null;
            return sigMap.get(signifier.toLowerCase());
        }
    }
    
    private static final Map<OptId,Integer> OPTMAP = new HashMap<>();
    
    private void countOption(OptId opt)
    {
        Integer currentCount = OPTMAP.get(opt);
        if (currentCount == null) OPTMAP.put(opt, 1);
        else OPTMAP.put(opt, ++currentCount);
    }
    
    public static int getCount(OptId opt)
    {
        Integer count = OPTMAP.get(opt);
        return count == null ? 0 : count;
    }
    
    // matchers

    @Option(name = GN, aliases = "-game_number",
        metaVar = "<range1,range2,...>",
        usage = "output games whose ordinal position in the input source " +
        "is contained in <range1,range2,...>")
    private void setGameNum(String gameno)
    {
        if (getCount(OptId.GAMENUM) > 0)
        {
            System.err.println("Option '" + OptId.GAMENUM + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.GAMENUM);
        PGNUtil.addMatchProcessor(new PGNUtil.MatchGameNumProcessor(gameno));
    }

    @Option(name = M, aliases = "-matches", metaVar = "<regex>",
        usage = "output games matching the regular expression <regex>")
    private void setContains(String regex)
    {
        countOption(OptId.MATCH);
        
        PGNUtil.addMatchProcessor(new PGNUtil.ContainsProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = NM, aliases = "-not_matches", metaVar = "<regex>",
        usage = "output games not matching the regular expression " +
        "<regex>")
    private void setNotContains(String regex)
    {
        countOption(OptId.NOTMATCH);
        
        PGNUtil.addMatchProcessor(new PGNUtil.NotContainsProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MW, aliases = "-match_winner", metaVar = "<regex>",
        usage = "output games won by player <regex>")
    private void setWinner(String regex)
    {
        if (getCount(OptId.MATCHWINNER) > 0)
        {
            System.err.println("Option '" + OptId.MATCHWINNER + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.MATCHWINNER);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchWinProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = ML, aliases = "-match_loser", metaVar = "<regex>",
        usage = "output games lost by player <regex>")
    private void setLoser(String regex)
    {
        if (getCount(OptId.MATCHLOSER) > 0)
        {
            System.err.println("Option '" + OptId.MATCHLOSER + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.MATCHLOSER);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchLossProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MP, aliases = "-match_player", metaVar = "<regex>",
        usage = "output games where <regex> is a player")
    private void setPlayer(String regex)
    {
        if (getCount(OptId.MATCHPLAYER) > 1)
        {
            System.err.println("Option '" + OptId.MATCHPLAYER + "' cannot be " +
                "set more than twice!");
            
            System.exit(-1);
        }
        
        if (getCount(OptId.MATCHPLAYER) == 1 && PGNUtil.outputSelectors != null)
        {
            for (OutputSelector selector : PGNUtil.outputSelectors)
            {
                if (selector.getValue().equals(OutputSelector.Value.OPPONENT))
                {
                    System.err.println("The 'opponent' selector only works " +
                        "while matching one player ('-mp')!");

                    System.exit(-1);
                }
            }
        }
        
        countOption(OptId.MATCHPLAYER);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchPlayerProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MO, forbids = {O, OF, NMO, NOF},
        aliases = "-match_opening", metaVar = "<oid1,oid1,...>",
        usage = "output games in which the opening-book moves are the " +
        "same as any of <oid,oid2,...>")
    private void setOpening(String opening)
    {
        if (getCount(OptId.MATCHOPENING) > 0)
        {
            System.err.println("Option '" + OptId.MATCHOPENING + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.MATCHOPENING);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchOpeningProcessor(
            opening.trim().replaceAll("\\W+", ",").replaceAll(",+", ",")));
    }

    @Option(name = OF, forbids = {O, MO, NMO, NOF},
        aliases = "-opening_file", metaVar = "<filename>",
        usage = "output games in which the opening-book moves are any of " +
        "those contained in <filename>")
    private void setOpeningFile(File of)
    {
        if (getCount(OptId.OPENINGFILE) > 0)
        {
            System.err.println("Option '" + OptId.OPENINGFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.OPENINGFILE);
        
        try
        {
            byte buf[] = new byte[1024];
            int read;
            InputStream is = new BufferedInputStream(new FileInputStream(of));
            OutputStream os = new ByteArrayOutputStream();
            
            while ((read = is.read(buf)) >= 0) os.write(buf, 0, read);
            
            PGNUtil.addMatchProcessor(new PGNUtil.MatchOpeningProcessor(
                os.toString().replaceAll("#.*", "").trim().
                    replaceAll("\\W+", ",").replaceAll(",+", ",")));
        }
        
        catch (FileNotFoundException e)
        {
            System.err.println("File '" + of + "' not found.");
            System.exit(-1);
        }
        
        catch (IOException e)
        {
            System.err.println("Error reading file '" + of + "'");
            System.exit(-1);
        }
    }

    @Option(name = NMO, forbids = {O, MO, OF, NOF},
        aliases = "-not_match_opening", metaVar = "<oid1,oid1,...>",
        usage = "output games in which the opening-book moves are not " +
        "the same as any of <oid,oid2,...>")
    private void setNotOpening(String opening)
    {
        if (getCount(OptId.NOTMATCHOPENING) > 0)
        {
            System.err.println("Option '" + OptId.NOTMATCHOPENING + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.NOTMATCHOPENING);
        
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchOpeningProcessor(
            opening.trim().replaceAll("\\W+", ",").replaceAll(",+", ",")));
    }

    @Option(name = NOF, forbids = {O, OF, NMO, MO},
        aliases = "-not_opening_file", metaVar = "<filename>",
        usage = "output games in which the opening-book moves are none of " +
        "those contained in <filename>")
    private void setNotOpeningFile(File of)
    {
        if (getCount(OptId.NOTOPENINGFILE) > 0)
        {
            System.err.println("Option '" + OptId.NOTOPENINGFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.NOTOPENINGFILE);
        
        try
        {
            byte buf[] = new byte[1024];
            int read;
            InputStream is = new BufferedInputStream(new FileInputStream(of));
            OutputStream os = new ByteArrayOutputStream();
            
            while ((read = is.read(buf)) >= 0) os.write(buf, 0, read);
            
            PGNUtil.addMatchProcessor(new PGNUtil.NotMatchOpeningProcessor(
                os.toString().replaceAll("#.*", "").trim().
                    replaceAll("\\W+", ",").replaceAll(",+", ",")));
        }
        
        catch (FileNotFoundException e)
        {
            System.err.println("File '" + of + "' not found.");
            System.exit(-1);
        }
        
        catch (IOException e)
        {
            System.err.println("Error reading file '" + of + "'");
            System.exit(-1);
        }
    }

    @Option(name = MT, aliases = "-match_tag", metaVar = "<tag>/<regex>",
        usage = "output games in which PGN tag <tag> has value <regex>")
    private void setTag(String tag)
    {
        countOption(OptId.MATCHTAG);
        
        // The regex passed to split() allows escaping of the delimiter character
        // ("/") with a backslash. See https://stackoverflow.com/questions/
        // 18677762/handling-delimiter-with-escape-characters-in-java-string-split-method
        // First, however, we must escape any escaped backslashes.

        String[] tagTokens = tag.replace("\\\\", "\0").split("(?<!\\\\)/");

        if (tagTokens.length != 2)
        {
            System.err.println("\nIncorrect token count in match-tag string.");
            System.exit(-1);
        }

        for (int i = 0; i < 2; i++)
            tagTokens[i] = tagTokens[i].replace("\0", "\\\\").replace("\\/", "/");
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchTagProcessor(tagTokens[0],
            Pattern.compile(tagTokens[1], Pattern.DOTALL)));
    }

    @Option(name = TC, aliases = "-time_ctrl",
        metaVar = "<timectrl>", hidden = true,
        usage = "match games in which the time control is <timectrl>")
    private void setTimeControl(String timectrl)
    {
        if (getCount(OptId.TIMECONTROL) > 0)
        {
            System.err.println("Option '" + OptId.TIMECONTROL + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.TIMECONTROL);
        
        try
        {
            PGNUtil.addMatchProcessor(new PGNUtil.MatchTimeCtrlProcessor(timectrl));
        }
        
        catch (InvalidTimeCtrlException e)
        {
            System.err.println("\nInvalid time control: '" + timectrl + "'");
            System.exit(-1);
        }
    }

    // replacers

    @Option(name = R, aliases = "-replace", metaVar = "<regx1>/<regx2>/<repl>",
        usage = "output all games (or all selected games), but for each " +
        "game matching <regx1>, replace <regx2> with <repl>")
    private void setReplace(String replaceStr)
    {
        if (getCount(OptId.REPLACE) > 0)
        {
            System.err.println("Option '" + OptId.REPLACE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.REPLACE);
        
        // The regex passed to split() allows escaping of the delimiter character
        // ("/") with a backslash. See https://stackoverflow.com/questions/
        // 18677762/handling-delimiter-with-escape-characters-in-java-string-split-method
        // First, however, we must escape any escaped backslashes.
        String replaceTokens[] =
            replaceStr.replace("\\\\", "\0").split("(?<!\\\\)/");

        if (replaceTokens.length != 3)
        {
            System.err.println("\nIncorrect token count in replace string.");
            System.exit(-1);
        }

        for (int i = 0; i < 3; i++)
            replaceTokens[i] = replaceTokens[i].replace("\0",
                "\\\\").replace("\\/", "/");
        
        int n = PGNUtil.replaceProcessors.size();
        
        // This one must go last.
        PGNUtil.replaceProcessors.add(n,
            new PGNUtil.ReplaceProcessor(Pattern.compile(replaceTokens[1],
                Pattern.DOTALL), replaceTokens[2]));
        
        PGNUtil.replaceProcessors.add(n, new PGNUtil.ReplaceContainsProcessor(
            Pattern.compile(replaceTokens[0], Pattern.DOTALL)));
    }

    @Option(name = RW, depends = {R}, aliases = "-replace_winner",
        metaVar = "<regex>",
        usage = "in combination with '" + R + "' option, select games won by " +
        "player <regex> for replacement")
    private void replaceWinner(String regex)
    {
        if (getCount(OptId.REPLACEWINNER) > 0)
        {
            System.err.println("Option '" + OptId.REPLACEWINNER + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.REPLACEWINNER);
        int n = PGNUtil.replaceProcessors.size();
        
        PGNUtil.replaceProcessors.add(n == 0 ? 0 : n - 1,
            new PGNUtil.ReplaceWinProcessor(
                Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = RL, depends = {R}, aliases = "-replace_loser",
        metaVar = "<regex>",
        usage = "in combination with '" + R + "' option, select games lost by " +
        "player <regex> for replacement")
    private void replaceLoser(String regex)
    {
        if (getCount(OptId.REPLACELOSER) > 0)
        {
            System.err.println("Option '" + OptId.REPLACELOSER + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.REPLACELOSER);
        int n = PGNUtil.replaceProcessors.size();
        
        PGNUtil.replaceProcessors.add(n == 0 ? 0 : n - 1,
            new PGNUtil.ReplaceLossProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = RO, depends = {R}, aliases = "-replace_opening",
        metaVar = "<oid1,oid1,...>",
        usage = "in combination with '" + R + "' option, select games in " +
        "which the opening-book moves are the same as any of <oid,oid2,...> " +
        "for replacement")
    private void replaceOpening(String opening)
    {
        if (getCount(OptId.REPLACEOPENING) > 0)
        {
            System.err.println("Option '" + OptId.REPLACEOPENING + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.REPLACEOPENING);
        int n = PGNUtil.replaceProcessors.size();
        
        PGNUtil.replaceProcessors.add(n == 0 ? 0 : n - 1,
            new PGNUtil.ReplaceOpeningProcessor(opening));
    }

    // duplicates

    @Option(name = D, forbids = {O, E}, aliases = "-duplicates",
        usage = "list games containing identical players and move lists; " +
            "each line of output contains one set of two or more " +
            "duplicate games numbers")
    private void duplicates(boolean d)
    {
        if (getCount(OptId.DUPLICATES) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATES + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATES);
        
        PGNUtil.DuplicateHandler handler = new PGNUtil.DuplicateHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    // events

    @Option(name = E, forbids = {D, O}, aliases = "-events",
        usage = "list one event per line, with game numbers (ordinal " +
        "position of the game as it was read from the input source)")
    private void events(boolean e)
    {
        if (getCount(OptId.EVENTS) > 0)
        {
            System.err.println("Option '" + OptId.EVENTS + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.EVENTS);
        PGNUtil.EventMapHandler handler = new PGNUtil.EventMapHandler();
        PGNUtil.setHandler(handler);
        
        PGNUtil.setExitProcessor(
            new PGNUtil.EventMapExitProcessor(handler.getEventMap()));
    }

    // opening stats

    @Option(name = O, forbids = {D, E},
        aliases = "-opening_stats", usage = "print win/loss/draw " +
        "statistics for each opening")
    private void openings(boolean o)
    {
        if (getCount(OptId.OPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.OPENINGS + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.OPENINGS);
        
        PGNUtil.OpeningsHandler handler = new PGNUtil.OpeningsHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.OpeningsExitProcessor(handler));
    }

    @Option(name = CMIN, depends = {O}, aliases = "-count_min",
        metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings " +
            "that appear in at least <min> games")
    private void minOpeningCount(int cmin)
    {
        if (getCount(OptId.MINGAMECOUNT) > 0)
        {
            System.err.println("Option '" + OptId.MINGAMECOUNT + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.MINGAMECOUNT);
        PGNUtil.addOpeningProcessor(new PGNUtil.MinGamesProcessor(cmin));
    }

    @Option(name = HWD, depends = {O}, aliases = "-hi_win_diff",
        metaVar = "<max>",
        usage = "in combination with '" + O + "' option, print only openings " +
            "for which the percentage win difference between white and " +
            "black is at most <max> percent")
    private void hiWinDiff(double max)
    {
        if (getCount(OptId.HIWINDIFF) > 0)
        {
            System.err.println("Option '" + OptId.HIWINDIFF + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.HIWINDIFF);
        PGNUtil.addOpeningProcessor(new PGNUtil.MaxWinDiffProcessor(max));
    }

    @Option(name = LWD, depends = {O}, aliases = "-lo_win_diff",
        metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings " +
            "for which the percentage win difference between white and " +
            "black is at least <min> percent")
    private void loWinDiff(double min)
    {
        if (getCount(OptId.LOWINDIFF) > 0)
        {
            System.err.println("Option '" + OptId.LOWINDIFF + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.LOWINDIFF);
        PGNUtil.addOpeningProcessor(new PGNUtil.MinWinDiffProcessor(min));
    }

    @Option(name = HDRAW, depends = {O}, aliases = "-hi_draw_pct",
        metaVar = "<max>",
        usage = "in combination with '" + O + "' option, print only openings " +
            "for which the percentage of draws is at most <max> percent")
    private void maxDraw(double max)
    {
        if (getCount(OptId.MAXDRAW) > 0)
        {
            System.err.println("Option '" + OptId.MAXDRAW + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.MAXDRAW);
        PGNUtil.addOpeningProcessor(new PGNUtil.MaxDrawProcessor(max));
    }

    @Option(name = LDRAW, depends = {O}, aliases = "-lo_draw_pct",
        metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings " +
            "for which the percentage of draws is at least <min> percent")
    private void minDraw(double min)
    {
        if (getCount(OptId.MINDRAW) > 0)
        {
            System.err.println("Option '" + OptId.MINDRAW + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.MINDRAW);
        PGNUtil.addOpeningProcessor(new PGNUtil.MinDrawProcessor(min));
    }

    @Option(name = HED, depends = {O, ELO}, aliases = "-hi_elo_diff",
        metaVar = "<diff>",
        usage = "in combination with '" + O + "' and '" + ELO + "' options, " +
            "print only openings for which the difference in player elo " +
            "ratings is at most <diff>")
    static Integer maxEloDiff;

    @Option(name = ELO, depends = {HED}, aliases = "-elo_file",
        metaVar = "<filename>",
        usage = "in combination with '" + O + "' and '" + HED + "' options, use " +
            "elo ratings contained in the file <filename>")
    private void setEloFile(File of)
    {
        if (getCount(OptId.ELOFILE) > 0)
        {
            System.err.println("Option '" + OptId.ELOFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.ELOFILE);
        PGNUtil.eloMap = new HashMap<>();
        
        try (Scanner fileScanner = new Scanner(of))
        {  
            while (fileScanner.hasNextLine())
            {
                String s = fileScanner.nextLine().trim();
                if (s.length() == 0) continue;
                
                PGNUtil.eloMap.put(s.replaceAll("^(\\S.*\\S)\\s+-?\\d+$", "$1"),
                    Integer.valueOf(s.replaceAll("^.*\\s+(-?\\d+)$", "$1")));
            }
        }
        
        catch (FileNotFoundException e)
        {
            System.err.println("File '" + of + "' not found.");
            System.exit(-1);
        }
        
        catch (NumberFormatException e)
        {
            System.err.println("Invalid integer value.  " + e.getMessage());
            System.exit(-1);
        }
    }

    // output-field selector

    @Option(name = S, forbids = {D, E}, aliases = "-select",
        metaVar = "<field1,field2,..>",
        usage = "select fields for output.  The field 'moves' selects " +
        "the game's move list, the field 'gameno' selects the ordinal " +
        "position of the game as it was read from the input source, and " +
        "the field 'oid' selects the game's opening identifier (not " +
        "ECO). The fields 'winner' and 'loser' select the winning and " +
        "losing player, respectively.  When the '" + MP + "' option is used, " +
        "the field 'opponent' selects the other player.  Any other field " +
        "will be selected from the game's tag list")
    private void selectors(String spec)
    {
        if (getCount(OptId.SELECTORS) > 0)
        {
            System.err.println("Option '" + OptId.SELECTORS + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.SELECTORS);
        String tokens[] = spec.split(",\\W*");
        PGNUtil.outputSelectors = new OutputSelector[tokens.length];
        int i;
        
        for (i = 0; i < tokens.length; i++)
        {
            PGNUtil.outputSelectors[i] = new OutputSelector(tokens[i]);
            
            if (getCount(OptId.MATCHPLAYER) > 1 &&
                PGNUtil.outputSelectors[i].getValue().equals(OutputSelector.Value.OPPONENT))
            {
                System.err.println("The 'opponent' selector only works " +
                    "while matching one player ('" + MP + "')!");

                System.exit(-1);
            }
        }
        
        if (PGNUtil.handler == null)
            PGNUtil.handler = new PGNUtil.SelectGameHandler(PGNUtil.outputSelectors);
    }

    @Option(name = OD, forbids = {D}, aliases = "-output_delim",
        metaVar = "<delim>",
        usage = "set the delimiter for output types where this is sensible")
    static String outputDelim = "|";

    @Option(name = I, aliases = "-inputfile", usage = "input PGN file; " +
        "if this option is not present, pgnutil reads from standard input",
        metaVar = "<filename>")
    private void setInput(File f)
    {
        if (getCount(OptId.INPUTFILE) > 0)
        {
            System.err.println("Option '" + OptId.INPUTFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.INPUTFILE);
        
        try
        {
            PGNUtil.pgn = new PGNFile(new BufferedReader(new FileReader(f)));
        }
        
        catch (FileNotFoundException e)
        {
            System.err.println("File not found: " + f);
            System.exit(-1);
        }
    }

    @Option(name = H, forbids = {D, E, O, M, GN, NM, MW, ML, MP, MO, NMO, R, V},
        aliases = "-help", usage = "print usage information")
    boolean help;

    @Option(name = V, forbids = {D, E, O, M, GN, NM, MW, ML, MP, MO, NMO, R, H},
        aliases = "-version",
        usage = "print version information")
    private void version(boolean v)
    {
        System.out.println("pgnutil version " + PGNUtil.VERSION);
        System.exit(0);
    }
        
//        @Argument(metaVar = "[target [target2 [target3] ...]]", usage = "targets")
//        private List<String> targets = new ArrayList<String>();
}
