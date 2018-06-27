/*
 */

package com.dotfx.pgnutil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

//import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Mark Chen
 */
public class PGNUtil
{
    public static final String VERSION = "0.1";
    
    public static class CLOpts
    {
        // matchers
        
        @Option(name = "-m", forbids = {"-d", "-e", "-o"}, aliases = "-matches",
            metaVar = "<regex>",
            usage = "output games matching the regular expression <regex>")
        private String containsStr;
        
        @Option(name = "-gn", forbids = {"-d", "-e", "-o"}, aliases = "-game_number",
            metaVar = "<range1,range2,...>",
            usage = "output games whose ordinal position in the input source " +
                "is contained in <range1,range2,...>")
        private String gameNumStr;

        @Option(name = "-nm", forbids = {"-d", "-e", "-o"},
            aliases = "-not_matches", metaVar = "<regex>",
            usage = "output games not matching the regular expression " +
            "<regex>")
        private String notContainsStr;

        @Option(name = "-me", forbids = {"-d", "-e", "-o"},
            aliases = "-match_event", metaVar = "<regex>",
            usage = "output games where the event matches <regex>")
        private String matchEventStr;

        @Option(name = "-mw", forbids = {"-d", "-e", "-o"},
            aliases = "-match_winner", metaVar = "<regex>",
            usage = "output games won by player <regex>")
        private String matchWinStr;

        @Option(name = "-ml", forbids = {"-d", "-e", "-o"},
            aliases = "-match_loser", metaVar = "<regex>",
            usage = "output games lost by player <regex>")
        private String matchLossStr;

        @Option(name = "-mp", forbids = {"-d", "-e", "-o"},
            aliases = "-match_player", metaVar = "<regex>",
            usage = "output games where <regex> is a player")
        private String matchPlayerStr;

        @Option(name = "-mo", forbids = {"-d", "-e", "-o"},
            aliases = "-match_opening",
            metaVar = "<oid1,oid1,...>",
            usage = "output games in which the opening-book moves are the " +
            "same as any of <oid,oid2,...>")
        private String matchOpeningStr;

        @Option(name = "-nmo", forbids = {"-d", "-e", "-o"},
            aliases = "-not_match_opening",
            metaVar = "<oid1,oid1,...>",
            usage = "output games in which the opening-book moves are not " +
            "the same as any of <oid,oid2,...>")
        private String notMatchOpeningStr;
        
        // replacers
        
        @Option(name = "-r", forbids = {"-d", "-e", "-o"}, aliases = "-replace",
            metaVar = "<regx1>/<regx2>/<repl>",
            usage = "output all games (or all selected games), but for each " +
            "game matching <regx1>, replace <regx2> with <repl>")
        private String replaceStr;

        @Option(name = "-rw", depends = {"-r"}, aliases = "-replace_winner",
            metaVar = "<regex>",
            usage = "in combination with \"-r\" option, select games won by " +
            "player <regex> for replacement")
        private String replWinStr;

        @Option(name = "-rl", depends = {"-r"}, aliases = "-replace_loser",
            metaVar = "<regex>",
            usage = "in combination with \"-r\" option, select games lost by " +
            "player <regex> for replacement")
        private String replLossStr;

        @Option(name = "-ro", depends = {"-r"}, aliases = "-replace_opening",
            metaVar = "<oid1,oid1,...>",
            usage = "in combination with \"-r\" option, select games in " +
            "which the opening-book moves are the same as any of <oid,oid2,...> " +
            "for replacement")
        private String replOpeningStr;
        
        // duplicates

        @Option(name = "-d", forbids = {"-o", "-e", "-m", "-gn", "-nm", "-me",
            "-mw", "-ml", "-mp", "-mo", "-nmo", "-r"},
            aliases = "-duplicates",
            usage = "list games containing identical players and move lists; " +
                "each line of output contains one set of two or more " +
                "duplicate games numbers")
        private Boolean duplicates;
        
        // events
        
        @Option(name = "-e", forbids = {"-d", "-o", "-m", "-gn", "-nm", "-me",
            "-mw", "-ml", "-mp", "-mo", "-nmo", "-r"}, aliases = "-events",
            usage = "list one event per line, with game numbers (ordinal " +
            "position of the game as it was read from the input source)")
        private Boolean events;
        
        // opening stats
        
