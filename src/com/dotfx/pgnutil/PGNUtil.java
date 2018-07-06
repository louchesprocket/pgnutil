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

import com.google.common.hash.HashCode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 *
 * @author Mark Chen
 */
public class PGNUtil
{
    public static interface GameProcessor
    {
        /**
         * 
         * @return true if we should continue processing this game, false if we
         *         should skip the game
         */
        public boolean processGame() throws ProcessorException;
    }
    
    // match processors
    
    static class ContainsProcessor implements GameProcessor
    {
        private final Pattern matchPattern;
        
        public ContainsProcessor(Pattern p) { matchPattern = p; }
        
        @Override public boolean processGame()
        {
            return game.matches(matchPattern);
        }
    }
    
    static class NotContainsProcessor implements GameProcessor
    {
        private final Pattern notMatchPattern;
        
        public NotContainsProcessor(Pattern p) { notMatchPattern = p; }
        
        @Override public boolean processGame()
        {
            return !game.matches(notMatchPattern);
        }
    }
    
    static class MatchTagProcessor implements GameProcessor
    {
        private final OutputSelector tag;
        private final Pattern tagPattern;
        
        public MatchTagProcessor(String tag, Pattern p)
        {
            this.tag = new OutputSelector(tag);
            tagPattern = p;
        }
        
        @Override public boolean processGame()
        {
            try
            {
                String value = game.get(tag);
                return (value != null) && tagPattern.matcher(value).find();
            }
            
            catch (InvalidSelectorException e) { return false; }
        }
    }
    
    static class MatchGameNumProcessor implements GameProcessor
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
    
    static class MatchOpeningProcessor implements GameProcessor
    {
        private final Set<OpeningID> matchOpeningSet;
        
        public MatchOpeningProcessor(String s)
        {
            matchOpeningSet = new HashSet<>();
            
            for (String token : s.split(",\\W*"))
                matchOpeningSet.add(OpeningID.fromString(token));
        }
        
        @Override public boolean processGame()
        {
            return matchOpeningSet.contains(game.getOpeningID());
        }
    }
    
    static class NotMatchOpeningProcessor extends MatchOpeningProcessor
    {
        public NotMatchOpeningProcessor(String s) { super(s); }
        @Override public boolean processGame() { return !super.processGame(); }
    }
    
    static class MatchWinProcessor implements GameProcessor
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
    
    static class MatchLossProcessor implements GameProcessor
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
    
    static class MatchPlayerProcessor implements GameProcessor
    {
        private final Pattern playerPattern;
        
        public MatchPlayerProcessor(Pattern p)
        { PGNUtil.playerPattern = playerPattern = p; }
        
        @Override public boolean processGame()
        {
            return playerPattern.matcher(game.getWhite()).find() ||
                playerPattern.matcher(game.getBlack()).find();
        }
    }
    
    static class MatchTimeCtrlProcessor implements GameProcessor
    {
        private final TimeCtrl matchTimeCtrl;
        
        public MatchTimeCtrlProcessor(String s)
            throws InvalidTimeCtrlException
        {
            matchTimeCtrl = new TimeCtrl(s, false);
        }
        
        @Override public boolean processGame()
        {
            TimeCtrl timeCtrl = game.getTimeCtrl();
            return timeCtrl != null ? timeCtrl.equals(matchTimeCtrl) : false;
        }
    }
    
    // replacement processors
    
    /**
     * An instance of this must be the last processor in the
     * replaceProcessors list.
     */
    static class ReplaceProcessor implements GameProcessor
    {
        private final Pattern replacePattern2; // to be replaced
        private final String replacement;
        
        public ReplaceProcessor(Pattern replacePattern, String replacement)
        {
            replacePattern2 = replacePattern;
            this.replacement = replacement;
        }
        
        @Override public boolean processGame() throws ProcessorException
        {
            try
            {
                game = game.replace(replacePattern2, replacement);
                return false;
            }
            
            catch (IOException | PGNException e)
            {
                throw new ProcessorException(e);
            }
        }
    }
    
