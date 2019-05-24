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
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Mark Chen
 */
public class CLOptions
{
    private static final String APF = "-apf";
    private static final String AQ = "-aq";
    private static final String CMIN = "-cmin";
    private static final String D = "-d";
    private static final String DO = "-do";
    private static final String DPO = "-dpo";
    private static final String E = "-e";
    private static final String EE = "-ee";
    private static final String ELO = "-elo";
    private static final String GN = "-gn";
    private static final String H = "-h";
    private static final String HDRAW = "-hdraw";
    private static final String HED = "-hed";
    private static final String HOOB = "-hoob";
    private static final String HPC = "-hpc";
    private static final String HWD = "-hwd";
    private static final String I = "-i";
    private static final String LDRAW = "-ldraw";
    private static final String LOOB = "-loob";
    private static final String LPC = "-lpc";
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
    private static final String NPF = "-npf";
    private static final String O = "-o";
    private static final String OD = "-od";
    private static final String OF = "-of";
    private static final String P = "-p";
    private static final String PERF = "-perf";
    private static final String PF = "-pf";
    private static final String R = "-r";
    private static final String RL = "-rl";
    private static final String RO = "-ro";
    private static final String RW = "-rw";
    private static final String S = "-s";
    private static final String TC = "-tc";
    private static final String V = "-v";
    private static final String VD = "-vd";
    private static final String VG = "-vg";
    
    private static enum OptId
    {
        ANYPLAYERFILE(APF),
        AQUARIUM(AQ),
        MINGAMECOUNT(CMIN),
        DUPLICATES(D),
        DUPLICATEOPENINGS(DO),
        DUPLICATEPOSTOPENINGS(DPO),
        ELOFILE(ELO),
        EVENTS(E),
        EVENTERRORS(EE),
        GAMENUM(GN),
        HIOOBCOUNT(HOOB),
        HIPLYCOUNT(HPC),
        HIWINDIFF(HWD),
        INPUTFILE(I),
        LOOOBCOUNT(LOOB),
        LOPLYCOUNT(LPC),
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
        NOTPLAYERFILE(NPF),
        OPENINGFILE(OF),
        OPENINGS(O),
        OUTPUTDELIM(OD),
        PERFORMANCE(PERF),
        PLAYERFILE(PF),
        PLAYERRESULTS(P),
        REPLACE(R),
        REPLACELOSER(RL),
        REPLACEOPENING(RO),
        REPLACEWINNER(RW),
        SELECTORS(S),
        TIMECONTROL(TC),
        VALUEDELIM(VD);
        
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
    
    private static Set<String> readLineFile(File file)
    {
        Set<String> fileLineSet = new HashSet<>();

        try
        {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(file));

            while ((line = reader.readLine()) != null)
                fileLineSet.add(line.trim());
        }

        catch (FileNotFoundException e)
        {
            System.err.println("File '" + file + "' not found.");
            System.exit(-1);
        }

        catch (IOException e)
        {
            System.err.println("Error reading file '" + file + "'");
            System.exit(-1);
        }

        return fileLineSet;
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
        
        try
        {
            PGNUtil.addMatchProcessor(new PGNUtil.MatchGameNumProcessor(gameno));
        }
        
        catch (NumberFormatException e)
        {
            System.err.println("exception: argument to '" + GN + "' option " +
                "must be an integer");
            
            System.exit(-1);
        }
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

    @Option(name = MP, aliases = "-match_player",
        metaVar = "<regex>", usage = "output games where <regex> is a player")
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
                        "while matching one player ('" + MP + "')!");