        @Option(name = "-o", forbids = {"-d", "-e", "-m", "-gn", "-nm", "-me",
            "-mw", "-ml", "-mp", "-mo", "-nmo", "-r"},
            aliases = "-opening_stats", usage = "print win/loss/draw " +
            "statistics for each opening")
        private Boolean openingStats;
        
        @Option(name = "-cmin", depends = {"-o"}, aliases = "-count_min",
            metaVar = "<min>",
            usage = "in combination with \"-o\" option, print only openings " +
                "that appear in at least <min> games")
        private int minGames;
        
        @Option(name = "-hdp", depends = {"-o"}, aliases = "-hi_diff_pct",
            metaVar = "<max>",
            usage = "in combination with \"-o\" option, print only openings " +
                "for which the percentage win difference between white and " +
                "black is at most <max> percent")
        private Double maxDiffPct;
        
        @Option(name = "-ldp", depends = {"-o"}, aliases = "-lo_diff_pct",
            metaVar = "<min>",
            usage = "in combination with \"-o\" option, print only openings " +
                "for which the percentage win difference between white and " +
                "black is at least <min> percent")
        private Double minDiffPct;
        
        @Option(name = "-hdraw", depends = {"-o"}, aliases = "-hi_draw_pct",
            metaVar = "<max>",
            usage = "in combination with \"-o\" option, print only openings " +
                "for which the percentage of draws is at most <max> percent")
        private Double maxDrawPct;
        
        @Option(name = "-ldraw", depends = {"-o"}, aliases = "-lo_draw_pct",
            metaVar = "<min>",
            usage = "in combination with \"-o\" option, print only openings " +
                "for which the percentage of draws is at least <min> percent")
        private Double minDrawPct;
        
        // output-field selector
        
        @Option(name = "-s", forbids = {"-d", "-e"}, aliases = "-select",
            metaVar = "<field1,field2,..>",
            usage = "select fields for output.  The field 'moves' selects " +
            "the game's move list, the field 'gameno' selects the ordinal " +
            "position of the game as it was read from the input source, and " +
            "the field 'oid' selects the game's opening identifier (not " +
            "ECO). The fields 'winner' and 'loser' select the winning and " +
            "losing player, respectively.  Any other field will be selected " +
            "from the game's tag list")
        private String outputFields;
        
        @Option(name = "-i", aliases = "-inputfile", usage = "input PGN file; " +
            "if this option is not present, pgnutil reads from standard input",
            metaVar = "<filename>")
        private String infile;
        
        @Option(name = "-h", forbids = {"-d", "-e", "-o", "-m", "-gn", "-nm",
            "-me", "-mw", "-ml", "-mp", "-mo", "-nmo", "-r"}, aliases = "-help",
            usage = "print usage information")
        private Boolean help;
        
        @Option(name = "-v", forbids = {"-d", "-e", "-o", "-m", "-h", "-gn",
            "-nm", "-me", "-mw", "-ml", "-mp", "-mo", "-nmo", "-r"},
            aliases = "-version",
            usage = "print version information")
        private Boolean version;
        
//        @Argument(metaVar = "[target [target2 [target3] ...]]", usage = "targets")
//        private List<String> targets = new ArrayList<String>();
    }
    
    private static class ProcessorException extends Exception
    {
        public ProcessorException(Exception e) { super(e); }
    }
    
    private static interface GameProcessor
    {
        /**
         * 
         * @return true if we should continue processing this game, false if we
         *         should skip the game
         */
        public boolean processGame() throws ProcessorException;
    }
    
    private static class ContainsProcessor implements GameProcessor
    {
        private final Pattern containsPattern;
        
        public ContainsProcessor(Pattern p) { containsPattern = p; }
        
        @Override public boolean processGame()
        {
            return game.matches(containsPattern);
        }
    }
    
    private static class NotContainsProcessor implements GameProcessor
    {
        private final Pattern notContainsPattern;
        
        public NotContainsProcessor(Pattern p) { notContainsPattern = p; }
        
        @Override public boolean processGame()
        {
            return !game.matches(notContainsPattern);
        }
    }
    
    private static class MatchGameNumProcessor implements GameProcessor
    {
        private final List<SimpleEntry<Integer,Integer>> ranges;
        
