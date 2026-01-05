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

public enum Piece
{
    WHITE_PAWN(Color.WHITE, Material.Type.PAWN, 0),
    WHITE_ROOK(Color.WHITE, Material.Type.ROOK, 1),
    WHITE_KNIGHT(Color.WHITE, Material.Type.KNIGHT, 2),
    WHITE_BISHOP(Color.WHITE, Material.Type.BISHOP, 3),
    WHITE_QUEEN(Color.WHITE, Material.Type.QUEEN, 4),
    WHITE_KING(Color.WHITE, Material.Type.KING, 5),

    BLACK_PAWN(Color.BLACK, Material.Type.PAWN, 6),
    BLACK_ROOK(Color.BLACK, Material.Type.ROOK, 7),
    BLACK_KNIGHT(Color.BLACK, Material.Type.KNIGHT, 8),
    BLACK_BISHOP(Color.BLACK, Material.Type.BISHOP, 9),
    BLACK_QUEEN(Color.BLACK, Material.Type.QUEEN, 10),
    BLACK_KING(Color.BLACK, Material.Type.KING, 11);

    private final Material.Type type;
    private final Color color;
    private final int code;

    Piece(Color color, Material.Type type, int code)
    {
        this.type = type;
        this.color = color;
        this.code = code;
    }

    public static Piece get(Color color, Material.Type type)
    {
        if (color == Color.WHITE)
            switch (type)
            {
                case PAWN:
                    return WHITE_PAWN;
                case ROOK:
                    return WHITE_ROOK;
                case KNIGHT:
                    return WHITE_KNIGHT;
                case BISHOP:
                    return WHITE_BISHOP;
                case QUEEN:
                    return WHITE_QUEEN;
                case KING:
                    return WHITE_KING;
                default:
                    return null;
            }

        else
            switch (type)
            {
                case PAWN:
                    return BLACK_PAWN;
                case ROOK:
                    return BLACK_ROOK;
                case KNIGHT:
                    return BLACK_KNIGHT;
                case BISHOP:
                    return BLACK_BISHOP;
                case QUEEN:
                    return BLACK_QUEEN;
                case KING:
                    return BLACK_KING;
                default:
                    return null;
            }
    }

    public static Piece get(Character p)
    {
        switch (p)
        {
            case 'P':
                return WHITE_PAWN;
            case 'p':
                return BLACK_PAWN;
            case 'R':
                return WHITE_ROOK;
            case 'r':
                return BLACK_ROOK;
            case 'N':
                return WHITE_KNIGHT;
            case 'n':
                return BLACK_KNIGHT;
            case 'B':
                return WHITE_BISHOP;
            case 'b':
                return BLACK_BISHOP;
            case 'Q':
                return WHITE_QUEEN;
            case 'q':
                return BLACK_QUEEN;
            case 'K':
                return WHITE_KING;
            case 'k':
                return BLACK_KING;
            default:
                return null;
        }
    }

    public final Material.Type getType()
    {
        return type;
    }
    public final Color getColor()
    {
        return color;
    }
    public final byte toByte()
    {
        return (byte) code;
    }

    @Override
    public String toString()
    {
        return color == Color.WHITE ? getType().toString().toUpperCase() :
                getType().toString().toLowerCase();
    }
}