                    System.exit(-1);
                }
            }
        }
        
        countOption(OptId.MATCHPLAYER);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchPlayerProcessor(
            Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MO, forbids = {O, OF, NMO, NOF},
        aliases = "-match_opening", metaVar = "<oid1,oid2,...>",
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
        aliases = "-opening_file", metaVar = "<file>",
        usage = "output games in which the opening-book moves are any of " +
        "those contained in <file>")
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
        aliases = "-not_match_opening", metaVar = "<oid1,oid2,...>",
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
        aliases = "-not_opening_file", metaVar = "<file>",
        usage = "output games in which the opening-book moves are none of " +
        "those contained in <file>")
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

    @Option(name = APF, aliases = "-any_player_file", metaVar = "<file>",
        usage = "output games in which either player is contained in <file>")
    private void setAnyPlayerFile(File playerFile)
    {
        if (getCount(OptId.ANYPLAYERFILE) > 0)
        {
            System.err.println("Option '" + OptId.ANYPLAYERFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.ANYPLAYERFILE);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchAnyPlayerSetProcessor(
            readLineFile(playerFile)));
    }

    @Option(name = PF, aliases = "-player_file", metaVar = "<file>",
        usage = "output games in which both players are contained " +
        "in <file>")
    private void setPlayerFile(File playerFile)
    {
        if (getCount(OptId.PLAYERFILE) > 0)
        {
            System.err.println("Option '" + OptId.PLAYERFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.PLAYERFILE);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchAllPlayerSetProcessor(
            readLineFile(playerFile)));
    }

    @Option(name = NPF, aliases = "-not_player_file", metaVar = "<file>",
        usage = "output games in which neither player is contained in <file>")
    private void setNotPlayerFile(File playerFile)
    {
        if (getCount(OptId.NOTPLAYERFILE) > 0)
        {
            System.err.println("Option '" + OptId.NOTPLAYERFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.NOTPLAYERFILE);
        
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchPlayerSetProcessor(
            readLineFile(playerFile)));
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

    @Option(name = LPC, aliases = "-lo_ply_count", metaVar = "<min>",
        usage = "output games containing at least <min> plies")
    private void setLoPlies(int plies)
    {
        if (getCount(OptId.LOPLYCOUNT) > 0)
        {
            System.err.println("Option '" + OptId.LOPLYCOUNT + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.LOPLYCOUNT);
        PGNUtil.addMatchProcessor(new PGNUtil.MinPlyCountProcessor(plies));
    }

    @Option(name = HPC, aliases = "-hi_ply_count", metaVar = "<max>",
        usage = "output games containing at most <max> plies")
    private void setHiPlies(int plies)
    {
        if (getCount(OptId.HIPLYCOUNT) > 0)
        {
            System.err.println("Option '" + OptId.HIPLYCOUNT + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.HIPLYCOUNT);
        PGNUtil.addMatchProcessor(new PGNUtil.MaxPlyCountProcessor(plies));
    }

    @Option(name = LOOB, aliases = "-lo_oob", metaVar = "<min>",
        usage = "output games containing at least <min> out-of-book plies")
    private void setLoOob(int plies)
    {
        if (getCount(OptId.LOOOBCOUNT) > 0)
        {
            System.err.println("Option '" + OptId.LOOOBCOUNT + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.LOOOBCOUNT);
        PGNUtil.addMatchProcessor(new PGNUtil.MinOobProcessor(plies));
    }

    @Option(name = HOOB, aliases = "-hi_oob", metaVar = "<max>",
        usage = "output games containing at most <max> out-of-book plies")
    private void setHiOob(int plies)
    {
        if (getCount(OptId.HIOOBCOUNT) > 0)
        {
            System.err.println("Option '" + OptId.HIOOBCOUNT + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.HIOOBCOUNT);
        PGNUtil.addMatchProcessor(new PGNUtil.MaxOobProcessor(plies));
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
        metaVar = "<oid1,oid2,...>",
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

    @Option(name = D, forbids = {DO, DPO, O, E, EE, P, S},
        aliases = "-duplicates",
        usage = "list games containing identical players and move lists; " +
            "each line of output contains one set of two or more " +
            "game numbers in which duplicates are found")
    private void duplicates(boolean d)
    {
        if (getCount(OptId.DUPLICATES) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATES + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATES);
        
        PGNUtil.DuplicateGameHandler handler = new PGNUtil.DuplicateGameHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DO, forbids = {D, DPO, O, E, EE, P, S},
        aliases = "-duplicate_openings",
        usage = "list games containing identical players and openings; " +
            "each line of output contains one set of two or more " +
            "game numbers in which duplicates are found")
    private void duplicateOpenings(boolean d)
    {
        if (getCount(OptId.DUPLICATEOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATEOPENINGS +
                "' cannot be set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATEOPENINGS);
        
        PGNUtil.DuplicateOpeningHandler handler =
            new PGNUtil.DuplicateOpeningHandler();
        
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DPO, forbids = {D, DO, O, E, EE, P, S},
        aliases = "-duplicate_post_openings",
        usage = "list games containing identical players and post-opening " +
            "moves; each line of output contains one set of two or more " +
            "game numbers in which duplicates are found")
    private void duplicatePostOpenings(boolean d)
    {
        if (getCount(OptId.DUPLICATEPOSTOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATEPOSTOPENINGS +
                "' cannot be set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATEPOSTOPENINGS);
        
        PGNUtil.DuplicatePostOpeningHandler handler =
            new PGNUtil.DuplicatePostOpeningHandler();
        
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    // events

    @Option(name = E, forbids = {D, DO, DPO, O, EE, P}, aliases = "-events",
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
        Events events = new Events(false);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(events));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(events));
    }

    // event errors

    @Option(name = EE, forbids = {D, DO, DPO, O, E, P, S},
        aliases = "-event_errors",
        usage = "list each event containing errors, one event per line, with " +
        "information about each error found")
    private void eventErrors(boolean e)
    {
        if (getCount(OptId.EVENTERRORS) > 0)
        {
            System.err.println("Option '" + OptId.EVENTERRORS + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.EVENTERRORS);
        Events events = new Events(true);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(events));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(events));
    }

    // opening stats

    @Option(name = O, forbids = {D, DO, DPO, E, EE, P},
        aliases = "-opening_stats", usage = "print win/loss/draw " +
        "statistics for each opening.  Valid output selectors ('" + S + "') are: " +
        "'eco,' 'oid,' 'count,' 'wwins,' 'bwins,' 'draws,' 'wwinpct,'" +
        "'bwinpct,' 'diff,' 'diffpct,' 'drawpct'")
    private void openings(boolean o)
    {
        if (getCount(OptId.OPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.OPENINGS + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.OPENINGS);
        
        OpeningStats os = new OpeningStats();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
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
        OpeningStats.addOpeningProcessor(new OpeningStats.MinGamesProcessor(cmin));
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
        OpeningStats.addOpeningProcessor(new OpeningStats.MaxWinDiffProcessor(max));
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
        OpeningStats.addOpeningProcessor(new OpeningStats.MinWinDiffProcessor(min));
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
        OpeningStats.addOpeningProcessor(new OpeningStats.MaxDrawProcessor(max));
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
        OpeningStats.addOpeningProcessor(new OpeningStats.MinDrawProcessor(min));
    }

    @Option(name = HED, depends = {O, ELO}, aliases = "-hi_elo_diff",
        metaVar = "<diff>",
        usage = "in combination with '" + O + "' and '" + ELO + "' options, " +
        "print only openings for which the difference in player elo " +
        "ratings is at most <diff>")
    static Integer maxEloDiff;

    @Option(name = ELO, depends = {HED}, aliases = "-elo_file",
        metaVar = "<file>",
        usage = "in combination with '" + O + "' and '" + HED + "' options, " +
        "use elo ratings contained in the file <file>")
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
        Pattern playerPattern = Pattern.compile("^(\\S.*\\S)\\s+-?\\d+$");
        Pattern eloPattern = Pattern.compile("^.*\\s+(-?\\d+)$");
        
        try (Scanner fileScanner = new Scanner(of))
        {
            while (fileScanner.hasNextLine())
            {
                String s = fileScanner.nextLine().trim();
                if (s.length() == 0) continue;
                
                PGNUtil.eloMap.put(playerPattern.matcher(s).replaceAll("$1"),
                    Integer.valueOf(eloPattern.matcher(s).replaceAll("$1")));
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
    
    // player results

    @Option(name = P, forbids = {D, DO, DPO, O, E, EE},
        aliases = "-player_results",
        usage = "list win/loss/draw statistics for each player.  Valid " +
        "output selectors ('" + S + "') are: 'player,' 'wins,' 'losses,' 'draws,' " +
        "'noresults,' 'count,' 'winpct'")
    private void playerResults(boolean e)
    {
        if (getCount(OptId.PLAYERRESULTS) > 0)
        {
            System.err.println("Option '" + OptId.PLAYERRESULTS +
                "' cannot be set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.PLAYERRESULTS);
        
        PlayerResults pr = new PlayerResults();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(pr));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(pr));
    }

    // output-field selector

    @Option(name = S, forbids = {D, DO, DPO, EE}, aliases = "-select",
        metaVar = "<field1,field2,...>",
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

    @Option(name = OD, forbids = {D, DO, DPO}, aliases = "-output_delim",
        metaVar = "<delim>",
        usage = "set the delimiter between output fields where this is sensible")
    static String outputDelim = "|";

    @Option(name = VD, aliases = "-value_delim",
        metaVar = "<delim>",
        usage = "set the delimiter for values within a single output field, " +
        "where this is sensible")
    static String valueDelim = ",";

    @Option(name = AQ, aliases = "-fix_aquarium",
        usage = "compensate for  Aquarium bugs, where possible")
    static boolean aquarium = false;

    @Option(name = PERF, aliases = "-performance",
        usage = "print performance statistics at end of output")
    static boolean performance = false;

    @Option(name = VG, aliases = "-validate_games",
        usage = "throw an exception for any game that contains move-sequence " +
            "errors")
    static boolean validateGames = false;

    @Option(name = I, aliases = "-inputfile", usage = "input PGN file; " +
        "if this option is not present, pgnutil reads from standard input",
        metaVar = "<filename>")
    private void setInput(File f)
    {
        if (getCount(OptId.INPUTFILE) > 0)
        {
            System.err.println("option '" + OptId.INPUTFILE + "' cannot be " +
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
            System.err.println("file not found: " + f);
            System.exit(-1);
        }
        
        catch (IOException e)
        {
            System.err.println("i/o exception: " + f);
            System.exit(-1);
        }
    }

    @Option(name = H, forbids = {D, DO, DPO, E, EE, O, M, GN, NM, MW, ML, MP,
        MO, NMO, R, V},
        aliases = "-help", usage = "print usage information")
    static boolean help = false;

    @Option(name = V, forbids = {D, DO, DPO, E, EE, O, M, GN, NM, MW, ML, MP,
        MO, NMO, R, H},
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