        public MatchGameNumProcessor(String s) throws NumberFormatException
        {
            ranges = new ArrayList();
            
            for (String token : s.split(",\\W*"))
            {
                String rangeBounds[] = token.split("-");
                Integer rangeStart = Integer.valueOf(rangeBounds[0]);
                
                ranges.add(new SimpleEntry(rangeStart, rangeBounds.length == 1 ?
                    rangeStart : Integer.valueOf(rangeBounds[1])));
            }
        }
        
        @Override public boolean processGame()
        {
            int gameno = game.getNumber();
            
            for (SimpleEntry<Integer,Integer> range : ranges)
            {
                if (gameno >= range.getKey() && gameno <= range.getValue())
                    return true;
            }
            
            return false;
        }
    }
    
    private static class MatchOpeningProcessor implements GameProcessor
    {
        private final Set<OpeningID> openingSet;
        
        public MatchOpeningProcessor(String s)
        {
            openingSet = new HashSet();
            
            for (String token : s.split(",\\W*"))
                openingSet.add(OpeningID.fromString(token));
        }
        
        @Override public boolean processGame()
        {
            return openingSet.contains(game.getOpeningID());
        }
    }
    
    private static class NotMatchOpeningProcessor extends MatchOpeningProcessor
    {
        public NotMatchOpeningProcessor(String s) { super(s); }
        @Override public boolean processGame() { return !super.processGame(); }
    }
    
    private static class MatchEventProcessor implements GameProcessor
    {
        private final Pattern matchEventPattern;
        
        public MatchEventProcessor(Pattern p) { matchEventPattern = p; }
        
        @Override public boolean processGame()
        {
            return matchEventPattern.matcher(game.getEvent()).find();
        }
    }
    
    private static class MatchWinProcessor implements GameProcessor
    {
        private final Pattern matchWinPattern;
        
        public MatchWinProcessor(Pattern p) { matchWinPattern = p; }
        
        @Override public boolean processGame()
        {
            String winner = game.getWinner();
            if (winner == null) return false;
            return matchWinPattern.matcher(winner).find();
        }
    }
    
    private static class MatchLossProcessor implements GameProcessor
    {
        private final Pattern matchLossPattern;
        
        public MatchLossProcessor(Pattern p) { matchLossPattern = p; }
        
        @Override public boolean processGame()
        {
            String loser = game.getLoser();
            if (loser == null) return false;
            return matchLossPattern.matcher(loser).find();
        }
    }
    
    private static class MatchPlayerProcessor implements GameProcessor
    {
        private final Pattern matchPlayerPattern;
        
        public MatchPlayerProcessor(Pattern p) { matchPlayerPattern = p; }
        
        @Override public boolean processGame()
        {
            return matchPlayerPattern.matcher(game.getWhite()).find() ||
                matchPlayerPattern.matcher(game.getBlack()).find();
        }
    }
    
    /**
     * An instance of this must be the last processor in the
     * replaceProcessors list.
     */
    private static class ReplaceProcessor implements GameProcessor
    {
        private final Pattern replacePattern;
        private final String replacement;
        
        public ReplaceProcessor(Pattern replacePattern, String replacement)
        {
            this.replacePattern = replacePattern;
            this.replacement = replacement;
        }
        
        @Override public boolean processGame() throws ProcessorException
        {
            try
            {
                game = game.replace(replacePattern, replacement);
                return false;
            }
            
            catch (IOException | PGNException e)
            {
                throw new ProcessorException(e);
            }
        }
    }
    
    private static interface GamePrinter { public void print(); }
    
    private static class DefaultGamePrinter implements GamePrinter
    {
        @Override public void print()
        {
            System.out.print(game.getOrigText());
        }
    }
    
    private static class SelectGamePrinter implements GamePrinter
    {
        private final String fields;
        
        public SelectGamePrinter(String fields) { this.fields = fields; }
        
        @Override public void print()
        {
            System.out.println(game.get(fields));
        }
    }
    
    private static interface OpeningProcessor
    {
        /**
         * 
         * @return true if we should continue processing this opening, false if
         * we should skip it
         */
        public boolean processOpening(OpeningStats stats)
            throws ProcessorException;
    }
    
    private static class MinGamesProcessor implements OpeningProcessor
    {
        private final int minGames;
        
        public MinGamesProcessor(int minGames) { this.minGames = minGames; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getGameCount() >= minGames;
        }
    }
    
