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
 * board representation using SAN
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class Board<T extends Board<T>> implements Comparable<T>
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
        
        public final int getLocation() { return location; }
        public static Square get(int square) { return squares[square]; }

        public static Square get(String s) throws IllegalMoveException
        {
            return get(s.charAt(0), s.charAt(1));
        }

        public static Square get(char file, char rank) throws IllegalMoveException
        {
            int rankTerm;

            switch (rank)
            {
                case '1': rankTerm = 0; break;
                case '2': rankTerm = 8; break;
                case '3': rankTerm = 16; break;
                case '4': rankTerm = 24; break;
                case '5': rankTerm = 32; break;
                case '6': rankTerm = 40; break;
                case '7': rankTerm = 48; break;
                case '8': rankTerm = 56; break;
                default: throw new IllegalMoveException("illegal square: '" + file + rank + "'");
            }

            switch (file)
            {
                case 'a': return squares[rankTerm];
                case 'b': return squares[rankTerm + 1];
                case 'c': return squares[rankTerm + 2];
                case 'd': return squares[rankTerm + 3];
                case 'e': return squares[rankTerm + 4];
                case 'f': return squares[rankTerm + 5];
                case 'g': return squares[rankTerm + 6];
                case 'h': return squares[rankTerm + 7];
                default: throw new IllegalMoveException("illegal square: '" + file + rank + "'");
            }
        }
        
        @Override
        public String toString() { return name().toLowerCase(); }
    }
    
    public enum PieceType
    {
        PAWN("p"),
        ROOK("R"),
        KNIGHT("N"),
        BISHOP("B"),
        QUEEN("Q"),
        KING("K");
        
        private static final Map<String,PieceType> sigMap = new HashMap<>();
        private final String signifier;
        
        static
        {
            for (PieceType r : PieceType.values()) sigMap.put(r.toString(), r);
        }
        
        PieceType(String signifier) { this.signifier = signifier; }
        @Override public String toString() { return signifier; }
        
        public static PieceType get(String signifier)
        {
            return sigMap.get(signifier);
        }
        public static PieceType get(char signifier) { return sigMap.get(String.valueOf(signifier)); }
    }
    
    public enum Piece
    {
        WHITE_PAWN(Color.WHITE, PieceType.PAWN, 0),
        WHITE_ROOK(Color.WHITE, PieceType.ROOK, 1),
        WHITE_KNIGHT(Color.WHITE, PieceType.KNIGHT, 2),
        WHITE_BISHOP(Color.WHITE, PieceType.BISHOP, 3),
        WHITE_QUEEN(Color.WHITE, PieceType.QUEEN, 4),
        WHITE_KING(Color.WHITE, PieceType.KING, 5),

        BLACK_PAWN(Color.BLACK, PieceType.PAWN, 6),
        BLACK_ROOK(Color.BLACK, PieceType.ROOK, 7),
        BLACK_KNIGHT(Color.BLACK, PieceType.KNIGHT, 8),
        BLACK_BISHOP(Color.BLACK, PieceType.BISHOP, 9),
        BLACK_QUEEN(Color.BLACK, PieceType.QUEEN, 10),
        BLACK_KING(Color.BLACK, PieceType.KING, 11);

        private final PieceType pieceType;
        private final Color color;
        private final int code;
        
        Piece(Color color, PieceType pieceType, int code)
        {
            this.pieceType = pieceType;
            this.color = color;
            this.code = code;
        }

        public static Piece get(Color color, PieceType type)
        {
            if (color == Color.WHITE)
                switch (type)
                {
                    case PAWN: return WHITE_PAWN;
                    case ROOK: return WHITE_ROOK;
                    case KNIGHT: return WHITE_KNIGHT;
                    case BISHOP: return WHITE_BISHOP;
                    case QUEEN: return WHITE_QUEEN;
                    case KING: return WHITE_KING;
                    default: return null;
                }

            else
                switch (type)
                {
                    case PAWN: return BLACK_PAWN;
                    case ROOK: return BLACK_ROOK;
                    case KNIGHT: return BLACK_KNIGHT;
                    case BISHOP: return BLACK_BISHOP;
                    case QUEEN: return BLACK_QUEEN;
                    case KING: return BLACK_KING;
                    default: return null;
                }
        }

        public static Piece get(Character p)
        {
            switch (p)
            {
                case 'P': return WHITE_PAWN;
                case 'p': return BLACK_PAWN;
                case 'R': return WHITE_ROOK;
                case 'r': return BLACK_ROOK;
                case 'N': return WHITE_KNIGHT;
                case 'n': return BLACK_KNIGHT;
                case 'B': return WHITE_BISHOP;
                case 'b': return BLACK_BISHOP;
                case 'Q': return WHITE_QUEEN;
                case 'q': return BLACK_QUEEN;
                case 'K': return WHITE_KING;
                case 'k': return BLACK_KING;
                default: return null;
            }
        }
        
        public final PieceType getType() { return pieceType; }
        public final Color getColor() { return color; }
        public final byte toByte() { return (byte)code; }
        
        @Override
        public String toString()
        {
            return color == Color.WHITE ? getType().toString().toUpperCase() :
                getType().toString().toLowerCase();
        }
    }
    
    private static final int whiteKCastleSquares[] = new int[] {4, 5, 6};
    private static final int whiteQCastleSquares[] = new int[] {2, 3, 4};
    private static final int blackKCastleSquares[] = new int[] {60, 61, 62};
    private static final int blackQCastleSquares[] = new int[] {58, 59, 60};
    private static final Color[] COLORS = new Color[] {Color.WHITE, Color.BLACK};
    private static final int[] NMOVES = new int[] {6, 15, 17, 10, -6, -15, -17, -10};

    private boolean whiteCanCastleQ;
    private boolean whiteCanCastleK;
    private boolean blackCanCastleQ;
    private boolean blackCanCastleK;
    private byte whiteKingLoc;
    private byte blackKingLoc;
    private byte epCandidate; // location of the capture square; -1 if none
    private short halfMoveClock; // ply of last capture or pawn move
    short ply; // zero at initial position
    byte whitePieceCount = 16;
    byte blackPieceCount = 16;
    final Piece position[];
    
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
            position[i] = Piece.WHITE_PAWN;
            position[i + 40] = Piece.BLACK_PAWN;
        }
        
        position[0] = Piece.WHITE_ROOK;
        position[1] = Piece.WHITE_KNIGHT;
        position[2] = Piece.WHITE_BISHOP;
        position[3] = Piece.WHITE_QUEEN;
        position[4] = Piece.WHITE_KING;
        position[5] = Piece.WHITE_BISHOP;
        position[6] = Piece.WHITE_KNIGHT;
        position[7] = Piece.WHITE_ROOK;
        
        position[56] = Piece.BLACK_ROOK;
        position[57] = Piece.BLACK_KNIGHT;
        position[58] = Piece.BLACK_BISHOP;
        position[59] = Piece.BLACK_QUEEN;
        position[60] = Piece.BLACK_KING;
        position[61] = Piece.BLACK_BISHOP;
        position[62] = Piece.BLACK_KNIGHT;
        position[63] = Piece.BLACK_ROOK;
    }
    
    public Board(Piece position[], short ply, byte epCandidate, boolean whiteCanCastleQ, boolean whiteCanCastleK,
        boolean blackCanCastleQ, boolean blackCanCastleK, byte whiteKingLoc, byte blackKingLoc, short halfMoveClock)
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

    /**
     * This constructor figures out where the kings are.
     *
     * @param position
     * @param ply
     * @param epCandidate
     * @param whiteCanCastleQ
     * @param whiteCanCastleK
     * @param blackCanCastleQ
     * @param blackCanCastleK
     * @param halfMoveClock
     * @throws InvalidPositionException
     */
    public Board(Piece position[], short ply, Square epCandidate, boolean whiteCanCastleQ, boolean whiteCanCastleK,
                 boolean blackCanCastleQ, boolean blackCanCastleK, short halfMoveClock)
            throws InvalidPositionException
    {
        byte whiteCount = 0;
        byte blackCount = 0;

        whiteKingLoc = blackKingLoc = -1;

        this.position = position;
        this.ply = ply;
        this.whiteCanCastleQ = whiteCanCastleQ;
        this.whiteCanCastleK = whiteCanCastleK;
        this.blackCanCastleQ = blackCanCastleQ;
        this.blackCanCastleK = blackCanCastleK;
        this.halfMoveClock = halfMoveClock;

        this.epCandidate = epCandidate == null ? -1 : (byte)epCandidate.getLocation();

        for (int i = 0; i < 64; i++)
        {
            if (position[i] != null)
            {
                if (position[i].getColor() == Color.WHITE)
                {
                    whiteCount++;

                    if (position[i] == Piece.WHITE_KING)
                    {
                        if (whiteKingLoc != -1) throw new InvalidPositionException("too many kings");
                        whiteKingLoc = (byte)i;
                    }
                }

                else
                {
                    blackCount++;

                    if (position[i] == Piece.BLACK_KING)
                    {
                        if (blackKingLoc != -1) throw new InvalidPositionException("too many kings");
                        blackKingLoc = (byte)i;
                    }
                }
            }
        }

        if (whiteKingLoc == -1 || blackKingLoc == -1) throw new InvalidPositionException("missing king");

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

    public final int getPly() { return ply; }
    public final Board copy() { return new Board(this); }
    public final Square getEpSquare() { return Square.get(epCandidate); }
    public final Piece[] getPosition() { return position; }
    
    public final boolean canMove(int start, int end)
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
                    if (diff == 16 && endRank == 3) return destPiece == null && position[start + 8] == null;
                    
                    if ((diff == 7 || diff == 9) && hi/8 - low/8 == 1)
                        return (destPiece != null || end == epCandidate) && !destSameColor;
                }
                
                else
                {
                    if (diff == -8) return destPiece == null;
                    if (diff == -16 && endRank == 4) return destPiece == null && position[start - 8] == null;
                    
                    if ((diff == -7 || diff == -9) && hi/8 - low/8 == 1)
                        return (destPiece != null || end == epCandidate) && !destSameColor;
                }

                return false;

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
                        if (i/8 - (i - 7)/8 != 1 || position[i] != null) return false;
                    
                    return !destSameColor && i/8 - (i - 7)/8 == 1;
                }
                
                else if (span % 9 == 0)
                {
                    for (i = low + 9; i < hi; i += 9)
                        if (i/8 - (i - 9)/8 != 1 || position[i] != null) return false;
                    
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
                        if (i/8 - (i - 7)/8 != 1 || position[i] != null) return false;
                    
                    return !destSameColor && i/8 - (i - 7)/8 == 1;
                }
                
                else if (span % 9 == 0) // same diagonal
                {
                    for (i = low + 9; i < hi; i += 9)
                        if (i/8 - (i - 9)/8 != 1 || position[i] != null) return false;
                    
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
                                return position[5] == null && position[6] == null && whiteCanCastleK;
                            
                            else // queen side
                                return position[1] == null && position[2] == null &&
                                    position[3] == null && whiteCanCastleQ;
                        }
                        
                        else
                        {
                            if (end - start == 2) // king side
                                return position[61] == null && position[62] == null && blackCanCastleK;
                            
                            else // queen side
                                return position[57] == null && position[58] == null &&
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
     * @return true if the mover is moving into check or (in case of castling) out of or through check
     */
    private final boolean isMovingIntoCheck(int start, int end)
    {
        Piece movingPiece = position[start];
        Color color = movingPiece.getColor();
        int span = end > start ? end - start : start - end;
        
        // en passant capture
        if (end == epCandidate && movingPiece.getType() == PieceType.PAWN)
        {
            int savedCaptureLoc = color == Color.WHITE ? epCandidate - 8 : epCandidate + 8;
            Piece savedCapture = position[savedCaptureLoc];
            
            position[end] = position[start];
            position[start] = null;
            position[savedCaptureLoc] = null;
            
            boolean ret = isInCheck(color, new int[] {color == Color.WHITE ? whiteKingLoc : blackKingLoc});
            
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

                return isInCheck(color, new int[] {color == Color.WHITE ? whiteKingLoc : blackKingLoc});
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
    
    private final boolean isInCheck(Color color, int squares[])
    {
        int savedKingLoc = color == Color.WHITE ? whiteKingLoc : blackKingLoc;
        
        for (int square : squares)
        {
            Piece saved = position[square]; // save contents of test square
            position[square] = position[savedKingLoc]; // move king to test square
            
            if (savedKingLoc != square)
                position[savedKingLoc] = null; // empty king's previous square
            
            for (int i = 0; i < 64; i++)
                if (position[i] != null && position[i].getColor() != color && canMove(i, square)) return true;
            
            position[savedKingLoc] = position[square]; // restore king to origin
            position[square] = saved; // restore test square
        }
        
        return false;
    }
    
    public final boolean moveTest(int start, int end)
    {
        return end >= 0 && end < 64 && canMove(start, end) && !isMovingIntoCheck(start, end);
    }
    
    /**
     * Does not do any kind of correctness checking.
     * 
     * @param start
     * @param end
     * @param promoteTo
     * @return 
     */
    public final Board move(int start, int end, PieceType promoteTo) throws IllegalMoveException
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
                        position[end] = Piece.get(piece.getColor(), promoteTo);
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
    
    public final Board copyAndMove(int start, int end, PieceType promoteTo) throws IllegalMoveException
    {
        return copy().move(start, end, promoteTo);
    }
    
    public final Board move(PgnGame.Move move) throws IllegalMoveException
    {
        return move(move.getMoveOnly());
    }
    
    public final Board move(List<TreeNode> moveList) throws IllegalMoveException
    {
        for (TreeNode node : moveList) move(node.getMoveText());
        return this;
    }
    
    public final Board goTo(List<String> moveList) throws IllegalMoveException
    {
        for (String moveSt : moveList) move(moveSt);
        return this;
    }

    public final Board goToMove(List<PgnGame.Move> moveList) throws IllegalMoveException
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
    public final Board move(String san) throws IllegalMoveException
    {
        Color color = (ply ^ 1) > ply ? Color.WHITE : Color.BLACK;
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

            throw new IllegalMoveException(getMoveErrorMsg(san, ply));
        }
        
        if (san.equalsIgnoreCase("O-O-O"))
        {
            if (color == Color.WHITE && canMove(4, 2) &&
                !isMovingIntoCheck(4, 2)) return move(4, 2, promoteTo);
            
            else if (color == Color.BLACK && canMove(60, 58) &&
                !isMovingIntoCheck(60, 58)) return move(60, 58, promoteTo);
        
            throw new IllegalMoveException(getMoveErrorMsg(san, ply));
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
            firstSquare = Square.get(sanFirstChar, '2').getLocation();
            
            for (int i = firstSquare; i < 56; i += 8)
            {
                Piece piece = position[i];
                
                if (piece != null && piece.getType() == PieceType.PAWN &&
                    piece.getColor() == color && canMove(i, endSquare) &&
                    !isMovingIntoCheck(i, endSquare))
                    return move(i, endSquare, promoteTo);
            }
        
            throw new IllegalMoveException(getMoveErrorMsg(san, ply));
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
            if (disambig.length() == 2) return move(Square.get(disambig).getLocation(), endSquare, promoteTo);
            char disambigChar = disambig.charAt(0);
            
            if (Character.isLetter(disambigChar)) // file
            {
                firstSquare = Square.get(disambigChar, '1').getLocation();
            
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
                firstSquare = Square.get('a', disambigChar).getLocation();
                
                for (int i = firstSquare; i < firstSquare + 8; i++)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType &&
                        piece.getColor() == color && canMove(i, endSquare) &&
                        !isMovingIntoCheck(i, endSquare))
                        return move(i, endSquare, promoteTo);
                }
            }
        
            throw new IllegalMoveException(getMoveErrorMsg(san, ply));
        }
        
        for (int i = 0; i < 64; i++)
        {
            Piece piece = position[i];
            
            if (piece != null && piece.getType() == pieceType &&
                piece.getColor() == color && i != endSquare &&
                canMove(i, endSquare) && !isMovingIntoCheck(i, endSquare))
                return move(i, endSquare, promoteTo);
        }
        
        throw new IllegalMoveException(getMoveErrorMsg(san, ply));
    }
    
    /**
     * 
     * @param san example: "N8f6"
     * @return example: "Ngf6+"
     * @throws IllegalMoveException 
     */
    public final String normalize(String san, boolean showCheck) throws IllegalMoveException
    {
        Color color = (ply ^ 1) > ply ? Color.WHITE : Color.BLACK;
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
            if (color == Color.WHITE) return coordToSan(4, 6, promoteTo, showCheck);
            else return coordToSan(60, 62, null, showCheck);
        }
        
        if (san.equalsIgnoreCase("O-O-O"))
        {
            if (color == Color.WHITE) return coordToSan(4, 2, promoteTo, showCheck);
            else return coordToSan(60, 58, promoteTo, showCheck);
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
            firstSquare = Square.get(sanFirstChar, '2').getLocation();
            
            for (int i = firstSquare; i < 56; i += 8)
            {
                Piece piece = position[i];
                
                if (piece != null && piece.getType() == PieceType.PAWN &&
                    piece.getColor() == color && moveTest(i, endSquare))
                    return coordToSan(i, endSquare, promoteTo, showCheck);
            }
        
            throw new IllegalMoveException(getMoveErrorMsg(san, ply));
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
                    promoteTo, showCheck);
            
            char disambigChar = disambig.charAt(0);
            
            if (Character.isLetter(disambigChar)) // file
            {
                firstSquare = Square.get(disambigChar, '1').getLocation();
            
                for (int i = firstSquare; i < 64; i += 8)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType && piece.getColor() == color &&
                            moveTest(i, endSquare))
                        return coordToSan(i, endSquare, promoteTo, showCheck);
                }
            }
            
            else // rank
            {
                firstSquare = Square.get('a', disambigChar).getLocation();
                
                for (int i = firstSquare; i < firstSquare + 8; i++)
                {
                    Piece piece = position[i];

                    if (piece != null && piece.getType() == pieceType &&
                        piece.getColor() == color && moveTest(i, endSquare))
                        return coordToSan(i, endSquare, promoteTo, showCheck);
                }
            }
        
            throw new IllegalMoveException(getMoveErrorMsg(san, ply));
        }
        
        for (int i = 0; i < 64; i++)
        {
            Piece piece = position[i];
            
            if (piece != null && piece.getType() == pieceType &&
                piece.getColor() == color && moveTest(i, endSquare))
                return coordToSan(i, endSquare, promoteTo, showCheck);
        }
        
        throw new IllegalMoveException(getMoveErrorMsg(san, ply));
    }
    
    public final String coordToSan(int start, int end, PieceType promoteTo, boolean showCheck)
        throws IllegalMoveException
    {
        Color moveColor = (ply ^ 1) > ply ? Color.WHITE : Color.BLACK;
        Color otherColor = (ply ^ 1) > ply ? Color.BLACK : Color.WHITE;
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
                ret.append("=").append(promoteTo).append(showCheck ? getCheckSymbol(otherColor) : "");
            
            return ret.toString();
        }
        
        if (pieceType == PieceType.KING)
        {
            if ((start == 4 && end == 6) || (start == 60 && end == 62))
            {
                move(start, end, null);
                return "O-O" + (showCheck ? getCheckSymbol(otherColor) : "");
            }
            
            if ((start == 4 && end == 2) || (start == 60 && end == 58))
            {
                move(start, end, null);
                return "O-O-O" + (showCheck ? getCheckSymbol(otherColor) : "");
            }
            
            move(start, end, null);
            
            return ret.append("K").append(captureSt).
                append(Square.get(end)).toString();
        }
        
        boolean disambigRank = false;
        boolean disambigFile = false;
        int candidateCount = 0;
        int candidates[] = new int[10]; // max number of same piece type
        
        for (int i = 0; i < 64; i++)
        {
            if (position[i] != null && position[i].getColor() == moveColor &&
                position[i].getType() == pieceType && moveTest(i, end))
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

        return ret.append(captureSt).append(Square.get(end)).append(showCheck ? getCheckSymbol(otherColor) : "")
                .toString();
    }
    
    /**
     * 
     * @param move example: "e2e4"
     * @return 
     */
    public final String coordToSan(String move, boolean showCheck) throws IllegalMoveException
    {
        int start = Square.get(move.substring(0, 2)).getLocation();
        int end = Square.get(move.substring(2, 4)).getLocation();
        
        PieceType promoteTo = move.length() > 4 ? PieceType.get(move.substring(4, 5)) : null;
        return coordToSan(start, end, promoteTo, showCheck);
    }

    /**
     *
     * @param move example: "f2xe1Q"
     * @return example: "fxe1=Q"
     */
    public final String longToSan(String move, boolean showCheck) throws IllegalMoveException
    {
        int start, end;
        PieceType promoteTo = null;

        switch (move.charAt(0))
        {
            case 'R':
            case 'N':
            case 'B':
            case 'Q':
            case 'K':
                start = Square.get(move.substring(1, 3)).getLocation();
                end = Square.get(move.substring(4, 6)).getLocation();
                break;

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
                start = Square.get(move.substring(0, 2)).getLocation();
                end = Square.get(move.substring(3, 5)).getLocation();

                int promoteIdx = move.indexOf('=');
                if (promoteIdx > -1) promoteTo = PieceType.get(move.substring(promoteIdx + 1, promoteIdx + 2));
                break;

            default: throw new IllegalMoveException(move);
        }

        return coordToSan(start, end, promoteTo, showCheck);
    }
    
    /**
     * 
     * @param color
     * @return 
     */
    private final String getCheckSymbol(Color color)
    {
        if (isInCheck(color, new int[] {color == Color.WHITE ? whiteKingLoc : blackKingLoc}))
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
                                if (i + j < 64 && moveTest(i, i + j)) return "+";
                                if (i - j >= 0 && moveTest(i, i - j)) return "+";
                            }
                            
                            for (int j = 8; j + i < 64; j += 8) if (moveTest(i, i + j)) return "+";
                            for (int j = 8; i - j >= 0; j += 8) if (moveTest(i, i - j)) return "+";
                            
                            break;
                            
                        case KNIGHT:
                            for (int j = 0; j < NMOVES.length; j++)
                            {
                                int dest = i + NMOVES[j];
                                
                                if (dest >= 0 && dest < 64 && moveTest(i, dest)) return "+";
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
                                if (i + j < 64 && moveTest(i, i + j)) return "+";
                                if (i - j >= 0 && moveTest(i, i - j)) return "+";
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

    public static Board fromFen(String fenSt) throws InvalidFenException
    {
        String[] fen = fenSt.trim().split(" ");
        Piece[] position = new Piece[64];
        Arrays.fill(position, null);
        int file = 0, rank = 7;

        for (int stPos = 0; stPos < fen[0].length(); stPos++)
        {
            char currentChar = fen[0].charAt(stPos);

            switch (currentChar)
            {
                case 'P':
                case 'p':
                case 'R':
                case 'r':
                case 'N':
                case 'n':
                case 'B':
                case 'b':
                case 'Q':
                case 'q':
                case 'K':
                case 'k':
                    Piece piece = Piece.get(currentChar);
                    if (file > 7 || piece == null) throw new InvalidFenException("invalid FEN: '" + fenSt + "'");
                    position[rank * 8 + file] = piece;
                    file++;
                    break;

                case '/':
                    rank--;
                    if (rank < 0 || file != 8) throw new InvalidFenException("invalid FEN: '" + fenSt + "'");
                    file = 0;
                    break;

                default:
                    int skipSquares = Character.getNumericValue(currentChar);
                    file += skipSquares;
                    if (skipSquares < 1 || file > 8) throw new InvalidFenException("invalid FEN: '" + fenSt + "'");
            }
        }

        try
        {
            short currentPly = (short)((Short.parseShort(fen[5]) - 1) * 2 + (fen[1].equals("w") ? 0 : 1));

            return new Board(
                    position,
                    currentPly,
                    fen[3].equals("-") ? null : Square.get(fen[3]), // e.p. square
                    fen[2].contains("Q"), // castling possibilities . . .
                    fen[2].contains("K"),
                    fen[2].contains("q"),
                    fen[2].contains("k"),
                    (short)(currentPly - Short.parseShort(fen[4])) // half-move clock
            );
        }

        catch (IllegalMoveException | NumberFormatException | NullPointerException | IndexOutOfBoundsException |
               InvalidPositionException e)
        {
            throw new InvalidFenException("invalid FEN: '" + fenSt + "'");
        }
    }
    
    public final String toFen()
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
        
        ret.append(" ").append((ply ^ 1) > ply ? Color.WHITE : Color.BLACK).append(" ");
        if (!whiteCanCastleK && !whiteCanCastleQ && !blackCanCastleK && !blackCanCastleQ) ret.append("- ");
        
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
    
    public final String toShortFen()
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
        
        ret.append(" ").append((ply ^ 1) > ply ? Color.WHITE : Color.BLACK).append(" ");
        if (!whiteCanCastleK && !whiteCanCastleQ && !blackCanCastleK && !blackCanCastleQ) ret.append("-");
        
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
     * @return true if all pieces are in the same places and same side to move
     */
    public final boolean looseEquals(Board that)
    {
        return (ply & 1) == (that.ply & 1) && Arrays.equals(position, that.position);
    }
    
    /**
     * 
     * @param that
     * @return true if all pieces are in the same places and same side to move
     */
    public final boolean positionEquals(Board that)
    {
//        if (whitePieceCount != that.whitePieceCount ||
//            blackPieceCount != that.blackPieceCount)
//            return false;
        
        return (ply & 1) == (that.ply & 1) && (whitePieceCount == that.whitePieceCount) &&
                (blackPieceCount == that.blackPieceCount) && Arrays.equals(position, that.position);
    }
    
    public final PositionId positionId()
    {
        byte buf[] = new byte[65];
        
        for (int i = 0; i < 64; i++)
        {
            if (position[i] == null) buf[i] = -1;
            else buf[i] = position[i].toByte();
        }
        
        buf[64] = (byte)(ply & 1);
        
        return new PositionId(buf);
    }

    /**
     *
     * @param san the offending move
     * @param ply internal (zero-based) ply number
     * @return
     */
    private static String getMoveErrorMsg(String san, int ply)
    {
        return "illegal move '" + ((ply + 2) / 2) + "." + ((ply & 1) == 1 ? ".." : "") + san + "'";
    }
    
    public final int getWhitePieceCount() { return whitePieceCount; }
    public final int getBlackPieceCount() { return blackPieceCount; }
    
    @Override
    public final String toString()
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
    public int compareTo(Board that)
    {
        if (ply > that.ply) return 1;
        if (ply < that.ply) return -1;

        int posComp = Arrays.compare(position, that.position);
        if (posComp != 0) return posComp;

        if (epCandidate > that.epCandidate) return 1;
        if (epCandidate < that.epCandidate) return -1;

        if (whiteCanCastleK && !that.whiteCanCastleK) return 1;
        if (!whiteCanCastleK && that.whiteCanCastleK) return -1;

        if (whiteCanCastleQ && !that.whiteCanCastleQ) return 1;
        if (!whiteCanCastleQ && that.whiteCanCastleQ) return -1;

        if (blackCanCastleK && !that.blackCanCastleK) return 1;
        if (!blackCanCastleK && that.blackCanCastleK) return -1;

        if (blackCanCastleQ && !that.blackCanCastleQ) return 1;
        if (!blackCanCastleQ && that.blackCanCastleQ) return -1;

        return halfMoveClock - that.halfMoveClock;
    }
    
    @Override
    public boolean equals(Object other)
    {
        try
        {
            Board that = (Board)other;
            
            return ply == that.ply &&
                Arrays.equals(position, that.position) &&
                epCandidate == that.epCandidate &&
                whiteCanCastleK == that.whiteCanCastleK &&
                whiteCanCastleQ == that.whiteCanCastleQ &&
                blackCanCastleK == that.blackCanCastleK &&
                blackCanCastleQ == that.blackCanCastleQ &&
                halfMoveClock == that.halfMoveClock;
        }
        
        catch (ClassCastException | NullPointerException e) { return false; }
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
