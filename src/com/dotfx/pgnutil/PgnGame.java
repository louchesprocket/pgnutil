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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.dotfx.pgnutil.eco.EcoTree;
import com.dotfx.pgnutil.eco.TreeNodeSet;
//import com.google.common.hash.HashCode;
//import com.google.common.hash.HashFunction;
//import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * @author Mark Chen
 */
public final class PgnGame
{
    public static final String ROUND_TAG;
    private static final int BUFSIZE = 1024;
    private static final int COMMENT_BUFSIZE = 1024;
    private static Pattern bookMarker;

    public enum Result
    {
        WHITEWIN("1-0"),
        BLACKWIN("0-1"),
        DRAW("1/2-1/2"),
        NORESULT("*");
        
        private static final Map<String,Result> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (Result r : Result.values()) sigMap.put(r.toString(), r);
        }
        
        private Result(String signifier) { this.signifier = signifier; }
        @Override public String toString() { return signifier; }
        
        public static Result get(String signifier)
        {
            if (signifier == null) return null;
            return sigMap.get(signifier);
        }
    }
    
    /**
     * One per ply.
     */
    public static final class Move implements Comparable<Move>
    {
        private final Color color;
        private final short number; // not ply
        private final String move;
        private final List<String> comments;
        
        public Move(Color color, short number, String move, List<String> comments)
        {
            this.color = color;
            this.number = number;
            this.move = move;
            this.comments = new ArrayList<>();
            
            for (String comment : comments)
                this.comments.add(comment.replaceAll("[\n\r]", " "));
        }
        
        public Color getColor() { return color; }
        public int getNumber() { return number; }
        public String getMove() { return move; }
        public List<String> getComments() { return comments; }
        public AquariumVars getAquariumVars() { return new AquariumVars(this); }
        
        public static String getMoveOnly(String san) // strips extraneous chars
        {
            int end = san.length();
            while (end > 0 && !Character.isLetterOrDigit(san.charAt(end - 1))) end--;
            return san.substring(0, end);
        }
        
        public String getMoveOnly() // strips extraneous chars
        {
            int end = move.length();
            while (end > 0 && !Character.isLetterOrDigit(move.charAt(end - 1))) end--;
            return move.substring(0, end);
        }
        
        public int getPly()
        {
            if (color == Color.WHITE) return (number - 1) * 2 + 1;
            return number * 2;
        }
        
        public boolean hasComment(Pattern regex)
        {
            for (String comment : comments)
                if (regex.matcher(comment).find()) return true;
            
            return false;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append(number).append(".");
            if (color.equals(Color.BLACK)) sb.append("..");
            sb.append(" ").append(move);
            if (!comments.isEmpty()) sb.append(" ");
            for (String comment : comments) sb.append("{").append(comment).append("}");
            
            return sb.toString();
        }

        @Override
        public int compareTo(Move other)
        {
            if (number == other.number)
            {
                if (color == other.color) return 0;
                if (color == Color.WHITE) return -1;
                return 1;
            }

            return number - other.number;
        }

        @Override
        public boolean equals(Object other)
        {
            return compareTo((Move)other) == 0;
        }
    }

    public static class Locator implements Comparable<Locator>
    {
        private final String fileName;
        private final int gameNo;

        private Locator(String fileName, int gameNo)
        {
            this.fileName = fileName;
            this.gameNo = gameNo;
        }

        public int getGameNo() { return gameNo; }

        @Override
        public String toString() { return (fileName.isEmpty() ? "" : fileName + ":") + gameNo; }

        @Override
        public int compareTo(Locator other)
        {
            int comp;
            if ((comp = fileName.compareTo(other.fileName)) == 0) return gameNo - other.gameNo;
            return comp;
        }

        @Override
        public boolean equals(Object other) { return compareTo((Locator)other) == 0; }
    }
    
    static
    {
        ROUND_TAG = "Round";

        // Aquarium: "[Black|White] out of book"
        // Banksia: "End of opening"
        bookMarker = Pattern.compile("(out\\s+of\\s+book)|(^End\\s+of\\s+opening)", Pattern.DOTALL);
    }

    private final String fileName;
    private final int number;
    private final CaseInsensitiveMap<String,String> tagPairs;
    private final List<String> gameComments;
    private final List<Move> moves;
    private final String origText;

    // cached values for internal use
    private transient List<Move> openingMoveList;
    private transient String openingString;

    private transient Clock lowClockWhite, lowClockBlack;
    private transient final Map<EcoTree.FileType,TreeNodeSet> xEcoCacheMap; // transposed ECO TreeNodeSets
    
    public PgnGame(String fileName, int number, CaseInsensitiveMap<String,String> tagPairs, List<String> gameComments,
                   List<Move> moves, String origText)
        throws PGNException
    {
        this.fileName = fileName == null ? "" : fileName;
        this.number = number;
        this.tagPairs = tagPairs;
        this.gameComments = gameComments;
        this.moves = moves;
        this.origText = origText;
        xEcoCacheMap = new HashMap<>();
        
        if (!CLOptions.validateGames) return;
        int lastMoveNumber = 0;

        for (int i = 0; i < moves.size(); i++)
        {
            Move move = moves.get(i);
            Color moveColor = move.getColor();
            int moveNumber = move.getNumber();
            int plyParity = i % 2;
            
            if (plyParity == 0)
            {
                if (moveColor != Color.WHITE || moveNumber != lastMoveNumber + 1)
                    throw new PGNException("move sequence error at game " + number + ", ply " + (i + 1));
            }
            
            else
            {
                if (moveColor != Color.BLACK || moveNumber != lastMoveNumber)
                    throw new PGNException("move sequence error at game " + number + ", ply " + (i + 1));
            }
            
            lastMoveNumber = move.getNumber();
        }
    }

    public static void setBookMarker(String regex) { bookMarker = Pattern.compile(regex, Pattern.DOTALL); }
    
    /**
     * 
     * @param moveSt example: "1. Nf3 c5 2. g3 Nc6"
     * @return 
     */
    public static List<String> parseMoveString(String moveSt)
    {
        List<String> moveList = new ArrayList<>();
        String moves[] = moveSt.split("\\s+");

        for (String move : moves)
        {
            int start = 0;
            int end = move.length() - 1;

            while (start <= end && !Character.isLetter(move.charAt(start))) start++;
            while (end > 0 && !Character.isLetterOrDigit(move.charAt(end))) end--;

            if (end <= start) continue;
            moveList.add(move.substring(start, end + 1));
        }

        return moveList;
    }
    
    public static PgnGame parseNext(String fileName, int number, CopyReader reader)
        throws IOException, PGNException
    {
        ArrayList<String> moveComments = new ArrayList<>();
        char buf[] = new char[BUFSIZE];
        int next, i, j;
        CaseInsensitiveMap<String,String> tagpairs = new CaseInsensitiveMap<>();
        List<PgnGame.Move> moves = new ArrayList<>();
        List<String> gameComments = new ArrayList<>();

        while (true) // parse tag pairs
        {
            next = eatWhiteSpace(reader);

            if (next == -1) return null;
            if (next == 0xFEFF) continue; // Unicode byte-order mark
            if (next != '[') break;

            // tag
            for (i = 0;; i++)
            {
                try { next = reader.readFully(buf, i, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                    next = reader.readFully(buf, i, 1);
                }
                
                if (next == -1) throw new PGNException("eof while parsing");
                if (Character.isWhitespace(buf[i])) break;
            }

            String tag = new String(buf, 0, i);
            next = eatWhiteSpace(reader);

            if (next != '"') throw new PGNException("unquoted value");

            // value
            for (i = 0;; i++)
            {
                boolean escaped = false;
                
                try { next = reader.readFully(buf, i, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                    next = reader.readFully(buf, i, 1);
                }
                
                if (next == -1) throw new PGNException("eof while parsing.");

                if (buf[i] == '\\') // PGN escape character
                {
                    try { next = reader.readFully(buf, i, 1); }

                    catch (IndexOutOfBoundsException e)
                    {
                        buf = Arrays.copyOf(buf, buf.length * 2);
                        next = reader.readFully(buf, i, 1);
                    }
                    
                    if (next == -1) throw new PGNException("eof while parsing.");
                    escaped = true;
                }

                if (buf[i] == '"' && !escaped) break;
            }

            String value = new String(buf, 0, i);
            next = eatWhiteSpace(reader);
            if (next != ']') throw new PGNException("missing ']'");

            tagpairs.put(tag, value);
        }

        while (next == '{' || next == ';') next = processComment(reader, next, gameComments);
        
        if (next == -1) throw new PGNException("eof while parsing"); // empty move list
        buf[0] = (char)next;
        
        for (i = 1;; i++) // parse each ply
        {
            for (j = 1; Character.isDigit(buf[j - 1]); j++) // move number
            {
                try { next = reader.readFully(buf, j, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                    next = reader.readFully(buf, j, 1);
                }
                
                if (next == -1) throw new PGNException("eof while parsing");
                next = buf[j];
            }
            
            if (Character.isWhitespace(buf[j - 1])) next = eatWhiteSpace(reader);
            if (next == -1) throw new PGNException("eof while parsing game " + number);
            
            // termination markers
            switch (buf[j - 1])
            {
                case '-':
                    if (j != 2)
                        throw new PGNException("invalid termination marker " +
                            "at game " + number);

                    next = reader.readFully(buf, 2, 1);
                    if (next == -1) throw new PGNException("eof while parsing");

                    if ((buf[0] != '0' && buf[0] != '1') ||
                        (buf[2] != '0' && buf[2] != '1') ||
                        (buf[0] == buf[2]) ||
                        !new String(buf, 0, 3).equals(tagpairs.get("Result")))
                        throw new PGNException("invalid termination marker " +
                            "at game " + number);

                    return new PgnGame(fileName, number, tagpairs, gameComments, moves, reader.getCopy());

                case '/':
                    if (j != 2)
                        throw new PGNException("invalid termination marker " +
                            "at game " + number);

                    next = reader.readFully(buf, 2, 5);
                    if (next == -1) throw new PGNException("EOF while parsing");

                    String terminator = new String(buf, 0, 7);

                    if (!terminator.equals("1/2-1/2") ||
                        !terminator.equals(tagpairs.get("Result")))
                        throw new PGNException("invalid termination marker " +
                            "at game " + number);

                    return new PgnGame(fileName, number, tagpairs, gameComments, moves, reader.getCopy());

                case '*':
                    if (!"*".equals(tagpairs.get("Result")))
                        throw new PGNException("invalid termination marker " +
                            "at game " + number);

                    return new PgnGame(fileName, number, tagpairs, gameComments, moves, reader.getCopy());
            }
            
//            movenum = new Integer(new String(buf, 0, i - 1 == 0 ? 1 : i - 1));
            
            if (next == '.')
            {
                do { next = reader.read(); } while (next == '.');
                if (Character.isWhitespace(next)) next = eatWhiteSpace(reader);
                if (next == -1) throw new PGNException("eof while parsing");
            }
            
            buf[0] = (char)next;
            
            // get the move
            for (j = 1;; j++)
            {
                try { next = reader.readFully(buf, j, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                    next = reader.readFully(buf, j, 1);
                }
                
                if (next == -1) throw new PGNException("empty move");
                
                if (Character.isWhitespace(buf[j]))
                {
                    next = eatWhiteSpace(reader);
                    break;
                }
                
                if (buf[j] == '{')
                {
                    next = '{';
                    break;
                }
                
                if (buf[j] == ';')
                {
                    next = ';';
                    break;
                }
            }
            
            String moveStr = new String(buf, 0, j);
        
            while (next == '{' || next == ';') next = processComment(reader, next, moveComments);
            
            if (next == -1) throw new PGNException("unexpected eof");
            buf[0] = (char)next;
            
            PgnGame.Move move = new PgnGame.Move((i & 1) == 0 ? Color.BLACK : Color.WHITE,
                (short)Math.round((float)i / (float)2), moveStr, moveComments);
            
            moves.add(move);
            moveComments = new ArrayList<>();
        }
    }
    
    /**
     * 
     * @param reader
     * @param commentStart the character that began the comment
     * @param commentList list to which the comment string will be appended
     * @return first non-whitespace character after the end of the comment, or
     *         -1 on eof
     * @throws IOException 
     */
    private static int processComment(CopyReader reader, int commentStart, List<String> commentList)
        throws IOException
    {
        int i, next;
        char buf[] = new char[COMMENT_BUFSIZE];
        
        for (i = 0;; i++) // one character per loop iteration
        {
            try { next = reader.readFully(buf, i, 1); }
            
            catch (IndexOutOfBoundsException e)
            {
                buf = Arrays.copyOf(buf, buf.length + COMMENT_BUFSIZE);
                next = reader.readFully(buf, i, 1);
            }
            
            if (next == -1) return -1; // eof not okay here
            if (buf[i] == '}' && commentStart == '{') break;

            if ((buf[i] == '\n' || buf[i] == '\r') && commentStart == ';') break;
        }

        commentList.add(new String(buf, 0, i));
        return eatWhiteSpace(reader);
    }
    
    private static int eatWhiteSpace(Reader reader) throws IOException
    {
        int next;
        
        do
        {
            next = reader.read();
            if (next == -1) break;
        }
        while (Character.isWhitespace(next));
//        while (next == '\u0020' || next == '\t');
        
        return next;
    }

    /**
     *
     * @return hash of player names and game half-moves up to <plies>
     */
    public UniqueId128 getHash(int plies)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getWhite()).append('\0').append(getBlack());
        appendMoves(sb, plies);
        return new UniqueId128(sb.toString().getBytes());
    }

    /**
     *
     * @param plies
     * @return hash of player names plus moves
     */
    public UniqueId128 getPostOpeningHash(int plies)
    {
        return new UniqueId128((getWhite() + '\0' + getBlack() + getPostOpeningString(plies)).getBytes());
    }

    /**
     *
     * @return hash of game moves
     */
    public UniqueId128 getMoveHash(int plies)
    {
        StringBuilder sb = new StringBuilder();
        appendMoves(sb, plies);
        return new UniqueId128(sb.toString().getBytes());
    }

    /**
     *
     * @param plies
     * @return hash of moves (only)
     */
    public UniqueId128 getPostOpeningMoveHash(int plies)
    {
        return new UniqueId128(getPostOpeningString(plies).getBytes());
    }

    private void appendMoves(StringBuilder sb, int plies)
    {
        List<Move> moveList = getMoveList();

        if (plies < 1) for (Move move : moveList) sb.append(move.getMove());

        else
        {
            int limit = Math.min(plies, moveList.size());
            for (int i = 0; i < limit; i++) sb.append(moveList.get(i).getMove());
        }
    }

    public String getFileName() { return fileName; }
    public int getNumber() { return number; }
    public Locator getLocator() { return new Locator(fileName, number); }
    public Set<String> getKeySet() { return tagPairs.keySet(); }
    public String getValue(String key) { return tagPairs.get(key); }
    public List<String> getGameComments() { return gameComments; }
    public List<Move> getMoveList() { return moves; }
    public int getPlyCount() { return moves.size(); }
    public String getOrigText() { return origText; }
    public boolean contains(CharSequence s) { return origText.contains(s); }
    
    // PGN seven-tag roster
    
    public String getEvent() { return getValue("Event"); }
    public String getSite() { return getValue("Site"); }
    public String getDate() { return getValue("Date"); }
    public String getWhite() { return getValue("White"); }
    public String getBlack() { return getValue("Black"); }
    public Result getResult() { return Result.get(getValue("Result")); }

    public String getRound()
    {
        if (CLOptions.aquarium)
        {
            String roundSt = getValue("Round");

            try
            {
                int roundNum = Integer.parseInt(roundSt);
                return String.valueOf(roundNum <= 0 ? roundNum + 128 : roundNum);
            }

            catch (NumberFormatException e) { return roundSt; }
        }

        else return getValue("Round");
    }

    public NormalizedRound getNormalizedRound()
    {
        try { return new NormalizedRound(getValue("Round")); }

        catch (IllegalArgumentException e)
        {
            System.err.println("Exception in game " + getNumber() + ": " + e.getMessage());
            System.exit(-1);
        }

        return null;
    }

    public String getTags()
    {
        StringJoiner sj = new StringJoiner(CLOptions.valueDelim);
        for (String key : new TreeSet<>(tagPairs.keySet())) sj.add(key); // alphabetize for consistency
        return sj.toString();
    }
    
    public boolean isDraw() { return getResult().equals(Result.DRAW); }
    public boolean isNoResult() { return getResult().equals(Result.NORESULT); }
    
    public Move getMove(int number, Color color)
    {
        return getMoveList().get(number * 2 - (color.equals(Color.WHITE) ? 2 : 1));
    }
    
    
    public String getWinner()
    {
        if (getResult().equals(Result.WHITEWIN)) return getWhite();
        if (getResult().equals(Result.BLACKWIN)) return getBlack();
        return null;
    }
    
    public String getLoser()
    {
        if (getResult().equals(Result.WHITEWIN)) return getBlack();
        if (getResult().equals(Result.BLACKWIN)) return getWhite();
        return null;
    }

    /**
     *
     * @return the first out-of-book Move or null if none is found
     */
    public Move getFirstOobMove()
    {
        return moves.get(getOpeningMoveList().get(openingMoveList.size() - 1).getPly());
    }

    /**
     *
     * @return the last book move, or null if none is found
     */
    public Move getLastBookMove()
    {
        return getOpeningMoveList().get(openingMoveList.size() - 1);
    }
    
    public Move getNextMove(Move move)
    {
        int ply = move.getPly();
        if (ply >= moves.size()) return null;
        return moves.get(ply);
    }

    /**
     *
     * @param plies number of half-moves past the opening to include
     * @return numbered move list up to plies half-moves after the opening
     */
    public String getPostOpeningString(int plies)
    {
        StringBuilder sb = new StringBuilder(1024).append(getOpeningString());
        Move move = getFirstOobMove();
        int postOpeningPly = 0;
        
        while (move != null)
        {
            if (move.getColor().equals(Color.WHITE)) sb.append(move.getNumber()).append(".");
            sb.append(move.getMove()).append(" ");
            if (++postOpeningPly == plies) break;
            move = getNextMove(move);
        }
        
        return sb.toString().trim();
    }

    public int getPostOpeningPlyCount()
    {
        return getMoveList().size() - getOpeningMoveList().size();
    }

    public Board getOpeningPosition() throws IllegalMoveException
    {
        return getOpeningPosition(bookMarker);
    }
    
    public Board getOpeningPosition(Pattern oobMarker) throws IllegalMoveException
    {
        Board board = new Board(true);
        for (Move move : getOpeningMoveList(oobMarker)) board.move(move.getMoveOnly());
        return board;
    }
    
    public PositionId getOpid() throws IllegalMoveException
    {
        return getOpeningPosition().positionId();
    }
    
    public String getOpeningFen() throws IllegalMoveException
    {
        return getOpeningPosition().toFen();
    }

    /**
     *
     * @return move-list string, including extra characters ("+," "#," etc.)
     */
    public String getFullOpeningString()
    {
        StringBuilder sb = new StringBuilder(1024);
        
        for (Move move : getOpeningMoveList())
        {
            if (move.getColor().equals(Color.WHITE)) sb.append(move.getNumber()).append(".");
            sb.append(move.getMove()).append(" ");
        }

        return sb.toString().trim();
    }

    public List<Move> getOpeningMoveList()
    {
        return getOpeningMoveList(bookMarker);
    }

    /**
     * Note that if the game contains no comments, then the entire game is regarded as opening.
     *
     * @return
     */
    public List<Move> getOpeningMoveList(Pattern oobMarker)
    {
        if (openingMoveList != null) return openingMoveList;

        int firstCommentIdx = -1;
        int oobIdx = -1;

        for (int i = 0; i < moves.size(); i++)
        {
            Move move = moves.get(i);

            if (!move.getComments().isEmpty())
            {
                // Presence of oobMarker means that book moves include the present move.  Otherwise, the first move
                // with a comment is the first out-of-book move.

                if (firstCommentIdx == -1) firstCommentIdx = i;

                if (move.hasComment(oobMarker))
                {
                    oobIdx = i;
                    break;
                }
            }
        }

        if (oobIdx > -1) openingMoveList = moves.subList(0, oobIdx + 1); // has oob marker
        else if (firstCommentIdx > -1) openingMoveList = moves.subList(0, firstCommentIdx); // has comment but no marker
        else openingMoveList = moves; // no comment or oob marker, so whole game is "opening"

        return openingMoveList;
    }

    /**
     * Note that if the game contains no comments, then the entire game is regarded as opening.
     *
     * @return move-list string, stripped of extra characters ("+," "#," etc.)
     */
    private String getOpeningString()
    {
        if (openingString != null) return openingString;

        StringBuilder sb = new StringBuilder(1024);
        
        for (Move move : getOpeningMoveList())
        {
            if (move.getColor().equals(Color.WHITE)) sb.append(move.getNumber()).append(".");
            sb.append(move.getMoveOnly()).append(" ");
        }

        openingString = sb.toString().trim();
        return openingString;
    }

    public MoveListId openingId(Pattern oobMarker) { return new MoveListId(getOpeningString()); }
    public MoveListId openingId() { return openingId(bookMarker); }
    
    public UniqueId128 getPlayerOpeningHash()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getWhite()).append('\0').append(getBlack()).append('\0').append(getOpeningString());
        return new UniqueId128(sb.toString().getBytes());
    }

    public int getDisagreeCount()
    {
        int disagreeCount = 0;
        int limit = getPostOpeningPlyCount() - 1; // don't count last move

        for (int i = getOpeningMoveList().size(); i < limit; i++)
            if (getMoveList().get(i).getAquariumVars().getExpectedResponse() != null) disagreeCount++;

        return disagreeCount;
    }

    public float getDisagreeRatio()
    {
        return ((float)getDisagreeCount()) / (getPostOpeningPlyCount() - 1);
    }

    // Aquarium only
    private void getLowClocks()
    {
        for (PgnGame.Move move : getMoveList())
        {
            Clock moveClock = move.getAquariumVars().getClk();

            if (move.getColor() == Color.WHITE)
            {
                if (moveClock != null && (lowClockWhite == null || moveClock.compareTo(lowClockWhite) < 0))
                    lowClockWhite = moveClock;
            }

            else
            {
                if (moveClock != null && (lowClockBlack == null || moveClock.compareTo(lowClockBlack) < 0))
                    lowClockBlack = moveClock;
            }
        }
    }

    // Aquarium only
    public Clock getLowClockWhite()
    {
        if (lowClockWhite != null) return lowClockWhite;
        getLowClocks();
        return lowClockWhite;
    }

    // Aquarium only
    public Clock getLowClockBlack()
    {
        if (lowClockBlack != null) return lowClockBlack;
        getLowClocks();
        return lowClockBlack;
    }

    // Aquarium only
    public Clock getLowClock()
    {
        if (getLowClockWhite().compareTo(getLowClockBlack()) <= 0) return getLowClockWhite();
        return getLowClockBlack();
    }

    // Aquarium only
    public List<String> getLowClockPlayers()
    {
        List<String> ret = new ArrayList<>();
        int comp = getLowClockWhite().compareTo(getLowClockBlack());

        if (comp < 0) ret.add(getWhite());

        else if (comp == 0)
        {
            ret.add(getWhite());
            ret.add(getBlack());
        }

        else ret.add(getBlack());

        return ret;
    }
    
    /**
     * 
     * @return the value of the TimeControl tag, if present and legible; otherwise, best guess or null
     */
    public TimeCtrl getTimeCtrl()
    {
        String timeCtrlSt = null;
        boolean official = false;
        
        try { timeCtrlSt = get(OutputSelector.TIMECONTROL); }
        catch (InvalidSelectorException e) {} // shouldn't happen
        
        if (timeCtrlSt == null || timeCtrlSt.equals("?")) // try to infer
        {
            Clock startClko = null;
            Clock lastClko = null;
            Color color = null;
            
            for (Move move : getMoveList())
            {
                if (color != null && !move.getColor().equals(color)) continue;
                
                Clock thisClko = new AquariumVars(move).getClko();
                if (thisClko == null) continue;
                
                if (startClko == null)
                {
                    startClko = thisClko;
                    color = move.getColor();
                }
                
                else if (thisClko.compareTo(lastClko) > 0 && move.getColor().equals(color))
                {
                    int moveNo = move.getNumber();
                    
                    // Correct for Aquarium weirdness.
                    
                    moveNo = moveNo % 10 >= 5 ? ((moveNo / 10) * 10) + 10 : (moveNo / 10) * 10;
                    timeCtrlSt = moveNo + "/" + startClko.inSecs();
                    
//                    timeCtrlSt = (moveNo -
//                        (color.equals(Color.WHITE) ? 1 : 0)) + "/" +
//                        startClko.inSecs();
                    
                    break;
                }
                
                lastClko = thisClko;
            }
        }
        
        else official = true;
        
        try { return new TimeCtrl(timeCtrlSt, official); }
        catch (InvalidTimeCtrlException e) { return null; }
    }
    
    /**
     * 
     * @param regex the regular expression to be matched against this game
     * @return true if a match is found, false otherwise
     */
    public boolean matches(Pattern regex)
    {
        return regex.matcher(origText).find();
    }
    
    public boolean matches(String regex)
    {
        return origText.matches(regex);
    }
    
    public boolean containsPosition(PositionId pos)
        throws IllegalMoveException
    {
        Board board = new Board(true);

        for (Move move : moves)
            if (board.move(move).positionId().equals(pos)) return true;
        
        return false;
    }
    
    public boolean containsPosition(Board pos)
        throws IllegalMoveException
    {
        Board board = new Board(true);
        int whitePieceCount = pos.getWhitePieceCount();
        int blackPieceCount = pos.getBlackPieceCount();

        for (Move move : moves)
        {
            if (board.getWhitePieceCount() < whitePieceCount || board.getBlackPieceCount() < blackPieceCount)
                return false;

            if (board.move(move).positionEquals(pos)) return true;
        }
        
        return false;
    }

    public boolean containsPosition(final Set<LooseBoard> positionSet, int minWhitePieces, int minBlackPieces)
            throws IllegalMoveException
    {
        LooseBoard looseBoard = new LooseBoard(new Board(true));
        Board board = looseBoard.getBoard();

        for (PgnGame.Move move : getMoveList())
        {
            if (board.getWhitePieceCount() < minWhitePieces || board.getBlackPieceCount() < minBlackPieces) break;
            if (positionSet.contains(looseBoard)) return true;
            board.move(move);
        }

        return false;
    }
    
    public PgnGame replace(Pattern replacee, String replacement)
        throws PGNException, IOException
    {
        CopyReader reader =
            new CopyReader(new StringReader(replacee.matcher(origText).replaceAll(replacement)),
                    PgnFile.COPY_BUF_INIT_SIZE);
        
        return parseNext(getFileName(), getNumber(), reader);
    }

    TreeNodeSet getXEcoNodeSet(EcoTree.FileType type) throws IllegalMoveException
    {
        TreeNodeSet nodeSet = xEcoCacheMap.get(type);

        if (nodeSet == null)
        {
            nodeSet = type.getEcoTree().getDeepestTranspositionSet(this);
            xEcoCacheMap.put(type, nodeSet);
        }

        return nodeSet;
    }
    
    public String get(OutputSelector selector) throws InvalidSelectorException
    {
        return get(new OutputSelector[] {selector});
    }

    public String get(OutputSelector selectors[]) throws InvalidSelectorException
    {
        StringBuilder ret = new StringBuilder();

        for (OutputSelector selector : selectors)
        {
            ret.append(CLOptions.outputDelim);
            selector.appendOutput(this, ret);
        }

        ret.delete(0, CLOptions.outputDelim.length());
        return ret.toString();
    }
    
    /**
     * Test for same players and same moves.
     * 
     * @param other
     * @return true if the parameter Game has the same players, same moves, and
     *         same result as this Game, false otherwise
     */
    public boolean isDuplicateOf(Object other)
    {
        if (!(other instanceof PgnGame)) return false;
        
        PgnGame otherGame = (PgnGame)other;
        String thisWhite = getWhite();
        String thisBlack = getBlack();
        String otherWhite = otherGame.getWhite();
        String otherBlack = otherGame.getBlack();
        
        if (thisWhite == null && otherWhite != null) return false;
        if (otherWhite == null && thisWhite != null) return false;
        if (thisBlack == null && otherBlack != null) return false;
        if (otherBlack == null && thisBlack != null) return false;

        if (thisWhite != null)
            if (!thisWhite.equals(otherWhite)) return false;
        
        if (thisBlack != null)
            if (!thisBlack.equals(otherBlack)) return false;
        
        List<Move> thisMoves = getMoveList();
        List<Move> otherMoves = otherGame.getMoveList();
        
        if (thisMoves.size() != otherMoves.size()) return false;
        
        for (int i = 0; i < thisMoves.size(); i++)
        {
            Move thisMove = thisMoves.get(i);
            Move otherMove = otherMoves.get(i);
            
            if (!thisMove.getMove().equals(otherMove.getMove())) return false;
        }
        
        return getResult().equals(otherGame.getResult());
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = getKeySet();
        
        for (String key : keys) sb.append("[").append(key).append(" \"").append(getValue(key)).append("\"]\n");
        sb.append("\n");
        
        List<String> comments = getGameComments();
        
        if (!comments.isEmpty())
        {
            for (String comment : comments)
            {
                sb.append("{").append(comment).append("}\n");
            }
        }
        
        for (Move move : getMoveList()) { sb.append(move).append("\n"); }
        sb.append(getValue("Result"));
        
        return sb.toString();
    }
}