    private static class MaxWinDiffProcessor implements OpeningProcessor
    {
        private final double maxDiff;
        
        public MaxWinDiffProcessor(double maxDiff) { this.maxDiff = maxDiff; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getWhiteWinPct() - stats.getBlackWinPct() <= maxDiff;
        }
    }
    
    private static class MinWinDiffProcessor implements OpeningProcessor
    {
        private final double minDiff;
        
        public MinWinDiffProcessor(double minDiff) { this.minDiff = minDiff; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getWhiteWinPct() - stats.getBlackWinPct() >= minDiff;
        }
    }
    
    private static class MinDrawProcessor implements OpeningProcessor
    {
        private final double minDraw;
        
        public MinDrawProcessor(double minDraw) { this.minDraw = minDraw; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getDrawPct() >= minDraw;
        }
    }
    
    private static class MaxDrawProcessor implements OpeningProcessor
    {
        private final double maxDraw;
        
        public MaxDrawProcessor(double maxDraw) { this.maxDraw = maxDraw; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getDrawPct() <= maxDraw;
        }
    }
    
    private static interface OpeningStatPrinter
    {
        public static OpeningStatPrinter get(String fields)
        {
            return fields == null ? new DefaultOpeningStatPrinter() :
                new CustomOpeningStatPrinter(fields);
        }
        
        public void print(OpeningStats stats);
    }
    
    private static class DefaultOpeningStatPrinter implements OpeningStatPrinter
    {
        @Override public void print(OpeningStats stats)
        {
            System.out.println(stats);
        }
    }
    
    private static class CustomOpeningStatPrinter implements OpeningStatPrinter
    {
        private final String fields;
        
        public CustomOpeningStatPrinter(String fields) { this.fields = fields; }
        
        @Override public void print(OpeningStats stats)
        {
            System.out.println(stats.get(fields));
        }
    }
    
    private static Game game;
    
