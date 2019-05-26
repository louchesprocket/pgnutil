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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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

                ranges.add(new SimpleEntry(rangeStart,
                    rangeBounds.length == 1 ?
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
    
    static class MatchAnyPlayerSetProcessor implements GameProcessor
    {
        private final Set<String> playerSet;
        
        public MatchAnyPlayerSetProcessor(Set<String> playerSet)
        {
            this.playerSet = playerSet;
        }
        
        @Override public boolean processGame()
        {
            return playerSet.contains(game.getWhite()) ||
                playerSet.contains(game.getBlack());
        }
    }
    
    static class MatchAllPlayerSetProcessor implements GameProcessor
    {
        private final Set<String> playerSet;
        
        public MatchAllPlayerSetProcessor(Set<String> playerSet)
        {
            this.playerSet = playerSet;
        }
        
        @Override public boolean processGame()
        {
            return playerSet.contains(game.getWhite()) &&
                playerSet.contains(game.getBlack());
        }
    }
    
    static class NotMatchPlayerSetProcessor implements GameProcessor
    {
        private final Set<String> playerSet;
        
        public NotMatchPlayerSetProcessor(Set<String> playerSet)
        {
            this.playerSet = playerSet;
        }
        
        @Override public boolean processGame()
        {
            return !playerSet.contains(game.getWhite()) &&
                !playerSet.contains(game.getBlack());
        }
    }
    
    static class MatchOpeningProcessor implements GameProcessor
    {
        private final Set<MoveListId> matchOpeningSet;
        
        public MatchOpeningProcessor(Set<MoveListId> openings)
        {
            matchOpeningSet = openings;
        }
        
        @Override public boolean processGame()
        {
            return matchOpeningSet.contains(game.getOpeningID());
        }
    }
    
    static class NotMatchOpeningProcessor extends MatchOpeningProcessor
    {
        public NotMatchOpeningProcessor(Set<MoveListId> openings)
        {
            super(openings);
        }
        
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
        {
            PGNUtil.playerPattern = playerPattern = p;
        }
        
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
    
    static class MinPlyCountProcessor implements GameProcessor
    {
        private final int plies;
        
        public MinPlyCountProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            return game.getPlyCount() >= plies;
        }
    }
    
    static class MaxPlyCountProcessor implements GameProcessor
    {
        private final int plies;
        
        public MaxPlyCountProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            return game.getPlyCount() <= plies;
        }
    }
    
    static class MinOobProcessor implements GameProcessor
    {
        private final int plies;
        
        MinOobProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            return game.getPlyCount() - game.getFirstOobMove().getPly() >=
                plies;
        }
    }
    
    static class MaxOobProcessor implements GameProcessor
    {
        private final int plies;
        
        MaxOobProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            return game.getPlyCount() - game.getFirstOobMove().getPly() <=
                plies;
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
        private final Set<MoveListId> replaceOpeningSet;
        
        public ReplaceOpeningProcessor(String s)
        {
            replaceOpeningSet = new HashSet<>();
            
            for (String token : s.split(",\\W*"))
            {
                try { replaceOpeningSet.add(MoveListId.fromString(token)); }
                
                catch (IllegalArgumentException e)
                {
                    System.err.println("invalid opening id: '" + token + "'");
                    System.exit(-1);
                }
            }
        }
        
        @Override public boolean processGame()
        {
            return replaceOpeningSet.contains(game.getOpeningID());
        }
    }
    
    // game handlers
    
    static interface GameHandler
    {
        public void handle() throws InvalidSelectorException;
    }
    
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
    
    static abstract class DuplicateHandler implements GameHandler
    {
        private final Map<HashCode,SortedSet<Integer>> gameMap;
        private final Set<HashCode> duplicates;
        
        DuplicateHandler()
        {
            gameMap = new HashMap(100000);
            duplicates = new HashSet();
        }
        
        final void handle(HashCode hash) throws InvalidSelectorException
        {
            SortedSet<Integer> gameIdxes = gameMap.get(hash);
            if (gameIdxes != null) duplicates.add(hash);

            else
            {
                gameIdxes = new TreeSet<>();
                gameMap.put(hash, gameIdxes);
            }

            gameIdxes.add((int)game.getNumber());
        }
        
        final SortedSet<SortedSet<Integer>> getDuplicates()
        {
            SortedSet<SortedSet<Integer>> ret = new TreeSet<>
            (
                new Comparator<SortedSet<Integer>>()
                {
                    @Override
                    public int compare(SortedSet<Integer> s1, SortedSet<Integer> s2)
                    {
                        return s1.first() - s2.first();
                    }
                }
            );
            
            for (HashCode duplicate : duplicates) ret.add(gameMap.get(duplicate));
            return ret;
        }
    }
    
    static class DuplicateGameHandler extends DuplicateHandler
    {
        @Override public void handle() throws InvalidSelectorException
        {
            super.handle(game.getHash());
        }
    }
    
    static class DuplicateOpeningHandler extends DuplicateHandler
    {
        @Override public void handle() throws InvalidSelectorException
        {
            super.handle(game.getPlayerOpeningHash());
        }
    }
    
    static class DuplicatePostOpeningHandler extends DuplicateHandler
    {
        @Override public void handle() throws InvalidSelectorException
        {
            super.handle(game.getPlayerPostOpeningHash());
        }
    }
    
    static class TallyHandler implements GameHandler
    {
        private final Tallier tallier;
        
        TallyHandler(Tallier tallier) { this.tallier = tallier; }
        
        @Override public void handle() throws InvalidSelectorException
        {
            tallier.tally(game);
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
    
    static class DuplicateExitProcessor implements ExitProcessor
    {
        private final DuplicateHandler printer;
        
        public DuplicateExitProcessor(DuplicateHandler printer)
        {
            this.printer = printer;
        }
        
        @Override public void process()
        {
            for (SortedSet<Integer> list : printer.getDuplicates())
            {
                Iterator<Integer> iter = list.iterator();
                System.out.print(iter.next());
                
                while (iter.hasNext())
                    System.out.print(CLOptions.valueDelim + iter.next());
                
                System.out.print("\n");
            }
        }
    }
    
    static class TallyExitProcessor implements ExitProcessor
    {
        private final Tallier iteratorProvider;
        
        TallyExitProcessor(Tallier iteratorProvider)
        {
            this.iteratorProvider = iteratorProvider;
        }
        
        @Override public void process()
        {
            try
            {
                Iterator<String> iter =
                    iteratorProvider.getOutputIterator(outputSelectors);
                
                while (iter.hasNext()) System.out.println(iter.next());
            }
            
            catch (InvalidSelectorException e)
            {
                System.err.println("Exception: " + e.getMessage());
                System.exit(-1);
            }
        }
    }
    
    public static final String VERSION = "0.3";
    
    // All of these are static in order to avoid parameter-passing overhead.
    
    private static Game game;
    static PGNFile pgn;
    static final List<GameProcessor> matchProcessors = new ArrayList<>();
    static final List<GameProcessor> replaceProcessors = new ArrayList<>();
    
    static Pattern playerPattern;
    static Map<String,Integer> eloMap;
    
    static GameHandler handler;
    static ExitProcessor exitProcessor;
    static OutputSelector outputSelectors[];
    
    static void addMatchProcessor(GameProcessor gp) { matchProcessors.add(gp); }
    static void setHandler(GameHandler printer) { PGNUtil.handler = printer; }
    static void setExitProcessor(ExitProcessor proc) { exitProcessor = proc; }
    
    public static void main(String args[])
    {
        CLOptions options = new CLOptions();
        CmdLineParser parser = new CmdLineParser(options);
        exitProcessor = new NullExitProcessor();
        long startTime = 0L;
        
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
            System.err.println("regular expression error.  " +
                e.getLocalizedMessage());
            
            System.exit(-1);
        }
        
        catch (IOException e)
        {
            System.err.println("i/o exception; failed to read input: " +
                e.getLocalizedMessage());
            
            System.exit(-1);
        }
        
        if (CLOptions.performance) startTime = System.currentTimeMillis();
        
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
            System.err.println("invalid selector: " + e.getLocalizedMessage());
            System.exit(-1);
        }

        catch (PGNException e)
        {
            System.err.println("PGN parsing exception: " +
                e.getLocalizedMessage());
            
            System.exit(-1);
        }

        catch (IOException | ProcessorException e)
        {
            System.err.println("exception: " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        
        if (CLOptions.performance)
            System.err.println("processed " + pgn.getGamesRead() +
                " games (" + pgn.getTotalBytesRead() + " chars) in " +
                (System.currentTimeMillis() - startTime) + "ms");
    }
}
