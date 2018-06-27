/*
 */

package com.dotfx.pgnutil;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 *
 * @author Mark Chen
 */
public class Game
{
    public static enum Color { WHITE, BLACK; }
    public static final Pattern OUT_OF_BOOK;
    public static final int HASHSEED = 0xa348ccf1;
    private static final HashFunction HASHFUNC;
    private static final int BUFSIZE = 1024;
    private static final int COMMENT_BUFSIZE = 1024;
    
    /**
     * One per ply.
     */
    public static class Move
    {
        private final Color color;
        private final short number;
        private final String move;
        private final List<String> comments;
        
        public Move(Color color, short number, String move, List<String> comments)
        {
            this.color = color;
            this.number = number;
            this.move = move;
            this.comments = new ArrayList();
            
            for (String comment : comments)
                this.comments.add(comment.replaceAll("[\n\r]", " "));
        }
        
        public Color getColor() { return color; }
        public int getNumber() { return number; }
        public String getMove() { return move; }
        public List<String> getComments() { return comments; }
        
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
            if (comments.size() > 0) sb.append(" ");
            
            for (String comment : comments)
                sb.append("{").append(comment).append("}");
            
            return sb.toString();
        }
    }
    
    static
    {
        HASHFUNC = Hashing.murmur3_128(HASHSEED);
        OUT_OF_BOOK = Pattern.compile("out\\s+of\\s+book", Pattern.DOTALL);
    }
    
    private final int number;
    private final Map<String,String> tagpairs;
    private final List<String> gameComments;
    private final List<Move> moves;
    private final String origText;
    
    public Game(int number, Map<String,String> tagpairs,
        List<String> gameComments, List<Move> moves, String origText)
    {
        this.number = number;
        this.tagpairs = tagpairs;
        this.gameComments = gameComments;
        this.moves = moves;
        this.origText = origText;
    }
    
    public static Game parseNext(int number, CopyReader reader)
        throws IOException, PGNException
    {
        String result;
        Game.Color color;
        ArrayList<String> moveComments = new ArrayList();
        char buf[] = new char[BUFSIZE];
        int next, i, j;
        Map<String,String> tagpairs = new HashMap<>();
        List<Game.Move> moves = new ArrayList();
        List<String> gameComments = new ArrayList();
        
        while (true) // parse tag pairs
        {
            next = eatWhiteSpace(reader);
            if (next == -1) return null; // normal eof condition
            if (next != '[') break;

            // tag
            for (i = 0;; i++)
            {
                try { next = reader.read(buf, i, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length + BUFSIZE);
                    next = reader.read(buf, i, 1);
                }
                
                if (next == -1) throw new PGNException("EOF while parsing");
                if (Character.isWhitespace(buf[i])) break;
            }

            String tag = new String(buf, 0, i);
            next = eatWhiteSpace(reader);

            if (next != '"') throw new PGNException("unquoted value");

            // value
            for (i = 0;; i++)
            {
                boolean escaped = false;
                
                try { next = reader.read(buf, i, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length + BUFSIZE);
                    next = reader.read(buf, i, 1);
                }
                
                if (next == -1) throw new PGNException("EOF while parsing.");

                if (buf[i] == '\\') // PGN escape character
                {
                    try { next = reader.read(buf, i, 1); }

                    catch (IndexOutOfBoundsException e)
                    {
                        buf = Arrays.copyOf(buf, buf.length + BUFSIZE);
                        next = reader.read(buf, i, 1);
                    }
                    
                    if (next == -1) throw new PGNException("EOF while parsing.");
                    escaped = true;
                }

                if (buf[i] == '"' && !escaped) break;
            }

            String value = new String(buf, 0, i);
            next = eatWhiteSpace(reader);
            if (next != ']') throw new PGNException("missing ']'");

            tagpairs.put(tag, value);
        }

        while (next == '{' || next == ';') 
            next = processComment(reader, next, gameComments); 
        
        if (next == -1) throw new PGNException("EOF while parsing"); // empty move list
        buf[0] = (char)next;
        
        for (i = 1;; i++) // parse each ply
        {
            for (j = 1; Character.isDigit(buf[j - 1]); j++) // move number
            {
                try { next = reader.read(buf, j, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length + BUFSIZE);
                    next = reader.read(buf, j, 1);
                }
                
                if (next == -1) throw new PGNException("EOF while parsing");
                next = buf[j];
            }
            
            if (Character.isWhitespace(buf[j - 1])) next = eatWhiteSpace(reader);
            if (next == -1) throw new PGNException("EOF while parsing game " + number);
            
            // termination markers
            else if
                (buf[j - 1] == '-' || buf[j - 1] == '/' || buf[j - 1] == '*')
            {
                if (buf[j - 1] == '-')
                {
                    if (j != 2)
                        throw new PGNException("invalid termination marker");

                    next = reader.read(buf, 2, 1);
                    if (next == -1) throw new PGNException("EOF while parsing");

                    if ((buf[0] != '0' && buf[0] != '1') ||
                        (buf[2] != '0' && buf[2] != '1'))
                        throw new PGNException("invalid termination marker");
                    
                    result = new String(buf, 0, 3);
                }
                
                else if (buf[j - 1] == '/')
                {
                    if (j != 2)
                        throw new PGNException("invalid termination marker");

                    next = reader.read(buf, 2, 5);
                    if (next == -1) throw new PGNException("EOF while parsing");

                    if (!new String(buf, 0, 7).equals("1/2-1/2"))
                        throw new PGNException("invalid termination marker");
                    
                    result = new String(buf, 0, 7);
                }
                
                else result = "*";
                
                if (!result.equals(tagpairs.get("Result")))
                    throw new PGNException("incorrect termination marker");
                
                Game game = new Game(number, tagpairs, gameComments, moves,
                    reader.getCopy());
                
                return game;
            }
            
//            movenum = new Integer(new String(buf, 0, i - 1 == 0 ? 1 : i - 1));
            
            if (next == '.')
            {
                do { next = reader.read(); } while (next == '.');
                next = eatWhiteSpace(reader);
                if (next == -1) throw new PGNException("EOF while parsing");
            }
            
            buf[0] = (char)next;
            
            // get the move
            for (j = 1;; j++)
            {
                try { next = reader.read(buf, j, 1); }

                catch (IndexOutOfBoundsException e)
                {
                    buf = Arrays.copyOf(buf, buf.length + BUFSIZE);
                    next = reader.read(buf, j, 1);
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
        
            while (next == '{' || next == ';')
                next = processComment(reader, next, moveComments);
            
            if (next == -1) throw new PGNException("unexpected eof");
            buf[0] = (char)next;
            
            color = i % 2 == 0 ? Game.Color.BLACK : Game.Color.WHITE;
            
            Game.Move move = new Game.Move(color,
                (short)Math.round((float)i / (float)2), moveStr, moveComments);
            
            moves.add(move);
            moveComments = new ArrayList();
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
    private static int processComment(Reader reader, int commentStart,
        List<String> commentList)
        throws IOException
    {
        int i, next;
        char buf[] = new char[COMMENT_BUFSIZE];
        
        for (i = 0;; i++) // one character per loop iteration
        {
            try { next = reader.read(buf, i, 1); }
            
            catch (IndexOutOfBoundsException e)
            {
                buf = Arrays.copyOf(buf, buf.length + COMMENT_BUFSIZE);
                next = reader.read(buf, i, 1);
            }
            
            if (next == -1) return -1; // eof not okay here
            if (buf[i] == '}' && commentStart == '{') break;

            if ((buf[i] == '\n' || buf[i] == '\r') &&
                commentStart == ';') break;
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
    
    public HashCode getHash()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getWhite()).append('\0').append(getBlack());
        for (Move move : getMoves()) sb.append(move.getMove());
        return HASHFUNC.hashBytes(sb.toString().getBytes());
    }
    
    public int getNumber() { return number; }
    public Set<String> getKeySet() { return tagpairs.keySet(); }
    public final String getValue(String key) { return tagpairs.get(key); }
    public List<String> getGameComments() { return gameComments; }
    public List<Move> getMoves() { return moves; }
    public String getOrigText() { return origText; }
    public boolean contains(CharSequence s) { return origText.contains(s); }
    
    // PGN seven-tag roster
    
    public String getEvent() { return getValue("Event"); }
    public String getSite() { return getValue("Site"); }
    public String getDate() { return getValue("Date"); }
    public String getRound() { return getValue("Round"); }
    public String getWhite() { return getValue("White"); }
    public String getBlack() { return getValue("Black"); }
    public String getResult() { return getValue("Result"); }
    
    public boolean isDraw() { return getResult().equals("1/2-1/2"); }
    public boolean isNoResult() { return getResult().equals("*"); }
    
    public String getWinner()
    {
        String result = getResult();
        
        if (result.equals("1-0")) return getWhite();
        if (result.equals("0-1")) return getBlack();
        return null;
    }
    
    public String getLoser()
    {
        String result = getResult();
        
        if (result.equals("1-0")) return getBlack();
        if (result.equals("0-1")) return getWhite();
        return null;
    }
    
    public OpeningID getOpeningID(Pattern oobMarker)
    {
        StringBuilder sb = new StringBuilder();
        
        for (Move move : moves)
        {
            if (move.getComments().size() > 0)
            {
                // Presence of oobMarker means that book moves include the
                // present move.  Otherwise, the first move with a comment is
                // the first out-of-book move.
                
                if (move.hasComment(oobMarker)) sb.append(move.getMove());
                break;
            }
            
            sb.append(move.getMove());
        }
        
        return new OpeningID(sb.toString());
    }
    
    public OpeningID getOpeningID() { return getOpeningID(OUT_OF_BOOK); }
    
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
    
    public Game replace(Pattern replacee, String replacement)
        throws PGNException, IOException
    {
        return parseNext(getNumber(),
            new CopyReader(new StringReader(replacee.matcher(origText).
                replaceAll(replacement))));
    }
    
    /**
     * 
     * @param spec a comma-separated list of requested fields.  The field
     *             "moves" corresponds to the game's move list.  The field
     *             "gameno" corresponds to the game's number (normally, the
     *             sequence number from the parsed PGN file).  The field 'oid'
     *             corresponds to the game's opening identifier.
     * 
     * @return a String containing pipe-separated values corresponding to the
     *         requested fields
     */
    public String get(String spec)
    {
        StringBuilder ret = new StringBuilder();
        String tokens[] = spec.split(",\\W*");
        
        for (String token : tokens)
        {
            if (token.equalsIgnoreCase("moves"))
            {
                StringBuilder moveList = new StringBuilder();
                List<Move> moves = getMoves();
                
                for (Move move : moves)
                    moveList.append(move.getMove()).append(" ");
                
                moveList.deleteCharAt(moveList.length() - 1);
                ret.append(moveList.toString()).append("|");
            }
            
            else if (token.equalsIgnoreCase("gameno"))
                ret.append(getNumber()).append("|");
            
            else if (token.equalsIgnoreCase("oid"))
                ret.append(getOpeningID()).append("|");
            
            else if (token.equalsIgnoreCase("winner"))
                ret.append(getWinner()).append("|");
            
            else if (token.equalsIgnoreCase("loser"))
                ret.append(getLoser()).append("|");
            
            else ret.append(getValue(token)).append("|");
        }
        
        ret.deleteCharAt(ret.length() - 1);
        return ret.toString();
    }
    
    /**
     * Test for same players and same moves.
     * @param other
     * @return true if the parameter Game has the same players, same moves, and
     *         same result as this Game, false otherwise
     */
    public boolean isDuplicateOf(Object other)
    {
        if (other == null || !(other instanceof Game)) return false;
        
        Game otherGame = (Game)other;
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
        
        List<Move> thisMoves = getMoves();
        List<Move> otherMoves = otherGame.getMoves();
        
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
        
        for (String key : keys)
        {
            sb.append("[").append(key).append(" \"").append(getValue(key)).
                append("\"]\n");
        }
        
        sb.append("\n");
        
        List<String> comments = getGameComments();
        
        if (comments.size() > 0)
        {
            for (String comment : comments)
            {
                sb.append("{").append(comment).append("}\n");
            }
        }
        
        for (Move move : getMoves()) { sb.append(move).append("\n"); }
        sb.append(getValue("Result"));
        
        return sb.toString();
    }
}