    static class ReplaceContainsProcessor implements GameProcessor
    {
        private final Pattern replacePattern1; // eligibility pattern
        
        public ReplaceContainsProcessor(Pattern p) { replacePattern1 = p; }
        
        @Override public boolean processGame()
        {
            return game.matches(replacePattern1);
        }
    }
    
    static class ReplaceWinProcessor implements GameProcessor
    {
        private final Pattern replaceWinPattern;
        
        public ReplaceWinProcessor(Pattern p) { replaceWinPattern = p; }
        
        @Override public boolean processGame()
        {
            String winner = game.getWinner();
            if (winner == null) return false;
            return replaceWinPattern.matcher(winner).find();
        }
    }
    
    static class ReplaceLossProcessor implements GameProcessor
    {
        private final Pattern replaceLossPattern;
        
        public ReplaceLossProcessor(Pattern p) { replaceLossPattern = p; }
        
        @Override public boolean processGame()
        {
            String loser = game.getLoser();
            if (loser == null) return false;
            return replaceLossPattern.matcher(loser).find();
        }
    }
    
    static class ReplaceOpeningProcessor implements GameProcessor
    {
        private final Set<OpeningID> replaceOpeningSet;
        
        public ReplaceOpeningProcessor(String s)
        {
            replaceOpeningSet = new HashSet<>();
            
            for (String token : s.split(",\\W*"))
                replaceOpeningSet.add(OpeningID.fromString(token));
        }
        
        @Override public boolean processGame()
        {
            return replaceOpeningSet.contains(game.getOpeningID());
        }
    }
    
    // game handlers
    
    static interface GameHandler
    { public void handle() throws InvalidSelectorException; }
    
    static class NullGameHandler implements GameHandler
    {
        @Override public void handle() throws InvalidSelectorException {}
    }
    
    private static class DefaultGameHandler implements GameHandler
    {
        @Override public void handle() throws InvalidSelectorException
        {
            System.out.print(game.getOrigText());
        }
    }
    
    static class SelectGameHandler implements GameHandler
    {
        private final OutputSelector selectors[];
        
        public SelectGameHandler(OutputSelector selectors[])
        {
            this.selectors = selectors;
        }
        
        @Override public void handle() throws InvalidSelectorException
        {
            System.out.println(game.get(selectors));
        }
    }
    
    static class EventMapHandler implements GameHandler
    {
        private final Map<String,List<GameInfo>> eventMap;
        
        public EventMapHandler() { eventMap = new HashMap<>(); }
        
        @Override public void handle() throws InvalidSelectorException
        {
            String event = game.getValue("Event");
            List<GameInfo> eventGames = eventMap.get(event);

            if (eventGames == null)
            {
                eventGames = new ArrayList();
                eventMap.put(event, eventGames);
            }

            eventGames.add(new GameInfo((int)game.getNumber(),
                game.getTimeCtrl()));
        }
        
        public Map<String,List<GameInfo>> getEventMap() { return eventMap; }
    }
    
    static class DuplicateHandler implements GameHandler
    {
        private final Map<HashCode,List<Integer>> gameMap;
        private final Set<HashCode> duplicates;
        
        public DuplicateHandler()
        {
            gameMap = new HashMap(100000);
            duplicates = new HashSet();
        }
        
        @Override public void handle() throws InvalidSelectorException
        {
            HashCode gameHash = game.getHash();
            List<Integer> gameIdxes = gameMap.get(gameHash);

            if (gameIdxes != null) duplicates.add(gameHash);

            else
            {
                gameIdxes = new ArrayList<>();
                gameMap.put(gameHash, gameIdxes);
            }

            gameIdxes.add((int)game.getNumber());
        }
        
        public List<List<Integer>> getDuplicates()
        {
            List<List<Integer>> ret = new ArrayList<>();
            for (HashCode duplicate : duplicates) ret.add(gameMap.get(duplicate));
            return ret;
        }
    }
    
