package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.Board;
import com.dotfx.pgnutil.IllegalMoveException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class ScidReader extends TreeReader
{
    static class Factory implements TreeReaderFactory
    {
        @Override
        public TreeReader newInstance() { return new ScidReader(); }
    }

    void readTree(InputStream in, TreeNode topNode) throws IOException, IllegalMoveException
    {
        try (BufferedReader dbReader = new BufferedReader(new InputStreamReader(in)))
        {
            String line;

            while ((line = dbReader.readLine()) != null)
            {
                StringBuilder sb = new StringBuilder();
                String s;

                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                sb.append(line);

                Board board = new Board(true);

                while (!line.contains("*")) // entry continued to next line
                {
                    line = dbReader.readLine();
                    sb.append(line);
                }

                s = sb.toString();
                List<String> moveList = new ArrayList<>();
                String parts[] = s.split("\"");
                String rawMoves[] = parts[2].trim().split("\\s+");

                for (String move : rawMoves)
                {
                    if (move.equals("*")) break;
                    int moveStart = 0;
                    int moveEnd = move.length() - 1;

                    while (!Character.isLetterOrDigit(move.charAt(moveEnd))) moveEnd--;
                    while (moveStart < moveEnd && !Character.isLetter(move.charAt(moveStart))) moveStart++;

                    if (moveStart == moveEnd) continue; // space after move #

                    moveList.add(board.normalize(move.substring(moveStart, moveEnd + 1), false));
                }

                topNode.addNodes(moveList, parts[0].trim(), parts[1].trim(), this);
            }
        }
    }
}
