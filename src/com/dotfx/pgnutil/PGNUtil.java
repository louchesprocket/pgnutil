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

import com.dotfx.pgnutil.eco.EcoTree;
import com.dotfx.pgnutil.eco.TreeNodeSet;
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
import java.util.stream.Collectors;

//import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 *
 * @author Mark Chen
 */
public class PGNUtil
{
    public interface GameProcessor
    {
        /**
         * 
         * @return true if we should continue processing this game, false if we
         *         should skip the game
         */
        public boolean processGame() throws ProcessorException;
    }
    
    // match processors
    
    static final class ContainsProcessor implements GameProcessor
    {
        private final Pattern matchPattern;
        
        public ContainsProcessor(Pattern p) { matchPattern = p; }
        @Override public boolean processGame()
        {
            return game.matches(matchPattern);
        }
    }
    
    static final class NotContainsProcessor implements GameProcessor
    {
        private final Pattern notMatchPattern;
        
        public NotContainsProcessor(Pattern p) { notMatchPattern = p; }
        @Override public boolean processGame()
        {
            return !game.matches(notMatchPattern);
        }
    }
    
    static final class MatchTagProcessor implements GameProcessor
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
                return tagPattern.matcher(value).find();
            }
            
            catch (InvalidSelectorException e) { return false; }
        }
    }
    
    static final class NotMatchTagProcessor implements GameProcessor
    {
        private final OutputSelector tag;
        private final Pattern tagPattern;
        
        public NotMatchTagProcessor(String tag, Pattern p)
        {
            this.tag = new OutputSelector(tag);
            tagPattern = p;
        }
        
        @Override public boolean processGame()
        {
            try
            {
                String value = game.get(tag);
                return !tagPattern.matcher(value).find();
            }
            
            catch (InvalidSelectorException e) { return false; }
        }
    }
    
    static final class MatchGameNumProcessor implements GameProcessor
    {
        private final List<SimpleEntry<Integer,Integer>> ranges;
        
        public MatchGameNumProcessor(String s) throws NumberFormatException
        {
            ranges = new ArrayList<>();
            
            for (String token : s.replaceAll("#.*", "").split(",\\W*"))
            { 
                String rangeBounds[] = token.split("-");
                Integer rangeStart = Integer.valueOf(rangeBounds[0].trim());

                ranges.add(new SimpleEntry<>(rangeStart, rangeBounds.length == 1 ?
                        rangeStart : Integer.valueOf(rangeBounds[1].trim())));
            }
        }
        
        @Override public boolean processGame()
        {
            int gameno = game.getNumber();
            
            for (SimpleEntry<Integer,Integer> range : ranges)
                if (gameno >= range.getKey() && gameno <= range.getValue()) return true;
            
            return false;
        }
    }
    
    static final class MatchAnyPlayerSetProcessor implements GameProcessor
    {
        private final Set<String> playerSet;
        
        public MatchAnyPlayerSetProcessor(Set<String> playerSet)
        {
            this.playerSet = playerSet;
        }
        
        @Override public boolean processGame()
        {
            return playerSet.contains(game.getWhite()) || playerSet.contains(game.getBlack());
        }
    }
    
    static final class MatchAllPlayerSetProcessor implements GameProcessor
    {
        private final Set<String> playerSet;
        
        public MatchAllPlayerSetProcessor(Set<String> playerSet)
        {
            this.playerSet = playerSet;
        }
        
        @Override public boolean processGame()
        {
            return playerSet.contains(game.getWhite()) && playerSet.contains(game.getBlack());
        }
    }
    
    static final class NotMatchPlayerSetProcessor implements GameProcessor
    {
        private final Set<String> playerSet;
        
        public NotMatchPlayerSetProcessor(Set<String> playerSet)
        {
            this.playerSet = playerSet;
        }
        
        @Override public boolean processGame()
        {
            return !playerSet.contains(game.getWhite()) && !playerSet.contains(game.getBlack());
        }
    }
    
    static final class MatchOpeningProcessor implements GameProcessor
    {
        private final Set<MoveListId> matchOpeningSet;
        
        public MatchOpeningProcessor(Set<MoveListId> openings)
        {
            matchOpeningSet = openings;
        }
        @Override public boolean processGame()
        {
            return matchOpeningSet.contains(game.openingId());
        }
    }
    
    static final class NotMatchOpeningProcessor implements GameProcessor
    {
        private final Set<MoveListId> notMatchOpeningSet;

        public NotMatchOpeningProcessor(Set<MoveListId> openings)
        {
            notMatchOpeningSet = openings;
        }
        @Override public boolean processGame() { return !notMatchOpeningSet.contains(game.openingId()); }
    }
    
    static final class MatchWinProcessor implements GameProcessor
    {
        private final Pattern matchWinPattern;
        
        public MatchWinProcessor(Pattern p) { matchWinPattern = p; }
        
        @Override public boolean processGame()
        {
            String winner = game.getWinner();
            return winner != null && matchWinPattern.matcher(winner).find();
        }
    }
    
    static final class MatchLossProcessor implements GameProcessor
    {
        private final Pattern matchLossPattern;
        
        public MatchLossProcessor(Pattern p) { matchLossPattern = p; }
        
        @Override public boolean processGame()
        {
            String loser = game.getLoser();
            return loser != null && matchLossPattern.matcher(loser).find();
        }
    }
    
    static final class MatchPlayerProcessor implements GameProcessor
    {
        private final Pattern p;

        public MatchPlayerProcessor(Pattern p) { this.p = p; }
        
        @Override public boolean processGame()
        {
            return p.matcher(game.getWhite()).find() || p.matcher(game.getBlack()).find();
        }
    }

    static final class ClockBelowProcessor implements GameProcessor
    {
        private final Clock c;

        public ClockBelowProcessor(Clock c) { this.c = c; }
        @Override public boolean processGame() { return game.getLowClock().compareTo(c) < 0; }
    }

    static final class ClockNotBelowProcessor implements GameProcessor
    {
        private final Clock c;

        public ClockNotBelowProcessor(Clock c) { this.c = c; }
        @Override public boolean processGame() { return game.getLowClock().compareTo(c) >= 0; }
    }

    static final class MinEloProcessor implements GameProcessor
    {
        private final Integer minElo;

        public MinEloProcessor(Integer minElo) { this.minElo = minElo; }

        @Override public boolean processGame()
        {
            Integer whiteElo = EloResolver.getWhiteElo(game);
            Integer blackElo = EloResolver.getBlackElo(game);
            return whiteElo != null && blackElo != null && whiteElo >= minElo && blackElo >= minElo;
        }
    }

    static final class MaxEloProcessor implements GameProcessor
    {
        private final Integer maxElo;

        public MaxEloProcessor(Integer maxElo) { this.maxElo = maxElo; }

        @Override public boolean processGame()
        {
            Integer whiteElo = EloResolver.getWhiteElo(game);
            Integer blackElo = EloResolver.getBlackElo(game);
            return whiteElo != null && blackElo != null && whiteElo <= maxElo && blackElo <= maxElo;
        }
    }

    static final class MaxEloDiffProcessor implements GameProcessor
    {
        private final Integer maxDiff;

        public MaxEloDiffProcessor(Integer maxDiff) { this.maxDiff = maxDiff; }

        @Override public boolean processGame()
        {
            Integer whiteElo = EloResolver.getWhiteElo(game);
            Integer blackElo = EloResolver.getBlackElo(game);
            return whiteElo != null && blackElo != null && Math.abs(whiteElo - blackElo) <= maxDiff;
        }
    }

    static final class MinEloDiffProcessor implements GameProcessor
    {
        private final Integer minDiff;

        public MinEloDiffProcessor(Integer minDiff) { this.minDiff = minDiff; }

        @Override public boolean processGame()
        {
            Integer whiteElo = EloResolver.getWhiteElo(game);
            Integer blackElo = EloResolver.getBlackElo(game);
            return whiteElo != null && blackElo != null && Math.abs(whiteElo - blackElo) >= minDiff;
        }
    }
    
    static final class MatchTimeCtrlProcessor implements GameProcessor
    {
        private final TimeCtrl matchTimeCtrl;
        
        public MatchTimeCtrlProcessor(String s) throws InvalidTimeCtrlException
        {
            matchTimeCtrl = new TimeCtrl(s, false);
        }
        
        @Override public boolean processGame()
        {
            TimeCtrl timeCtrl = game.getTimeCtrl();
            return timeCtrl != null && timeCtrl.equals(matchTimeCtrl);
        }
    }
    
    static final class MinPlyCountProcessor implements GameProcessor
    {
        private final int plies;
        
        public MinPlyCountProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            return game.getPlyCount() >= plies;
        }
    }
    
    static final class MaxPlyCountProcessor implements GameProcessor
    {
        private final int plies;
        
        public MaxPlyCountProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            return game.getPlyCount() <= plies;
        }
    }
    
    static final class MinOobProcessor implements GameProcessor
    {
        private final int plies;
        
        MinOobProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            if (plies < 1) return true;
            PgnGame.Move firstOobMove = game.getFirstOobMove();
            return firstOobMove != null && game.getPlyCount() - firstOobMove.getPly() >= plies;
        }
    }
    
    static final class MaxOobProcessor implements GameProcessor
    {
        private final int plies;
        
        MaxOobProcessor(int plies) { this.plies = plies; }
        
        @Override public boolean processGame()
        {
            PgnGame.Move firstOobMove = game.getFirstOobMove();
            // if all book moves, return true
            return firstOobMove == null || game.getPlyCount() - firstOobMove.getPly() <= plies;
        }
    }

    static final class MatchPositionSetProcessor implements GameProcessor
    {
        private final Set<LooseBoard> positionSet;
        private final int minWhitePieces, minBlackPieces;

        public MatchPositionSetProcessor(Set<LooseBoard> positionSet)
        {
            this.positionSet = positionSet;
            minWhitePieces = positionSet.stream().mapToInt(LooseBoard::getWhitePieceCount).min().orElse(0);
            minBlackPieces = positionSet.stream().mapToInt(LooseBoard::getBlackPieceCount).min().orElse(0);
        }

        @Override public boolean processGame()
        {
            try { return game.containsPosition(positionSet, minWhitePieces, minBlackPieces); }

            catch (IllegalMoveException | StringIndexOutOfBoundsException | NullPointerException e)
            {
                System.err.println("PGN error in game #" + game.getNumber() + ": " + e.getMessage());
                return false;
            }
        }
    }
    
    static final class MatchEcoProcessor implements GameProcessor
    {
        private final EcoTree ecoTree;
        private final Pattern ecoMatcher;
        
        MatchEcoProcessor(Pattern ecoMatcher, EcoTree.FileType treeType)
        {
            ecoTree = treeType.getEcoTree();
            this.ecoMatcher = ecoMatcher;
        }
        
        @Override public boolean processGame()
        {
            return ecoMatcher.matcher(ecoTree.getDeepestDefined(game).getCode()).find();
        }
    }
    
    static final class MatchEcoDescProcessor implements GameProcessor
    {
        private final EcoTree ecoTree;
        private final Pattern ecoMatcher;
        
        MatchEcoDescProcessor(Pattern ecoMatcher, EcoTree.FileType treeType)
        {
            ecoTree = treeType.getEcoTree();
            this.ecoMatcher = ecoMatcher;
        }
        
        @Override public boolean processGame()
        {
            return ecoMatcher.matcher(ecoTree.getDeepestDefined(game).getDesc()).find();
        }
    }
    
    static final class MatchXEcoProcessor implements GameProcessor
    {
        private final EcoTree ecoTree;
        private final Pattern ecoMatcher;
        
        MatchXEcoProcessor(Pattern ecoMatcher, EcoTree.FileType treeType)
        {
            ecoTree = treeType.getEcoTree();
            this.ecoMatcher = ecoMatcher;
        }
        
        @Override public boolean processGame()
        {
            TreeNodeSet treeNodeSet;
            
            try { treeNodeSet = ecoTree.getDeepestTranspositionSet(game); }
            
            catch (IllegalMoveException e)
            {
                System.err.println(e);
                return false;
            }
            
            return ecoMatcher.matcher(treeNodeSet.toString()).find();
        }
    }
    
    static final class MatchXEcoDescProcessor implements GameProcessor
    {
        private final EcoTree ecoTree;
        private final Pattern ecoMatcher;
        
        MatchXEcoDescProcessor(Pattern ecoMatcher, EcoTree.FileType treeType)
        {
            ecoTree = treeType.getEcoTree();
            this.ecoMatcher = ecoMatcher;
        }
        
        @Override public boolean processGame()
        {
            TreeNodeSet treeNodeSet;
            
            try { treeNodeSet = ecoTree.getDeepestTranspositionSet(game); }
            
            catch (IllegalMoveException e)
            {
                System.err.println(e);
                return false;
            }
            
            return ecoMatcher.matcher(treeNodeSet.getDesc()).find();
        }
    }
    
    // replacement processors
    
    /**
     * An instance of this must be the last processor in the replaceProcessors list.
     */
    static final class ReplaceProcessor implements GameProcessor
    {
        private final Pattern replacePattern; // to be replaced
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
                return true;
            }
            
            catch (IOException | PGNException e) { throw new ProcessorException(e); }
        }
    }
    
    static final class ReplaceContainsProcessor implements GameProcessor
    {
        private final Pattern replacePattern1; // eligibility pattern
        
        public ReplaceContainsProcessor(Pattern p) { replacePattern1 = p; }
        @Override public boolean processGame() { return game.matches(replacePattern1); }
    }
    
    static final class ReplaceWinProcessor implements GameProcessor
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
    
    static final class ReplaceLossProcessor implements GameProcessor
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
    
    static final class ReplaceOpeningProcessor implements GameProcessor
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
            return replaceOpeningSet.contains(game.openingId());
        }
    }
    
    // game handlers
    
    interface GameHandler
    {
        default void init() throws InvalidSelectorException {}
        void handle() throws InvalidSelectorException, IllegalMoveException;
    }
    
    private static final class DefaultGameHandler implements GameHandler
    {
        @Override public void handle() throws InvalidSelectorException
        {
            System.out.print(game.getOrigText());
        }
    }
    
    static final class SelectGameHandler implements GameHandler
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
        
        final void handle(HashCode hash)
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
            SortedSet<SortedSet<Integer>> ret = new TreeSet<>(Comparator.comparingInt(SortedSet::first));
            for (HashCode duplicate : duplicates) ret.add(gameMap.get(duplicate));
            return ret;
        }
    }
    
    static final class DuplicateGameHandler extends DuplicateHandler
    {
        private final int plies;

        public DuplicateGameHandler(int plies) { this.plies = plies; }
        @Override public void handle() throws InvalidSelectorException
        {
            super.handle(game.getHash(plies));
        }
    }

    static final class DuplicateMoveHandler extends DuplicateHandler
    {
        private final int plies;

        public DuplicateMoveHandler(int plies) { this.plies = plies; }
        @Override public void handle() throws InvalidSelectorException
        {
            super.handle(game.getMoveHash(plies));
        }
    }
    
    static final class DuplicateOpeningHandler extends DuplicateHandler
    {
        @Override public void handle() throws InvalidSelectorException
        {
            super.handle(game.getPlayerOpeningHash());
        }
    }
    
    static final class TallyHandler implements GameHandler
    {
        private final Tallier tallier;
        
        TallyHandler(Tallier tallier) { this.tallier = tallier; }
        @Override public void init() throws InvalidSelectorException { tallier.init(outputSelectors); }
        @Override public void handle() throws IllegalMoveException { tallier.tally(game); }
    }
    
    // exit processors
    
    private interface ExitProcessor { void process(); }
    
    private static final class NullExitProcessor implements ExitProcessor
    {
        @Override public void process() {}
    }
    
    static final class DuplicateExitProcessor implements ExitProcessor
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
                while (iter.hasNext()) System.out.print(CLOptions.valueDelim + iter.next());
                System.out.print("\n");
            }
        }
    }
    
    static final class TallyExitProcessor implements ExitProcessor
    {
        private final Tallier iteratorProvider;
        
        TallyExitProcessor(Tallier iteratorProvider) { this.iteratorProvider = iteratorProvider; }
        
        @Override public void process()
        {
            try
            {
                Iterator<String> iter = iteratorProvider.getOutputIterator();
                while (iter.hasNext()) System.out.println(iter.next());
            }
            
            catch (InvalidSelectorException e)
            {
                System.err.println("Exception: " + e.getMessage());
                System.exit(-1);
            }
        }
    }
    
    public static final String VERSION = "1.1";

    private static PgnGame game;
    private static final List<PgnFile> pgnFileList = new ArrayList<>();

    private static final List<GameProcessor> matchProcessors = new ArrayList<>();
    private static final List<GameProcessor> replaceProcessors = new ArrayList<>();
    private static GameHandler handler;
    private static ExitProcessor exitProcessor;

    static OutputSelector outputSelectors[];
    
    private static long gamesRead = 0L;
    private static long charsRead = 0L;

    static void addInputFile(PgnFile f) { pgnFileList.add(f); }
    static void addMatchProcessor(GameProcessor gp) { matchProcessors.add(gp); }
    static void addReplaceProcessor(GameProcessor gp) { replaceProcessors.add(gp); }
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
            parser.parseArgument(args);
            if (CLOptions.help) throw new CmdLineException("");
            CLOptionResolver.resolveOpts(CLOptions.getSetOpts());
            
            if (pgnFileList.isEmpty())
                pgnFileList.add(new PgnFile(new BufferedReader(new InputStreamReader(System.in))));
            
            if (handler == null) handler = new DefaultGameHandler();
            handler.init();
        }
        
        catch (CmdLineException e)
        {
            System.err.println(e.getLocalizedMessage() + "\nUsage:");
            parser.printUsage(System.err);
            System.exit(-1);
        }
        
        catch (PatternSyntaxException e)
        {
            System.err.println("regular expression error.  " + e.getLocalizedMessage());
            System.exit(-1);
        }
        
        catch (InvalidSelectorException e)
        {
            System.err.println("invalid selector: " + e.getLocalizedMessage());
            System.exit(-1);
        }
        
        catch (IOException e)
        {
            System.err.println("i/o exception; failed to read input: " + e.getLocalizedMessage());
            System.exit(-1);
        }
        
        if (CLOptions.performance) startTime = System.currentTimeMillis();
        
        try
        {
            for (PgnFile pgn : pgnFileList)
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
                
                gamesRead += pgn.getGamesRead();
                charsRead += pgn.getTotalCharsRead();
            }
            
            exitProcessor.process();
        }

        catch (InvalidSelectorException | IllegalMoveException | PGNException e)
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }

        catch (IOException | ProcessorException e)
        {
            System.err.println("exception: " + e.getLocalizedMessage());
            System.exit(-1);
        }
        
        if (CLOptions.performance)
            System.err.println("processed " + gamesRead + " games (" + charsRead + " chars) in " +
                (System.currentTimeMillis() - startTime) + "ms");
    }
}