    static class OpeningsHandler implements GameHandler
    {
        private final Map<OpeningID,OpeningStats> openingsMap;
        
        public OpeningsHandler() {  openingsMap = new HashMap<>(10000); }
        
        @Override public void handle() throws InvalidSelectorException
        {
            if (CLOptions.maxEloDiff != null)
            {
                Integer whiteElo = eloMap.get(game.getWhite().trim());
                Integer blackElo = eloMap.get(game.getBlack().trim());
                
                if (whiteElo == null || blackElo == null ||
                    Math.abs(whiteElo - blackElo) > CLOptions.maxEloDiff)
                    return;
            }
            
            OpeningID openingID = game.getOpeningID();
            OpeningStats stats = openingsMap.get(openingID);

            if (stats == null)
            {
                stats = new OpeningStats(openingID, game.get(OutputSelector.ECO));
                openingsMap.put(openingID, stats);
            }

            stats.count(game);
        }
        
        public Map<OpeningID,OpeningStats> getMap() { return openingsMap; }
    }
    
    // opening-stat processors
    
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
    
    static class MinGamesProcessor implements OpeningProcessor
    {
        private final int minGames;
        
        public MinGamesProcessor(int minGames) { this.minGames = minGames; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getGameCount() >= minGames;
        }
    }
    
    static class MaxWinDiffProcessor implements OpeningProcessor
    {
        private final double maxDiff;
        
        public MaxWinDiffProcessor(double maxDiff) { this.maxDiff = maxDiff; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getWhiteWinPct() - stats.getBlackWinPct() <= maxDiff;
        }
    }
    
    static class MinWinDiffProcessor implements OpeningProcessor
    {
        private final double minDiff;
        
        public MinWinDiffProcessor(double minDiff) { this.minDiff = minDiff; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getWhiteWinPct() - stats.getBlackWinPct() >= minDiff;
        }
    }
    
    static class MinDrawProcessor implements OpeningProcessor
    {
        private final double minDraw;
        
        public MinDrawProcessor(double minDraw) { this.minDraw = minDraw; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getDrawPct() >= minDraw;
        }
    }
    
    static class MaxDrawProcessor implements OpeningProcessor
    {
        private final double maxDraw;
        
        public MaxDrawProcessor(double maxDraw) { this.maxDraw = maxDraw; }
        
        @Override public boolean processOpening(OpeningStats stats)
        {
            return stats.getDrawPct() <= maxDraw;
        }
    }
    
    // opening-stat printers
    
    private static interface OpeningStatPrinter
    {
        public static OpeningStatPrinter get(OutputSelector selectors[])
        {
            return selectors == null ? new DefaultOpeningStatPrinter() :
                new CustomOpeningStatPrinter(selectors);
        }
        
        public void print(OpeningStats stats) throws InvalidSelectorException;
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
        private final OutputSelector selectors[];
        
        public CustomOpeningStatPrinter(OutputSelector selectors[])
        {
            this.selectors = selectors;
        }
        
        @Override public void print(OpeningStats stats)
            throws InvalidSelectorException
        {
            System.out.println(stats.get(selectors));
        }
    }
    
    // exit processors
    
    private static interface ExitProcessor
    {
        public void process();
    }
    
    private static class NullExitProcessor implements ExitProcessor
    {
        @Override public void process() {}
    }
    
    static class EventMapExitProcessor implements ExitProcessor
    {
        private final Map<String,List<GameInfo>> eventMap;
        
        public EventMapExitProcessor(Map<String,List<GameInfo>> eventMap)
        {
            this.eventMap = eventMap;
        }
        
        @Override public void process()
        {
            for (String event : eventMap.keySet())
            {
                List<GameInfo> games = eventMap.get(event);
                
                System.out.print(event + CLOptions.outputDelim +
                    guessTimeCtrl(games) + CLOptions.outputDelim);
                
                for (GameInfo gi : games)
                    System.out.print(gi.getGameNum() + " ");
                
                System.out.print("\n");
            }
        }
    