    public static void main(String args[]) throws Exception
    {
        Reader reader;
        CLOpts options = new CLOpts();
        CmdLineParser parser = new CmdLineParser(options);
        List<GameProcessor> matchProcessors = new ArrayList<>();
        List<GameProcessor> replaceProcessors = new ArrayList<>();
        List<OpeningProcessor> openingProcessors = new ArrayList<>();
        
        try
        {
            parser.parseArgument(args);
            if (options.help != null) throw new CmdLineException("");
        }
        
        catch (CmdLineException e)
        {
            System.err.println(e.getLocalizedMessage() + "\nUsage:");
            parser.printUsage(System.err);
            System.exit(-1);
        }
        
        if (options.version != null)
        {
            System.out.println("pgnutil version " + VERSION);
            System.exit(0);
        }
        
        if (options.infile == null)
            reader = new BufferedReader(new InputStreamReader(System.in));
        
        else reader = new BufferedReader(new FileReader(options.infile));
        
        if (options.duplicates != null)
        {
            List<List<Integer>> dupes =
                new PGNDuplicateFinder(reader).getDuplicates();

            for (List<Integer> list : dupes)
            {
                for (Integer idx : list) System.out.print(idx + " ");
                System.out.print("\n");
            }
            
            return;
        }
        
        if (options.events != null)
        {
            Map<String,List<Integer>> events =
                new PGNEventMapper(reader).getEventMap();

            for (String event : events.keySet())
            {
                System.out.print("{" + event + "} ");
                List<Integer> games = events.get(event);
                for (Integer gameNo : games) System.out.print(gameNo + " " );
                System.out.print("\n");
            }
            
            return;
        }
        
        if (options.openingStats != null)
        {
            Map<OpeningID,OpeningStats> statMap =
                new PGNOpeningMapper(reader).getStats();
            
            if (options.minGames > 0)
                openingProcessors.add(new MinGamesProcessor(options.minGames));
            
            if (options.maxDiffPct != null)
                openingProcessors.add(new MaxWinDiffProcessor(options.maxDiffPct));
            
            if (options.minDiffPct != null)
                openingProcessors.add(new MinWinDiffProcessor(options.minDiffPct));
            
            if (options.minDrawPct != null)
                openingProcessors.add(new MinDrawProcessor(options.minDrawPct));
            
            if (options.maxDrawPct != null)
                openingProcessors.add(new MaxDrawProcessor(options.maxDrawPct));
            
            OpeningStatPrinter printer = OpeningStatPrinter.get(options.outputFields);

            nextOpening:
            for (OpeningID id : statMap.keySet())
            {
                OpeningStats stats = statMap.get(id);
                
                for (OpeningProcessor processor : openingProcessors)
                    if (!processor.processOpening(stats)) continue nextOpening;
                
                printer.print(stats);
            }
            
            return;
        }
        
        // At this point, we're not doing duplicates, events, or opening stats,
        // so this is a match and/or replace operation.
        
        // Add match processors to the matchProcessors list.
        
        if (options.matchEventStr != null)
            matchProcessors.add(new MatchEventProcessor(
                Pattern.compile(options.matchEventStr, Pattern.DOTALL)));
        
        if (options.matchWinStr != null)
            matchProcessors.add(new MatchWinProcessor(
                Pattern.compile(options.matchWinStr, Pattern.DOTALL)));
        
        if (options.matchLossStr != null)
            matchProcessors.add(new MatchLossProcessor(
                Pattern.compile(options.matchLossStr, Pattern.DOTALL)));
        
        if (options.matchPlayerStr != null)
            matchProcessors.add(new MatchPlayerProcessor(
                Pattern.compile(options.matchPlayerStr, Pattern.DOTALL)));
        
        if (options.gameNumStr != null)
            matchProcessors.add(new MatchGameNumProcessor(
                options.gameNumStr));
        
        if (options.matchOpeningStr != null)
            matchProcessors.add(new MatchOpeningProcessor(
                options.matchOpeningStr));
        
        if (options.notMatchOpeningStr != null)
            matchProcessors.add(new NotMatchOpeningProcessor(
                options.notMatchOpeningStr));
        
        if (options.containsStr != null)
            matchProcessors.add(new ContainsProcessor(
                Pattern.compile(options.containsStr, Pattern.DOTALL)));
        
        if (options.notContainsStr != null)
            matchProcessors.add(new NotContainsProcessor(
                Pattern.compile(options.notContainsStr, Pattern.DOTALL)));
        
        // Now add replacement processors to the replaceProcessors list.
        
        if (options.replaceStr != null)
        {
            // The regex passed to split() allows escaping of the delimiter character
            // ("/") with a backslash. See https://stackoverflow.com/questions/
            // 18677762/handling-delimiter-with-escape-characters-in-java-string-split-method
            // First, however, we must escape any escaped backslashes.
            
            String replaceTokens[] = options.replaceStr.replace("\\\\",
                "\0").split("(?<!\\\\)/");
            
            if (replaceTokens.length != 3)
            {
                System.err.println("\nIncorrect token count in replace string.");
                parser.printUsage(System.err);
                System.exit(-1);
            }
            
            for (int i = 0; i < 3; i++)
                replaceTokens[i] = replaceTokens[i].replace("\0",
                    "\\\\").replace("\\/", "/");
        
            if (options.replWinStr != null)
                replaceProcessors.add(new MatchWinProcessor(
                    Pattern.compile(options.replWinStr, Pattern.DOTALL)));
        
            if (options.replLossStr != null)
                replaceProcessors.add(new MatchLossProcessor(
                    Pattern.compile(options.replLossStr, Pattern.DOTALL)));
        
            if (options.replOpeningStr != null)
                replaceProcessors.add(new MatchOpeningProcessor(
                    options.replOpeningStr));
            
            replaceProcessors.add(new ContainsProcessor(
                Pattern.compile(replaceTokens[0], Pattern.DOTALL)));
            
            // This must go last.
            replaceProcessors.add(new ReplaceProcessor(
                Pattern.compile(replaceTokens[1], Pattern.DOTALL),
                replaceTokens[2]));
        }
        
        PGNFile pgn = new PGNFile(reader);
        
        GamePrinter printer = options.outputFields == null ?
            new DefaultGamePrinter() :
            new SelectGamePrinter(options.outputFields);
        
        nextGame:
        while ((game = pgn.nextGame()) != null)
        {
            for (GameProcessor processor : matchProcessors)
                if (!processor.processGame()) continue nextGame;
            
            for (GameProcessor processor : replaceProcessors)
                if (!processor.processGame()) break;
            
            printer.print();
        }
    }
}
