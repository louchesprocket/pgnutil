/*
 * The MIT License
 *
 * Copyright (c) 2025 Mark Chen <chen@dotfx.com>.
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

import com.cedarsoftware.util.CaseInsensitiveMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameValidator implements PgnGame.Parser
{
    public final PgnGame parseNext(String fileName, int number, CopyReader reader)
            throws IOException, PGNException
    {
        ArrayList<String> moveComments = new ArrayList<>();
        char buf[] = new char[PgnGame.Parser.BUFSIZE];
        int next, i, j;
        CaseInsensitiveMap<String,String> tagpairs = new CaseInsensitiveMap<>();
        int lastPly = 0;
        List<PgnGame.Move> moves = new ArrayList<>();
        List<String> gameComments = new ArrayList<>();
        Board board = new Board(true);

        while (true) // parse tag pairs
        {
            next = PgnGame.Parser.eatWhiteSpace(reader);

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
            next = PgnGame.Parser.eatWhiteSpace(reader);

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
            next = PgnGame.Parser.eatWhiteSpace(reader);
            if (next != ']') throw new PGNException("missing ']'");

            tagpairs.put(tag, value);
        }

        while (next == '{' || next == ';') next = PgnGame.Parser.processComment(reader, next, gameComments);

        if (next == -1) throw new PGNException("eof while parsing"); // empty move list
        buf[0] = (char)next;

        for (i = 1;; i++) // parse each ply
        {
            for (j = 1; Character.isDigit(buf[j - 1]); j++) // move number
            {
                try { next = reader.readFully(buf, j, 1); }

                catch (IndexOutOfBoundsException e) // reallocate
                {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                    next = reader.readFully(buf, j, 1);
                }

                if (next == -1) throw new PGNException("eof while parsing");
                next = buf[j];
            }

            if (Character.isWhitespace(buf[j - 1])) next = PgnGame.Parser.eatWhiteSpace(reader);
            if (next == -1) throw new PGNException("eof while parsing game " + number);

            // termination markers
            switch (buf[j - 1])
            {
                case '-':
                    if (j != 2) throw new PGNException("invalid termination marker at game " + number);

                    next = reader.readFully(buf, 2, 1);
                    if (next == -1) throw new PGNException("eof while parsing");

                    if ((buf[0] != '0' && buf[0] != '1') ||
                            (buf[2] != '0' && buf[2] != '1') ||
                            (buf[0] == buf[2]) || !new String(buf, 0, 3).equals(tagpairs.get("Result")))
                        throw new PGNException("invalid termination marker " +
                                "at game " + number);

                    return new PgnGame(fileName, number, tagpairs, gameComments, moves, reader.getCopy());

                case '/':
                    if (j != 2) throw new PGNException("invalid termination marker at game " + number);

                    next = reader.readFully(buf, 2, 5);
                    if (next == -1) throw new PGNException("EOF while parsing");

                    String terminator = new String(buf, 0, 7);

                    if (!terminator.equals("1/2-1/2") || !terminator.equals(tagpairs.get("Result")))
                        throw new PGNException("invalid termination marker at game " + number);

                    return new PgnGame(fileName, number, tagpairs, gameComments, moves, reader.getCopy());

                case '*':
                    if (!"*".equals(tagpairs.get("Result")))
                        throw new PGNException("invalid termination marker at game " + number);

                    return new PgnGame(fileName, number, tagpairs, gameComments, moves, reader.getCopy());
            }

            if (next == '.')
            {
                do { next = reader.read(); } while (next == '.');

                if (Character.isWhitespace(next)) next = PgnGame.Parser.eatWhiteSpace(reader);
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
                    next = PgnGame.Parser.eatWhiteSpace(reader);
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

            while (next == '{' || next == ';') next = PgnGame.Parser.processComment(reader, next, moveComments);

            if (next == -1) throw new PGNException("unexpected eof");
            buf[0] = (char)next;

            try
            {
                moves.add(new PgnGame.Move((short)i, board.normalize(moveStr, true), moveComments));
                moveComments = new ArrayList<>();
            }

            catch (IllegalMoveException e)
            {
                throw new PGNException("PGN error in " + (fileName == null ? "" : "file " + fileName + ", ") +
                        "game #" + number + ": " + e.getMessage());
            }
        }
    }
}