        public static TimeCtrl guessTimeCtrl(List<GameInfo> games)
        {
            TimeCtrl best = null;

            for (int i = 0; i < games.size(); i++)
            {
                TimeCtrl current = games.get(i).getTimeCtrl();
                if (current == null) continue;
                if (current.isOfficial()) return current;
                if (best == null || current.compareTo(best) > 0) best = current;
            }

            return best;
        }
    }
    
    static class DuplicateExitProcessor implements ExitProcessor
    {
        private final DuplicateHandler printer;
        
        public DuplicateExitProcessor(DuplicateHandler printer)
        {
            this.printer = printer;
        }
        
        @Override public void process()
        {
            for (List<Integer> list : printer.getDuplicates())
            {
                for (Integer idx : list) System.out.print(idx + " ");
                System.out.print("\n");
            }
        }
    }
    
    static class OpeningsExitProcessor implements ExitProcessor
    {
        private final OpeningsHandler handler;
        
        public OpeningsExitProcessor(OpeningsHandler handler)
        {
            this.handler = handler;
        }
        
        @Override public void process()
        {
            Map<OpeningID,OpeningStats> statMap = handler.getMap();
            
            OpeningStatPrinter printer =
                OpeningStatPrinter.get(outputSelectors);

            nextOpening:
            for (OpeningID id : statMap.keySet())
            {
                OpeningStats stats = statMap.get(id);
                
                try
                {
                    for (OpeningProcessor processor : openingProcessors)
                        if (!processor.processOpening(stats))
                            continue nextOpening;

                    printer.print(stats);
                }
            
                catch (InvalidSelectorException | ProcessorException e)
                {
                    System.err.println("Exception: " + e.getMessage());
                    System.exit(-1);
                }
            }
        }
    }
    
    public static final String VERSION = "0.2";
    
    // All of these are static in order to avoid parameter-passing overhead.
    
    private static Game game;
    static PGNFile pgn;
    static final List<GameProcessor> matchProcessors = new ArrayList<>();
    static final List<GameProcessor> replaceProcessors = new ArrayList<>();
    static final List<OpeningProcessor> openingProcessors = new ArrayList<>();
    
    static Pattern playerPattern;
    static Map<String,Integer> eloMap;
    
    static GameHandler handler;
    static ExitProcessor exitProcessor;
    static OutputSelector outputSelectors[];
    
    static void addMatchProcessor(GameProcessor gp) { matchProcessors.add(gp); }
    static void addOpeningProcessor(OpeningProcessor op) { openingProcessors.add(op); }
    static void setHandler(GameHandler printer) { PGNUtil.handler = printer; }
    static void setExitProcessor(ExitProcessor proc) { exitProcessor = proc; }
    
    public static void main(String args[]) throws Exception
    {
        CLOptions options = new CLOptions();
        CmdLineParser parser = new CmdLineParser(options);
        exitProcessor = new NullExitProcessor();
        
        try
        {
            pgn = new PGNFile(new BufferedReader(new InputStreamReader(System.in)));
            parser.parseArgument(args);
            if (handler == null) handler = new DefaultGameHandler();
            if (options.help) throw new CmdLineException("");
        }
        
        catch (CmdLineException e)
        {
            System.err.println(e.getLocalizedMessage() + "\nUsage:");
            parser.printUsage(System.err);
            System.exit(-1);
        }
        
        catch (PatternSyntaxException e)
        {
            System.err.println("Regular expression error.  " +
                e.getLocalizedMessage());
            
            System.exit(-1);
        }
        
        try
        {
            nextGame:
            while ((game = pgn.nextGame()) != null)
            {
                for (GameProcessor processor : matchProcessors)
                    if (!processor.processGame()) continue nextGame;

                for (GameProcessor processor : replaceProcessors)
                    if (!processor.processGame()) break;

                handler.handle();
            }
            
            exitProcessor.process();
        }

        catch (InvalidSelectorException e)
        {
            System.err.println("Invalid selector: " + e.getMessage());
            System.exit(-1);
        }
    }
}
