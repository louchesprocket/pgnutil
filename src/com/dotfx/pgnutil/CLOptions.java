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

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import com.dotfx.pgnutil.eco.EcoTree;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Mark Chen
 */
public class CLOptions
{
    public static final String APF = "-apf";
    public static final String AQ = "-aq";
    public static final String BM = "-bm";
    public static final String CMIN = "-cmin";
    public static final String CSR = "-csr";
    public static final String D = "-d";
    public static final String DM = "-dm";
    public static final String DO = "-do";
    public static final String E = "-e";
    public static final String ECO = "-eco";
    public static final String ELO = "-elo";
    public static final String GN = "-gn";
    public static final String H = "-h";
    public static final String HDRAW = "-hdraw";
    public static final String HED = "-hed";
    public static final String HOOB = "-hoob";
    public static final String HPC = "-hpc";
    public static final String HWD = "-hwd";
    public static final String I = "-i";
    public static final String LDRAW = "-ldraw";
    public static final String LOOB = "-loob";
    public static final String LPC = "-lpc";
    public static final String LWD = "-lwd";
    public static final String M = "-m";
    public static final String MECO = "-meco";
    public static final String MECODESC = "-mecodesc";
    public static final String ML = "-ml";
    public static final String MP = "-mp";
    public static final String MO = "-mo";
    public static final String MPOS = "-mpos";
    public static final String MSCIDECO = "-mscideco";
    public static final String MSCIDECODESC = "-mscidecodesc";
    public static final String MW = "-mw";
    public static final String MT = "-mt";
    public static final String MXECO = "-mxeco";
    public static final String MXECODESC = "-mxecodesc";
    public static final String MXSCIDECO = "-mxscideco";
    public static final String MXSCIDECODESC = "-mxscidecodesc";
    public static final String NM = "-nm";
    public static final String NMO = "-nmo";
    public static final String NMT = "-nmt";
    public static final String NOF = "-nof";
    public static final String NPF = "-npf";
    public static final String O = "-o";
    public static final String OD = "-od";
    public static final String OF = "-of";
    public static final String P = "-p";
    public static final String PERF = "-perf";
    public static final String PF = "-pf";
    public static final String R = "-r";
    public static final String RL = "-rl";
    public static final String RO = "-ro";
    public static final String RW = "-rw";
    public static final String S = "-s";
    public static final String SCIDECO = "-scideco";
    public static final String TC = "-tc";
    public static final String V = "-v";
    public static final String VD = "-vd";
    public static final String VG = "-vg";
    public static final String XECO = "-xeco";
    public static final String XSCIDECO = "-xscideco";
    
    private static enum OptId
    {
        ANYPLAYERFILE(APF),
        AQUARIUM(AQ),
        BOOKMARKER(BM),
        MINGAMECOUNT(CMIN),
        CHECKSEQUENTIALROUNDS(CSR),
        DUPLICATES(D),
        DUPLICATEMOVES(DM),
        DUPLICATEOPENINGS(DO),
        ECOOPENINGS(ECO),
        ELOFILE(ELO),
        EVENTS(E),
        GAMENUM(GN),
        HIOOBCOUNT(HOOB),
        HIPLYCOUNT(HPC),
        HIWINDIFF(HWD),
        INPUTFILE(I),
        LOOOBCOUNT(LOOB),
        LOPLYCOUNT(LPC),
        LOWINDIFF(LWD),
        MATCH(M),
        MATCHECO(MECO),
        MATCHECODESC(MECODESC),
        MATCHLOSER(ML),
        MATCHOPENING(MO),
        MATCHPLAYER(MP),
        MATCHPOSITION(MPOS),
        MATCHSCIDECO(MSCIDECO),
        MATCHSCIDECODESC(MSCIDECODESC),
        MATCHTAG(MT),
        MATCHTRANSECO(MXECO),
        MATCHTRANSECODESC(MXECODESC),
        MATCHTRANSSCIDECO(MXSCIDECO),
        MATCHTRANSSCIDECODESC(MXSCIDECODESC),
        MATCHWINNER(MW),
        MAXDRAW(HDRAW),
        MINDRAW(LDRAW),
        NOTMATCH(NM),
        NOTMATCHOPENING(NMO),
        NOTMATCHTAG(NMT),
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
        SCIDECOOPENINGS(SCIDECO),
        SELECTORS(S),
        TIMECONTROL(TC),
        VALUEDELIM(VD),
        XECOOPENINGS(XECO),
        XSCIDECOOPENINGS(XSCIDECO);
        
