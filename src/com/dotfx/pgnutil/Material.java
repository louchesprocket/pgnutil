/*
 * The MIT License
 *
 * Copyright (c) 2026 Mark Chen <chen@dotfx.com>.
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

import java.util.HashMap;
import java.util.Map;

public class Material
{
    public enum Type
    {
        PAWN("p", 0),
        KNIGHT("N", 4),
        BISHOP("B", 8),
        ROOK("R", 12),
        QUEEN("Q", 16),
        KING("K", 20);

        public static final int MATERIAL_HASH_SEED = 0;

        private static final Map<String, Type> sigMap = new HashMap<>();

        private final String signifier;
        private final int hashShift;

        static { for (Type r : Type.values()) sigMap.put(r.toString(), r);}

        Type(String signifier, int hashShift)
        {
            this.signifier = signifier;
            this.hashShift = hashShift;
        }

        @Override
        public String toString() { return signifier; }
        public static Type get(String signifier) { return sigMap.get(signifier); }
        public static Type get(char signifier) { return sigMap.get(String.valueOf(signifier)); }

        /**
         *
         * @param checksum the running commutative checksum
         * @return updated commutative checksum of this PieceType
         */
        public int updateMaterialHash(int checksum) { return checksum + (1 << hashShift); }

        /**
         *
         * @param count number of this piece to include in the hash
         * @param checksum the running commutative checksum
         * @return
         */
        public int updateMaterialHash(int count, int checksum) { return checksum + (count << hashShift); }

        public int setMaterialHash(int count, int checksum)
        {
            return ((~(0xF << hashShift)) & checksum) | (count << hashShift);
        }

        public int getCount(int checksum) { return (checksum >> hashShift) & 0xF; }
    }

    public static class CountException extends Exception
    {
        public CountException(String msg) { super(msg); }
    }

    private int whitePieceCount, blackPieceCount;
    private int whiteMatHash = Type.MATERIAL_HASH_SEED;
    private int blackMatHash = Type.MATERIAL_HASH_SEED;

    public Material(String spec) throws CountException
    {
        if (!spec.contains("k")) spec += "k";
        if (!spec.contains("K")) spec += "K";

        parseMaterial(spec);
        verifyMaterial(whiteMatHash);
        verifyMaterial(blackMatHash);
    }

    /**
     *
     * @param spec example: "3P2N2p2b" for "3 white pawns and two white knights vs. 2 black pawns and two black
     *             bishops"; kings are optional, but, if present, may only appear with qty. 1
     *
     * @throws CountException
     */
    private void parseMaterial(String spec) throws CountException
    {
        int stLen = spec.length();
        int idx = 0;
        int idx1 = 0;
        int qty = 1;

        Piece piece;

        while (idx < stLen)
        {
            char c = spec.charAt(idx);

            switch (c)
            {
                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                    idx1 = idx + 1;
                    while (Character.isDigit(spec.charAt(idx1))) idx1++;
                    qty = Integer.parseInt(spec.substring(idx, idx1));
                    idx = idx1;
                    continue;

                case 'P': case 'N': case 'B': case 'R': case 'Q': case 'K':
                case 'p': case 'n': case 'b': case 'r': case 'q': case 'k':
                    try
                    {
                        piece = Piece.get(c);

                        if (piece.getColor() == Color.WHITE)
                        {
                            whitePieceCount += qty;
                            whiteMatHash = piece.getType().updateMaterialHash(qty, whiteMatHash);
                        }

                        else
                        {
                            blackPieceCount += qty;
                            blackMatHash = piece.getType().updateMaterialHash(qty, blackMatHash);
                        }
                    }

                    catch (NullPointerException e) { throw new CountException("invalid piece '" + c + "'"); }

                    qty = 1;
                    break;

                default: throw new CountException("invalid character '" + c + "'");
            }

            idx++;
        }
    }

    private void verifyMaterial(int hash) throws CountException
    {
        for (Type type : Type.values())
        {
            int count = type.getCount(hash);

            switch (type)
            {
                case PAWN:
                    if (count > 8) throw new CountException("piece count " + count + " is invalid for type " + type);
                    break;

                case QUEEN:
                    if (count > 9) throw new CountException("piece count " + count + " is invalid for type " + type);
                    break;

                case KING:
                    if (count != 1) throw new CountException("piece count " + count + " is invalid for type " + type);
                    break;

                default:
                    if (count < 0 || count > 10)
                        throw new CountException("piece count " + count + " is invalid for type " + type);

            }
        }
    }

    public int getWhitePieceCount() { return whitePieceCount; }
    public int getWhiteMatHash() { return whiteMatHash; }
    public int getBlackPieceCount() { return blackPieceCount; }
    public int getBlackMatHash() { return blackMatHash; }

    public boolean equalsBoardMaterial(Board<?> board)
    {
        if (whitePieceCount != board.getWhitePieceCount() ||
                blackPieceCount != board.getBlackPieceCount()) return false;

        int boardWhiteMat = Material.Type.MATERIAL_HASH_SEED;
        int boardBlackMat = Material.Type.MATERIAL_HASH_SEED;

        for (Piece piece : board.getPosition())
        {
            if (piece != null)
            {
                if (piece.getColor() == Color.WHITE)
                    boardWhiteMat = piece.getType().updateMaterialHash(boardWhiteMat);

                else boardBlackMat = piece.getType().updateMaterialHash(boardBlackMat);
            }
        }

        return boardWhiteMat == whiteMatHash && boardBlackMat == blackMatHash;
    }

    public boolean equalsBoardMatIgnoreColors(Board<?> board)
    {
        if ((whitePieceCount != board.getWhitePieceCount() && whitePieceCount != board.getBlackPieceCount()) ||
            (blackPieceCount != board.getBlackPieceCount() && blackPieceCount != board.getWhitePieceCount()))
            return false;

        int boardWhiteMat = Material.Type.MATERIAL_HASH_SEED;
        int boardBlackMat = Material.Type.MATERIAL_HASH_SEED;

        for (Piece piece : board.getPosition())
        {
            if (piece != null)
            {
                if (piece.getColor() == Color.WHITE)
                    boardWhiteMat = piece.getType().updateMaterialHash(boardWhiteMat);

                else boardBlackMat = piece.getType().updateMaterialHash(boardBlackMat);
            }
        }

        return (boardWhiteMat == whiteMatHash && boardBlackMat == blackMatHash) ||
                (boardWhiteMat == blackMatHash && boardBlackMat == whiteMatHash);
    }

    public boolean equalsBoardMaterialDiff(Board<?> board)
    {
        int boardWhiteMat = Material.Type.MATERIAL_HASH_SEED;
        int boardBlackMat = Material.Type.MATERIAL_HASH_SEED;

        for (Piece piece : board.getPosition())
        {
            if (piece != null)
            {
                if (piece.getColor() == Color.WHITE)
                    boardWhiteMat = piece.getType().updateMaterialHash(boardWhiteMat);

                else boardBlackMat = piece.getType().updateMaterialHash(boardBlackMat);
            }
        }

        return boardWhiteMat - boardBlackMat == whiteMatHash - blackMatHash;
    }

    public boolean equalsBoardMatDiffIgnoreColors(Board<?> board)
    {
        int boardWhiteMat = Material.Type.MATERIAL_HASH_SEED;
        int boardBlackMat = Material.Type.MATERIAL_HASH_SEED;

        for (Piece piece : board.getPosition())
        {
            if (piece != null)
            {
                if (piece.getColor() == Color.WHITE)
                    boardWhiteMat = piece.getType().updateMaterialHash(boardWhiteMat);

                else boardBlackMat = piece.getType().updateMaterialHash(boardBlackMat);
            }
        }

        return (boardWhiteMat - boardBlackMat == whiteMatHash - blackMatHash) ||
                (boardWhiteMat - boardBlackMat == blackMatHash - whiteMatHash);
    }
}
