/*
 * The MIT License
 *
 * Copyright (c) 2024 Mark Chen <chen@dotfx.com>.
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

import java.util.Arrays;
import java.util.List;

/**
 * This class is for loose sorting and comparison of positions (piece positions and side to move only).
 */
public class LooseBoard implements Comparable<LooseBoard>
{
    private final Board board;

    public LooseBoard(boolean initialPosition) { this.board = new Board(initialPosition); }
    public LooseBoard(Board board) { this.board = board; }

    public Board getBoard() { return board; }
    public Board.Piece[] getPosition() { return board.getPosition(); }
    public int getPly() { return board.getPly(); }
    public int getWhitePieceCount() { return board.getWhitePieceCount(); }
    public int getBlackPieceCount() { return board.getBlackPieceCount(); }

    public LooseBoard goTo(List<String> moveList)
            throws IllegalMoveException
    {
        board.goTo(moveList);
        return this;
    }

    public int compareTo(LooseBoard that)
    {
        if (that == null) return 1;

        int plyDiff = (board.getPly() & 1) - (that.getPly() & 1); // parity check
        if (plyDiff != 0) return plyDiff;

        Board.Piece[] position = board.getPosition();
        Board.Piece[] thatPosition = that.getPosition();

        for (int i = 0; i < position.length; i++)
        {
            if (position[i] == null)
            {
                if (thatPosition[i] != null) return -1;
                else continue;
            }

            if (thatPosition[i] == null) return 1;

            if (position[i].toByte() > thatPosition[i].toByte()) return 1;
            if (position[i].toByte() < thatPosition[i].toByte()) return -1;
        }

        return 0;
    }

    @Override
    public final boolean equals(Object other)
    {
        try { return compareTo((LooseBoard)other) == 0; }
        catch (ClassCastException e) { return false; }
    }

    @Override
    public final int hashCode()
    {
        return LooseBoard.class.hashCode() ^ Arrays.hashCode(board.getPosition()) ^
                Integer.hashCode(board.getPly() & 1);
    }
}
