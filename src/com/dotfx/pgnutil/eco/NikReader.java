package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.Board;
import com.dotfx.pgnutil.IllegalMoveException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * For parsing niklasf's JSON file.
 */
public class NikReader extends TreeReader
{
    static class Factory implements TreeReaderFactory
    {
        @Override
        public TreeReader newInstance() { return new NikReader(); }
    }

    @Override
    void readTree(InputStream in, TreeNode topNode) throws IOException, IllegalMoveException
    {
        try (BufferedReader dbReader = new BufferedReader(new InputStreamReader(in)))
        {
            String line;

            while ((line = dbReader.readLine()) != null) {
                if (!line.trim().startsWith("{")) continue;

                Board board = new Board(true);
                List<String> moveList = new ArrayList<>();
                String parts[] = line.split("\"");
                String rawMoves[] = parts[17].trim().split("\\s+");

                for (String move : rawMoves) moveList.add(board.coordToSan(move, false));

                topNode.addNodes(moveList, parts[3], parts[7], this);
            }
        }
    }
}
