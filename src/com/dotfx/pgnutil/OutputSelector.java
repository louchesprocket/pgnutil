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

import com.dotfx.pgnutil.eco.EcoTree;
import com.dotfx.pgnutil.eco.TreeNodeSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Mark Chen
 */
public final class OutputSelector
{
    public interface OutputHandler
    {
        void appendOutput(PgnGame game, StringBuilder sb) throws SelectorException;
    }

    private final class DefaultOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getValue(literal));
        }
    }

    private static final class TagsOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getTags());
        }
    }

    private static final class RoundOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getRound());
        }
    }

    private static final class MoveListOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            for (PgnGame.Move move : game.getMoveList())
            {
                if (move.isWhite()) sb.append(move.getNumber()).append(".");
                sb.append(move.getMove()).append(" ");
            }

            sb.deleteCharAt(sb.length() - 1);
        }
    }

    private static final class DecoratedMovesOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            for (PgnGame.Move move : game.getMoveList())
            {
                if (move.getColor() == Color.WHITE) sb.append(move.getNumber()).append(".");
                sb.append(move.getMove());
                for (String comment : move.getComments()) sb.append(" {").append(comment).append("}");
                sb.append(" ");
            }

            sb.deleteCharAt(sb.length() - 1);
        }
    }

    public static final class PlayerOutputHandler implements OutputHandler
    {
        private final Pattern playerPattern;

        public PlayerOutputHandler(Pattern playerPattern) { this.playerPattern = playerPattern; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            if (playerPattern.matcher(game.getWhite()).find()) sb.append(game.getWhite());
            else sb.append(game.getBlack());
        }
    }

    public static final class OpponentOutputHandler implements OutputHandler
    {
        private final Pattern playerPattern;

        public OpponentOutputHandler(Pattern playerPattern) { this.playerPattern = playerPattern; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            if (playerPattern.matcher(game.getWhite()).find()) sb.append(game.getBlack());
            else sb.append(game.getWhite());
        }
    }

    public static final class PlayerEloOutputHandler implements OutputHandler
    {
        private final Pattern playerPattern;

        public PlayerEloOutputHandler(Pattern playerPattern) { this.playerPattern = playerPattern; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            if (playerPattern.matcher(game.getWhite()).find()) sb.append(game.getValue("WhiteElo"));
            else sb.append(game.getValue("BlackElo"));
        }
    }

    public static final class OpponentEloOutputHandler implements OutputHandler
    {
        private final Pattern playerPattern;

        public OpponentEloOutputHandler(Pattern playerPattern) { this.playerPattern = playerPattern; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            if (playerPattern.matcher(game.getWhite()).find()) sb.append(game.getValue("BlackElo"));
            else sb.append(game.getValue("WhiteElo"));
        }
    }

    private static final class OpeningFenOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            try { sb.append(game.getOpeningFen()); }

            catch (IllegalMoveException e)
            {
                System.err.println("in game " + game.getNumber() + ": " + e.getMessage());
            }
        }
    }

    private static final class OpeningIdOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            try { sb.append(game.getOpid()); }

            catch (IllegalMoveException e)
            {
                System.err.println("in game " + game.getNumber() + ": " + e.getMessage());
            }
        }
    }

    // Aquarium only
    static final class LowClockOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getLowClock());
        }
    }

    // Aquarium only
    static final class LowClockWhiteOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getLowClockWhite());
        }
    }

    // Aquarium only
    static final class LowClockBlackOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getLowClockBlack());
        }
    }

    // Aquarium only
    static final class LowClockPlayersOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            List<String> lowClockPlayers = game.getLowClockPlayers();
            sb.append(lowClockPlayers.get(0));
            if (lowClockPlayers.size() > 1) sb.append(CLOptions.valueDelim).append(lowClockPlayers.get(1));
        }
    }

    // Aquarium only
    static final class ClockBelowPlayersOutputHandler implements OutputHandler
    {
        private final Clock clock;

        public ClockBelowPlayersOutputHandler(Clock clock) { this.clock = clock; }
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            boolean whiteAdded = false;

            if (game.getLowClockWhite().compareTo(clock) < 0)
            {
                sb.append(game.getWhite());
                whiteAdded = true;
            }

            if (game.getLowClockBlack().compareTo(clock) < 0)
            {
                if (whiteAdded) sb.append(CLOptions.valueDelim);
                sb.append(game.getBlack());
            }
        }
    }

    private static final class EcoOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private EcoOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(type.getEcoTree().getDeepestDefined(game).getCode());
        }
    }

    private static final class EcoDescOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private EcoDescOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(type.getEcoTree().getDeepestDefined(game).getDesc());
        }
    }

    private static final class EcoMovesOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private EcoMovesOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb) throws SelectorException
        {
            sb.append(new TreeNodeSet(type.getEcoTree().getDeepestDefined(game)).getMoveString());
        }
    }

    private static final class XEcoOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            try { sb.append(game.getXEcoNodeSet(type)); }

            catch (IllegalMoveException e)
            {
                System.err.println("in game " + game.getNumber() + ": " + e.getMessage());
            }
        }
    }

    private static final class XEcoDescOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoDescOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            try { sb.append(game.getXEcoNodeSet(type).getDesc()); }

            catch (IllegalMoveException e)
            {
                System.err.println("in game " + game.getNumber() + ": " + e.getMessage());
            }
        }
    }

    private static final class XEcoMovesOutputHandler implements OutputHandler
    {
        private final EcoTree.FileType type;

        private XEcoMovesOutputHandler(EcoTree.FileType type) { this.type = type; }

        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            try { sb.append(game.getXEcoNodeSet(type).getMoveString()); }

            catch (IllegalMoveException e)
            {
                System.err.println("in game " + game.getNumber() + ": " + e.getMessage());
            }
        }
    }

    private static final class FileNameOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getFileName());
        }
    }

    private static final class GameNumOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getNumber());
        }
    }

    private static final class OpeningMovesOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getFullOpeningString());
        }
    }

    private static final class OidOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.openingId());
        }
    }

    private static final class PliesOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getMoveList().size());
        }
    }

    private static final class WinnerOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getWinner());
        }
    }

    private static final class LoserOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getLoser());
        }
    }

    private static final class TextSizeOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getOrigText().length());
        }
    }

    private static final class TimeCtrlOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(game.getTimeCtrl());
        }
    }

    private static final class DisagreePctOutputHandler implements OutputHandler
    {
        @Override
        public void appendOutput(PgnGame game, StringBuilder sb)
        {
            sb.append(Formats.PERCENT.format(game.getDisagreeRatio()));
        }
    }

    public static enum Value
    {
        // standard seven tags
        BLACK("black", null),
        DATE("date", null),
        EVENT("event", null),
        RESULT("result", null),
        ROUND("round", new RoundOutputHandler()), // to handle Aquarium bugs, if needed
        SITE("site", null),
        WHITE("white", null),
        
        // Aquarium
        BLACKELO("blackelo", null),
        CLASSES("classes", null),
        TIMECONTROL("timecontrol", null),
        WHITEELO("whiteelo", null),
        
        // Arena extras
        BLACKTYPE("blacktype", null),
        PLYCOUNT("plycount", null),
        TERMINATION("termination", null),
        TIME("time", null),
        VARIATION("variation", null),
        WHITETYPE("whitetype", null),
        
        // special

        AVGPLIES("avgplies", null),
        CBPLAYERS("cbplayers", null), // Aquarium only. Handler set in CLOptionResolver.ClockBelowHandler
        DISAGREEPCT("disagreepct", new DisagreePctOutputHandler()),
        FILENAME("filename", new FileNameOutputHandler()),
        GAMENO("gameno", new GameNumOutputHandler()),
        LOWCLOCK("lowclock", new LowClockOutputHandler()), // Aquarium only
        LOWCLOCKBLACK("lowclockblack", new LowClockBlackOutputHandler()), // Aquarium only
        LOWCLOCKPLAYERS("lowclockplayers", new LowClockPlayersOutputHandler()), // Aquarium only
        LOWCLOCKWHITE("lowclockwhite", new LowClockWhiteOutputHandler()), // Aquarium only
        TAGS("tags", new TagsOutputHandler()),
        TEXTSIZE("textsize", new TextSizeOutputHandler()),
        TIMECTRL("timectrl", new TimeCtrlOutputHandler()), // Aquarium only. Infers time control, if tag not present
        WINNER("winner", new WinnerOutputHandler()),
        LOSER("loser", new LoserOutputHandler()),
        MOVES("moves", new MoveListOutputHandler()),
        DECORATEDMOVES("decoratedmoves", new DecoratedMovesOutputHandler()),
        OID("oid", new OidOutputHandler()), // opening move list identifier
        OPENINGMOVES("openingmoves", new OpeningMovesOutputHandler()),
        OPENINGFEN("openingfen", new OpeningFenOutputHandler()),
        OPID("opid", new OpeningIdOutputHandler()), // opening position identifier
        OPPONENT("opponent", null), // handler set in CLOptionResolver.PlayerHandler
        OPPONENTELO("opponentelo", null), // handler set in CLOptionResolver.PlayerHandler
        PLAYERELO("playerelo", null),
        PLIES("plies", new PliesOutputHandler()),
        STDECO("stdeco", new EcoOutputHandler(EcoTree.FileType.STD)),
        STDECODESC("stdecodesc", new EcoDescOutputHandler(EcoTree.FileType.STD)),
        STDECOMOVES("stdecomoves", new EcoMovesOutputHandler(EcoTree.FileType.STD)),
        SCIDECO("scideco", new EcoOutputHandler(EcoTree.FileType.SCIDDB)),
        SCIDECODESC("scidecodesc", new EcoDescOutputHandler(EcoTree.FileType.SCIDDB)),
        SCIDECOMOVES("scidecomoves", new EcoMovesOutputHandler(EcoTree.FileType.SCIDDB)),
        XSTDECO("xstdeco", new XEcoOutputHandler(EcoTree.FileType.STD)),
        XSTDECODESC("xstdecodesc", new XEcoDescOutputHandler(EcoTree.FileType.STD)),
        XSTDECOMOVES("xstdecomoves", new XEcoMovesOutputHandler(EcoTree.FileType.STD)),
        XSCIDECO("xscideco", new XEcoOutputHandler(EcoTree.FileType.SCIDDB)),
        XSCIDECODESC("xscidecodesc", new XEcoDescOutputHandler(EcoTree.FileType.SCIDDB)),
        XSCIDECOMOVES("xscidecomoves", new XEcoMovesOutputHandler(EcoTree.FileType.SCIDDB)),
        
        // additional opening-stat selectors
        BWINPCT("bwinpct", null),
        BWINS("bwins", null),
        COUNT("count", null), // also applies to player results
        DIFF("diff", null),
        DIFFPCT("diffpct", null),
        DRAWPCT("drawpct", null),
        DRAWS("draws", null), // also applies to player results
        WWINPCT("wwinpct", null),
        WWINS("wwins", null),

        // additional ECO-stat selectors
        ECO("eco", null),
        ECODESC("ecodesc", null),
        ECOMOVES("ecomoves", null),
        
        // additional event selectors
        LASTROUND("lastround", null),
        ROUNDCOUNT("roundcount", null),
        
        // additional player-results selectors
        PLAYER("player", null),
        WINS("wins", null),
        LOSSES("losses", null),
        NORESULTS("noresults", null),
        WINPCT("winpct", null),

        OTHER(null, null);
        
        private static final Map<String,Value> sigMap = new HashMap<>();
        private final String signifier;
        private final OutputHandler outputHandler;
        
        static
        {
            for (Value v : Value.values()) sigMap.put(v.toString(), v);
        }
        
        Value(String signifier, OutputHandler outputHandler)
        {
            this.signifier = signifier;
            this.outputHandler = outputHandler;
        }

        @Override public String toString() { return signifier; }
        
        public static Value get(String signifier)
        {
            if (signifier == null) return null;
            return sigMap.get(signifier.toLowerCase());
        }

        public OutputHandler getOutputHandler() { return outputHandler; }
    }

    public static final OutputSelector TIMECONTROL = new OutputSelector("TimeControl");
    
    private final Value value;
    private final String literal;
    private OutputHandler handler;
    
    public OutputSelector(String literal)
    {
        value = (Value.get(literal) != null ? Value.get(literal) : Value.OTHER);

        // If the selector is not on the "special" list, then assume that it is a literal tag.
        handler = (value.getOutputHandler() == null ? new DefaultOutputHandler() : value.getOutputHandler());
        this.literal = literal;
    }

    public void setOutputHandler(OutputHandler handler) { this.handler = handler; }
    public Value getValue() { return value; }

    public void appendOutput(PgnGame game, StringBuilder sb) throws SelectorException
    {
        handler.appendOutput(game, sb);
    }
    
    @Override
    public String toString() { return literal; }
}
