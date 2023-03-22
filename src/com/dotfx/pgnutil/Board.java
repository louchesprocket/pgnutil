/*
 * The MIT License
 *
 * Copyright 2019 Mark Chen <chen@dotfx.com>.
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

import com.dotfx.pgnutil.eco.TreeNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class Board
{
    public enum Square
    {
        A1, B1, C1, D1, E1, F1, G1, H1,
        A2, B2, C2, D2, E2, F2, G2, H2,
        A3, B3, C3, D3, E3, F3, G3, H3,
        A4, B4, C4, D4, E4, F4, G4, H4,
        A5, B5, C5, D5, E5, F5, G5, H5,
        A6, B6, C6, D6, E6, F6, G6, H6,
        A7, B7, C7, D7, E7, F7, G7, H7,
        A8, B8, C8, D8, E8, F8, G8, H8;
        
        private static final Square[] squares = new Square[64];
        private static final Map<String,Square> nameMap = new HashMap<>();
        private int location;
        
        static
        {
            Square values[] = Square.values();
            for (int i = 0; i < values.length; i++) values[i].location = i;
            
            for (Square s : Square.values())
            {
                squares[s.getLocation()] = s;
                nameMap.put(s.name().toLowerCase(), s);
            }
        }
        
        public int getLocation() { return location; }
        public static Square get(int square) { return squares[square]; }
        public static Square get(String s) { return nameMap.get(s.toLowerCase());}
        
        @Override
        public String toString() { return name().toLowerCase(); }
    }
    
    public enum PieceType
    {
        PAWN("p"),
        ROOK("R"),
        BISHOP("B"),
        KNIGHT("N"),
        QUEEN("Q"),
        KING("K");
        
        private static final Map<String,PieceType> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (PieceType r : PieceType.values()) sigMap.put(r.toString(), r);
        }
        
        private PieceType(String signifier) { this.signifier = signifier; }
        @Override public String toString() { return signifier; }
        
        public static PieceType get(String signifier)
        {
            return sigMap.get(signifier);
        }
        
        public static PieceType get(char signifier)
        {
            return sigMap.get(new String(new char[] {signifier}));
        }
    }
    
    public static class Piece
    {
        private final PieceType fileType;
        private final Color color;
        
        public Piece(Color color, PieceType fileType)
        {
            this.fileType = fileType;
            this.color = color;
        }
        
        public PieceType getType() { return fileType; }
        public Color getColor() { return color; }
        
        public byte toByte()
        {
            if (color == Color.WHITE)
            {
                switch (fileType)
                {
                    case PAWN: return (byte)0;
                    case ROOK: return (byte)1;
                    case KNIGHT: return (byte)2;
                    case BISHOP: return (byte)3;
                    case QUEEN: return (byte)4;
                    default: return (byte)5; // king
                }
            }
            
            else
            {
                switch (fileType)
                {
                    case PAWN: return (byte)6;
                    case ROOK: return (byte)7;
                    case KNIGHT: return (byte)8;
                    case BISHOP: return (byte)9;
                    case QUEEN: return (byte)10;
                    default: return (byte)11; // king
                }
            }
        }
        
        @Override
        public String toString()
        {
            return color == Color.WHITE ? getType().toString().toUpperCase() :
                getType().toString().toLowerCase();
        }
        
        @Override
        public boolean equals(Object other)
        {
            try
            {
                Piece that = (Piece)other;
                if (that.fileType != fileType) return false;
                return that.color == color;
            }
            
            catch (ClassCastException | NullPointerException e) { return false; }
        }
        
        @Override
        public int hashCode()
        {
            return Piece.class.hashCode() ^ fileType.hashCode() ^ color.hashCode();
        }
    }
    
    private static final int whiteKCastleSquares[] = new int[] {4, 5, 6};
    private static final int whiteQCastleSquares[] = new int[] {2, 3, 4};
    private static final int blackKCastleSquares[] = new int[] {60, 61, 62};
    private static final int blackQCastleSquares[] = new int[] {58, 59, 60};
    private static final Color[] COLORS = new Color[] {Color.WHITE, Color.BLACK};
    private static final int[] NMOVES = new int[] {6, 15, 17, 10, -6, -15, -17, -10};
    
    private final Piece position[];
    private short ply; // zero at initial position
    private boolean whiteCanCastleQ;
    private boolean whiteCanCastleK;
    private boolean blackCanCastleQ;
    private boolean blackCanCastleK;
    private byte whiteKingLoc;
    private byte blackKingLoc;
    private byte epCandidate; // location of the capture square; -1 if none
    private short halfMoveClock; // ply of last capture or pawn move
    private byte whitePieceCount = 16;
    private byte blackPieceCount = 16;
    
    public Board(boolean initialPosition)
    {
        position = new Piece[64];
        Arrays.fill(position, null);
        whiteKingLoc = 4;
        blackKingLoc = 60;
        epCandidate = -1;
        if (!initialPosition) return;
        
        whiteCanCastleQ = true;
        whiteCanCastleK = true;
        blackCanCastleQ = true;
        blackCanCastleK = true;
        
        for (int i = 8; i < 16; i++)
        {
            position[i] = new Piece(Color.WHITE, PieceType.PAWN);
            position[i + 40] = new Piece(Color.BLACK, PieceType.PAWN);
        }
        
        position[0] = new Piece(Color.WHITE, PieceType.ROOK);
        position[1] = new Piece(Color.WHITE, PieceType.KNIGHT);
        position[2] = new Piece(Color.WHITE, PieceType.BISHOP);
        position[3] = new Piece(Color.WHITE, PieceType.QUEEN);
        position[4] = new Piece(Color.WHITE, PieceType.KING);
        position[5] = new Piece(Color.WHITE, PieceType.BISHOP);
        position[6] = new Piece(Color.WHITE, PieceType.KNIGHT);
        position[7] = new Piece(Color.WHITE, PieceType.ROOK);
        
        position[56] = new Piece(Color.BLACK, PieceType.ROOK);
        position[57] = new Piece(Color.BLACK, PieceType.KNIGHT);
        position[58] = new Piece(Color.BLACK, PieceType.BISHOP);
        position[59] = new Piece(Color.BLACK, PieceType.QUEEN);
        position[60] = new Piece(Color.BLACK, PieceType.KING);
        position[61] = new Piece(Color.BLACK, PieceType.BISHOP);
        position[62] = new Piece(Color.BLACK, PieceType.KNIGHT);
        position[63] = new Piece(Color.BLACK, PieceType.ROOK);
    }
    
    public Board(Piece position[], short ply, byte epCandidate,
        boolean whiteCanCastleQ, boolean whiteCanCastleK,
        boolean blackCanCastleQ, boolean blackCanCastleK, byte whiteKingLoc,
        byte blackKingLoc, short halfMoveClock)
    {
        byte whiteCount = 0;
        byte blackCount = 0;
        
        this.position = position;
        this.ply = ply;
        this.epCandidate = epCandidate;
        this.whiteCanCastleQ = whiteCanCastleQ;
        this.whiteCanCastleK = whiteCanCastleK;
        this.blackCanCastleQ = blackCanCastleQ;
        this.blackCanCastleK = blackCanCastleK;
        this.whiteKingLoc = whiteKingLoc;
        this.blackKingLoc = blackKingLoc;
        this.halfMoveClock = halfMoveClock;
        
        for (int i = 0; i < 64; i++)
        {
            if (position[i] != null)
            {
                if (position[i].getColor() == Color.WHITE) whiteCount++;
                else blackCount++;
            }
        }
        
        whitePieceCount = whiteCount;
        blackPieceCount = blackCount;
    }
    
    public Board(Board other)
    {
        position = new Piece[64];
        System.arraycopy(other.position, 0, position, 0, 64);
        
        this.ply = other.ply;
        this.epCandidate = other.epCandidate;
        this.whiteCanCastleQ = other.whiteCanCastleQ;
        this.whiteCanCastleK = other.whiteCanCastleK;
        this.blackCanCastleQ = other.blackCanCastleQ;
        this.blackCanCastleK = other.blackCanCastleK;
        this.whiteKingLoc = other.whiteKingLoc;
        this.blackKingLoc = other.blackKingLoc;
        this.halfMoveClock = other.halfMoveClock;
        this.whitePieceCount = other.whitePieceCount;
        this.blackPieceCount = other.blackPieceCount;
    }

    public int getPly() { return ply; }
    public Board copy()
    {
        return new Board(this);
    }
    
    public boolean canMove(int start, int end)
    {
        if (start < 0 || start > 63 || end < 0 || end > 63) return false;
        
        int low, hi;
        
        if (start > end)
        {
            hi = start;
            low = end;
        }
        
        else
        {
            hi = end;
            low = start;
        }
        
        int span = hi - low;
        int i = 0;
        
        Piece piece = position[start];
        if (piece == null) return false;
        
        Piece destPiece = position[end];
        Color moveColor = piece.getColor();
        
//        if (colors[ply % 2] != moveColor) return false;
        
        Color destColor = destPiece == null ? null : destPiece.getColor();
        boolean destSameColor = moveColor == destColor;
        
        switch (piece.getType())
        {
            case PAWN:
                int diff = end - start;
                int endRank = end / 8;
                
                if (moveColor == Color.WHITE)
                {
                    if (diff == 8) return destPiece == null;
                    
                    if (diff == 16 && endRank == 3)
                        return destPiece == null && position[start + 8] == null;
                    
                    if ((diff == 7 || diff == 9) && hi/8 - low/8 == 1)
                        return (destPiece != null || end == epCandidate)
                            && !destSameColor;
                    
                    return false;
                }
                
                else
                {
                    if (diff == -8) return destPiece == null;
                    
                    if (diff == -16 && endRank == 4)
                        return destPiece == null && position[start - 8] == null;
                    
                    if ((diff == -7 || diff == -9) && hi/8 - low/8 == 1)
                        return (destPiece != null || end == epCandidate)
                            && !destSameColor;
                    
                    return false;
                }
                
            case ROOK:
                if (low / 8 == hi / 8) // same rank
                {
                    for (i = low + 1; i < hi; i++)
                        if (position[i] != null) return false;
                }
                
                else if (span % 8 == 0) // same file
                {
                    for (i = low + 8; i < hi; i += 8)
                        if (position[i] != null) return false;
                }
                
                else return false;
                
                return !destSameColor;
                
            case KNIGHT:
                switch (span)
                {
                    case 6:
                    case 10:
                        return !destSameColor && low/8 == hi/8 - 1;
                        
                    case 15:
                    case 17:
                        return !destSameColor && low/8 == hi/8 - 2;
                        
                    default: return false;
                }
                
            case BISHOP:
                if (span % 7 == 0 && span != 63)
                {
                    for (i = low + 7; i < hi; i += 7)
                    {
                        if (i/8 - (i - 7)/8 != 1 || position[i] != null)
                            return false;
                    }
                    
                    return !destSameColor && i/8 - (i - 7)/8 == 1;
                }
                
                else if (span % 9 == 0)
                {
                    for (i = low + 9; i < hi; i += 9)
                    {
                        if (i/8 - (i - 9)/8 != 1 || position[i] != null)
                            return false;
                    }
                    
                    return !destSameColor && i/8 - (i - 9)/8 == 1;
                }
                
                else return false;
                
            case QUEEN:
                if (low / 8 == hi / 8) // same rank
                {
                    for (i = low + 1; i < hi; i++)
                        if (position[i] != null) return false;
                }
                
                else if (span % 8 == 0) // same file
                {
                    for (i = low + 8; i < hi; i += 8)
                        if (position[i] != null) return false;
                }
                
                else if (span % 7 == 0 && span != 63) // same diagonal
                {
                    for (i = low + 7; i < hi; i += 7)
                    {
                        if (i/8 - (i - 7)/8 != 1 || position[i] != null)
                            return false;
                    }
                    
                    return !destSameColor && i/8 - (i - 7)/8 == 1;
                }
                
                else if (span % 9 == 0) // same diagonal
                {
                    for (i = low + 9; i < hi; i += 9)
                    {
                        if (i/8 - (i - 9)/8 != 1 || position[i] != null)
                            return false;
                    }
                    
                    return !destSameColor && i/8 - (i - 9)/8 == 1;
                }
                
                else return false;
                
                return !destSameColor;
                
            case KING:
                switch (span)
                {
                    case 1: return hi/8 == low/8 && !destSameColor;
                    case 7:
                    case 8:
                    case 9:
                        return hi/8 - low/8 == 1 && !destSameColor;
                        
                    case 2: // castle
                        if (moveColor == Color.WHITE)
                        {
                            if (end - start == 2) // king side
                                return position[5] == null &&
                                    position[6] == null && whiteCanCastleK;
                            
                            else // queen side
                                return position[1] == null &&
                                    position[2] == null &&
                                    position[3] == null && whiteCanCastleQ;
                        }
                        
                        else
                        {
                            if (end - start == 2) // king side
                                return position[61] == null &&
                                    position[62] == null && blackCanCastleK;
                            
                            else // queen side
                                return position[57] == null &&
                                    position[58] == null &&
                                    position[59] == null && blackCanCastleQ;
                        }
                        
                    default: return false;
                }
                
            default: return false;
        }
    }
    
    /**
     * 
     * @param start
     * @param end
     * @return true if the mover is moving into check or (in case of castling)
     * out of or through check
     */
    private boolean isMovingIntoCheck(int start, int end)
    {
        Piece movingPiece = position[start];
        Color color = movingPiece.getColor();
        int span = end > start ? end - start : start - end;
        
        // en passant capture
        if (end == epCandidate && movingPiece.getType() == PieceType.PAWN)
        {
            int savedCaptureLoc =
                color == Color.WHITE ? epCandidate - 8 : epCandidate + 8;
            
            Piece savedCapture = position[savedCaptureLoc];
            
            position[end] = position[start];
            position[start] = null;
            position[savedCaptureLoc] = null;
            
            boolean ret = isInCheck(color,
                new int[] {color == Color.WHITE ? whiteKingLoc : blackKingLoc});
            
            position[start] = movingPiece;
            position[end] = null;
            position[savedCaptureLoc] = savedCapture;
            
            return ret;
        }
        
        else
        {
            Piece savedCapture = position[end];
            byte savedWKingLoc = whiteKingLoc;
            byte savedBKingLoc = blackKingLoc;
            
            position[end] = position[start];
            position[start] = null;
        
            try
            {
                if (movingPiece.getType() == PieceType.KING)
                {
                    if (span == 2) // castle
                    {
                        if (color == Color.WHITE)
                        {
                            if (end - start == 2) // king side
                                return isInCheck(color, whiteKCastleSquares);

                            // queen side
                            else return isInCheck(color, whiteQCastleSquares);
                        }

                        else
                        {
                            if (end - start == 2) // king side
                                return isInCheck(color, blackKCastleSquares);

                            // queen side
                            else return isInCheck(color, blackQCastleSquares);
                        }
                    }

                    else if (color == Color.WHITE) whiteKingLoc = (byte)end;
                    else blackKingLoc = (byte)end;
                }

                return isInCheck(color, new int[]
                    {color == Color.WHITE ? whiteKingLoc : blackKingLoc});
            }
            
            finally
            {
                position[start] = movingPiece;
                position[end] = savedCapture;
                whiteKingLoc = savedWKingLoc;
                blackKingLoc = savedBKingLoc;
            }
        }
    }
    
    private boolean isInCheck(Color color, int squares[])
    {
        int savedKingLoc = color == Color.WHITE ? whiteKingLoc : blackKingLoc;
        
        for (int square : squares)
        {
            Piece saved = position[square]; // save contents of test square
            position[square] = position[savedKingLoc]; // move king to test square
            
            if (savedKingLoc != square)
                position[savedKingLoc] = null; // empty king's previous square
            
            for (int i = 0; i < 64; i++)
            {
                if (position[i] != null && position[i].getColor() != color &&
                    canMove(i, square)) return true;
            }
            
            position[savedKingLoc] = position[square]; // restore king to origin
            position[square] = saved; // restore test square
        }
        
        return false;
    }
    
    public boolean moveTest(int start, int end)
    {
        return end >= 0 && end < 64 && canMove(start, end) &&
            !isMovingIntoCheck(start, end);
    }
    
    /**
     * Does not do any kind of correctness checking.
     * 
     * @param start
     * @param end
     * @param promoteTo
     * @return 
     */
    public Board move(int start, int end, PieceType promoteTo) throws IllegalMoveException
    {
        ply++;
        
        if (position[end] != null)
        {
            halfMoveClock = ply;
            
            if (position[end].getColor() == Color.WHITE) whitePieceCount--;
            else blackPieceCount--;
        }
        
        position[end] = position[start];
        position[start] = null;
        Piece piece = position[end];

        try
        {
            switch (piece.getType())
            {
                case KING:
                    epCandidate = -1;
                    int diff = end - start;

                    if (piece.getColor() == Color.WHITE)
                    {
                        whiteKingLoc = (byte) end;
                        whiteCanCastleK = false;
                        whiteCanCastleQ = false;

                        if (diff == -2)
                        {
                            position[3] = position[0];
                            position[0] = null;
                        }

                        else if (diff == 2)
                        {
                            position[5] = position[7];
                            position[7] = null;
                        }
                    }

                    else
                    {
                        blackKingLoc = (byte) end;
                        blackCanCastleK = false;
                        blackCanCastleQ = false;

                        if (diff == -2)
                        {
                            position[59] = position[56];
                            position[56] = null;
                        }

                        else if (diff == 2)
                        {
                            position[61] = position[63];
                            position[63] = null;
                        }
                    }

                    break;

                case ROOK:
                    epCandidate = -1;

                    if (piece.getColor() == Color.WHITE)
                    {
                        if (start == 7) whiteCanCastleK = false;
                        else if (start == 0) whiteCanCastleQ = false;
                    }

                    else
                    {
                        if (start == 63) blackCanCastleK = false;
                        else if (start == 56) blackCanCastleQ = false;
                    }

                    break;

                case PAWN:
                    halfMoveClock = ply;
                    int endRank = end / 8;
                    int span = end > start ? end - start : start - end;

                    if (span == 16)
                    {
                        if (piece.getColor() == Color.WHITE) epCandidate = (byte)(start + 8);
                        else epCandidate = (byte)(start - 8);
                    }

                    else if (end == epCandidate && (span == 7 || span == 9))
                    {
                        if (piece.getColor() == Color.WHITE)
                        {
                            position[epCandidate - 8] = null;
                            blackPieceCount--;
                        }

                        else
                        {
                            position[epCandidate + 8] = null;
                            whitePieceCount--;
                        }

                        epCandidate = -1;
                    }

                    else if (endRank == 0 || endRank == 7)
                    {
                        position[end] = new Piece(piece.getColor(), promoteTo);
                        epCandidate = -1;
                    }

                    break;

                default:
                    epCandidate = -1;
            }
        }

        catch (NullPointerException e)
        {
            throw new IllegalMoveException("illegal move: " + piece + " from " + start + " to " + end);
        }
        
        return this;
    }
    
    public Board copyAndMove(int start, int end, PieceType promoteTo) throws IllegalMoveException
    {
        return copy().move(start, end, promoteTo);
    }
    
    public Board move(PgnGame.Move move) throws IllegalMoveException
    {
        return move(move.getMoveOnly());
    }
    
    public Board move(List<TreeNode> moveList) throws IllegalMoveException
    {
        for (TreeNode node : moveList) move(node.getMoveText());
        return this;
    }
    
    public Board goTo(List<String> moveList) throws IllegalMoveException
    {
        for (String moveSt : moveList) move(moveSt);
        return this;
    }

    public Board goToMove(List<PgnGame.Move> moveList) throws IllegalMoveException
    {
        for (PgnGame.Move move : moveList) move(move);
        return this;
    }
    
    /**
     * Executes one move with correctness checking.
     * 
     * @param san example: "Nf6"
     * @return
     * @throws IllegalMoveException 
     */
    public Board move(String san) throws IllegalMoveException
    {
        Color color = COLORS[ply % 2];
        PieceType promoteTo = null;
        int len = san.length();
        int chopLen = len;
        
        for (int i = len - 1; i >= 0; i--)
        {
            if (!Character.isLetterOrDigit(san.charAt(i))) chopLen--;
            else break;
        }
        
        san = san.substring(0, chopLen);
        len = chopLen;
        char lastChar = san.charAt(len - 1);
        
        if (san.equalsIgnoreCase("O-O"))
        {
            if (color == Color.WHITE && canMove(4, 6) && !isMovingIntoCheck(4, 6))
                return move(4, 6, promoteTo);
            
            else if (color == Color.BLACK && canMove(60, 62) && !isMovingIntoCheck(60, 62))
                return move(60, 62, null);
        
            throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
        }
        
        if (san.equalsIgnoreCase("O-O-O"))
        {
            if (color == Color.WHITE && canMove(4, 2) &&
                !isMovingIntoCheck(4, 2)) return move(4, 2, promoteTo);
            
            else if (color == Color.BLACK && canMove(60, 58) &&
                !isMovingIntoCheck(60, 58)) return move(60, 58, promoteTo);
        
            throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
        }
        
        if (Character.isUpperCase(lastChar)) // promotion
        {
            promoteTo = PieceType.get(lastChar);
            while (!Character.isDigit(san.charAt(san.length() - 1))) san = san.substring(0, --len);
        }
        
        int firstSquare;
        int endSquare = Square.get(san.substring(len - 2)).getLocation();
        char sanFirstChar = san.charAt(0);
        
        if (Character.isLowerCase(sanFirstChar)) // pawn move
        {
            firstSquare = Square.get(new String(new char[] {sanFirstChar, '2'})).getLocation();
            
            for (int i = firstSquare; i < 56; i += 8)
            {
                Piece piece = position[i];
                
                if (piece != null && piece.getType() == PieceType.PAWN &&
                    piece.getColor() == color && canMove(i, endSquare) &&
                    !isMovingIntoCheck(i, endSquare))
                    return move(i, endSquare, promoteTo);
            }
        
            throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
        }
        
        String sanStart = san.substring(0, len - 2); // destination chopped off
        int sanStartLen = sanStart.length();
        char sanStartLast = sanStart.charAt(sanStartLen - 1);
        
        if (sanStartLast == 'x' || !Character.isLetterOrDigit(sanStartLast))
            sanStart = sanStart.substring(0, sanStartLen - 1); // chop capture symbol
        
        PieceType pieceType = PieceType.get(sanStart.charAt(0));
        String disambig = sanStart.length() > 1 ? sanStart.substring(1) : null;
        
        if (disambig != null)
        {
            int disambigLen = disambig.length();
            if (disambigLen == 2) return move(Square.get(disambig).getLocation(), endSquare, promoteTo);
            char disambigChar = disambig.charAt(0);
            
            if (Character.isLetter(disambigChar)) // file
            {
                firstSquare = Square.get(new String(new char[] {disambigChar, '1'})).getLocation();
            
                for (int i = firstSquare; i < 64; i += 8)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType &&
                        piece.getColor() == color && canMove(i, endSquare) &&
                        !isMovingIntoCheck(i, endSquare))
                        return move(i, endSquare, promoteTo);
                }
            }
            
            else // rank
            {
                firstSquare = Square.get(new String(new char[] {'a', disambigChar})).getLocation();
                
                for (int i = firstSquare; i < firstSquare + 8; i++)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType &&
                        piece.getColor() == color && canMove(i, endSquare) &&
                        !isMovingIntoCheck(i, endSquare))
                        return move(i, endSquare, promoteTo);
                }
            }
        
            throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
        }
        
        for (int i = 0; i < 64; i++)
        {
            Piece piece = position[i];
            
            if (piece != null && piece.getType() == pieceType &&
                piece.getColor() == color && i != endSquare &&
                canMove(i, endSquare) && !isMovingIntoCheck(i, endSquare))
                return move(i, endSquare, promoteTo);
        }
        
        throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
    }
    
    /**
     * 
     * @param san example: "N8f6"
     * @return example: "Ngf6+"
     * @throws IllegalMoveException 
     */
    public String normalize(String san) throws IllegalMoveException
    {
        Color color = COLORS[ply % 2];
        PieceType promoteTo = null;
        int len = san.length();
        int chopLen = len;
        
        for (int i = len - 1; i >= 0; i--)
        {
            if (!Character.isLetterOrDigit(san.charAt(i))) chopLen--;
            else break;
        }
        
        san = san.substring(0, chopLen);
        len = chopLen;
        char lastChar = san.charAt(len - 1);
        
        if (san.equalsIgnoreCase("O-O"))
        {
            if (color == Color.WHITE) return coordToSan(4, 6, promoteTo);
            else return coordToSan(60, 62, null);
        }
        
        if (san.equalsIgnoreCase("O-O-O"))
        {
            if (color == Color.WHITE) return coordToSan(4, 2, promoteTo);
            else return coordToSan(60, 58, promoteTo);
        }
        
        if (Character.isUpperCase(lastChar)) // promotion
        {
            promoteTo = PieceType.get(lastChar);
            while (!Character.isDigit(san.charAt(san.length() - 1))) san = san.substring(0, --len);
        }
        
        int firstSquare;
        int endSquare = Square.get(san.substring(len - 2)).getLocation();
        char sanFirstChar = san.charAt(0);
        
        if (Character.isLowerCase(sanFirstChar)) // pawn move
        {
            firstSquare = Square.get(new String(new char[] {sanFirstChar, '2'})).getLocation();
            
            for (int i = firstSquare; i < 56; i += 8)
            {
                Piece piece = position[i];
                
                if (piece != null && piece.getType() == PieceType.PAWN &&
                    piece.getColor() == color && moveTest(i, endSquare))
                    return coordToSan(i, endSquare, promoteTo);
            }
        
            throw new IllegalMoveException("illegal move: '" + san +
                "' at ply " + (ply + 1));
        }
        
        String sanStart = san.substring(0, len - 2); // destination chopped off
        int sanStartLen = sanStart.length();
        char sanStartLast = sanStart.charAt(sanStartLen - 1);
        
        if (sanStartLast == 'x' || !Character.isLetterOrDigit(sanStartLast))
            sanStart = sanStart.substring(0, sanStartLen - 1); // chop capture symbol
        
        PieceType pieceType = PieceType.get(sanStart.charAt(0));
        String disambig = sanStart.length() > 1 ? sanStart.substring(1) : null;
        
        if (disambig != null)
        {
            int disambigLen = disambig.length();
            
            if (disambigLen == 2)
                return coordToSan(Square.get(disambig).getLocation(), endSquare,
                    promoteTo);
            
            char disambigChar = disambig.charAt(0);
            
            if (Character.isLetter(disambigChar)) // file
            {
                firstSquare = Square.get(new String(new char[] {disambigChar, '1'})).getLocation();
            
                for (int i = firstSquare; i < 64; i += 8)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType && piece.getColor() == color &&
                            moveTest(i, endSquare))
                        return coordToSan(i, endSquare, promoteTo);
                }
            }
            
            else // rank
            {
                firstSquare = Square.get(new String(new char[] {'a', disambigChar})).getLocation();
                
                for (int i = firstSquare; i < firstSquare + 8; i++)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType &&
                        piece.getColor() == color && moveTest(i, endSquare))
                        return coordToSan(i, endSquare, promoteTo);
                }
            }
        
            throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
        }
        
        for (int i = 0; i < 64; i++)
        {
            Piece piece = position[i];
            
            if (piece != null && piece.getType() == pieceType &&
                piece.getColor() == color && moveTest(i, endSquare))
                return coordToSan(i, endSquare, promoteTo);
        }
        
        throw new IllegalMoveException("illegal move: '" + san + "' at ply " + (ply + 1));
    }
    
    public String coordToSan(int start, int end, PieceType promoteTo)
        throws IllegalMoveException
    {
        Color moveColor = COLORS[ply % 2];
        Color otherColor = COLORS[(ply + 1) % 2];
        PieceType pieceType;
        String captureSt = position[end] == null ? "" : "x";
        StringBuilder ret = new StringBuilder();
        
        try { pieceType = position[start].getType(); }
        
        catch (NullPointerException e)
        {
            throw new IllegalMoveException("illegal move at ply " + (ply + 1));
        }
        
        if (pieceType == PieceType.PAWN)
        {
            int span = end - start > 0 ? end - start : start - end;
            
            if (span % 8 != 0)
            {
                switch (start % 8)
                {
                    case 0: ret.append("ax"); break;
                    case 1: ret.append("bx"); break;
                    case 2: ret.append("cx"); break;
                    case 3: ret.append("dx"); break;
                    case 4: ret.append("ex"); break;
                    case 5: ret.append("fx"); break;
                    case 6: ret.append("gx"); break;
                    case 7: ret.append("hx"); break;
                }
            }
            
            move(start, end, promoteTo);
            
            ret.append(Square.get(end));
            int endRank = end/8;
            
            if (endRank == 0 || endRank == 7)
                ret.append("=").append(promoteTo).append(getCheckSymbol(otherColor));
            
            return ret.toString();
        }
        
        if (pieceType == PieceType.KING)
        {
            if ((start == 4 && end == 6) || (start == 60 && end == 62))
            {
                move(start, end, null);
                return "O-O" + getCheckSymbol(otherColor);
            }
            
            if ((start == 4 && end == 2) || (start == 60 && end == 58))
            {
                move(start, end, null);
                return "O-O-O" + getCheckSymbol(otherColor);
            }
            
            move(start, end, null);
            
            return ret.append("K").append(captureSt).
                append(Square.get(end)).toString();
        }
        
        boolean disambigRank = false;
        boolean disambigFile = false;
        int candidateCount = 0;
        int candidates[] = new int[10]; // max number of same piece types
        
        for (int i = 0; i < 64; i++)
        {
            if (position[i] != null && position[i].getColor() == moveColor &&
                position[i].getType() == pieceType && canMove(i, end))
                candidates[candidateCount++] = i;
        }
        
        while (--candidateCount > 0)
        {
            int candidate1 = candidates[candidateCount];
            
            for (int i = 0; i < candidateCount; i++)
            {
                int candidate2 = candidates[i];
                
                if (disambigFile == false)
                    if ((candidate2 - candidate1) % 8 != 0) disambigFile = true;
                
                else if (disambigRank == false)
                    if ((candidate2 - candidate1) % 8 == 0) disambigRank = true;
            }
        }
        
        ret.append(pieceType);
        
        if (disambigFile)
        {
            switch (start % 8)
            {
                case 0: ret.append("a"); break;
                case 1: ret.append("b"); break;
                case 2: ret.append("c"); break;
                case 3: ret.append("d"); break;
                case 4: ret.append("e"); break;
                case 5: ret.append("f"); break;
                case 6: ret.append("g"); break;
                case 7: ret.append("h"); break;
            }
        }
        
        if (disambigRank)
        {
            switch (start / 8)
            {
                case 0: ret.append("1"); break;
                case 1: ret.append("2"); break;
                case 2: ret.append("3"); break;
                case 3: ret.append("4"); break;
                case 4: ret.append("5"); break;
                case 5: ret.append("6"); break;
                case 6: ret.append("7"); break;
                case 7: ret.append("8"); break;
            }
        }
        
        move(start, end, null);
        
        return ret.append(captureSt).append(Square.get(end)).
            append(getCheckSymbol(otherColor)).toString();
    }
    
    /**
     * 
     * @param move example: "e2e4"
     * @return 
     */
    public String coordToSan(String move) throws IllegalMoveException
    {
        int start = Square.get(move.substring(0, 2)).getLocation();
        int end = Square.get(move.substring(2, 4)).getLocation();
        
        PieceType promoteTo = move.length() > 4 ? PieceType.get(move.substring(4,5)) : null; // ?
        
        return coordToSan(start, end, promoteTo);
    }
    
    /**
     * 
     * @param color
     * @return 
     */
    private String getCheckSymbol(Color color)
    {
        if (isInCheck(color,
            new int[] {color == Color.WHITE ? whiteKingLoc : blackKingLoc}))
        {
            for (int i = 0; i < 64; i++)
            {
                Piece piece = position[i];
                
                if (piece != null && piece.getColor() == color)
                {
                    switch (piece.getType())
                    {
                        case PAWN:
                            if (color == Color.WHITE)
                            {
                                if (moveTest(i, i + 7) ||
                                    moveTest(i, i + 8) ||
                                    moveTest(i, i + 9))
                                    return "+";
                            }
                            
                            else
                            {
                                if (moveTest(i, i - 7) ||
                                    moveTest(i, i - 8) ||
                                    moveTest(i, i - 9))
                                    return "+";
                            }
                            
                            break;
                                
                        case ROOK:
                            for (int j = 1; j < 7; j++)
                            {
                                if (i + j < 64 && moveTest(i, i + j))
                                    return "+";
                                
                                if (i - j >= 0 && moveTest(i, i - j))
                                    return "+";
                            }
                            
                            for (int j = 8; j + i < 64; j += 8)
                                if (moveTest(i, i + j)) return "+";
                            
                            for (int j = 8; i - j >= 0; j += 8)
                                if (moveTest(i, i - j)) return "+";
                            
                            break;
                            
                        case KNIGHT:
                            for (int j = 0; j < NMOVES.length; j++)
                            {
                                int dest = i + NMOVES[j];
                                
                                if (dest >= 0 && dest < 64 &&
                                    moveTest(i, dest))
                                    return "+";
                            }
                            
                            break;
                            
                        case BISHOP:
                            for (int j = 7; j + i < 64; j += 7)
                                if (moveTest(i, i + j)) return "+";
                            
                            for (int j = 9; j + i < 64; j += 9)
                                if (moveTest(i, i + j)) return "+";
                            
                            for (int j = 7; i - j >= 0; j += 7)
                                if (moveTest(i, i - j)) return "+";
                            
                            for (int j = 9; i - j >= 0; j += 9)
                                if (moveTest(i, i - j)) return "+";
                            
                            break;
                            
                        case QUEEN:
                            for (int j = 1; j < 7; j++)
                            {
                                if (i + j < 64 && moveTest(i, i + j))
                                    return "+";
                                
                                if (i - j >= 0 && moveTest(i, i - j))
                                    return "+";
                            }
                            
                            for (int j = 8; j + i < 64; j += 8)
                                if (moveTest(i, i + j)) return "+";
                            
                            for (int j = 8; i - j >= 0; j += 8)
                                if (moveTest(i, i - j)) return "+";
                            
                            for (int j = 7; j + i < 64; j += 7)
                                if (moveTest(i, i + j)) return "+";
                            
                            for (int j = 9; j + i < 64; j += 9)
                                if (moveTest(i, i + j)) return "+";
                            
                            for (int j = 7; i - j >= 0; j += 7)
                                if (moveTest(i, i - j)) return "+";
                            
                            for (int j = 9; i - j >= 0; j += 9)
                                if (moveTest(i, i - j)) return "+";
                            
                            break;
                            
                        case KING:
                            if (moveTest(i, i + 1) ||
                                moveTest(i, i + 7) ||
                                moveTest(i, i + 8) ||
                                moveTest(i, i + 9) ||
                                moveTest(i, i - 1) ||
                                moveTest(i, i - 7) ||
                                moveTest(i, i - 8) ||
                                moveTest(i, i - 9))
                                return "+";
                    }
                }
            }
            
            return "#";
        }
        
        return "";
    }
    
    public String toFen()
    {
        StringBuilder ret = new StringBuilder();
        
        for (int i = 56; i >= 0; i -= 8)
        {
            int emptyCounter = 0;
            
            for (int j = 0; j < 8; j++)
            {
                if (position[i + j] != null)
                {
                    if (emptyCounter > 0)
                    {
                        ret.append(emptyCounter);
                        emptyCounter = 0;
                    }
                    
                    ret.append(position[i + j]);
                }
                
                else emptyCounter++;
            }
            
            if (emptyCounter > 0) ret.append(emptyCounter);
            if (i > 0) ret.append("/");
        }
        
        ret.append(" ").append(COLORS[ply % 2]).append(" ");
        
        if (!whiteCanCastleK && !whiteCanCastleQ && !blackCanCastleK &&
            !blackCanCastleQ)
            ret.append("- ");
        
        else
        {
            if (whiteCanCastleK) ret.append("K");
            if (whiteCanCastleQ) ret.append("Q");
            if (blackCanCastleK) ret.append("k");
            if (blackCanCastleQ) ret.append("q");
            ret.append(" ");
        }
        
        if (epCandidate == -1) ret.append("- ");
        else ret.append(Square.get(epCandidate).toString().toLowerCase()).append(" ");
        
        ret.append((ply - halfMoveClock)).append(" ").append(((ply + 2)/2));
        
        return ret.toString();
    }
    
    public String toShortFen()
    {
        StringBuilder ret = new StringBuilder();
        
        for (int i = 56; i >= 0; i -= 8)
        {
            int emptyCounter = 0;
            
            for (int j = 0; j < 8; j++)
            {
                if (position[i + j] != null)
                {
                    if (emptyCounter > 0)
                    {
                        ret.append(emptyCounter);
                        emptyCounter = 0;
                    }
                    
                    ret.append(position[i + j]);
                }
                
                else emptyCounter++;
            }
            
            if (emptyCounter > 0) ret.append(emptyCounter);
            if (i > 0) ret.append("/");
        }
        
        ret.append(" ").append(COLORS[ply % 2]).append(" ");
        
        if (!whiteCanCastleK && !whiteCanCastleQ && !blackCanCastleK &&
            !blackCanCastleQ)
            ret.append("-");
        
        else
        {
            if (whiteCanCastleK) ret.append("K");
            if (whiteCanCastleQ) ret.append("Q");
            if (blackCanCastleK) ret.append("k");
            if (blackCanCastleQ) ret.append("q");
        }
        
        return ret.toString();
    }
    
    /**
     * 
     * @param that
     * @return true if all pieces are in the same places
     */
    public boolean looseEquals(Board that)
    {
        return Arrays.equals(position, that.position);
    }
    
    /**
     * 
     * @param that
     * @return true if all pieces are in the same places and same side to move
     */
    public boolean positionEquals(Board that)
    {
//        if (whitePieceCount != that.whitePieceCount ||
//            blackPieceCount != that.blackPieceCount)
//            return false;
        
        return ply % 2 == that.ply % 2 &&
            Arrays.equals(position, that.position);
    }
    
    public PositionId positionId()
    {
        byte buf[] = new byte[65];
        
        for (int i = 0; i < 64; i++)
        {
            if (position[i] == null) buf[i] = -1;
            else buf[i] = position[i].toByte();
        }
        
        buf[64] = (byte)(ply % 2);
        
        return new PositionId(buf);
    }
    
    public int getWhitePieceCount() { return whitePieceCount; }
    public int getBlackPieceCount() { return blackPieceCount; }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 56; i >= 0; i -= 8)
        {
            sb.append("|");
            
            for (int j = 0; j < 8; j++)
            {
                Piece piece = position[i + j];
                sb.append(piece != null ? piece : " ").append("|");
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object other)
    {
        try
        {
            Board that = (Board)other;
            
            return Arrays.equals(position, that.position) &&
                ply == that.ply &&
                epCandidate == that.epCandidate &&
                whiteCanCastleK == that.whiteCanCastleK &&
                whiteCanCastleQ == that.whiteCanCastleQ &&
                blackCanCastleK == that.blackCanCastleK &&
                blackCanCastleQ == that.blackCanCastleQ &&
                halfMoveClock == that.halfMoveClock;
        }
        
        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        return Board.class.hashCode() ^ Arrays.hashCode(position) ^
            Short.hashCode(ply) ^ Byte.hashCode(epCandidate) ^
            Boolean.hashCode(whiteCanCastleK) ^
            Boolean.hashCode(whiteCanCastleQ) ^
            Boolean.hashCode(blackCanCastleK) ^
            Boolean.hashCode(blackCanCastleQ) ^
            Short.hashCode(halfMoveClock);
    }
}