        private static final Map<String,OptId> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (OptId v : OptId.values()) sigMap.put(v.toString(), v);
        }
        
        OptId(String signifier) { this.signifier = signifier; }
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
    
    private static Set<String> readLinesSet(File file)
    {
        Set<String> fileLineSet = new HashSet<>();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
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
    
    private static String readFully(File file)
    {
        CharArrayWriter writer = new CharArrayWriter();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            char buf[] = new char[1024];
            int read;
            
            while ((read = reader.read(buf, 0, buf.length)) >= 0)
                writer.write(buf, 0, read);
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
        
        return writer.toString();
    }
    
    private static String[] splitCommaSeparated(String s)
    {
        return s.replaceAll("#.*", "").trim().replaceAll("\\W+", ",").
            replaceAll(",+", ",").split(",");
    }
    
    private Set<MoveListId> getOpeningsSet(String openingsSt)
    {
        Set<MoveListId> openingSet = new HashSet<>();
            
        for (String s : splitCommaSeparated(openingsSt))
        {
            try { openingSet.add(MoveListId.fromString(s)); }

            catch (IllegalArgumentException e)
            {
                System.err.println("invalid opening id: '" + s + "'");
                System.exit(-1);
            }
        }
        
        return openingSet;
    }
    
    // matchers

    @Option(name = GN, aliases = "-game_number",
        metaVar = "<range1,range2,...>",
        usage = "output games whose ordinal position in the input source is contained in <range1,range2,...>")
    private void setGameNum(String gameno)
    {
        if (getCount(OptId.GAMENUM) > 0)
        {
            System.err.println("Option '" + OptId.GAMENUM + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.GAMENUM);
        
        try { PGNUtil.addMatchProcessor(new PGNUtil.MatchGameNumProcessor(gameno)); }
        
        catch (NumberFormatException e)
        {
            System.err.println("exception: argument to '" + GN + "' option must be an integer");
            System.exit(-1);
        }
    }

    @Option(name = M, aliases = "-matches", metaVar = "<regex>",
        usage = "output games matching the regular expression <regex>")
    private void setContains(String regex)
    {
        countOption(OptId.MATCH);
        PGNUtil.addMatchProcessor(new PGNUtil.ContainsProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = NM, aliases = "-not_matches", metaVar = "<regex>",
        usage = "output games not matching the regular expression <regex>")
    private void setNotContains(String regex)
    {
        countOption(OptId.NOTMATCH);
        PGNUtil.addMatchProcessor(new PGNUtil.NotContainsProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MW, aliases = "-match_winner", metaVar = "<regex>",
        usage = "output games won by player <regex>")
    private void setWinner(String regex)
    {
        if (getCount(OptId.MATCHWINNER) > 0)
        {
            System.err.println("Option '" + OptId.MATCHWINNER + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHWINNER);
        PGNUtil.addMatchProcessor(new PGNUtil.MatchWinProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = ML, aliases = "-match_loser", metaVar = "<regex>",
        usage = "output games lost by player <regex>")
    private void setLoser(String regex)
    {
        if (getCount(OptId.MATCHLOSER) > 0)
        {
            System.err.println("Option '" + OptId.MATCHLOSER + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHLOSER);
        PGNUtil.addMatchProcessor(new PGNUtil.MatchLossProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MP, aliases = "-match_player",
        metaVar = "<regex>", usage = "output games where <regex> is a player")
    private void setPlayer(String regex)
    {
        if (getCount(OptId.MATCHPLAYER) > 1)
        {
            System.err.println("Option '" + OptId.MATCHPLAYER + "' cannot be set more than twice!");
            System.exit(-1);
        }
        
        if (getCount(OptId.MATCHPLAYER) == 1 && PGNUtil.outputSelectors != null)
        {
            for (OutputSelector selector : PGNUtil.outputSelectors)
            {
                if (selector.getValue().equals(OutputSelector.Value.OPPONENT))
                {
                    System.err.println("The 'opponent' selector only works while matching one player ('" + MP + "')!");
                    System.exit(-1);
                }
            }
        }
        
        countOption(OptId.MATCHPLAYER);
        PGNUtil.addMatchProcessor(new PGNUtil.MatchPlayerProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MO, forbids = {OF, NMO, NOF},
        aliases = "-match_opening", metaVar = "<oid1,oid2,...>",
        usage = "output games in which the opening-book moves are the same as any of <oid,oid2,...>")
    private void setOpening(String openingsSt)
    {
        if (getCount(OptId.MATCHOPENING) > 0)
        {
            System.err.println("Option '" + OptId.MATCHOPENING + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHOPENING);
        
        PGNUtil.addMatchProcessor(
            new PGNUtil.MatchOpeningProcessor(getOpeningsSet(openingsSt)));
    }

    @Option(name = OF, forbids = {MO, NMO, NOF},
        aliases = "-opening_file", metaVar = "<file>",
        usage = "output games in which the opening-book moves are any of those contained in <file>")
    private void setOpeningFile(File of)
    {
        if (getCount(OptId.OPENINGFILE) > 0)
        {
            System.err.println("Option '" + OptId.OPENINGFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.OPENINGFILE);
        
        PGNUtil.addMatchProcessor(
            new PGNUtil.MatchOpeningProcessor(getOpeningsSet(readFully(of))));
    }

    @Option(name = NMO, forbids = {MO, OF, NOF},
        aliases = "-not_match_opening", metaVar = "<oid1,oid2,...>",
        usage = "output games in which the opening-book moves are not the same as any of <oid,oid2,...>")
    private void setNotOpening(String openingsSt)
    {
        if (getCount(OptId.NOTMATCHOPENING) > 0)
        {
            System.err.println("Option '" + OptId.NOTMATCHOPENING + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.NOTMATCHOPENING);
        
        PGNUtil.addMatchProcessor(
            new PGNUtil.NotMatchOpeningProcessor(getOpeningsSet(openingsSt)));
    }

    @Option(name = NOF, forbids = {OF, NMO, MO},
        aliases = "-not_opening_file", metaVar = "<file>",
        usage = "output games in which the opening-book moves are none of those contained in <file>")
    private void setNotOpeningFile(File of)
    {
        if (getCount(OptId.NOTOPENINGFILE) > 0)
        {
            System.err.println("Option '" + OptId.NOTOPENINGFILE + "' cannot be " +
                "set more than once!");
            
            System.exit(-1);
        }
        
        countOption(OptId.NOTOPENINGFILE);

        PGNUtil.addMatchProcessor(
            new PGNUtil.NotMatchOpeningProcessor(getOpeningsSet(readFully(of))));
    }

    @Option(name = MPOS, forbids = {},
        aliases = "-match_pos", metaVar = "<move_string>",
        usage = "output games that contain the position reached by SAN string <move_string>")
    private void setPosition(String moveSt)
    {
        if (getCount(OptId.MATCHPOSITION) > 0)
        {
            System.err.println("Option '" + OptId.MATCHPOSITION + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHPOSITION);
        
        Board board = new Board(true);
        
        try { board.goTo(PgnGame.parseMoveString(moveSt)); }
        
        catch (IllegalMoveException | NullPointerException e)
        {
            System.err.println("illegal move in parameter: '" + moveSt + "'");
            System.exit(-1);
        }
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchPositionProcessor(board));
    }

    @Option(name = MECO, forbids = {},
        aliases = "-match_eco", metaVar = "<regex>",
        usage = "output games belonging to ECO code <regex>")
    private void setEco(String eco)
    {
        if (getCount(OptId.MATCHECO) > 0)
        {
            System.err.println("Option '" + OptId.MATCHECO + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHECO);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.STD));
    }

    @Option(name = MECODESC, forbids = {},
        aliases = "-match_eco_desc", metaVar = "<regex>",
        usage = "output games whose ECO description matches <regex>")
    private void setEcoDesc(String eco)
    {
        if (getCount(OptId.MATCHECODESC) > 0)
        {
            System.err.println("Option '" + OptId.MATCHECODESC + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHECODESC);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoDescProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.STD));
    }

    @Option(name = MSCIDECO, forbids = {},
        aliases = "-match_scid_eco", metaVar = "<regex>",
        usage = "output games belonging to Scid ECO code  <regex>")
    private void setScidEco(String eco)
    {
        if (getCount(OptId.MATCHSCIDECO) > 0)
        {
            System.err.println("Option '" + OptId.MATCHSCIDECO + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHSCIDECO);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.SCIDDB));
    }

    @Option(name = MSCIDECODESC, forbids = {},
        aliases = "-match_scid_eco_desc", metaVar = "<regex>",
        usage = "output games whose Scid ECO description matches <regex>")
    private void setScidEcoDesc(String eco)
    {
        if (getCount(OptId.MATCHSCIDECODESC) > 0)
        {
            System.err.println("Option '" + OptId.MATCHSCIDECODESC + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHSCIDECODESC);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoDescProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.SCIDDB));
    }

    @Option(name = MXECO, forbids = {},
        aliases = "-match_trans_eco", metaVar = "<regex>",
        usage = "output games belonging to ECO code <regex>, matching transpositionally")
    private void setXEco(String eco)
    {
        if (getCount(OptId.MATCHTRANSECO) > 0)
        {
            System.err.println("Option '" + OptId.MATCHTRANSECO + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHTRANSECO);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.STD));
    }

    @Option(name = MXECODESC, forbids = {},
        aliases = "-match_trans_eco_desc", metaVar = "<regex>",
        usage = "output games whose ECO description matches <regex>, " +
        "matching transpositionally")
    private void setXEcoDesc(String eco)
    {
        if (getCount(OptId.MATCHTRANSECODESC) > 0)
        {
            System.err.println("Option '" + OptId.MATCHTRANSECODESC + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHTRANSECODESC);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoDescProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.STD));
    }

    @Option(name = MXSCIDECO, forbids = {},
        aliases = "-match_trans_scid_eco", metaVar = "<regex>",
        usage = "output games belonging to Scid ECO code <regex>, matching transpositionally")
    private void setXScidEco(String eco)
    {
        if (getCount(OptId.MATCHTRANSSCIDECO) > 0)
        {
            System.err.println("Option '" + OptId.MATCHTRANSSCIDECO + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHTRANSSCIDECO);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.SCIDDB));
    }

    @Option(name = MXSCIDECODESC, forbids = {},
        aliases = "-match_trans_scid_eco_desc", metaVar = "<regex>",
        usage = "output games whose Scid ECO description matches <regex>, matching transpositionally")
    private void setXScidEcoDesc(String eco)
    {
        if (getCount(OptId.MATCHTRANSSCIDECODESC) > 0)
        {
            System.err.println("Option '" + OptId.MATCHTRANSSCIDECODESC + "' cannot be " + "set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MATCHTRANSSCIDECODESC);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoDescProcessor(Pattern.compile(eco, Pattern.DOTALL),
            EcoTree.FileType.SCIDDB));
    }

    @Option(name = APF, aliases = "-any_player_file", metaVar = "<file>",
        usage = "output games in which either player is contained in <file>")
    private void setAnyPlayerFile(File playerFile)
    {
        if (getCount(OptId.ANYPLAYERFILE) > 0)
        {
            System.err.println("Option '" + OptId.ANYPLAYERFILE + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.ANYPLAYERFILE);
        PGNUtil.addMatchProcessor(new PGNUtil.MatchAnyPlayerSetProcessor(readLinesSet(playerFile)));
    }

    @Option(name = PF, aliases = "-player_file", metaVar = "<file>",
        usage = "output games in which both players are contained in <file>")
    private void setPlayerFile(File playerFile)
    {
        if (getCount(OptId.PLAYERFILE) > 0)
        {
            System.err.println("Option '" + OptId.PLAYERFILE + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.PLAYERFILE);
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchAllPlayerSetProcessor(readLinesSet(playerFile)));
    }

    @Option(name = NPF, aliases = "-not_player_file", metaVar = "<file>",
        usage = "output games in which neither player is contained in <file>")
    private void setNotPlayerFile(File playerFile)
    {
        if (getCount(OptId.NOTPLAYERFILE) > 0)
        {
            System.err.println("Option '" + OptId.NOTPLAYERFILE + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.NOTPLAYERFILE);
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchPlayerSetProcessor(readLinesSet(playerFile)));
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

    @Option(name = NMT, aliases = "-not_match_tag", metaVar = "<tag>/<regex>",
        usage = "output games in which PGN tag <tag> does not have value <regex>")
    private void setNotTag(String tag)
    {
        countOption(OptId.NOTMATCHTAG);
        
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
        
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchTagProcessor(tagTokens[0],
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
            System.err.println("Option '" + OptId.HIPLYCOUNT + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.HIPLYCOUNT);
        PGNUtil.addMatchProcessor(new PGNUtil.MaxPlyCountProcessor(plies));
    }

    @Option(name = BM, aliases = "-book_marker", metaVar = "<regex>",
        usage = "use <regex> as the delimiter between book and non-book moves")
    private void setBookMarker(String regex)
    {
        if (getCount(OptId.BOOKMARKER) > 0)
        {
            System.err.println("Option '" + OptId.BOOKMARKER + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.BOOKMARKER);
        PGNUtil.setBookMarker(regex);
    }

    @Option(name = LOOB, aliases = "-lo_oob", metaVar = "<min>",
        usage = "output games containing at least <min> out-of-book plies")
    private void setLoOob(int plies)
    {
        if (getCount(OptId.LOOOBCOUNT) > 0)
        {
            System.err.println("Option '" + OptId.LOOOBCOUNT + "' cannot be set more than once!");
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
            System.err.println("Option '" + OptId.HIOOBCOUNT + "' cannot be set more than once!");
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
            System.err.println("Option '" + OptId.REPLACE + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.REPLACE);
        
        // The regex passed to split() allows escaping of the delimiter character
        // ("/") with a backslash. See https://stackoverflow.com/questions/
        // 18677762/handling-delimiter-with-escape-characters-in-java-string-split-method
        // First, however, we must escape any escaped backslashes.
        String replaceTokens[] =
            replaceStr.replace("\\\\", "\0").split("(?<!\\\\)/", -1);

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
        usage = "in combination with '" + R + "' option, select games won by player <regex> for replacement")
    private void replaceWinner(String regex)
    {
        if (getCount(OptId.REPLACEWINNER) > 0)
        {
            System.err.println("Option '" + OptId.REPLACEWINNER + "' cannot be set more than once!");
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
        usage = "in combination with '" + R + "' option, select games lost by player <regex> for replacement")
    private void replaceLoser(String regex)
    {
        if (getCount(OptId.REPLACELOSER) > 0)
        {
            System.err.println("Option '" + OptId.REPLACELOSER + "' cannot be set more than once!");
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
            System.err.println("Option '" + OptId.REPLACEOPENING + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.REPLACEOPENING);
        int n = PGNUtil.replaceProcessors.size();
        
        PGNUtil.replaceProcessors.add(n == 0 ? 0 : n - 1,
            new PGNUtil.ReplaceOpeningProcessor(opening));
    }

    // duplicates

    @Option(name = D, forbids = {DO, DM, O, E, CSR, P, S},
        aliases = "-duplicates",
        usage = "list games containing identical players and move lists; " +
        "each line of output contains one set of two or more " +
        "game numbers in which duplicates are found")
    private void duplicates(boolean d)
    {
        if (getCount(OptId.DUPLICATES) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATES + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATES);
        
        PGNUtil.DuplicateGameHandler handler = new PGNUtil.DuplicateGameHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DO, forbids = {D, DM, O, E, CSR, P, S},
        aliases = "-duplicate_openings",
        usage = "list games containing identical players and openings; each line of output contains one set of two " +
                "or more game numbers in which duplicates are found")
    private void duplicateOpenings(boolean d)
    {
        if (getCount(OptId.DUPLICATEOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATEOPENINGS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATEOPENINGS);
        
        PGNUtil.DuplicateOpeningHandler handler = new PGNUtil.DuplicateOpeningHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DM, forbids = {D, DO, O, E, CSR, P, S},
        aliases = "-duplicate_moves",
        usage = "list games containing identical moves; each line of output contains one set of two or more " +
                "game numbers in which duplicates are found")
    private void duplicatePostOpenings(boolean d)
    {
        if (getCount(OptId.DUPLICATEMOVES) > 0)
        {
            System.err.println("Option '" + OptId.DUPLICATEMOVES + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.DUPLICATEMOVES);
        
        PGNUtil.DuplicateMoveHandler handler = new PGNUtil.DuplicateMoveHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    // events

    @Option(name = E, forbids = {D, DO, DM, O, CSR, P}, aliases = "-events",
        usage = "list one event per line, with game numbers (ordinal " +
        "position of the game as it was read from the input source)")
    private void events(boolean e)
    {
        if (getCount(OptId.EVENTS) > 0)
        {
            System.err.println("Option '" + OptId.EVENTS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.EVENTS);
        Tallier events = new Events(false);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(events));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(events));
    }

    // event errors

    @Option(name = CSR, forbids = {D, DO, DM, O, E, P, S}, aliases = "-check_sequential_rounds",
        usage = "list each event containing non-sequential rounds, one event per line, with " +
        "information about each non-sequential round found")
    private void eventErrors(boolean e)
    {
        if (getCount(OptId.CHECKSEQUENTIALROUNDS) > 0)
        {
            System.err.println("Option '" + OptId.CHECKSEQUENTIALROUNDS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.CHECKSEQUENTIALROUNDS);
        Tallier events = new Events(true);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(events));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(events));
    }

    // opening stats

    @Option(name = O, forbids = {D, DO, DM, E, CSR, P}, aliases = "-opening_stats",
            usage = "print win/loss/draw statistics for each opening.  Valid output selectors ('" + S + "') are: " +
            "'eco,' 'oid,' 'count,' 'wwins,' 'bwins,' 'draws,' 'wwinpct,' 'bwinpct,' 'diff,' 'diffpct,' 'drawpct'")
    private void openings(boolean o)
    {
        if (getCount(OptId.OPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.OPENINGS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.OPENINGS);
        
        Tallier os = OpeningStats.getInstance();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }

    /*
    @Option(name = ECO, depends = {O}, aliases = "-eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each ECO code")
    private void ecoOpenings(boolean o)
    {
        if (getCount(OptId.ECOOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.ECOOPENINGS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.ECOOPENINGS);
        
        Tallier os = new EcoStats(EcoTree.FileType.STD, false);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }

    @Option(name = SCIDECO, depends = {O}, aliases = "-scid_eco_stats",
            usage = "combined with the '" + O + "' " + "option, print win/loss/draw statistics for each Scid ECO code")
    private void ScidEcoOpenings(boolean o)
    {
        if (getCount(OptId.SCIDECOOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.SCIDECOOPENINGS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.SCIDECOOPENINGS);
        
        Tallier os = new EcoStats(EcoTree.FileType.SCIDDB, false);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }

    @Option(name = XECO, depends = {O}, aliases = "-trans_eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each ECO code, " +
                    "matching openings transpositionally")
    private void XEcoOpenings(boolean o)
    {
        if (getCount(OptId.XECOOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.XECOOPENINGS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.XECOOPENINGS);
        
        Tallier os = new EcoStats(EcoTree.FileType.STD, true);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }

    @Option(name = XSCIDECO, depends = {O}, aliases = "-trans_scid_eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each Scid ECO code, " +
                    "matching openings transpositionally")
    private void XScidEcoOpenings(boolean o)
    {
        if (getCount(OptId.XSCIDECOOPENINGS) > 0)
        {
            System.err.println("Option '" + OptId.XSCIDECOOPENINGS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.XSCIDECOOPENINGS);
        
        Tallier os = new EcoStats(EcoTree.FileType.SCIDDB, true);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }
     */

    @Option(name = CMIN, depends = {O}, aliases = "-count_min",
        metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings that appear in at least <min> games")
    private void minOpeningCount(int cmin)
    {
        if (getCount(OptId.MINGAMECOUNT) > 0)
        {
            System.err.println("Option '" + OptId.MINGAMECOUNT + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MINGAMECOUNT);
        OpeningStats.addOpeningProcessor(new OpeningProcessors.MinGamesProcessor(cmin));
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
            System.err.println("Option '" + OptId.HIWINDIFF + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.HIWINDIFF);
        OpeningStats.addOpeningProcessor(new OpeningProcessors.MaxWinDiffProcessor(max));
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
            System.err.println("Option '" + OptId.LOWINDIFF + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.LOWINDIFF);
        OpeningStats.addOpeningProcessor(new OpeningProcessors.MinWinDiffProcessor(min));
    }

    @Option(name = HDRAW, depends = {O}, aliases = "-hi_draw_pct",
        metaVar = "<max>",
        usage = "in combination with '" + O + "' option, print only openings " +
        "for which the percentage of draws is at most <max> percent")
    private void maxDraw(double max)
    {
        if (getCount(OptId.MAXDRAW) > 0)
        {
            System.err.println("Option '" + OptId.MAXDRAW + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MAXDRAW);
        OpeningStats.addOpeningProcessor(new OpeningProcessors.MaxDrawProcessor(max));
    }

    @Option(name = LDRAW, depends = {O}, aliases = "-lo_draw_pct",
        metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings " +
        "for which the percentage of draws is at least <min> percent")
    private void minDraw(double min)
    {
        if (getCount(OptId.MINDRAW) > 0)
        {
            System.err.println("Option '" + OptId.MINDRAW + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.MINDRAW);
        OpeningStats.addOpeningProcessor(new OpeningProcessors.MinDrawProcessor(min));
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
            System.err.println("Option '" + OptId.ELOFILE + "' cannot be set more than once!");
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

    @Option(name = P, forbids = {D, DO, DM, O, E, CSR},
        aliases = "-player_results",
        usage = "list win/loss/draw statistics for each player.  Valid " +
        "output selectors ('" + S + "') are: 'player,' 'wins,' 'losses,' 'draws,' " +
        "'noresults,' 'count,' 'winpct'")
    private void playerResults(boolean e)
    {
        if (getCount(OptId.PLAYERRESULTS) > 0)
        {
            System.err.println("Option '" + OptId.PLAYERRESULTS + "' cannot be set more than once!");
            System.exit(-1);
        }
        
        countOption(OptId.PLAYERRESULTS);
        
        Tallier pr = PlayerResults.getInstance();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(pr));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(pr));
    }

    // output-field selector

    @Option(name = S, forbids = {D, DO, DM, CSR}, aliases = "-select",
        metaVar = "<selector1,selector2,...>",
        usage = "select values for output. By default, these are simply tags from the game's header (e.g., 'Event'), " +
                "though a number of special selectors are also available. For example, 'moves' selects the game's " +
                "move list, 'gameno' selects the ordinal position of the game as it was read from the input source, " +
                "'oid' selects the game's opening identifier (not the ECO code), 'stdeco' selects the ECO code, and " +
                "'scideco' selects the Scid ECO code. The selectors 'winner' and 'loser' select the winning and " +
                "losing player, respectively.  When the '" + MP + "' option is used, 'opponent' selects the other " +
                "player. For other available selectors, please see the README file.")
    private void selectors(String spec)
    {
        if (getCount(OptId.SELECTORS) > 0)
        {
            System.err.println("Option '" + OptId.SELECTORS + "' cannot be set more than once!");
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
                System.err.println("The 'opponent' selector only works while matching one player ('" + MP + "')!");
                System.exit(-1);
            }
        }
        
        if (PGNUtil.handler == null) PGNUtil.handler = new PGNUtil.SelectGameHandler(PGNUtil.outputSelectors);
    }

    @Option(name = OD, forbids = {D, DO, DM}, aliases = "-output_delim",
        metaVar = "<delim>",
        usage = "set the delimiter between output fields where this is sensible")
    static String outputDelim = "|";

    @Option(name = VD, aliases = "-value_delim",
        metaVar = "<delim>",
        usage = "set the delimiter for values within a single output field, where this is sensible")
    public static String valueDelim = ",";

    @Option(name = AQ, aliases = "-fix_aquarium",
        usage = "compensate for Aquarium bugs, where possible")
    static boolean aquarium = false;

    @Option(name = PERF, aliases = "-performance",
        usage = "print performance statistics at end of output")
    static boolean performance = false;

    @Option(name = VG, aliases = "-validate_games",
        usage = "throw an exception for any game that contains move-sequence errors")
    static boolean validateGames = false;

    @Option(name = I, aliases = "-inputfile", usage = "input PGN file (may be specified more than once); " +
        "if this option is not present, pgnutil reads from standard input",
        metaVar = "<filename>")
    private void setInput(File f)
    {
        countOption(OptId.INPUTFILE);
        
        try { PGNUtil.pgnFileList.add(new PGNFile(new BufferedReader(new FileReader(f)))); }
        
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

    @Option(name = H, forbids = {D, DO, DM, E, CSR, O, M, GN, NM, MW, ML, MP, MO, NMO, R, V},
        aliases = "-help", usage = "print usage information")
    static boolean help = false;

    @Option(name = V, forbids = {D, DO, DM, E, CSR, O, M, GN, NM, MW, ML, MP, MO, NMO, R, H},
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
