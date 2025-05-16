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
import java.util.*;
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
    public static final String CB = "-cb";
    public static final String CNB = "-cnb";
    public static final String CMIN = "-cmin";
    public static final String CSR = "-csr";
    public static final String D = "-d";
    public static final String DDB = "-ddb"; // util ECO diff
    public static final String DDBS = "-ddbs"; // util SCID ECO diff
    public static final String DM = "-dm";
    public static final String DO = "-do";
    public static final String DOOB = "-doob";
    public static final String DOOBM = "-doobm";
    public static final String E = "-e";
    public static final String ECO = "-eco"; // ECO output for "-o"
    public static final String EF = "-ef"; // specify standard ECO file
    public static final String ELO = "-elo";
    public static final String FF = "-ff";
    public static final String GN = "-gn";
    public static final String GNF = "-gnf";
    public static final String H = "-h";
    public static final String HDRAW = "-hdraw";
    public static final String HED = "-hed";
    public static final String HELO = "-helo";
    public static final String HOOB = "-hoob";
    public static final String HPC = "-hpc";
    public static final String HWD = "-hwd";
    public static final String I = "-i";
    public static final String LDRAW = "-ldraw";
    public static final String LED = "-led";
    public static final String LELO = "-lelo";
    public static final String LOOB = "-loob";
    public static final String LPC = "-lpc";
    public static final String LWD = "-lwd";
    public static final String M = "-m";
    public static final String ME = "-me"; // match ECO
    public static final String MED = "-med"; // match ECO desription
    public static final String MFEN = "-mfen"; // match FEN
    public static final String MKDB = "-mkdb"; // util make ECO d.b.
    public static final String MKDBS = "-mkdbs"; // util make SCID ECO d.b.
    public static final String ML = "-ml";
    public static final String MO = "-mo";
    public static final String MP = "-mp";
    public static final String MPOS = "-mpos";
    public static final String MSE = "-mse"; // match SCID ECO
    public static final String MSED = "-msed"; // match SCID ECO description
    public static final String MW = "-mw";
    public static final String MT = "-mt";
    public static final String MXE = "-mxe"; // match ECO transpositionally
    public static final String MXED = "-mxed"; // match ECO description transpositionally
    public static final String MXSE = "-mxse"; // match SCID ECO transpositionally
    public static final String MXSED = "-mxsed"; // match SCID ECO description transpositionally
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
    public static final String POSF = "-posf";
    public static final String PP = "-pp";
    public static final String R = "-r";
    public static final String RL = "-rl";
    public static final String RO = "-ro";
    public static final String RW = "-rw";
    public static final String S = "-s";
    public static final String SECO = "-seco"; // SCID ECO output for "-o"
    public static final String TC = "-tc";
    public static final String TF = "-tf"; // find time faults
    public static final String V = "-v";
    public static final String VD = "-vd";
    public static final String VG = "-vg";
    public static final String XECO = "-xeco"; // transpositional ECO output for "-o"
    public static final String XSECO = "-xseco"; // transpositional SCID ECO output for "-o"
    
    public enum OptId
    {
        ANYPLAYERFILE(APF),
        AQUARIUM(AQ),
        BOOKMARKER(BM),
        CHECKSEQUENTIALROUNDS(CSR),
        CLOCKBELOW(CB),
        CLOCKNOTBELOW(CNB),
        DIFFDB(DDB),
        DIFFDBS(DDBS),
        DUPLICATES(D),
        DUPLICATEMOVES(DM),
        DUPLICATEOPENINGS(DO),
        DUPLICATEOOB(DOOB),
        DUPLICATEOOBMOVES(DOOBM),
        ECOFILE(EF),
        ELOFILE(ELO),
        EVENTS(E),
        FENFILE(FF),
        GAMENUM(GN),
        GAMENUMFILE(GNF),
        HIELO(HELO),
        HIELODIFF(HED),
        HIOOBCOUNT(HOOB),
        HIPLYCOUNT(HPC),
        HIWINDIFF(HWD),
        INPUTFILE(I),
        LOOOBCOUNT(LOOB),
        LOPLYCOUNT(LPC),
        LOWELO(LELO),
        LOWELODIFF(LED),
        LOWINDIFF(LWD),
        MAKEDB(MKDB),
        MAKEDBSCID(MKDBS),
        MATCH(M),
        MATCHECO(ME),
        MATCHECODESC(MED),
        MATCHFEN(MFEN),
        MATCHLOSER(ML),
        MATCHOPENING(MO),
        MATCHPLAYER(MP),
        MATCHPOSITION(MPOS),
        MATCHSCIDECO(MSE),
        MATCHSCIDECODESC(MSED),
        MATCHTAG(MT),
        MATCHTRANSECO(MXE),
        MATCHTRANSECODESC(MXED),
        MATCHTRANSSCIDECO(MXSE),
        MATCHTRANSSCIDECODESC(MXSED),
        MATCHWINNER(MW),
        MAXDRAW(HDRAW),
        MINDRAW(LDRAW),
        MINGAMECOUNT(CMIN),
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
        POSITIONFILE(POSF),
        PRINTPOS(PP),
        REPLACE(R),
        REPLACELOSER(RL),
        REPLACEOPENING(RO),
        REPLACEWINNER(RW),
        SCIDECO(SECO),
        SELECTORS(S),
        STDECO(ECO),
        TIMECONTROL(TC),
        TIMEFAULT(TF),
        VALUEDELIM(VD),
        XSTDECO(XECO),
        XSCIDECO(XSECO);
        
        private static final Map<String,OptId> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (OptId v : OptId.values())
            {
                if (sigMap.containsKey(v.toString()))
                    throw new ExceptionInInitializerError("'" + v.toString() + "' is already mapped");

                sigMap.put(v.toString(), v);
            }
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

    public static boolean isSet(OptId opt) { return getCount(opt) > 0; }
    public static Set<OptId> getSetOpts() { return OPTMAP.keySet(); }
    public static Map<OptId,Integer> getOptMap() { return OPTMAP; }
    
    private static Set<String> readLinesSet(File file)
    {
        Set<String> fileLineSet = new HashSet<>();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            while ((line = reader.readLine()) != null) fileLineSet.add(line.trim());
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
            
            while ((read = reader.read(buf, 0, buf.length)) >= 0) writer.write(buf, 0, read);
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
            try { openingSet.add(MoveListId.fromHexString(s)); }

            catch (IllegalArgumentException e)
            {
                System.err.println("invalid opening id: '" + s + "'");
                System.exit(-1);
            }
        }
        
        return openingSet;
    }

    // utility

    @Option(name = PP, aliases = "-print_position", metaVar = "<move_list>", hidden = true,
            usage = "output a board representation following the <move_list>")
    private void printPosition(String moveSt)
    {
        countOption(OptId.get(PP));

        CLOptionResolver.addCondition(new OptId[] {OptId.PRINTPOS}, null, null,
                new CLOptionResolver.PrintPositionHandler(moveSt));
    }

    @Option(name = MKDB, aliases = "-make_db", metaVar = "<input_file>", hidden = true,
            usage = "output an ECO file using Lichess-formatted <input_file> as input")
    private void makeDb(File f)
    {
        countOption(OptId.get(MKDB));

        CLOptionResolver.addCondition(new OptId[] {OptId.MAKEDB}, null, null,
                new CLOptionResolver.MakeDbHandler(EcoTree.FileType.LICHESS, f));
    }

    @Option(name = MKDBS, aliases = "-make_db_scid", metaVar = "<input_file>", hidden = true,
            usage = "output an ECO file using SCID-formatted <input_file> as input")
    private void makeDbS(File f)
    {
        countOption(OptId.get(MKDBS));

        CLOptionResolver.addCondition(new OptId[] {OptId.MAKEDBSCID}, null, null,
                new CLOptionResolver.MakeDbHandler(EcoTree.FileType.SCID, f));
    }

    @Option(name = DDB, aliases = "-diff_db", metaVar = "<input_file>", hidden = true,
            usage = "report differences between Lichess-formatted <input_file> and internal ECO d.b.")
    private void diffDb(File f)
    {
        countOption(OptId.get(DDB));

        CLOptionResolver.addCondition(new OptId[] {OptId.DIFFDB}, null, null,
                new CLOptionResolver.DbDiffHandler(EcoTree.FileType.LICHESS, f));
    }

    @Option(name = DDBS, aliases = "-diff_db_scid", metaVar = "<input_file>", hidden = true,
            usage = "report differences between SCID-formatted <input_file> and internal SCID ECO d.b.")
    private void diffDbS(File f)
    {
        countOption(OptId.get(DDBS));

        CLOptionResolver.addCondition(new OptId[] {OptId.DIFFDBS}, null, null,
                new CLOptionResolver.DbDiffHandler(EcoTree.FileType.SCID, f));
    }
    
    // matchers

    @Option(name = GN, aliases = "-game_num", metaVar = "<range1,range2,...>", forbids = {GNF},
        usage = "output games whose ordinal position in the input source is contained in <range1,range2,...>")
    private void setGameNum(String gameno)
    {
        countOption(OptId.get(GN));

        try
        {
            PGNUtil.addMatchProcessor(new PGNUtil.MatchGameNumProcessor(gameno));

            CLOptionResolver.addCondition(new OptId[] {OptId.get(GN)}, new OptId[] {OptId.INPUTFILE}, null,
                    new CLOptionResolver.GameNumHandler());
        }
        
        catch (NumberFormatException e)
        {
            System.err.println("exception: argument to '" + GN + "' option must contain only integers");
            System.exit(-1);
        }
    }

    @Option(name = GNF, aliases = "-game_num_file", metaVar = "<file>", forbids = {GN},
            usage = "output games whose ordinal positions in the input source are listed in <file>")
    private void setGameFile(File gnf)
    {
        countOption(OptId.get(GNF));

        try
        {
            PGNUtil.addMatchProcessor(new PGNUtil.MatchGameNumProcessor(readFully(gnf)));

            CLOptionResolver.addCondition(new OptId[] {OptId.get(GNF)}, new OptId[] {OptId.INPUTFILE}, null,
                    new CLOptionResolver.GameNumHandler());
        }

        catch (NumberFormatException e)
        {
            System.err.println("exception: file '" + gnf + "' must contain only integer ranges");
            System.exit(-1);
        }
    }

    @Option(name = M, aliases = "-matches", metaVar = "<regex>",
        usage = "output games matching the regular expression <regex>")
    private void setContains(String regex)
    {
        countOption(OptId.get(M));
        PGNUtil.addMatchProcessor(new PGNUtil.ContainsProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = NM, aliases = "-not_matches", metaVar = "<regex>",
        usage = "output games not matching the regular expression <regex>")
    private void setNotContains(String regex)
    {
        countOption(OptId.get(NM));
        PGNUtil.addMatchProcessor(new PGNUtil.NotContainsProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MW, aliases = "-match_winner", metaVar = "<regex>",
        usage = "output games won by player <regex>")
    private void setWinner(String regex)
    {
        countOption(OptId.get(MW));
        PGNUtil.addMatchProcessor(new PGNUtil.MatchWinProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = ML, aliases = "-match_loser", metaVar = "<regex>",
        usage = "output games lost by player <regex>")
    private void setLoser(String regex)
    {
        countOption(OptId.get(ML));
        PGNUtil.addMatchProcessor(new PGNUtil.MatchLossProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = MP, aliases = "-match_player", metaVar = "<regex>",
            usage = "output games where <regex> is a player")
    private void setPlayer(String regex)
    {
        countOption(OptId.get(MP));
        Pattern playerPattern = Pattern.compile(regex, Pattern.DOTALL);

        CLOptionResolver.addCondition(new OptId[] {OptId.get(MP)}, new OptId[] {OptId.SELECTORS}, null,
                new CLOptionResolver.PlayerHandler(playerPattern));

        PGNUtil.addMatchProcessor(new PGNUtil.MatchPlayerProcessor(playerPattern));
    }

    @Option(name = TF, aliases = "-time_fault", forbids = {CB},
            usage = "output games where at least one player's clock went negative (Aquarium only)")
    private void setTimeFault(boolean tf)
    {
        countOption(OptId.get(TF));
        PGNUtil.addMatchProcessor(new PGNUtil.ContainsProcessor(Pattern.compile("\\[%clk -", Pattern.DOTALL)));
    }

    @Option(name = LELO, aliases = "-lo_elo", metaVar = "<value>",
            usage = "output games where the elo rating of both players is at least <value>")
    private void setMinElo(Integer minElo)
    {
        countOption(OptId.get(LELO));
        PGNUtil.addMatchProcessor(new PGNUtil.MinEloProcessor(minElo));
    }

    @Option(name = HELO, aliases = "-hi_elo", metaVar = "<value>",
            usage = "output games where the elo rating of both players is at most <value>")
    private void setMaxElo(Integer maxElo)
    {
        countOption(OptId.get(HELO));
        PGNUtil.addMatchProcessor(new PGNUtil.MaxEloProcessor(maxElo));
    }

    @Option(name = HED, aliases = "-hi_elo_diff", metaVar = "<diff>",
            usage = "output games in which the difference in player elo ratings is at most <diff>")
    private void setMaxEloDiff(Integer maxEloDiff)
    {
        countOption(OptId.get(HED));
        PGNUtil.addMatchProcessor(new PGNUtil.MaxEloDiffProcessor(maxEloDiff));
    }

    @Option(name = LED, aliases = "-lo_elo_diff", metaVar = "<diff>",
            usage = "output games in which the difference in player elo ratings is at least <diff>")
    private void setMinEloDiff(Integer minEloDiff)
    {
        countOption(OptId.get(LED));
        PGNUtil.addMatchProcessor(new PGNUtil.MinEloDiffProcessor(minEloDiff));
    }

    @Option(name = CB, aliases = "-clock_below", forbids = {TF}, metaVar = "<time>",
            usage = "output games in which at least one player's clock went below <time> (Aquarium only)")
    private void setClockBelow(String time)
    {
        countOption(OptId.get(CB));

        try
        {
            Clock clock = new Clock(time);

            CLOptionResolver.addCondition(new OptId[] {OptId.get(CB)}, new OptId[] {OptId.SELECTORS}, null,
                    new CLOptionResolver.ClockBelowHandler(clock));

            PGNUtil.addMatchProcessor(new PGNUtil.ClockBelowProcessor(clock));
        }

        catch (InvalidClockException e)
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    @Option(name = CNB, aliases = "-clock_not_below", metaVar = "<time>",
            usage = "output games in which neither player's clock went below <time> (Aquarium only)")
    private void setClockNotBelow(String time)
    {
        countOption(OptId.get(CNB));

        try { PGNUtil.addMatchProcessor(new PGNUtil.ClockNotBelowProcessor(new Clock(time))); }

        catch (InvalidClockException e)
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    @Option(name = ELO, aliases = "-elo_file", metaVar = "<file>",
            usage = "use elo ratings contained in the file <file> for any elo-matching operation")
    private void setEloFile(File eloFile)
    {
        countOption(OptId.get(ELO));
        EloResolver.readEloMap(eloFile);
    }

    @Option(name = MO, aliases = "-match_opening", metaVar = "<oid1,oid2,...>",
        usage = "output games in which the opening-book moves are the same as any of <oid,oid2,...>")
    private void setOpening(String openingsSt)
    {
        countOption(OptId.get(MO));
        PGNUtil.addMatchProcessor(new PGNUtil.MatchOpeningProcessor(getOpeningsSet(openingsSt)));
    }

    @Option(name = OF, aliases = "-opening_file", metaVar = "<file>",
        usage = "output games in which the opening-book moves are any of those contained in <file>")
    private void setOpeningFile(File of)
    {
        countOption(OptId.get(OF));
        PGNUtil.addMatchProcessor(new PGNUtil.MatchOpeningProcessor(getOpeningsSet(readFully(of))));
    }

    @Option(name = NMO, aliases = "-not_match_opening", metaVar = "<oid1,oid2,...>",
        usage = "output games in which the opening-book moves are not the same as any of <oid,oid2,...>")
    private void setNotOpening(String openingsSt)
    {
        countOption(OptId.get(NMO));
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchOpeningProcessor(getOpeningsSet(openingsSt)));
    }

    @Option(name = NOF, aliases = "-not_opening_file", metaVar = "<file>",
        usage = "output games in which the opening-book moves are none of those contained in <file>")
    private void setNotOpeningFile(File of)
    {
        countOption(OptId.get(NOF));
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchOpeningProcessor(getOpeningsSet(readFully(of))));
    }

    @Option(name = MPOS, aliases = "-match_pos", metaVar = "<move_string>",
        usage = "output games that contain the position reached by SAN string <move_string>")
    private void setPosition(String moveSt)
    {
        countOption(OptId.get(MPOS));

        LooseBoard board = new LooseBoard(true);
        try { board.goTo(PgnGame.parseMoveString(moveSt)); }
        
        catch (IllegalMoveException e)
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }

        catch (NullPointerException e)
        {
            System.err.println("illegal move in parameter '" + moveSt + "'");
            System.exit(-1);
        }
        
        PGNUtil.addMatchProcessor(new PGNUtil.MatchPositionSetProcessor(Collections.singleton(board)));
    }

    @Option(name = POSF, aliases = "-position_file", metaVar = "<file>",
            usage = "output games that contain any of the positions reached by SAN strings listed in <file>")
    private void setPosFile(File posFile)
    {
        countOption(OptId.get(POSF));
        Set<LooseBoard> positionSet = new HashSet<>();

        for (String moveSt : readLinesSet(posFile))
        {
            try { positionSet.add(new LooseBoard(true).goTo(PgnGame.parseMoveString(moveSt))); }

            catch (IllegalMoveException e)
            {
                System.err.println(e.getLocalizedMessage());
                System.exit(-1);
            }

            catch (NullPointerException e)
            {
                System.err.println("illegal move in parameter '" + moveSt + "'");
                System.exit(-1);
            }
        }

        PGNUtil.addMatchProcessor(new PGNUtil.MatchPositionSetProcessor(positionSet));
    }

    @Option(name = MFEN, aliases = "-match_fen", metaVar = "<fen>",
            usage = "output games that contain the FEN position <fen>")
    private void setFen(String fen)
    {
        countOption(OptId.get(MFEN));

        try
        {
            PGNUtil.addMatchProcessor(new PGNUtil.MatchPositionSetProcessor(
                    Collections.singleton(new LooseBoard(Board.fromFen(fen)))));
        }

        catch (InvalidFenException e)
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }
    }

    @Option(name = FF, aliases = "-fen_file", metaVar = "<file>",
            usage = "output games that contain any of the FEN positions listed in <file>")
    private void setFenFile(File fenFile)
    {
        countOption(OptId.get(FF));
        Set<LooseBoard> positionSet = new HashSet<>();

        try { for (String fen : readLinesSet(fenFile)) positionSet.add(new LooseBoard(Board.fromFen(fen))); }

        catch (InvalidFenException e)
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }

        PGNUtil.addMatchProcessor(new PGNUtil.MatchPositionSetProcessor(positionSet));
    }

    @Option(name = EF, aliases = "-eco_file", metaVar = "<file>",
            usage = "use <file> as the standard (Lichess-formatted) ECO database for any ECO-related operation. " +
                    "See https://github.com/lichess-org/chess-openings")
    private void ecoFile(File ef)
    {
        countOption(OptId.get(EF));
        EcoTree.FileType.STD.getEcoTree(EcoTree.FileType.LICHESS, ef); // read Lichess-formatted d.b. as standard
    }

    @Option(name = ME, aliases = "-match_eco", metaVar = "<regex>",
        usage = "output games belonging to ECO code <regex>")
    private void setEco(String eco)
    {
        countOption(OptId.get(ME));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(ME)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
                                EcoTree.FileType.STD));
                    }
                });
    }

    @Option(name = MED, aliases = "-match_eco_desc", metaVar = "<regex>",
        usage = "output games whose ECO description matches <regex>")
    private void setEcoDesc(String eco)
    {
        countOption(OptId.get(MED));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MED)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoDescProcessor(Pattern.compile(eco,
                                Pattern.DOTALL), EcoTree.FileType.STD));
                    }
                });
    }

    @Option(name = MSE, aliases = "-match_scid_eco", metaVar = "<regex>",
        usage = "output games belonging to Scid ECO code <regex>")
    private void setScidEco(String eco)
    {
        countOption(OptId.get(MSE));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MSE)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
                                EcoTree.FileType.SCIDDB));
                    }
                });
    }

    @Option(name = MSED, aliases = "-match_scid_eco_desc", metaVar = "<regex>",
        usage = "output games whose Scid ECO description matches <regex>")
    private void setScidEcoDesc(String eco)
    {
        countOption(OptId.get(MSED));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MSED)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchEcoDescProcessor(Pattern.compile(eco,
                                Pattern.DOTALL), EcoTree.FileType.SCIDDB));
                    }
                });
    }

    @Option(name = MXE, aliases = "-match_trans_eco", metaVar = "<regex>",
        usage = "output games belonging to ECO code <regex>, matching transpositionally")
    private void setXEco(String eco)
    {
        countOption(OptId.get(MXE));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MXE)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
                                EcoTree.FileType.STD));
                    }
                });
    }

    @Option(name = MXED, aliases = "-match_trans_eco_desc", metaVar = "<regex>",
        usage = "output games whose ECO description matches <regex>, matching transpositionally")
    private void setXEcoDesc(String eco)
    {
        countOption(OptId.get(MXED));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MXED)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoDescProcessor(Pattern.compile(eco,
                                Pattern.DOTALL), EcoTree.FileType.STD));
                    }
                });
    }

    @Option(name = MXSE, aliases = "-match_trans_scid_eco", metaVar = "<regex>",
        usage = "output games belonging to Scid ECO code <regex>, matching transpositionally")
    private void setXScidEco(String eco)
    {
        countOption(OptId.get(MXSE));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MXSE)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoProcessor(Pattern.compile(eco, Pattern.DOTALL),
                                EcoTree.FileType.SCIDDB));
                    }
                });
    }

    @Option(name = MXSED, aliases = "-match_trans_scid_eco_desc", metaVar = "<regex>",
        usage = "output games whose Scid ECO description matches <regex>, matching transpositionally")
    private void setXScidEcoDesc(String eco)
    {
        countOption(OptId.get(MXSED));

        // delayed initialization, in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(MXSED)}, null, null,
                new CLOptionResolver.OptHandler()
                {
                    @Override
                    public void handleOpts(Map<OptId,Integer> setOpts, Set<OptId> intersects)
                    {
                        PGNUtil.addMatchProcessor(new PGNUtil.MatchXEcoDescProcessor(Pattern.compile(eco,
                                Pattern.DOTALL), EcoTree.FileType.SCIDDB));
                    }
                });
    }

    @Option(name = APF, aliases = "-any_player_file", metaVar = "<file>",
        usage = "output games in which either player is contained in <file>")
    private void setAnyPlayerFile(File playerFile)
    {
        countOption(OptId.get(APF));
        PGNUtil.addMatchProcessor(new PGNUtil.MatchAnyPlayerSetProcessor(readLinesSet(playerFile)));
    }

    @Option(name = PF, aliases = "-player_file", metaVar = "<file>",
        usage = "output games in which both players are contained in <file>")
    private void setPlayerFile(File playerFile)
    {
        countOption(OptId.get(PF));
        PGNUtil.addMatchProcessor(new PGNUtil.MatchAllPlayerSetProcessor(readLinesSet(playerFile)));
    }

    @Option(name = NPF, aliases = "-not_player_file", metaVar = "<file>",
        usage = "output games in which neither player is contained in <file>")
    private void setNotPlayerFile(File playerFile)
    {
        countOption(OptId.get(NPF));
        PGNUtil.addMatchProcessor(new PGNUtil.NotMatchPlayerSetProcessor(readLinesSet(playerFile)));
    }

    @Option(name = MT, aliases = "-match_tag", metaVar = "<tag>/<regex>",
        usage = "output games in which PGN tag <tag> has value <regex>")
    private void setTag(String tag)
    {
        countOption(OptId.get(MT));
        
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
        countOption(OptId.get(NMT));
        
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

    @Option(name = TC, aliases = "-time_ctrl", metaVar = "<timectrl>", hidden = true,
        usage = "match games in which the time control is <timectrl>")
    private void setTimeControl(String timectrl)
    {
        countOption(OptId.get(TC));
        
        try { PGNUtil.addMatchProcessor(new PGNUtil.MatchTimeCtrlProcessor(timectrl));}
        
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
        countOption(OptId.get(LPC));
        PGNUtil.addMatchProcessor(new PGNUtil.MinPlyCountProcessor(plies));
    }

    @Option(name = HPC, aliases = "-hi_ply_count", metaVar = "<max>",
        usage = "output games containing at most <max> plies")
    private void setHiPlies(int plies)
    {
        countOption(OptId.get(HPC));
        PGNUtil.addMatchProcessor(new PGNUtil.MaxPlyCountProcessor(plies));
    }

    @Option(name = BM, aliases = "-book_marker", metaVar = "<regex>",
        usage = "use <regex> as the delimiter between book and non-book moves")
    private void setBookMarker(String regex)
    {
        countOption(OptId.get(BM));
        PgnGame.setBookMarker(regex);
    }

    @Option(name = LOOB, aliases = "-lo_oob", metaVar = "<min>",
        usage = "output games containing at least <min> out-of-book plies")
    private void setLoOob(int plies)
    {
        countOption(OptId.get(LOOB));
        PGNUtil.addMatchProcessor(new PGNUtil.MinOobProcessor(plies));
    }

    @Option(name = HOOB, aliases = "-hi_oob", metaVar = "<max>",
        usage = "output games containing at most <max> out-of-book plies")
    private void setHiOob(int plies)
    {
        countOption(OptId.get(HOOB));
        PGNUtil.addMatchProcessor(new PGNUtil.MaxOobProcessor(plies));
    }

    // replacers

    @Option(name = R, aliases = "-replace", metaVar = "<regx1>/<regx2>/<repl>",
        usage = "output all games (or all selected games), but for each game matching <regx1>, replace <regx2> " +
                "with <repl>")
    private void setReplace(String replaceStr)
    {
        countOption(OptId.get(R));
        
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

        PGNUtil.addReplaceProcessor(new PGNUtil.ReplaceContainsProcessor(Pattern.compile(replaceTokens[0],
                Pattern.DOTALL)));
        
        // The actual replacer must go last.
        CLOptionResolver.addCondition(new OptId[] {OptId.get(R)}, null, null,
            new CLOptionResolver.ReplaceHandler(Pattern.compile(replaceTokens[1], Pattern.DOTALL), replaceTokens[2]));
    }

    @Option(name = RW, depends = {R}, aliases = "-replace_winner", metaVar = "<regex>",
        usage = "in combination with '" + R + "' option, select games won by player <regex> for replacement")
    private void replaceWinner(String regex)
    {
        countOption(OptId.get(RW));
        PGNUtil.addReplaceProcessor(new PGNUtil.ReplaceWinProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = RL, depends = {R}, aliases = "-replace_loser", metaVar = "<regex>",
        usage = "in combination with '" + R + "' option, select games lost by player <regex> for replacement")
    private void replaceLoser(String regex)
    {
        countOption(OptId.get(RL));
        PGNUtil.addReplaceProcessor(new PGNUtil.ReplaceLossProcessor(Pattern.compile(regex, Pattern.DOTALL)));
    }

    @Option(name = RO, depends = {R}, aliases = "-replace_opening", metaVar = "<oid1,oid2,...>",
        usage = "in combination with '" + R + "' option, select games in which the opening-book moves are the same " +
                "as any of <oid,oid2,...> for replacement")
    private void replaceOpening(String opening)
    {
        countOption(OptId.get(RO));
        PGNUtil.addReplaceProcessor(new PGNUtil.ReplaceOpeningProcessor(opening));
    }

    // duplicates

    @Option(name = D, forbids = {S}, aliases = "-duplicates", metaVar = "<plies>",
        usage = "list games containing identical players and move lists up to <plies> half-moves ('0' means the " +
            "whole game); each line of output contains one set of two or more game numbers in which duplicates are " +
            "found")
    private void duplicates(int plies)
    {
        countOption(OptId.get(D));
        PGNUtil.DuplicateGameHandler handler = new PGNUtil.DuplicateGameHandler(plies);
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DO, forbids = {S}, aliases = "-duplicate_openings",
        usage = "list games containing identical players and openings; each line of output contains one set of two " +
                "or more game numbers in which duplicates are found")
    private void duplicateOpenings(boolean d)
    {
        countOption(OptId.get(DO));
        
        PGNUtil.DuplicateOpeningHandler handler = new PGNUtil.DuplicateOpeningHandler();
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DM, forbids = {S}, aliases = "-duplicate_moves", metaVar = "<plies>",
        usage = "list games containing identical move lists up to <plies> half-moves ('0' means the whole game); " +
                "each line of output contains one set of two or more game numbers in which duplicates are found")
    private void duplicatePostOpenings(int plies)
    {
        countOption(OptId.get(DM));
        PGNUtil.DuplicateMoveHandler handler = new PGNUtil.DuplicateMoveHandler(plies);
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DOOB, forbids = {S}, aliases = "-duplicate_oob", metaVar = "<plies>",
            usage = "list games containing identical players and move lists up to <plies> half-moves after the " +
                    "opening; each line of output contains one set of two or more game numbers in which duplicates " +
                    "are found")
    private void duplicateOob(int plies)
    {
        countOption(OptId.get(DOOB));

        PGNUtil.DuplicatePostOpeningHandler handler = new PGNUtil.DuplicatePostOpeningHandler(plies);
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    @Option(name = DOOBM, forbids = {S}, aliases = "-duplicate_oob_moves", metaVar = "<plies>",
            usage = "list games containing identical move lists up to <plies> half-moves after the opening; each " +
                    "line of output contains one set of two or more game numbers in which duplicates are found")
    private void duplicateOobMoves(int plies)
    {
        countOption(OptId.get(DOOBM));

        PGNUtil.DuplicatePostOpeningMoveHandler handler = new PGNUtil.DuplicatePostOpeningMoveHandler(plies);
        PGNUtil.setHandler(handler);
        PGNUtil.setExitProcessor(new PGNUtil.DuplicateExitProcessor(handler));
    }

    // events

    @Option(name = E, aliases = "-events",
        usage = "list one event per line, with game numbers (ordinal position of the game as it was read from the " +
                "input source)")
    private void events(boolean e)
    {
        countOption(OptId.get(E));
        Tallier events = Events.getInstance();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(events));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(events));
    }

    // event errors

    @Option(name = CSR, forbids = {S}, aliases = "-check_sequential_rounds",
        usage = "list each event containing non-sequential rounds, one event per line, with " +
        "information about each non-sequential round found")
    private void eventErrors(boolean e)
    {
        countOption(OptId.get(CSR));
        Tallier events = Events.getEventErrorInstance();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(events));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(events));
    }

    // opening stats

    @Option(name = O, aliases = "-opening_stats",
            usage = "print win/loss/draw statistics for each opening.  Valid output selectors ('" + S + "') include: " +
            "'eco,' 'oid,' 'count,' 'wwins,' 'bwins,' 'draws,' 'wwinpct,' 'bwinpct,' 'diff,' 'diffpct,' 'drawpct'")
    private void openings(boolean o)
    {
        countOption(OptId.get(O));
        // The rest is handled in CLOptionResolver.
    }

    // ECO stats

    @Option(name = ECO, depends = {O}, aliases = "-eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each ECO code")
    private void ecoOpenings(boolean o)
    {
        countOption(OptId.get(ECO));

        // delay tree initialization in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(ECO)}, null, null,
                new CLOptionResolver.StdEcoHandler(false));
    }

    @Option(name = SECO, depends = {O}, aliases = "-scid_eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each Scid ECO code")
    private void ScidEcoOpenings(boolean o)
    {
        countOption(OptId.get(SECO));
        Tallier os = EcoStats.getInstance(EcoTree.FileType.SCIDDB, false);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }

    @Option(name = XECO, depends = {O}, aliases = "-trans_eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each ECO code, " +
                    "matching openings transpositionally")
    private void XEcoOpenings(boolean o)
    {
        countOption(OptId.get(XECO));

        // delay tree initialization in case "-ef" is set
        CLOptionResolver.addCondition(new OptId[] {OptId.get(XECO)}, null, null,
                new CLOptionResolver.StdEcoHandler(true));
    }

    @Option(name = XSECO, depends = {O}, aliases = "-trans_scid_eco_stats",
            usage = "combined with the '" + O + "' option, print win/loss/draw statistics for each Scid ECO code, " +
                    "matching openings transpositionally")
    private void XScidEcoOpenings(boolean o)
    {
        countOption(OptId.get(XSECO));
        Tallier os = EcoStats.getInstance(EcoTree.FileType.SCIDDB, true);
        PGNUtil.setHandler(new PGNUtil.TallyHandler(os));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(os));
    }

    @Option(name = CMIN, depends = {O}, aliases = "-count_min", metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings that appear in at least <min> games")
    private void minOpeningCount(int cmin)
    {
        countOption(OptId.get(CMIN));
        OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MinGamesProcessor(cmin));
    }

    @Option(name = HWD, depends = {O}, aliases = "-hi_win_diff", metaVar = "<max>",
        usage = "in combination with '" + O + "' option, print only openings for which the percentage win difference " +
                "between white and black is at most <max> percent")
    private void hiWinDiff(double max)
    {
        countOption(OptId.get(HWD));
        OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MaxWinDiffProcessor(max));
    }

    @Option(name = LWD, depends = {O}, aliases = "-lo_win_diff", metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings for which the percentage win difference " +
                "between white and black is at least <min> percent")
    private void loWinDiff(double min)
    {
        countOption(OptId.get(LWD));
        OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MinWinDiffProcessor(min));
    }

    @Option(name = HDRAW, depends = {O}, aliases = "-hi_draw_pct", metaVar = "<max>",
        usage = "in combination with '" + O + "' option, print only openings for which the percentage of draws is " +
                "at most <max> percent")
    private void maxDraw(double max)
    {
        countOption(OptId.get(HDRAW));
        OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MaxDrawProcessor(max));
    }

    @Option(name = LDRAW, depends = {O}, aliases = "-lo_draw_pct", metaVar = "<min>",
        usage = "in combination with '" + O + "' option, print only openings for which the percentage of draws is " +
                "at least <min> percent")
    private void minDraw(double min)
    {
        countOption(OptId.get(LDRAW));
        OpeningProcessors.addOpeningProcessor(new OpeningProcessors.MinDrawProcessor(min));
    }
    
    // player results

    @Option(name = P, forbids = {D, DO, DM, O, E, CSR}, aliases = "-player_results",
        usage = "list win/loss/draw statistics for each player.  Valid output selectors ('" + S + "') are: " +
                "'player,' 'wins,' 'losses,' 'draws,' 'noresults,' 'count,' 'winpct'")
    private void playerResults(boolean e)
    {
        countOption(OptId.get(P));
        
        Tallier pr = PlayerResults.getInstance();
        PGNUtil.setHandler(new PGNUtil.TallyHandler(pr));
        PGNUtil.setExitProcessor(new PGNUtil.TallyExitProcessor(pr));
    }

    // output-field selector

    @Option(name = S, forbids = {D, DO, DM, CSR}, aliases = "-select", metaVar = "<selector1,selector2,...>",
        usage = "select values for output. By default, these are simply tags from the game's header (e.g., 'Event'), " +
                "though a number of special selectors are also available. For example, 'moves' selects the game's " +
                "move list, 'gameno' selects the ordinal position of the game as it was read from the input source, " +
                "'oid' selects the game's opening identifier (not the ECO code), 'stdeco' selects the ECO code, and " +
                "'scideco' selects the Scid ECO code. The selectors 'winner' and 'loser' select the winning and " +
                "losing player, respectively.  When the '" + MP + "' option is used, 'opponent' selects the other " +
                "player. For other available selectors, please see the README file.")
    private void selectors(String spec)
    {
        countOption(OptId.get(S));

        String tokens[] = spec.split(",\\W*");
        PGNUtil.outputSelectors = new OutputSelector[tokens.length];
        for (int i = 0; i < tokens.length; i++) PGNUtil.outputSelectors[i] = new OutputSelector(tokens[i]);
        // The rest is handled in CLOptionResolver.
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
        usage = "reports incorrect game moves and canonicalizes internal SAN representation; entails a performance " +
                "penalty")
    static boolean validateGames = false;

    @Option(name = I, aliases = "-inputfile", metaVar = "<filename>",
            usage = "input PGN file (may be specified more than once); if this option is not present, pgnutil reads " +
                    "from standard input")
    private void setInput(File f)
    {
        countOption(OptId.get(I));
        
        try { PGNUtil.addInputFile(new PgnFile(f.getName(), new BufferedReader(new FileReader(f)))); }
        
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

    @Option(name = H, forbids = {D, DO, DM, E, CSR, O, M, GNF, NM, MW, ML, MP, MO, NMO, R, V},
        aliases = "-help", usage = "print usage information")
    static boolean help = false;

    @Option(name = V, forbids = {D, DO, DM, E, CSR, O, M, GNF, NM, MW, ML, MP, MO, NMO, R, H},
        aliases = "-version", usage = "print version information")
    private void version(boolean v)
    {
        System.out.println("pgnutil version " + PGNUtil.VERSION);
        System.exit(0);
    }
        
//        @Argument(metaVar = "[target [target2 [target3] ...]]", usage = "targets")
//        private List<String> targets = new ArrayList<String>();
}
